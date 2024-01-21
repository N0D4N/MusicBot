package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.MessageBuilder;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RandomCmd extends Command {
    private final ConcurrentMap<Integer, TimestampToRandomNumbers> guildToRandom = new ConcurrentHashMap<>();
    private static final Long SixHours = 21_600_000L;

    private static final SecureRandom secureRandom;

    static {
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public RandomCmd() {
        this.name = "random";
        this.aliases = new String[]{"r"};
        this.help = "get random number non-repeated number";
        this.guildOnly = true;
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
        } catch (Exception ex) {
            event.getChannel().sendMessage(new MessageBuilder("**" + event.getMember().getEffectiveName() + "** unknown error").build()).queue();
            return;
        }
        int returnRand = 0;
        long now = Instant.now().toEpochMilli();
        if (!guildToRandom.containsKey(max) || (strings.length == 2 && (strings[0].equalsIgnoreCase("new") || strings[0].equalsIgnoreCase("n")))) {
            // We haven't randomed for such number, or should clear it per user request
            ConcurrentHashMap.KeySetView<Integer, Boolean> randomNumbers = ConcurrentHashMap.newKeySet();
            returnRand = getRandomNumber(max);
            randomNumbers.add(returnRand);
            guildToRandom.put(max, new TimestampToRandomNumbers(now, randomNumbers));
        } else { // We already randomed for such number
            final TimestampToRandomNumbers timestampToRandomNumbers = guildToRandom.get(max);
            if (now - timestampToRandomNumbers.Timestamp <= SixHours && timestampToRandomNumbers.RandomNumbers.size() < max) {
                // It's not time to refresh and clear random numbers and we still have possible random values
                final Set<Integer> randomNumbers = timestampToRandomNumbers.RandomNumbers;
                while (true) {
                    final int rand = getRandomNumber(max);
                    if (!randomNumbers.contains(rand)) {
                        if (randomNumbers.add(rand)) {
                            returnRand = rand;
                            break;
                        }
                    }
                }
            } else {
                // First random was more than 6 hours ago, or we already exceeded max number of random numbers
                timestampToRandomNumbers.RandomNumbers.clear();
                returnRand = getRandomNumber(max);
                timestampToRandomNumbers.RandomNumbers.add(returnRand);
                timestampToRandomNumbers.Timestamp = now;
            }
        }
        event.getChannel().sendMessage(formatNumber(returnRand, max)).reference(event.getMessage()).mentionRepliedUser(false).queue();
        if (now % 3 == 0) {
            for (Map.Entry<Integer, TimestampToRandomNumbers> entry : this.guildToRandom.entrySet()) {
                if (now - entry.getValue().Timestamp > SixHours) {
                    this.guildToRandom.remove(entry.getKey());
                    entry.getValue().RandomNumbers.clear();
                }
            }
        }
    }

    public static Integer getRandomNumber(Integer max) {
        return secureRandom.nextInt(max) + 1;
    }

    public static String formatNumber(Integer randomedValue, Integer maxValue) {
        return String.format("%0" + ((int) Math.log10(maxValue) + 1) + "d", randomedValue);
    }

    private final class TimestampToRandomNumbers {
        public Long Timestamp;
        public Set<Integer> RandomNumbers;

        public TimestampToRandomNumbers(Long timestamp, Set<Integer> randomNumbers) {
            Timestamp = timestamp;
            RandomNumbers = randomNumbers;
        }
    }
}
