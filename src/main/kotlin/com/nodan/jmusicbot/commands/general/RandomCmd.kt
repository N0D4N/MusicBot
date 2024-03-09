package com.nodan.jmusicbot.commands.general

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.MessageBuilder
import java.security.SecureRandom
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class RandomCmd : Command() {
    private val guildToRandom: ConcurrentMap<Int, TimestampToRandomNumbers> = ConcurrentHashMap(2)

    init {
        this.name = "random"
        this.aliases = arrayOf("r")
        this.help = "get random number non-repeated number"
        this.guildOnly = true
    }

    override fun execute(commandEvent: CommandEvent) {
        if (commandEvent.args.isEmpty()) {
            commandEvent.channel.sendMessage(MessageBuilder().append("You must provide 1 number, or 1 number prepended with \"new\" \"n\"").build()).queue()
        }
        val max: Int

        val strings = commandEvent.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            max = Integer.parseUnsignedInt(if (strings.size == 2) strings[1] else strings[0])
        } catch (ex: NumberFormatException) {
            commandEvent.channel.sendMessage(MessageBuilder("**" + commandEvent.member.effectiveName + "** wrong number format").build()).queue()
            return
        } catch (ex: Exception) {
            commandEvent.channel.sendMessage(MessageBuilder("**" + commandEvent.member.effectiveName + "** unknown error").build()).queue()
            return
        }
        val returnRand: Int
        val now = Instant.now().toEpochMilli()
        if (!guildToRandom.containsKey(max) || (strings.size == 2 && (strings[0].equals("new", ignoreCase = true) || strings[0].equals("n", ignoreCase = true)))) {
            // We haven't randomed for such number, or should clear it per user request
            val randomNumbers = ConcurrentHashMap.newKeySet<Int>()
            returnRand = getRandomNumber(max)
            randomNumbers.add(returnRand)
            guildToRandom[max] = TimestampToRandomNumbers(now, randomNumbers)
        } else { // We already randomed for such number
            val timestampToRandomNumbers = guildToRandom[max]
            if (now - timestampToRandomNumbers!!.Timestamp <= SixHours && timestampToRandomNumbers.RandomNumbers.size < max) {
                // It's not time to refresh and clear random numbers and we still have possible random values
                val randomNumbers = timestampToRandomNumbers.RandomNumbers
                while (true) {
                    val rand = getRandomNumber(max)
                    if (!randomNumbers.contains(rand)) {
                        if (randomNumbers.add(rand)) {
                            returnRand = rand
                            break
                        }
                    }
                }
            } else {
                // First random was more than 6 hours ago, or we already exceeded max number of random numbers
                timestampToRandomNumbers.RandomNumbers.clear()
                returnRand = getRandomNumber(max)
                timestampToRandomNumbers.RandomNumbers.add(returnRand)
                timestampToRandomNumbers.Timestamp = now
            }
        }
        commandEvent.channel.sendMessage(formatNumber(returnRand, max)).reference(commandEvent.message).mentionRepliedUser(false).queue()
        if (now % 3L == 0L) {
            for ((key, value) in this.guildToRandom) {
                if (now - value.Timestamp > SixHours) {
                    guildToRandom.remove(key)
                    value.RandomNumbers.clear()
                }
            }
        }
    }

    private inner class TimestampToRandomNumbers(var Timestamp: Long, val RandomNumbers: MutableSet<Int>)

    companion object {
        private const val SixHours = 21600000L

        private val secureRandom: SecureRandom = SecureRandom.getInstanceStrong()

        fun getRandomNumber(max: Int): Int {
            return secureRandom.nextInt(max) + 1
        }

        fun formatNumber(randomedValue: Int?, maxValue: Int): String {
            return String.format("%0" + (StrictMath.log10(maxValue.toDouble()).toInt() + 1) + "d", randomedValue)
        }
    }
}