package moe.nodan.jmusicbot.audio

import com.jagrosh.jmusicbot.Bot
import net.dv8tion.jda.api.entities.Activity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern

class AnisonUpdateTask(private val bot: Bot) : TimerTask() {
    override fun run() = try {
        val json = readJsonFromUrl()
        val onAir = REMOVE_TAGS.matcher(json.getString("on_air")).replaceAll("")
        val s = Dash.matcher(InLive.matcher(onAir).replaceAll("")).replaceAll("—")
        bot.jda.presence.activity = Activity.listening(s)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    companion object {
        private val url: URL = URI("https://anison.fm/status.php?widget=true").toURL()

        private val REMOVE_TAGS: Pattern = Pattern.compile("<.+?>")
        private val InLive: Pattern = Pattern.compile("В эфире: ")
        private val Dash: Pattern = Pattern.compile("&#151;")

        @Throws(IOException::class)
        private fun readAll(rd: Reader): String {
            val sb = StringBuilder(512)
            var cp: Int
            while ((rd.read().also { cp = it }) != -1) {
                sb.append(cp.toChar())
            }
            return sb.toString()
        }

        @Throws(IOException::class)
        fun readJsonFromUrl(): JSONObject {
            url.openStream().use {
                val rd = BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8))
                val jsonText = readAll(rd)
                return JSONObject(jsonText)
            }
        }
    }
}