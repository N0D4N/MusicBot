package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.MessageBuilder;

public class OldRandomCmd extends Command {
    public OldRandomCmd() {
        this.name = "old random";
        this.help = "get random number";
        this.aliases = new String[] {"or", "orandom"};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.getChannel().sendMessage(new MessageBuilder().append("You must provide at least 1 argument").build()).queue();
        }
        int max = 0;
        try {
            max = Integer.parseUnsignedInt(event.getArgs());
        } catch (NumberFormatException ex) {
            event.getChannel().sendMessage(new MessageBuilder("**" + event.getMember().getEffectiveName() + "** wrong number format").build()).queue();
            return;
        }
        catch (Exception ex){
            event.getChannel().sendMessage(new MessageBuilder("**" + event.getMember().getEffectiveName() + "** unknown error").build()).queue();
            return;
        }
        if (max <= 1) {
            event.getChannel().sendMessage(new MessageBuilder("**" + event.getMember().getEffectiveName() + "** number must be greater than 1").build()).queue();
            return;
        }
        event.getChannel().sendMessage(String.valueOf(RandomCmd.getRandomNumber(null, max, RandomCmd.getSeed(event)))).reference(event.getMessage()).mentionRepliedUser(false).queue();
    }
}