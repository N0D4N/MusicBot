package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.MessageBuilder;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RandomCmd extends Command {
    private final ConcurrentMap<Integer, TimestampToRandomNumbers> guildToRandom;
    private final Long SixHours = 21_600_000L;

    public RandomCmd() {
        this.name = "random";
        this.aliases = new String[]{"r"};
        this.help = "get random number non-repeated number";
        this.guildOnly = true;
        this.guildToRandom = new ConcurrentHashMap<>();
    }

    public static byte[] getSeed(CommandEvent event){
        int hash = Objects.hash(event.getMessage().getIdLong(), System.currentTimeMillis());
        return ByteBuffer.allocate(4).putInt(hash).array();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.getChannel().sendMessage(new MessageBuilder().append("You must provide 1 number, or 1 number prepended with \"new\" \"n\"").build()).queue();
        }
        int max = Integer.MIN_VALUE;

        String[] strings = event.getArgs().split(" ");
        try {
            max = Integer.parseUnsignedInt(strings.length == 2 ? strings[1] : strings[0]);
        } catch (NumberFormatException ex) {
            event.getChannel().sendMessage(new MessageBuilder("**" + event.getMember().getEffectiveName() + "** wrong number format").build()).queue();
            return;
        }
        catch (Exception ex){
            event.getChannel().sendMessage(new MessageBuilder("**" + event.getMember().getEffectiveName() + "** unknown error").build()).queue();
            return;
        }
        int returnRand = 0;
        long now = Instant.now().toEpochMilli();
        if (!guildToRandom.containsKey(max) || (strings.length == 2 && (strings[0].equalsIgnoreCase("new") || strings[0].equalsIgnoreCase("n")))) {
            // We haven't randomed for such number, or should clear it per user request
            ConcurrentHashMap.KeySetView<Integer, Boolean> randomNumbers = ConcurrentHashMap.newKeySet();
            returnRand = getRandomNumber(null, max, getSeed(event));
            randomNumbers.add(returnRand);
            guildToRandom.put(max, new TimestampToRandomNumbers(now, randomNumbers));
        } else { // We already randomed for such number
            final TimestampToRandomNumbers timestampToRandomNumbers = guildToRandom.get(max);
            if(now - timestampToRandomNumbers.Timestamp <= SixHours && timestampToRandomNumbers.RandomNumbers.size() < max){
                // It's not time to refresh and clear random numbers and we still have possible random values
                final SecureRandom secureRandom = new SecureRandom();
                final Set<Integer> randomNumbers = timestampToRandomNumbers.RandomNumbers;
                while(true) {
                    final int rand = getRandomNumber(secureRandom, max, getSeed(event));
                    if (!randomNumbers.contains(rand)) {
                        if(randomNumbers.add(rand)){
                            returnRand = rand;
                            break;
                        }
                    }
                }
            }
            else {
                // First random was more than 6 hours ago, or we already exceeded max number of random numbers
                timestampToRandomNumbers.RandomNumbers.clear();
                returnRand = getRandomNumber(null, max, getSeed(event));
                timestampToRandomNumbers.RandomNumbers.add(returnRand);
                timestampToRandomNumbers.Timestamp = now;
            }
        }
        event.getChannel().sendMessage(String.valueOf(returnRand)).reference(event.getMessage()).mentionRepliedUser(false).queue();
        if(now % 3 == 0) {
            for (Map.Entry<Integer, TimestampToRandomNumbers> entry : this.guildToRandom.entrySet()) {
                if(now - entry.getValue().Timestamp > SixHours){
                    this.guildToRandom.remove(entry.getKey());
                    entry.getValue().RandomNumbers.clear();
                }
            }
        }
    }

    public static Integer getRandomNumber(@Nullable SecureRandom random, Integer max, byte[] seed){
        if(random == null) {
            random = new SecureRandom(seed == null ? new byte[4] : seed);
        }
        return random.nextInt(max) + 1;
    }

    private class TimestampToRandomNumbers {
        public Long Timestamp;
        public Set<Integer> RandomNumbers;

        public TimestampToRandomNumbers(Long timestamp, Set<Integer> randomNumbers) {
            Timestamp = timestamp;
            RandomNumbers = randomNumbers;
        }
    }
}
