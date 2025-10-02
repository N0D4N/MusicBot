/*
 * Copyright 2018 John Grosh (jagrosh)
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
package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 
 * 
 * @author John Grosh (jagrosh)
 */
public class BotConfig
{
    private final static String START_TOKEN = "/// START OF JMUSICBOT CONFIG ///";
    private final static String END_TOKEN = "/// END OF JMUSICBOT CONFIG ///";
    private final static Logger LOG = LoggerFactory.getLogger("Config");
    
    private Path path = null;
    private String token, prefix, altprefix, helpWord, playlistsFolder, logLevel,
            successEmoji, warningEmoji, errorEmoji, loadingEmoji, searchingEmoji,
            evalEngine;
    private boolean stayInChannel, songInGame, npImages, updatealerts, useEval, dbots;
    private long owner, maxSeconds, aloneTimeUntilStop;
    private int maxYTPlaylistPages;
    private double skipratio;
    private OnlineStatus status;
    private Activity game;
    private Config aliases, transforms;

    private boolean valid = false;
    private String oauth2Token;
    private String ytcUrl;

    public BotConfig()
    {
    }
    
    public void load()
    {
        valid = false;
        
        // read config from file
        try 
        {
            // get the path to the config, default config.txt
            path = getConfigPath();
            
            // load in the config file, plus the default values
            //Config config = ConfigFactory.parseFile(path.toFile()).withFallback(ConfigFactory.load());
            var config = ConfigFactory.load();
            
            // set values
            token = config.getString("token");
            prefix = config.getString("prefix");
            altprefix = config.getString("altprefix");
            helpWord = config.getString("help");
            owner = config.getLong("owner");
            successEmoji = config.getString("success");
            warningEmoji = config.getString("warning");
            errorEmoji = config.getString("error");
            loadingEmoji = config.getString("loading");
            searchingEmoji = config.getString("searching");
            game = OtherUtil.parseGame(config.getString("game"));
            status = OtherUtil.parseStatus(config.getString("status"));
            stayInChannel = config.getBoolean("stayinchannel");
            songInGame = config.getBoolean("songinstatus");
            npImages = config.getBoolean("npimages");
            updatealerts = config.getBoolean("updatealerts");
            logLevel = config.getString("loglevel");
            useEval = config.getBoolean("eval");
            evalEngine = config.getString("evalengine");
            maxSeconds = config.getLong("maxtime");
            maxYTPlaylistPages = config.getInt("maxytplaylistpages");
            aloneTimeUntilStop = config.getLong("alonetimeuntilstop");
            playlistsFolder = config.getString("playlistsfolder");
            aliases = config.getConfig("aliases");
            transforms = config.getConfig("transforms");
            skipratio = config.getDouble("skipratio");
            oauth2Token = config.getString("oauth2token");
            ytcUrl = config.getString("ytcUrl");
            dbots = owner == 113156185389092864L;
            
            // we may need to write a new config file
            var write = false;

            // validate bot token
            if(token==null || token.isEmpty() || token.equalsIgnoreCase("BOT_TOKEN_HERE"))
            {
                LOG.warn("No token provided! Exiting.\n\nConfig Location: " + path.toAbsolutePath().toString());
                return;
            }
            
            // validate bot owner
            if(owner<=0)
            {
                LOG.error("Invalid User ID! Exiting.\n\nConfig Location: " + path.toAbsolutePath().toString());
                return;
            }
            
            if(write)
                writeToFile();
            
            // if we get through the whole config, it's good to go
            valid = true;
        }
        catch (ConfigException ex)
        {
            LOG.error(ex + ": " + ex.getMessage() + "\n\nConfig Location: " + path.toAbsolutePath().toString());
        }
    }
    
    private void writeToFile()
    {
        var bytes = loadDefaultConfig().replace("BOT_TOKEN_HERE", token)
                .replace("0 // OWNER ID", Long.toString(owner))
                .trim().getBytes();
        try 
        {
            Files.write(path, bytes);
        }
        catch(IOException ex) 
        {
            LOG.warn("Failed to write new config options to config.txt: "+ex
                + "\nPlease make sure that the files are not on your desktop or some other restricted area.\n\nConfig Location: " 
                + path.toAbsolutePath().toString());
        }
    }
    
