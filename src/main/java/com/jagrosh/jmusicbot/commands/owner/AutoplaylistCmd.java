/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.interactions.InteractionContextType;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class AutoplaylistCmd extends OwnerCommand
{
    private final Bot bot;
    
    public AutoplaylistCmd(Bot bot)
    {
        this.bot = bot;
        this.contexts = new InteractionContextType[] {InteractionContextType.GUILD};
        this.name = "autoplaylist";
        this.arguments = "<name|NONE>";
        this.help = "sets the default playlist for the server";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    public void execute(CommandEvent commandEvent)
    {
        if(commandEvent.getArgs().isEmpty())
        {
            commandEvent.reply(commandEvent.getClient().getError()+" Please include a playlist name or NONE");
            return;
        }
        if(commandEvent.getArgs().equalsIgnoreCase("none"))
        {
            Settings settings = commandEvent.getClient().getSettingsFor(commandEvent.getGuild());
            settings.setDefaultPlaylist(null);
            commandEvent.reply(commandEvent.getClient().getSuccess()+" Cleared the default playlist for **"+ commandEvent.getGuild().getName()+"**");
            return;
        }
        var pname = commandEvent.getArgs().replaceAll("\\s+", "_");
        if(bot.getPlaylistLoader().getPlaylist(pname)==null)
        {
            commandEvent.reply(commandEvent.getClient().getError()+" Could not find `"+pname+".txt`!");
        }
        else
        {
            Settings settings = commandEvent.getClient().getSettingsFor(commandEvent.getGuild());
            settings.setDefaultPlaylist(pname);
            commandEvent.reply(commandEvent.getClient().getSuccess()+" The default playlist for **"+ commandEvent.getGuild().getName()+"** is now `"+pname+"`");
        }
    }
}
