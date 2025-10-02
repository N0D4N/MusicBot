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
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.TvHtml5Embedded;
import net.dv8tion.jda.api.entities.Guild;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public final class PlayerManager extends DefaultAudioPlayerManager
{
    private final Bot bot;
    
    public PlayerManager(Bot bot)
    {
        this.bot = bot;
    }
    
    public void init()
    {
        TransformativeAudioSourceManager.createTransforms(bot.getConfig().getTransforms()).forEach(this::registerSourceManager);

        var ytSrcOptions = new YoutubeSourceOptions();
        ytSrcOptions.setAllowSearch(true);
        ytSrcOptions.setRemoteCipherUrl(bot.getConfig().getYtcUrl(), "");

        var yt = new YoutubeAudioSourceManager(ytSrcOptions, new TvHtml5Embedded());
        yt.setPlaylistPageCount(bot.getConfig().getMaxYTPlaylistPages());
        yt.useOauth2(bot.getConfig().getOauth2Token(), true);
        this.registerSourceManager(yt);

        this.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        this.registerSourceManager(new BandcampAudioSourceManager());
        this.registerSourceManager(new VimeoAudioSourceManager());
        this.registerSourceManager(new TwitchStreamAudioSourceManager());
        this.registerSourceManager(new BeamAudioSourceManager());
        this.registerSourceManager(new GetyarnAudioSourceManager());
        this.registerSourceManager(new NicoAudioSourceManager());
        this.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));

        AudioSourceManagers.registerLocalSource(this);
    }
    
    public Bot getBot()
    {
        return bot;
    }
    
    public static boolean hasHandler(Guild guild)
    {
        return guild.getAudioManager().getSendingHandler()!=null;
    }
    
    public AudioHandler setUpHandler(Guild guild)
    {
        AudioHandler handler;
        if(guild.getAudioManager().getSendingHandler()==null)
        {
            var player = this.createPlayer();
            player.setVolume(bot.getSettingsManager().getSettings(guild).getVolume());
            handler = new AudioHandler(this, guild, player);
            player.addListener(handler);
            guild.getAudioManager().setSendingHandler(handler);
        }
        else
            handler = (AudioHandler) guild.getAudioManager().getSendingHandler();
        return handler;
    }
}
