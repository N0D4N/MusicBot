/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.entities.Pair;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import moe.nodan.jmusicbot.utils.Util;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public final class NowplayingHandler
{
    private final Bot bot;
    private final HashMap<Long,Pair<Long,Long>> lastNP; // guild -> channel,message
    
    public NowplayingHandler(Bot bot)
    {
        this.bot = bot;
        this.lastNP = new HashMap<>();
    }
    
    public void init()
    {
        if(!bot.getConfig().useNPImages())
            bot.getThreadpool().scheduleWithFixedDelay(() -> updateAll(), 0, 5, TimeUnit.SECONDS);
    }
    
    public void setLastNPMessage(Message m)
    {
        lastNP.put(m.getGuild().getIdLong(), new Pair<>(m.getChannel().getIdLong(), m.getIdLong()));
    }
    
    public void clearLastNPMessage(Guild guild)
    {
        lastNP.remove(guild.getIdLong());
    }
    
    private void updateAll()
    {
        Set<Long> toRemove = new HashSet<>();
        for(long guildId: lastNP.keySet())
        {
            var guild = bot.getJDA().getGuildById(guildId);
            if(guild==null)
            {
                toRemove.add(guildId);
                continue;
            }
            var pair = lastNP.get(guildId);
            var tc = guild.getTextChannelById(pair.key());
            if(tc==null)
            {
                toRemove.add(guildId);
                continue;
            }
            var handler = (AudioHandler)guild.getAudioManager().getSendingHandler();
            var msg = handler.getNowPlaying(bot.getJDA());
            if(msg==null)
            {
                msg = handler.getNoMusicPlaying(bot.getJDA());
                toRemove.add(guildId);
            }
            try 
            {
                tc.editMessageById(pair.value(), msg.getContent()).queue(m->{}, t -> lastNP.remove(guildId));
            } 
            catch(Exception e) 
            {
                toRemove.add(guildId);
            }
        }
        toRemove.forEach(id -> lastNP.remove(id));
    }

    // "event"-based methods
    public void onTrackUpdate(AudioTrack track, AudioHandler handler)
    {
        if (track != null) {
            var guild = handler.guild(bot.getJDA());
            var voiceState = guild.getSelfMember().getVoiceState();

            if (voiceState != null && voiceState.inAudioChannel()) {
                var channel = voiceState.getChannel().asVoiceChannel();
                try {
                    channel.modifyStatus(Util.getTrackName(track.getInfo()));
                }
                catch (Exception e) {

                }
            }
        }
        // update bot status if applicable
        if(bot.getConfig().getSongInStatus())
        {
            if(track!=null && bot.getJDA().getGuilds().stream().filter(g -> {
                final var voiceState = g.getSelfMember().getVoiceState();
                return voiceState != null && voiceState.inAudioChannel();
            }).count()<= 1)
            {
                bot.getJDA().getPresence().setActivity(Activity.listening(Util.getTrackName(track.getInfo())));
            }
            else
                bot.resetGame();
        }
    }
    
    public void onMessageDelete(Guild guild, long messageId)
    {
        var pair = lastNP.get(guild.getIdLong());
        if(pair==null)
            return;
        if(pair.value() == messageId)
            lastNP.remove(guild.getIdLong());
    }
}
