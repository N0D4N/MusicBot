package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.MessageBuilder;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RandomCmd extends Command {
    private final ConcurrentMap<Integer, TimestampToRandomNumbers> guildToRandom = new ConcurrentHashMap<>(2);
    private static final Long SixHours = 21_600_000L;

    private static final SecureRandom secureRandom;

    static {
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (final NoSuchAlgorithmException e) {
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
    protected void execute(final CommandEvent commandEvent) {
        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.getChannel().sendMessage(new MessageBuilder().append("You must provide 1 number, or 1 number prepended with \"new\" \"n\"").build()).queue();
        }
        int max = Integer.MIN_VALUE;

        final var strings = commandEvent.getArgs().split(" ");
        try {
            max = Integer.parseUnsignedInt(strings.length == 2 ? strings[1] : strings[0]);
        } catch (final NumberFormatException ex) {
            commandEvent.getChannel().sendMessage(new MessageBuilder("**" + commandEvent.getMember().getEffectiveName() + "** wrong number format").build()).queue();
            return;
        } catch (final Exception ex) {
            commandEvent.getChannel().sendMessage(new MessageBuilder("**" + commandEvent.getMember().getEffectiveName() + "** unknown error").build()).queue();
            return;
        }
        int returnRand = 0;
        final long now = Instant.now().toEpochMilli();
        if (!this.guildToRandom.containsKey(max) || (strings.length == 2 && (strings[0].equalsIgnoreCase("new") || strings[0].equalsIgnoreCase("n")))) {
            // We haven't randomed for such number, or should clear it per user request
            final ConcurrentHashMap.KeySetView<Integer, Boolean> randomNumbers = ConcurrentHashMap.newKeySet();
            returnRand = getRandomNumber(max);
            randomNumbers.add(returnRand);
            this.guildToRandom.put(max, new TimestampToRandomNumbers(now, randomNumbers));
        } else { // We already randomed for such number
            final var timestampToRandomNumbers = this.guildToRandom.get(max);
            if (now - timestampToRandomNumbers.Timestamp <= SixHours && timestampToRandomNumbers.RandomNumbers.size() < max) {
                // It's not time to refresh and clear random numbers and we still have possible random values
                final var randomNumbers = timestampToRandomNumbers.RandomNumbers;
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
        commandEvent.getChannel().sendMessage(formatNumber(returnRand, max)).reference(commandEvent.getMessage()).mentionRepliedUser(false).queue();
        if (now % 3L == 0L) {
            for (final var entry : this.guildToRandom.entrySet()) {
                if (now - entry.getValue().Timestamp > SixHours) {
                    this.guildToRandom.remove(entry.getKey());
                    entry.getValue().RandomNumbers.clear();
                }
            }
        }
    }

    static Integer getRandomNumber(final Integer max) {
        return secureRandom.nextInt(max) + 1;
    }

    static String formatNumber(final Integer randomedValue, final Integer maxValue) {
        return String.format("%0" + (StrictMath.log10(maxValue) + 1.0D) + "d", randomedValue);
    }

    private final class TimestampToRandomNumbers {
        Long Timestamp;
        final Set<Integer> RandomNumbers;

        TimestampToRandomNumbers(final Long timestamp, final Set<Integer> randomNumbers) {
            this.Timestamp = timestamp;
            this.RandomNumbers = randomNumbers;
        }
    }
}
