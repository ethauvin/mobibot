/*
 * Mobibot.kt
 *
 * Copyright (c) 2004-2021, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
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

import net.thauvin.erik.mobibot.PinboardUtils.addPin
import net.thauvin.erik.mobibot.PinboardUtils.deletePin
import net.thauvin.erik.mobibot.PinboardUtils.updatePin
import net.thauvin.erik.mobibot.Utils.buildCmdSyntax
import net.thauvin.erik.mobibot.Utils.colorize
import net.thauvin.erik.mobibot.Utils.ensureDir
import net.thauvin.erik.mobibot.Utils.getIntProperty
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.isoLocalDate
import net.thauvin.erik.mobibot.Utils.today
import net.thauvin.erik.mobibot.commands.AddLog
import net.thauvin.erik.mobibot.commands.ChannelFeed
import net.thauvin.erik.mobibot.commands.Cycle
import net.thauvin.erik.mobibot.commands.Debug
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
import net.thauvin.erik.mobibot.commands.links.LinksMgr
import net.thauvin.erik.mobibot.commands.links.Posting
import net.thauvin.erik.mobibot.commands.links.Tags
import net.thauvin.erik.mobibot.commands.links.View
import net.thauvin.erik.mobibot.commands.tell.Tell
import net.thauvin.erik.mobibot.entries.EntriesMgr
import net.thauvin.erik.mobibot.entries.EntryLink
import net.thauvin.erik.mobibot.modules.Bitcoin
import net.thauvin.erik.mobibot.modules.Calc
import net.thauvin.erik.mobibot.modules.CurrencyConverter
import net.thauvin.erik.mobibot.modules.Dice
import net.thauvin.erik.mobibot.modules.GoogleSearch
import net.thauvin.erik.mobibot.modules.Joke
import net.thauvin.erik.mobibot.modules.Lookup
import net.thauvin.erik.mobibot.modules.Ping
import net.thauvin.erik.mobibot.modules.RockPaperScissors
import net.thauvin.erik.mobibot.modules.StockQuote
import net.thauvin.erik.mobibot.modules.Twitter
import net.thauvin.erik.mobibot.modules.War
import net.thauvin.erik.mobibot.modules.Weather2
import net.thauvin.erik.mobibot.modules.WorldTime
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.pinboard.PinboardPoster
import net.thauvin.erik.semver.Version
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jibble.pircbot.PircBot
import java.io.BufferedOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.regex.Pattern
import kotlin.system.exitProcess

/**
 * Implements the #mobitopia bot.
 */
@Version(properties = "version.properties", className = "ReleaseInfo", template = "version.mustache", type = "kt")
class Mobibot(nickname: String, channel: String, logsDirPath: String, p: Properties) : PircBot() {
    // Commands and Modules
    private val addons = Addons()

    /** Main channel. */
    val channel: String

    // IRC port
    private val ircPort: Int

    /** IRC server. */
    val ircServer: String

    /** Logger. */
    val logger: Logger = LogManager.getLogger(Mobibot::class.java)

    /** Logger default level. */
    val loggerLevel: Level

    /** Log directory. */
    val logsDir: String

    // Pinboard posts handler
    private val pinboard: PinboardPoster = PinboardPoster()

    /** Tell command. */
    val tell: Tell

    /** Today's date. */
    val today = today()

    /** Twitter module. */
    val twitter: Twitter

    /** The backlogs URL. */
    val backlogsUrl: String

    // Ident message
    private val identMsg: String

    // Ident nick
    private val identNick: String

    // NickServ ident password
    private val identPwd: String

    // Is pinboard enabled?
    private var isPinboardEnabled = false

    /** Timer. */
    val timer = Timer(true)

    /** Weblog URL */
    val weblogUrl: String

    /** The current channel name. */
    private val channelName: String
        get() = channel.substring(1)

