package moe.nodan.jmusicbot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.audio.AudioHandler
import com.jagrosh.jmusicbot.settings.Settings
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.interactions.InteractionContextType

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 * @author N0D4N
 */
abstract class MusicCommand(val bot: Bot) : Command() {
    protected var bePlaying: Boolean = false
    protected var beListening: Boolean = false

    init {
        this.contexts = arrayOf(InteractionContextType.GUILD)
        this.category = Category("Music")
    }

    override fun execute(event: CommandEvent) {
        val settings = event.client.getSettingsFor<Settings>(event.guild)
        val tchannel = settings.getTextChannel(event.guild)
        if (tchannel != null && event.textChannel != tchannel) {
            try {
                event.message.delete().queue()
            } catch (ignore: PermissionException) {
            }
            event.replyInDm(
                event.client.error + " You can only use that command in " + tchannel.asMention + "!"
            )
            return
        }
        bot.playerManager.setUpHandler(event.guild) // no point constantly checking for this later
        if (bePlaying && !(event.guild.audioManager.sendingHandler as AudioHandler).isMusicPlaying(event.jda)
        ) {
            event.reply(event.client.error + " There must be music playing to use that!")
            return
        }
        if (beListening) {
            var current = event.guild.selfMember.voiceState?.channel?.asVoiceChannel()

            if (current == null)
                current = settings.getVoiceChannel(event.guild)

            val userState = event.member.voiceState
            if (userState != null && !userState.inAudioChannel() || userState!!.isDeafened || (current != null && userState.channel != current)) {
                event.replyError("You must be listening in " + (if (current == null) "a voice channel" else current.asMention) + " to use that!")
                return
            }

            val afkChannel = userState.guild.afkChannel
            if (afkChannel != null && afkChannel == userState.channel) {
                event.replyError("You cannot use that command in an AFK channel!")
                return
            }

            val selfVoiceState = event.guild.selfMember.voiceState

            if (selfVoiceState != null && !selfVoiceState.inAudioChannel()) {
                try {
                    event.guild.audioManager.openAudioConnection(userState.channel!!)
                } catch (ex: PermissionException) {
                    event.reply(
                        event.client.error + " I am unable to connect to " + userState.channel!!
                            .asMention + "!"
                    )
                    return
                }
            }
        }

        doCommand(event)
    }

    abstract fun doCommand(event: CommandEvent?)
}