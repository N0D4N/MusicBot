package moe.nodan.jmusicbot.utils

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import java.io.File

object Util {
    @JvmStatic
    fun getTrackName(info: AudioTrackInfo): String {
        var audioTitle: String = info.title

        if (audioTitle.equals("Unknown title", ignoreCase = true)) {
            try {
                audioTitle = File(info.uri).nameWithoutExtension
            } catch (e: Exception) { }
        }

        return audioTitle
    }
}