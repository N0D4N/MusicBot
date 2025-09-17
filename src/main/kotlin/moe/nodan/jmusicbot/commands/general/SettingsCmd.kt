package moe.nodan.jmusicbot.commands.general

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.settings.QueueType
import com.jagrosh.jmusicbot.settings.RepeatMode
import com.jagrosh.jmusicbot.settings.Settings
import com.jagrosh.jmusicbot.utils.FormatUtil
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 * @author N0D4N
 */
class SettingsCmd(bot: Bot) : Command() {
    init {
        this.name = "settings"
        this.help = "shows the bots settings"
        this.aliases = bot.config.getAliases(this.name)
        this.contexts = arrayOf(InteractionContextType.GUILD)
    }

    override fun execute(event: CommandEvent) {
        val s: Settings = event.client.getSettingsFor<Settings>(event.guild)
        val builder = MessageCreateBuilder()
            .addContent(EMOJI + " **")
            .addContent(FormatUtil.filter(event.selfUser.name))
            .addContent("** settings:")
        val tchan: TextChannel? = s.getTextChannel(event.guild)
        val vchan = s.getVoiceChannel(event.guild)
        val role = s.getRole(event.guild)
        val ebuilder = EmbedBuilder()
            .setColor(event.selfMember.color)
            .setDescription(
                ("Text Channel: " + (if (tchan == null) "Any" else "**#" + tchan.name + "**")
                        + "\nVoice Channel: " + (if (vchan == null) "Any" else vchan.asMention)
                        + "\nDJ Role: " + (if (role == null) "None" else "**" + role.name + "**")
                        + "\nCustom Prefix: " + (if (s.prefix == null) "None" else "`" + s.prefix + "`")
                        + "\nRepeat Mode: " + (if (s.repeatMode == RepeatMode.OFF)
                    s.repeatMode.userFriendlyName
                else
                    "**" + s.repeatMode.userFriendlyName + "**")
                        + "\nQueue Type: " + (if (s.queueType == QueueType.FAIR)
                    s.queueType.userFriendlyName
                else
                    "**" + s.queueType.userFriendlyName + "**")
                        + "\nDefault Playlist: " + (if (s.defaultPlaylist == null) "None" else "**" + s.defaultPlaylist + "**"))
            )
            .setFooter(
                (event.jda.guilds.size.toString() + " servers | "
                        + event.jda.guilds.stream()
                    .filter { g: Guild? -> g!!.selfMember.voiceState?.inAudioChannel() == true }.count()
                        + " audio connections"), null
            )
        event.channel.sendMessage(builder.setEmbeds(ebuilder.build()).build()).queue()
    }

    companion object {
        private const val EMOJI = "\uD83C\uDFA7" // 🎧
    }
}