/*
 * Mobibot.kt
 *
 * Copyright 2004-2023 Erik C. Thauvin (erik@thauvin.net)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.thauvin.erik.mobibot

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import net.thauvin.erik.mobibot.Utils.appendIfMissing
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.capitalise
import net.thauvin.erik.mobibot.Utils.getIntProperty
import net.thauvin.erik.mobibot.Utils.helpCmdSyntax
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.isChannelOp
import net.thauvin.erik.mobibot.Utils.lastOrEmpty
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.Utils.toIsoLocalDate
import net.thauvin.erik.mobibot.commands.ChannelFeed
import net.thauvin.erik.mobibot.commands.Cycle
import net.thauvin.erik.mobibot.commands.Die
import net.thauvin.erik.mobibot.commands.Ignore
import net.thauvin.erik.mobibot.commands.Info
import net.thauvin.erik.mobibot.commands.Me
import net.thauvin.erik.mobibot.commands.Modules
import net.thauvin.erik.mobibot.commands.Msg
import net.thauvin.erik.mobibot.commands.Nick
import net.thauvin.erik.mobibot.commands.Recap
import net.thauvin.erik.mobibot.commands.Recap.Companion.storeRecap
import net.thauvin.erik.mobibot.commands.Say
import net.thauvin.erik.mobibot.commands.Users
import net.thauvin.erik.mobibot.commands.Versions
import net.thauvin.erik.mobibot.commands.links.Comment
import net.thauvin.erik.mobibot.commands.links.LinksManager
import net.thauvin.erik.mobibot.commands.links.Posting
import net.thauvin.erik.mobibot.commands.links.Tags
import net.thauvin.erik.mobibot.commands.links.View
import net.thauvin.erik.mobibot.commands.seen.Seen
import net.thauvin.erik.mobibot.commands.tell.Tell
import net.thauvin.erik.mobibot.modules.Calc
import net.thauvin.erik.mobibot.modules.ChatGpt
import net.thauvin.erik.mobibot.modules.CryptoPrices
import net.thauvin.erik.mobibot.modules.CurrencyConverter
import net.thauvin.erik.mobibot.modules.Dice
import net.thauvin.erik.mobibot.modules.GoogleSearch
import net.thauvin.erik.mobibot.modules.Joke
import net.thauvin.erik.mobibot.modules.Lookup
import net.thauvin.erik.mobibot.modules.Mastodon
import net.thauvin.erik.mobibot.modules.Ping
import net.thauvin.erik.mobibot.modules.RockPaperScissors
import net.thauvin.erik.mobibot.modules.StockQuote
import net.thauvin.erik.mobibot.modules.Twitter
import net.thauvin.erik.mobibot.modules.War
import net.thauvin.erik.mobibot.modules.Weather2
import net.thauvin.erik.mobibot.modules.WolframAlpha
import net.thauvin.erik.mobibot.modules.WorldTime
import net.thauvin.erik.semver.Version
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.ActionEvent
import org.pircbotx.hooks.events.DisconnectEvent
import org.pircbotx.hooks.events.JoinEvent
import org.pircbotx.hooks.events.MessageEvent
import org.pircbotx.hooks.events.NickChangeEvent
import org.pircbotx.hooks.events.PartEvent
import org.pircbotx.hooks.events.PrivateMessageEvent
import org.pircbotx.hooks.events.QuitEvent
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties
import java.util.regex.Pattern
import kotlin.system.exitProcess

@Version(properties = "version.properties", className = "ReleaseInfo", template = "version.mustache", type = "kt")
class Mobibot(nickname: String, val channel: String, logsDirPath: String, p: Properties) : ListenerAdapter() {
    // The bot configuration.
    private val config: Configuration

    // Commands and Modules
    private val addons: Addons

    // Seen command
    private val seen: Seen

    // Tell command
    private val tell: Tell

    /** Logger. */
    val logger: Logger = LoggerFactory.getLogger(Mobibot::class.java)

    /**
     * Connects to the server and joins the channel.
     */
    fun connect() {
        PircBotX(config).startBot()
    }

    /**
     * Responds with the default help.
     */
    private fun helpDefault(event: GenericMessageEvent) {
        event.sendMessage("Type a URL on $channel to post it.")
        event.sendMessage("For more information on a specific command, type:")
        event.sendMessage(
            helpFormat(
                helpCmdSyntax("%c ${Constants.HELP_CMD} <command>", event.bot().nick, event is PrivateMessageEvent)
            )
        )
        event.sendMessage("The commands are:")
        event.sendList(addons.names.commands, 8, isBold = true, isIndent = true)
        if (event.isChannelOp(channel)) {
            event.sendMessage("The op commands are:")
            event.sendList(addons.names.ops, 8, isBold = true, isIndent = true)
        }
    }

    /**
     * Responds with the default, commands or modules help.
     */
    private fun helpResponse(event: GenericMessageEvent, topic: String) {
        if (topic.isBlank() || !addons.help(channel, topic.lowercase().trim(), event)) {
            helpDefault(event)
        }
    }

    override fun onAction(event: ActionEvent?) {
        event?.channel?.let {
            if (channel == it.name) {
                event.user?.let { user ->
                    storeRecap(user.nick, event.action, true)
                }
            }
        }
    }

    override fun onDisconnect(event: DisconnectEvent?) {
        event?.let {
            with(event.getBot<PircBotX>()) {
                LinksManager.socialManager.notification("$nick disconnected from $serverHostname")
                seen.add(userChannelDao.getChannel(channel).users)
            }
        }
        LinksManager.socialManager.shutdown()
    }

    override fun onPrivateMessage(event: PrivateMessageEvent?) {
        event?.user?.let { user ->
            if (logger.isTraceEnabled) logger.trace("<<< ${user.nick}: ${event.message}")
            val cmds = event.message.trim().split(" ".toRegex(), 2)
            val cmd = cmds[0].lowercase()
            val args = cmds.lastOrEmpty().trim()
            if (cmd.startsWith(Constants.HELP_CMD)) { // help
                helpResponse(event, args)
            } else if (!addons.exec(channel, cmd, args, event)) { // Execute command or module
                helpDefault(event)
            }
        }
    }

    override fun onJoin(event: JoinEvent?) {
        event?.user?.let { user ->
            with(event.getBot<PircBotX>()) {
                if (user.nick == nick) {
                    LinksManager.socialManager.notification(
                        "$nick has joined ${event.channel.name} on $serverHostname"
                    )
                    seen.add(userChannelDao.getChannel(channel).users)
                } else {
                    tell.send(event)
                    seen.add(user.nick)
                }
            }
        }
    }

    override fun onMessage(event: MessageEvent?) {
        event?.user?.let { user ->
            tell.send(event)
            if (event.message.matches("(?i)${Pattern.quote(event.bot().nick)}:.*".toRegex())) { // mobibot: <command>
                if (logger.isTraceEnabled) logger.trace(">>> ${user.nick}: ${event.message}")
                val cmds = event.message.substring(event.bot().nick.length + 1).trim().split(" ".toRegex(), 2)
                val cmd = cmds[0].lowercase()
                val args = cmds.lastOrEmpty().trim()
                if (cmd.startsWith(Constants.HELP_CMD)) { // mobibot: help
                    helpResponse(event, args)
                } else {
                    // Execute module or command
                    addons.exec(channel, cmd, args, event)
                }
            } else if (addons.match(channel, event)) { // Links, e.g.: https://www.example.com/ or L1: , etc.
                if (logger.isTraceEnabled) logger.trace(">>> ${user.nick}: ${event.message}")
            }
            storeRecap(user.nick, event.message, false)
            seen.add(user.nick)
        }
    }

    override fun onNickChange(event: NickChangeEvent?) {
        event?.let {
            tell.send(event)
            if (!it.oldNick.equals(it.newNick, true)) {
                seen.add(it.oldNick)
            }
            seen.add(it.newNick)
        }
    }

    override fun onPart(event: PartEvent?) {
        event?.user?.let { user ->
            with(event.getBot<PircBotX>()) {
                if (user.nick == nick) {
                    LinksManager.socialManager.notification(
                        "$nick has left ${event.channel.name} on $serverHostname"
                    )
                    seen.add(userChannelDao.getChannel(channel).users)
                } else {
                    seen.add(user.nick)
                }
            }
        }
    }

    override fun onQuit(event: QuitEvent?) {
        event?.user?.let { user ->
            seen.add(user.nick)
        }
    }

    companion object {
        @JvmStatic
        @Throws(Exception::class)
        fun main(args: Array<String>) {
            // Set up the command line options
            val parser = ArgParser(Constants.CLI_CMD)
            val debug by parser.option(
                ArgType.Boolean,
                Constants.DEBUG_ARG,
                Constants.DEBUG_ARG.substring(0, 1),
                "Print debug & logging data directly to the console"
            ).default(false)
            val property by parser.option(
                ArgType.String,
                Constants.PROPS_ARG,
                Constants.PROPS_ARG.substring(0, 1),
                "Use alternate properties file"
            ).default("./${ReleaseInfo.PROJECT}.properties")
            val version by parser.option(
                ArgType.Boolean,
                Constants.VERSION_ARG,
                Constants.VERSION_ARG.substring(0, 1),
                "Print version info"
            ).default(false)

            // Parse the command line
            parser.parse(args)

            if (version) {
                // Output the version
                println(
                    "${ReleaseInfo.PROJECT.capitalise()} ${ReleaseInfo.VERSION}" +
                            " (${ReleaseInfo.BUILDDATE.toIsoLocalDate()})"
                )
                println(ReleaseInfo.WEBSITE)
            } else {
                // Load the properties
                val p = Properties()
                try {
                    Files.newInputStream(
                        Paths.get(property)
                    ).use { fis ->
                        p.load(fis)
                    }
                } catch (ignore: FileNotFoundException) {
                    System.err.println("Unable to find properties file.")
                    exitProcess(1)
                } catch (ignore: IOException) {
                    System.err.println("Unable to open properties file.")
                    exitProcess(1)
                }
                val nickname = p.getProperty("nick", Mobibot::class.java.name.lowercase())
                val channel = p.getProperty("channel")
                val logsDir = p.getProperty("logs", ".").appendIfMissing(File.separatorChar)

                // Redirect stdout and stderr
                if (!debug) {
                    try {
                        val stdout = PrintStream(
                            BufferedOutputStream(
                                FileOutputStream(
                                    logsDir + channel.substring(1) + '.' + Utils.today() + ".log", true
                                )
                            ), true
                        )
                        System.setOut(stdout)
                    } catch (ignore: IOException) {
                        System.err.println("Unable to open output (stdout) log file.")
                        exitProcess(1)
                    }
                    try {
                        val stderr = PrintStream(
                            BufferedOutputStream(
                                FileOutputStream("$logsDir$nickname.err", true)
                            ), true
                        )
                        System.setErr(stderr)
                    } catch (ignore: IOException) {
                        System.err.println("Unable to open error (stderr) log file.")
                        exitProcess(1)
                    }
                }

                // Start the bot
                Mobibot(nickname, channel, logsDir, p).connect()
            }
        }
    }

    /**
     * Initialize the bot.
     */
    init {
        val ircServer = p.getProperty("server", Constants.DEFAULT_SERVER)
        config = Configuration.Builder().apply {
            name = nickname
            login = p.getProperty("login", nickname)
            realName = p.getProperty("realname", nickname)
            addServer(
                ircServer,
                p.getIntProperty("port", Constants.DEFAULT_PORT)
            )
            addAutoJoinChannel(channel)
            addListener(this@Mobibot)
            version = "${ReleaseInfo.PROJECT} ${ReleaseInfo.VERSION}"
            isAutoNickChange = true
            val identPwd = p.getProperty("ident")
            if (!identPwd.isNullOrBlank()) {
                nickservPassword = identPwd
            }
            val identNick = p.getProperty("ident-nick")
            if (!identNick.isNullOrBlank()) {
                nickservNick = identNick
            }
            val identMsg = p.getProperty("ident-msg")
            if (!identMsg.isNullOrBlank()) {
                nickservCustomMessage = identMsg
            }
            isAutoReconnect = true

            //socketConnectTimeout = Constants.CONNECT_TIMEOUT
            //socketTimeout = Constants.CONNECT_TIMEOUT
            //messageDelay = StaticDelay(500)
        }.buildConfiguration()

        // Load the current entries
        with(LinksManager) {
            entries.channel = channel
            entries.ircServer = ircServer
            entries.logsDir = logsDirPath
            entries.backlogs = p.getProperty("backlogs", "")
            entries.load()

            // Set up pinboard
            pinboard.setApiToken(p.getProperty("pinboard-api-token", ""))
        }

        addons = Addons(p)

        // Load the commands
        addons.add(ChannelFeed(channel.removePrefix("#")))
        addons.add(Comment())
        addons.add(Cycle())
        addons.add(Die())
        addons.add(Ignore())
        addons.add(LinksManager())
        addons.add(Me())
        addons.add(Modules(addons.names.modules))
        addons.add(Msg())
        addons.add(Nick())
        addons.add(Posting())
        addons.add(Recap())
        addons.add(Say())

        // Seen command
        seen = Seen("${logsDirPath}${nickname}-seen.ser")
        addons.add(seen)

        addons.add(Tags())

        // Tell command
        tell = Tell("${logsDirPath}${nickname}.ser")
        addons.add(tell)

        addons.add(Users())
        addons.add(Versions())
        addons.add(View())

        // Load social modules
        LinksManager.socialManager.add(addons, Twitter(), Mastodon())

        // Load the modules
        addons.add(Calc())
        addons.add(ChatGpt())
        addons.add(CryptoPrices())
        addons.add(CurrencyConverter())
        addons.add(Dice())
        addons.add(GoogleSearch())
        addons.add(Info(tell, seen))
        addons.add(Joke())
        addons.add(Lookup())
        addons.add(Ping())
        addons.add(RockPaperScissors())
        addons.add(StockQuote())
        addons.add(War())
        addons.add(Weather2())
        addons.add(WolframAlpha())
        addons.add(WorldTime())

        // Sort the addons
        addons.names.sort()
    }
}