    /** The enabled modules names. */
    val modulesNames: List<String>
        get() = addons.modulesNames

    /**
     * Sends an action to the current channel.
     */
    fun action(action: String) {
        action(channel, action)
    }

    /**
     * Sends an action to the channel.
     */
    private fun action(channel: String, action: String) {
        if (channel.isNotBlank() && action.isNotBlank()) {
            sendAction(channel, action)
        }
    }

    /**
     * Adds pin on pinboard.
     */
    fun addPin(entry: EntryLink) {
        if (isPinboardEnabled) {
            addPin(pinboard, ircServer, entry)
        }
    }

    /**
     * Connects to the server and joins the channel.
     */
    fun connect() {
        try {
            connect(ircServer, ircPort)
        } catch (e: Exception) {
            if (logger.isDebugEnabled) {
                logger.debug("Unable to connect to $ircServer, will try again.", e)
            }
            var retries = 0
            while (retries++ < MAX_RECONNECT && !isConnected) {
                @Suppress("MagicNumber")
                sleep(10)
                try {
                    connect(ircServer, ircPort)
                } catch (ex: Exception) {
                    if (retries == MAX_RECONNECT) {
                        if (logger.isDebugEnabled) {
                            logger.debug("Unable to reconnect to $ircServer, after $MAX_RECONNECT retries.", ex)
                        }
                        System.err.println("An error has occurred while reconnecting; ${ex.message}")
                        exitProcess(1)
                    }
                }
            }
        }
        identify()
        joinChannel()
    }

    /**
     * Deletes pin on pinboard.
     */
    fun deletePin(index: Int, entry: EntryLink) {
        if (isPinboardEnabled) {
            deletePin(pinboard, entry)
        }
        if (twitter.isAutoPost) {
            twitter.removeEntry(index)
        }
    }

    /**
     * Responds with the default help.
     */
    @Suppress("MagicNumber")
    fun helpDefault(sender: String, isOp: Boolean, isPrivate: Boolean) {
        send(sender, "Type a URL on $channel to post it.", isPrivate)
        send(sender, "For more information on a specific command, type:", isPrivate)
        send(
            sender,
            helpFormat(buildCmdSyntax("%c ${Constants.HELP_CMD} <command>", nick, isPrivate)),
            isPrivate
        )
        send(sender, "The commands are:", isPrivate)
        sendList(sender, addons.names, 8, isPrivate, isBold = true, isIndent = true)
        if (isOp) {
            send(sender, "The op commands are:", isPrivate)
            sendList(sender, addons.ops, 8, isPrivate, isBold = true, isIndent = true)
        }
    }


    /**
     * Responds with the default, commands or modules help.
     */
    private fun helpResponse(sender: String, topic: String, isPrivate: Boolean) {
        val isOp = isOp(sender)
        if (topic.isBlank() || !addons.help(sender, topic.lowercase().trim(), isOp, isPrivate)) {
            helpDefault(sender, isOp, isPrivate)
        }
    }

    /**
     * Identifies the bot.
     */
    private fun identify() {
        // Identify with NickServ
        if (identPwd.isNotBlank()) {
            identify(identPwd)
        }
        // Identify with a specified nick
        if (identNick.isNotBlank() && identMsg.isNotBlank()) {
            sendMessage(identNick, identMsg)
        }
    }

    /**
     * Returns {@code true} if the specified sender is an Op on the [channel][.ircChannel].
     */
    fun isOp(sender: String): Boolean {
        for (user in getUsers(channel)) {
            if (user.nick == sender) {
                return user.isOp
            }
        }
        return false
    }

    /**
     * Joins the bot's channel.
     */
    private fun joinChannel() {
        joinChannel(channel)
        twitter.notification("$name ${ReleaseInfo.VERSION} has joined $channel")
    }

    override fun onDisconnect() {
        if (weblogUrl.isNotBlank()) {
            version = weblogUrl
        }
        @Suppress("MagicNumber")
        sleep(5)
        connect()
    }

