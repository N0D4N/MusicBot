package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.MessageBuilder;

public final class OldRandomCmd extends Command {
    public OldRandomCmd() {
        this.name = "old random";
        this.help = "get random number";
        this.aliases = new String[]{"or", "orandom", "rr"};
        this.guildOnly = true;
    }

    @Override
    protected void execute(final CommandEvent commandEvent) {
        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.getChannel().sendMessage(new MessageBuilder().append("You must provide at least 1 argument").build()).queue();
        }
        var max = 0;
        try {
            max = Integer.parseUnsignedInt(commandEvent.getArgs());
        } catch (final NumberFormatException ex) {
            commandEvent.getChannel().sendMessage(new MessageBuilder("**" + commandEvent.getMember().getEffectiveName() + "** wrong number format").build()).queue();
            return;
        } catch (final Exception ex) {
            commandEvent.getChannel().sendMessage(new MessageBuilder("**" + commandEvent.getMember().getEffectiveName() + "** unknown error").build()).queue();
            return;
        }
        if (max <= 1) {
            commandEvent.getChannel().sendMessage(new MessageBuilder("**" + commandEvent.getMember().getEffectiveName() + "** number must be greater than 1").build()).queue();
            return;
        }
        commandEvent.getChannel().sendMessage(RandomCmd.formatNumber(RandomCmd.getRandomNumber(max), max)).reference(commandEvent.getMessage()).mentionRepliedUser(false).queue();
    }
}
