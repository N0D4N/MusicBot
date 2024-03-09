package com.nodan.jmusicbot.commands.general

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.MessageBuilder

class OldRandomCmd : Command() {
    init {
        this.name = "old random"
        this.help = "get random number"
        this.aliases = arrayOf("or", "orandom", "rr")
        this.guildOnly = true
    }

    override fun execute(commandEvent: CommandEvent) {
        if (commandEvent.args.isEmpty()) {
            commandEvent.channel.sendMessage(MessageBuilder("You must provide at least 1 argument").build()).queue()
            return
        }
        val max: Int
        try {
            max = Integer.parseUnsignedInt(commandEvent.args)
        } catch (ex: NumberFormatException) {
            commandEvent.channel.sendMessage(MessageBuilder("**" + commandEvent.member.effectiveName + "** wrong number format").build()).queue()
            return
        } catch (ex: Exception) {
            commandEvent.channel.sendMessage(MessageBuilder("**" + commandEvent.member.effectiveName + "** unknown error").build()).queue()
            return
        }
        if (max <= 1) {
            commandEvent.channel.sendMessage(MessageBuilder("**" + commandEvent.member.effectiveName + "** number must be greater than 1").build()).queue()
            return
        }
        commandEvent.channel.sendMessage(RandomCmd.formatNumber(RandomCmd.getRandomNumber(max), max)).reference(commandEvent.message).mentionRepliedUser(false).queue()
    }
}