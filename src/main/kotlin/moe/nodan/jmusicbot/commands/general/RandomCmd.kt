package moe.nodan.jmusicbot.commands.general

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
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
            commandEvent.channel.sendMessage("You must provide 1 number, or 1 number prepended with \"new\" \"n\"").queue()
        }
        val max: Int

        val strings = commandEvent.args.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            max = Integer.parseUnsignedInt(if (strings.size == 2) strings[1] else strings[0])
        } catch (_: NumberFormatException) {
            commandEvent.channel.sendMessage("**${commandEvent.member.effectiveName}** wrong number format").queue()

            return
        } catch (_: Exception) {
            commandEvent.channel.sendMessage("**${commandEvent.member.effectiveName}** unknown error").queue()

            return
        }
        val returnRand: Int
        val now = Instant.now().toEpochMilli()
        if (!guildToRandom.containsKey(max) || (strings.size == 2 && (strings[0].equals("new", true) || strings[0].equals("n", true)))) {
            // We haven't randomed for such number, or should clear it per user request
            val randomNumbers = ConcurrentHashMap.newKeySet<Int>()
            returnRand = getRandomNumber(max)
            randomNumbers.add(returnRand)
            guildToRandom[max] = TimestampToRandomNumbers(now, randomNumbers)
        } else { // We already randomed for such number
            val timestampToRandomNumbers = guildToRandom[max]
            if (now - timestampToRandomNumbers!!.timestamp <= SIXHOURS && timestampToRandomNumbers.randomNumbers.size < max) {
                // It's not time to refresh and clear random numbers, and we still have possible random values
                val randomNumbers = timestampToRandomNumbers.randomNumbers
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
                timestampToRandomNumbers.randomNumbers.clear()
                returnRand = getRandomNumber(max)
                timestampToRandomNumbers.randomNumbers.add(returnRand)
                timestampToRandomNumbers.timestamp = now
            }
        }
        commandEvent.channel.sendMessage(formatNumber(returnRand, max)).reference(commandEvent.message)
            .mentionRepliedUser(false).queue()
        if (now % 3L == 0L) {
            for ((key, value) in this.guildToRandom) {
                if (now - value.timestamp > SIXHOURS) {
                    guildToRandom.remove(key)
                    value.randomNumbers.clear()
                }
            }
        }
    }

    private inner class TimestampToRandomNumbers(var timestamp: Long, val randomNumbers: MutableSet<Int>)

    companion object {
        private const val SIXHOURS = 21600000L

        private val secureRandom: SecureRandom = SecureRandom.getInstanceStrong()

        fun getRandomNumber(max: Int): Int = secureRandom.nextInt(max) + 1

        fun formatNumber(randomedValue: Int?, maxValue: Int): String =
            "%0${(StrictMath.log10(maxValue.toDouble()).toInt() + 1)}d".format(randomedValue)
    }
}