    override fun onMessage(
        channel: String,
        sender: String,
        login: String,
        hostname: String,
        message: String
    ) {
        if (logger.isDebugEnabled) logger.debug(">>> $sender: $message")
        tell.send(sender, true)
        if (message.matches("(?i)${Pattern.quote(nick)}:.*".toRegex())) { // mobibot: <command>
            val cmds = message.substring(message.indexOf(':') + 1).trim().split(" ".toRegex(), 2)
            val cmd = cmds[0].lowercase()
            val args = if (cmds.size > 1) {
                cmds[1].trim()
            } else ""
            if (cmd.startsWith(Constants.HELP_CMD)) { // mobibot: help
                helpResponse(sender, args, false)
            } else {
                // Execute module or command
                addons.exec(sender, login, cmd, args, isOp(sender), false)
            }
        } else {
            // Links, e.g.: https://www.example.com/ or L1: , etc.
            addons.match(sender, login, message, isOp(sender), false)
        }
        storeRecap(sender, message, false)
    }

    override fun onPrivateMessage(
        sender: String,
        login: String,
        hostname: String,
        message: String
    ) {
        if (logger.isDebugEnabled) logger.debug(">>> $sender : $message")
        val cmds = message.split(" ".toRegex(), 2)
        val cmd = cmds[0].lowercase()
        val args = if (cmds.size > 1) {
            cmds[1].trim()
        } else ""
        val isOp = isOp(sender)
        if (cmd.startsWith(Constants.HELP_CMD)) { // help
            helpResponse(sender, args, true)
        } else if (isOp && Constants.KILL_CMD == cmd) { // kill
            twitter.notification("$name killed by $sender on $channel")
            sendRawLine("QUIT : Poof!")
            exitProcess(0)
        } else if (isOp && Constants.DIE_CMD == cmd) { // die
            send("$sender has just signed my death sentence.")
            timer.cancel()
            twitter.shutdown()
            twitter.notification("$name stopped by $sender on $channel")
            @Suppress("MagicNumber")
            sleep(3)
            quitServer("The Bot Is Out There!")
            exitProcess(0)
        } else if (!addons.exec(sender, login, cmd, args, isOp, true)) { // Execute command or module
            helpDefault(sender, isOp, true)
        }
    }

    override fun onAction(sender: String, login: String, hostname: String, target: String, action: String) {
        if (channel == target) {
            storeRecap(sender, action, true)
        }
    }

    override fun onJoin(channel: String, sender: String, login: String, hostname: String) {
        tell.send(sender)
    }

    override fun onNickChange(oldNick: String, login: String, hostname: String, newNick: String) {
        tell.send(newNick)
    }

    /**
     * Sends a private message or notice.
     */
    fun send(sender: String, message: String?, isPrivate: Boolean) {
        if (message != null && sender.isNotBlank()) {
            if (isPrivate) {
                if (logger.isDebugEnabled) logger.debug("Sending message to $sender : $message")
                sendMessage(sender, message)
            } else {
                if (logger.isDebugEnabled) logger.debug("Sending notice to $sender: $message")
                sendNotice(sender, message)
            }
        }
    }

    /**
     * Sends a notice to the channel.
     */
    fun send(notice: String?) {
        notice?.let { send(channel, it, false) }
    }

    /**
     * Sends a message.
     */
    fun send(who: String, message: Message) {
        send(if (message.isNotice) who else channel, message.msg, message.color, message.isPrivate)
    }

    /**
     * Sends a message.
     */
    fun send(who: String, message: String, color: String, isPrivate: Boolean) {
        send(who, colorize(message, color), isPrivate)
    }