    private static String loadDefaultConfig()
    {
        var original = OtherUtil.loadResource(new JMusicBot(), "/reference.conf");
        return original==null 
                ? "token = BOT_TOKEN_HERE\r\nowner = 0 // OWNER ID" 
                : original.substring(original.indexOf(START_TOKEN)+START_TOKEN.length(), original.indexOf(END_TOKEN)).trim();
    }
    
    private static Path getConfigPath()
    {
        var path = OtherUtil.getPath(System.getProperty("config.file", System.getProperty("config", "config.txt")));
        if(path.toFile().exists())
        {
            if(System.getProperty("config.file") == null)
                System.setProperty("config.file", System.getProperty("config", path.toAbsolutePath().toString()));
            ConfigFactory.invalidateCaches();
        }
        return path;
    }
    
    public static void writeDefaultConfig()
    {
        LOG.info("Generating default config file");
        var path = BotConfig.getConfigPath();
        var bytes = BotConfig.loadDefaultConfig().getBytes();
        try
        {
            LOG.info("Writing default config file to " + path.toAbsolutePath().toString());
            Files.write(path, bytes);
        }
        catch(Exception ex)
        {
            LOG.error("An error occurred writing the default config file: " + ex.getMessage());
        }
    }
    
    public boolean isValid()
    {
        return valid;
    }
    
    public String getConfigLocation()
    {
        return path.toFile().getAbsolutePath();
    }
    
    public String getPrefix()
    {
        return prefix;
    }
    
    public String getAltPrefix()
    {
        return "NONE".equalsIgnoreCase(altprefix) ? null : altprefix;
    }
    
    public String getToken()
    {
        return token;
    }
    
    public double getSkipRatio()
    {
        return skipratio;
    }
    
    public long getOwnerId()
    {
        return owner;
    }
    
    public String getSuccess()
    {
        return successEmoji;
    }
    
    public String getWarning()
    {
        return warningEmoji;
    }
    
    public String getError()
    {
        return errorEmoji;
    }
    
    public String getLoading()
    {
        return loadingEmoji;
    }
    
    public String getSearching()
    {
        return searchingEmoji;
    }
    
    public Activity getGame()
    {
        return game;
    }
    
    public boolean isGameNone()
    {
        return game != null && game.getName().equalsIgnoreCase("none");
    }
    
    public OnlineStatus getStatus()
    {
        return status;
    }
    
    public String getHelp()
    {
        return helpWord;
    }
    
    public boolean getStay()
    {
        return stayInChannel;
    }
    
    public boolean getSongInStatus()
    {
        return songInGame;
    }
    
    public String getPlaylistsFolder()
    {
        return playlistsFolder;
    }
    
    public boolean getDBots()
    {
        return dbots;
    }
    
    public boolean useUpdateAlerts()
    {
        return updatealerts;
    }

    public String getLogLevel()
    {
        return logLevel;
    }

    public boolean useEval()
    {
        return useEval;
    }
    
    public String getEvalEngine()
    {
        return evalEngine;
    }
    
    public boolean useNPImages()
    {
        return npImages;
    }
    
    public long getMaxSeconds()
    {
        return maxSeconds;
    }
    
    public int getMaxYTPlaylistPages()
    {
        return maxYTPlaylistPages;
    }
    
    public String getMaxTime()
    {
        return TimeUtil.formatTime(maxSeconds * 1000L);
    }

    public long getAloneTimeUntilStop()
    {
        return aloneTimeUntilStop;
    }
    
    public boolean isTooLong(AudioTrack track)
    {
        if(maxSeconds<=0)
            return false;
        return Math.round(track.getDuration()/1000.0) > maxSeconds;
    }

    public String[] getAliases(String command)
    {
        try
        {
            return aliases.getStringList(command).toArray(new String[0]);
        }
        catch(NullPointerException | ConfigException.Missing e)
        {
            return new String[0];
        }
    }
    
    public Config getTransforms()
    {
        return transforms;
    }

    public String getOauth2Token() {
        return oauth2Token;
    }

    public String getYtcUrl() {
        return ytcUrl;
    }
}
