package com.nodan.jmusicbot.commands.general

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class OldRandomCmd : Command() {
    init {
        this.name = "old random"
        this.help = "get random number"
        this.aliases = arrayOf("or", "orandom", "rr")
        this.guildOnly = true
    }

    override fun execute(commandEvent: CommandEvent) {
        if (commandEvent.args.isEmpty()) {
            commandEvent.channel.sendMessage("You must provide at least 1 argument").queue()
            return
        }
        val max: Int
        try {
            max = commandEvent.args.toInt()
        } catch (_: NumberFormatException) {
            commandEvent.channel.sendMessage("**${commandEvent.member.effectiveName}** wrong number format").queue()

            return
        } catch (_: Exception) {
            commandEvent.channel.sendMessage("**${commandEvent.member.effectiveName}** unknown error").queue()
            return
        }
        if (max <= 1) {
            commandEvent.channel.sendMessage("**${commandEvent.member.effectiveName}** number must be greater than 1").queue()

            return
        }
        commandEvent.channel.sendMessage(RandomCmd.formatNumber(RandomCmd.getRandomNumber(max), max))
            .reference(commandEvent.message).mentionRepliedUser(false).queue()
    }
}