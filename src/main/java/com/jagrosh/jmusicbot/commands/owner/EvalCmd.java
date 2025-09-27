/*
 * Copyright 2016 John Grosh (jagrosh).
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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import javax.script.ScriptEngineManager;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class EvalCmd extends OwnerCommand 
{
    private final Bot bot;
    private final String engine;
    
    public EvalCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "eval";
        this.help = "evaluates nashorn code";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.engine = bot.getConfig().getEvalEngine();
        this.contexts = new InteractionContextType[] {InteractionContextType.GUILD, InteractionContextType.BOT_DM};
    }
    
    @Override
    protected void execute(CommandEvent commandEvent)
    {
        var se = new ScriptEngineManager().getEngineByName(engine);
        if(se == null)
        {
            commandEvent.replyError("The eval engine provided in the config (`"+engine+"`) doesn't exist. This could be due to an invalid "
                    + "engine name, or the engine not existing in your version of java (`"+System.getProperty("java.version")+"`).");
            return;
        }
        se.put("bot", bot);
        se.put("event", commandEvent);
        se.put("jda", commandEvent.getJDA());
        if (commandEvent.getChannelType() != ChannelType.PRIVATE) {
            se.put("guild", commandEvent.getGuild());
            se.put("channel", commandEvent.getChannel());
        }
        try
        {
            commandEvent.reply(commandEvent.getClient().getSuccess()+" Evaluated Successfully:\n```\n"+se.eval(commandEvent.getArgs())+" ```");
        } 
        catch(Exception e)
        {
            commandEvent.reply(commandEvent.getClient().getError()+" An exception was thrown:\n```\n"+e+" ```");
        }
    }
    
}