    /**
     * Send a formatted commands/modules, etc. list.
     */
    @JvmOverloads
    fun sendList(
        nick: String,
        list: List<String>,
        maxPerLine: Int,
        isPrivate: Boolean,
        isBold: Boolean = false,
        isIndent: Boolean = false
    ) {
        var i = 0
        while (i < list.size) {
            send(
                nick,
                helpFormat(
                    list.subList(i, list.size.coerceAtMost(i + maxPerLine)).joinToString(" ", truncated = ""),
                    isBold,
                    isIndent
                ),
                isPrivate
            )
            i += maxPerLine
        }
    }

    /**
     * Sets the pinboard authentication.
     */
    private fun setPinboardAuth(apiToken: String) {
        if (apiToken.isNotBlank()) {
            pinboard.apiToken = apiToken
            isPinboardEnabled = true
            if (logger.isDebugEnabled) {
                val consoleHandler = ConsoleHandler()
                consoleHandler.level = java.util.logging.Level.FINE
                pinboard.logger.addHandler(consoleHandler)
                pinboard.logger.level = java.util.logging.Level.FINE
            }
        }
    }

    /**
     * Sleeps for the specified number of seconds.
     */
    fun sleep(secs: Int) {
        try {
            @Suppress("MagicNumber")
            Thread.sleep(secs * 1000L)
        } catch (ignore: InterruptedException) {
            // Do nothing
        }
    }

    /**
     * Updates pin on pinboard.
     */
    fun updatePin(oldUrl: String, entry: EntryLink) {
        if (isPinboardEnabled) {
            updatePin(pinboard, ircServer, oldUrl, entry)
        }
    }

    /**
     * Returns the bot's version.
     */
    override fun onVersion(sourceNick: String, sourceLogin: String, sourceHostname: String, target: String) {
        sendRawLine("NOTICE " + sourceNick + " :\u0001VERSION " + ReleaseInfo.PROJECT + ' ' + ReleaseInfo.VERSION
                    + "\u0001")
    }

    companion object {
        // Maximum number of times the bot will try to reconnect, if disconnected
        private const val MAX_RECONNECT = 10

        /**
         * The Truth is Out There!
         */
        @JvmStatic
        fun main(args: Array<String>) {
            // Setup the command line options
            val options = Options()
                .addOption(
                    Constants.HELP_ARG.substring(0, 1),
                    Constants.HELP_ARG,
                    false,
                    "print this help message"
                )
                .addOption(
                    Constants.DEBUG_ARG.substring(0, 1), Constants.DEBUG_ARG, false,
                    "print debug & logging data directly to the console"
                )
                .addOption(
                    Option.builder(Constants.PROPS_ARG.substring(0, 1)).hasArg()
                        .argName("file")
                        .desc("use alternate properties file")
                        .longOpt(Constants.PROPS_ARG).build()
                )
                .addOption(
                    Constants.VERSION_ARG.substring(0, 1),
                    Constants.VERSION_ARG,
                    false,
                    "print version info"
                )

            // Parse the command line
            val parser: CommandLineParser = DefaultParser()
            val commandLine: CommandLine
            try {
                commandLine = parser.parse(options, args)
            } catch (e: ParseException) {
                System.err.println("CLI Parsing failed. Reason: ${e.message}")
                exitProcess(1)
            }
            when {
                commandLine.hasOption(Constants.HELP_ARG[0]) -> {
                    // Output the usage
                    HelpFormatter().printHelp(Mobibot::class.java.name, options)
                }
                commandLine.hasOption(Constants.VERSION_ARG[0]) -> {
                    // Output the version
                    println("${ReleaseInfo.PROJECT} ${ReleaseInfo.VERSION} (${isoLocalDate(ReleaseInfo.BUILDDATE)})")
                    println(ReleaseInfo.WEBSITE)
                }
                else -> {
                    // Load the properties
                    val p = Properties()
                    try {
                        Files.newInputStream(
                            Paths.get(commandLine.getOptionValue(Constants.PROPS_ARG[0], "./mobibot.properties"))
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
                    val logsDir = ensureDir(p.getProperty("logs", "."), false)

                    // Redirect stdout and stderr
                    if (!commandLine.hasOption(Constants.DEBUG_ARG[0])) {
                        try {
                            val stdout = PrintStream(
                                BufferedOutputStream(
                                    FileOutputStream(
                                        logsDir + channel.substring(1) + '.' + today() + ".log", true
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

                    // Create the bot
                    val bot = Mobibot(nickname, channel, logsDir, p)

                    // Connect
                    bot.connect()
                }
            }
        }
    }

    /**
     * Initialize the bot.
     */
    init {
        System.getProperties().setProperty("sun.net.client.defaultConnectTimeout", Constants.CONNECT_TIMEOUT.toString())
        System.getProperties().setProperty("sun.net.client.defaultReadTimeout", Constants.CONNECT_TIMEOUT.toString())

        name = nickname
        ircServer = p.getProperty("server", Constants.DEFAULT_SERVER)
        ircPort = getIntProperty(p.getProperty("port"), Constants.DEFAULT_PORT)
        this.channel = channel
        logsDir = logsDirPath

        // Store the default logger level
        loggerLevel = logger.level

        setVerbose(true)
        setAutoNickChange(true)
        login = p.getProperty("login", name)
        // Set the real name
        version = ReleaseInfo.PROJECT
        // setMessageDelay(1000);

        // Set NICKSERV identification
        identPwd = p.getProperty("ident", "")
        identNick = p.getProperty("ident-nick", "")
        identMsg = p.getProperty("ident-msg", "")

        // Set the URLs
        weblogUrl = p.getProperty("weblog", "")
        backlogsUrl = ensureDir(p.getProperty("backlogs", weblogUrl), true)

        // Load the current entries and backlogs, if any
        try {
            LinksMgr.startup(logsDir + EntriesMgr.CURRENT_XML, logsDir + EntriesMgr.NAV_XML, this.channel)
            if (logger.isDebugEnabled) logger.debug("Last feed: ${LinksMgr.startDate}")
        } catch (e: Exception) {
            logger.error("An error occurred while loading the logs.", e)
        }

        // Set the pinboard authentication
        setPinboardAuth(p.getProperty("pinboard-api-token", ""))

        // Load the commands
        addons.add(AddLog(this), p)
        addons.add(ChannelFeed(this, channelName), p)
        addons.add(Cycle(this), p)
        addons.add(Debug(this), p)
        addons.add(Ignore(this), p)
        addons.add(Info(this), p)
        addons.add(Me(this), p)
        addons.add(Modules(this), p)
        addons.add(Msg(this), p)
        addons.add(Nick(this), p)
        addons.add(Recap(this), p)
        addons.add(Say(this), p)
        addons.add(Users(this), p)
        addons.add(Versions(this), p)

        // Tell command
        tell = Tell(this)
        addons.add(tell, p)

        // Load the links commands
        addons.add(Comment(this), p)
        addons.add(Posting(this), p)
        addons.add(Tags(this), p)
        addons.add(LinksMgr(this), p)
        addons.add(View(this), p)

        // Load the modules
        addons.add(Bitcoin(this), p)
        addons.add(Calc(this), p)
        addons.add(CurrencyConverter(this), p)
        addons.add(Dice(this), p)
        addons.add(GoogleSearch(this), p)
        addons.add(Joke(this), p)
        addons.add(Lookup(this), p)
        addons.add(Ping(this), p)
        addons.add(RockPaperScissors(this), p)
        addons.add(StockQuote(this), p)
        addons.add(War(this), p)
        addons.add(Weather2(this), p)
        addons.add(WorldTime(this), p)

        // Twitter module
        twitter = Twitter(this)
        addons.add(twitter, p)

        // Sort the addons
        addons.sort()

        // Save the entries
        LinksMgr.saveEntries(this, true)
    }
}
