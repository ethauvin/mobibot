/*
 * Mobibot.java
 *
 * Copyright (c) 2004-2020, Erik C. Thauvin (erik@thauvin.net)
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

package net.thauvin.erik.mobibot;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.commands.AbstractCommand;
import net.thauvin.erik.mobibot.commands.AddLog;
import net.thauvin.erik.mobibot.commands.ChannelFeed;
import net.thauvin.erik.mobibot.commands.Cycle;
import net.thauvin.erik.mobibot.commands.Ignore;
import net.thauvin.erik.mobibot.commands.Info;
import net.thauvin.erik.mobibot.commands.Me;
import net.thauvin.erik.mobibot.commands.Modules;
import net.thauvin.erik.mobibot.commands.Msg;
import net.thauvin.erik.mobibot.commands.Nick;
import net.thauvin.erik.mobibot.commands.Recap;
import net.thauvin.erik.mobibot.commands.Say;
import net.thauvin.erik.mobibot.commands.Users;
import net.thauvin.erik.mobibot.commands.Versions;
import net.thauvin.erik.mobibot.commands.links.Comment;
import net.thauvin.erik.mobibot.commands.links.Posting;
import net.thauvin.erik.mobibot.commands.links.Tags;
import net.thauvin.erik.mobibot.commands.links.UrlMgr;
import net.thauvin.erik.mobibot.commands.links.View;
import net.thauvin.erik.mobibot.commands.tell.Tell;
import net.thauvin.erik.mobibot.entries.EntriesMgr;
import net.thauvin.erik.mobibot.entries.EntryLink;
import net.thauvin.erik.mobibot.modules.AbstractModule;
import net.thauvin.erik.mobibot.modules.Calc;
import net.thauvin.erik.mobibot.modules.CurrencyConverter;
import net.thauvin.erik.mobibot.modules.Dice;
import net.thauvin.erik.mobibot.modules.GoogleSearch;
import net.thauvin.erik.mobibot.modules.Joke;
import net.thauvin.erik.mobibot.modules.Lookup;
import net.thauvin.erik.mobibot.modules.ModuleException;
import net.thauvin.erik.mobibot.modules.Ping;
import net.thauvin.erik.mobibot.modules.RockPaperScissors;
import net.thauvin.erik.mobibot.modules.StockQuote;
import net.thauvin.erik.mobibot.modules.Twitter;
import net.thauvin.erik.mobibot.modules.War;
import net.thauvin.erik.mobibot.modules.Weather2;
import net.thauvin.erik.mobibot.modules.WorldTime;
import net.thauvin.erik.mobibot.msg.Message;
import net.thauvin.erik.semver.Version;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.lowerCase;

/**
 * Implements the #mobitopia bot.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Jan 31, 2004
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
@Version(properties = "version.properties",
         className = "ReleaseInfo")
public class Mobibot extends PircBot {
    // Info strings
    @SuppressWarnings("indentation")
    public static final List<String> INFO =
            List.of(
                    ReleaseInfo.PROJECT + " v" + ReleaseInfo.VERSION
                    + " (" + Utils.green("https://www.mobitopia.org/mobibot/") + ')',
                    "Written by Erik C. Thauvin (" + Utils.green("https://erik.thauvin.net/") + ')');
    // Logger
    private static final Logger LOGGER = LogManager.getLogger(Mobibot.class);
    // Maximum number of times the bot will try to reconnect, if disconnected
    private static final int MAX_RECONNECT = 10;
    // Timer
    private static final Timer TIMER = new Timer(true);
    // Ignore command
    public final Ignore ignoreCommand;
    // Automatically post links to Twitter
    public final boolean isTwitterAutoPost;
    // Tell object
    public final Tell tell;
    // Commands
    private final List<AbstractCommand> commands = new ArrayList<>(20);
    // Commands Names
    private final List<String> commandsNames = new ArrayList<>();
    // Main channel
    private final String ircChannel;
    // IRC port
    private final int ircPort;
    // IRC server
    private final String ircServer;
    // Logger default level
    private final Level loggerLevel;
    // Log directory
    private final String logsDir;
    // Modules
    private final List<AbstractModule> modules = new ArrayList<>(0);
    // Modules
    private final List<String> modulesNames = new ArrayList<>(0);
    // Operators commands names
    private final List<String> opsCommandsNames = new ArrayList<>();
    // Today's date
    private final String today = Utils.today();
    // Twitter auto-posts.
    private final Set<Integer> twitterEntries = new HashSet<>();
    // Twitter handle for channel join notifications
    private final String twitterHandle;
    // Twitter module
    private final Twitter twitterModule;
    // Backlogs URL
    private String backLogsUrl = "";
    // Ident message
    private String identMsg = "";
    // Ident nick
    private String identNick = "";
    // NickServ ident password
    private String identPwd = "";
    // Pinboard posts handler
    private Pinboard pinboard;
    // Weblog URL
    private String weblogUrl = "";


    /**
     * Creates a new {@link Mobibot} instance.
     *
     * @param channel     The irc channel.
     * @param nickname    The bot's nickname.
     * @param logsDirPath The path to the logs directory.
     * @param p           The bot's properties.
     */
    public Mobibot(final String nickname, final String channel, final String logsDirPath, final Properties p) {
        super();
        System.getProperties().setProperty("sun.net.client.defaultConnectTimeout",
                                           String.valueOf(Constants.CONNECT_TIMEOUT));
        System.getProperties().setProperty("sun.net.client.defaultReadTimeout",
                                           String.valueOf(Constants.CONNECT_TIMEOUT));

        setName(nickname);

        ircServer = p.getProperty("server", Constants.DEFAULT_SERVER);
        ircPort = Utils.getIntProperty(p.getProperty("port"), Constants.DEFAULT_PORT);
        ircChannel = channel;
        logsDir = logsDirPath;

        // Set the logger level
        loggerLevel = LOGGER.getLevel();

        // Load the current entries and backlogs, if any
        try {
            UrlMgr.startup(logsDir + EntriesMgr.CURRENT_XML, logsDir + EntriesMgr.NAV_XML, ircChannel);
            LOGGER.debug("Last feed: {}", UrlMgr.getStartDate());
        } catch (Exception e) {
            LOGGER.error("An error occurred while loading the logs.", e);
        }

        // Initialize the bot
        setVerbose(true);
        setAutoNickChange(true);
        setLogin(p.getProperty("login", getName()));
        setVersion(p.getProperty("weblog", ""));
        // setMessageDelay(1000);
        setIdentity(p.getProperty("ident", ""), p.getProperty("ident-nick", ""), p.getProperty("ident-msg", ""));

        // Set the URLs
        setWeblogUrl(getVersion());
        setBacklogsUrl(Utils.ensureDir(p.getProperty("backlogs", weblogUrl), true));

        // Set the pinboard authentication
        setPinboardAuth(p.getProperty("pinboard-api-token"));

        // Load the commands
        addCommand(new AddLog());
        addCommand(new ChannelFeed(getChannelName(), p.getProperty("feed", "")));
        addCommand(new Cycle());
        addCommand(new Info());
        addCommand(new Me());
        addCommand(new Modules());
        addCommand(new Msg());
        addCommand(new Nick());
        addCommand(new Recap());
        addCommand(new Say());
        addCommand(new Users());
        addCommand(new Versions());

        // Ignore command
        ignoreCommand = new Ignore(p.getProperty("ignore", ""));
        addCommand(ignoreCommand);

        // Tell command
        tell = new Tell(this, p.getProperty("tell-max-days"), p.getProperty("tell-max-size"));
        if (tell.isEnabled()) {
            addCommand(tell);
        }

        // Load the links commands
        addCommand(new Comment());
        addCommand(new Posting());
        addCommand(new Tags());
        addCommand(new UrlMgr(p.getProperty("tags", ""), p.getProperty("tags-keywords", "")));
        addCommand(new View());

        // Load the modules
        addModule(new Calc());
        addModule(new CurrencyConverter());
        addModule(new Dice());
        addModule(new GoogleSearch());
        addModule(new Joke());
        addModule(new Lookup());
        addModule(new Ping());
        addModule(new RockPaperScissors());
        addModule(new StockQuote());
        addModule(new War());
        addModule(new Weather2());
        addModule(new WorldTime());

        // Twitter module
        twitterModule = new Twitter();
        addModule(twitterModule);

        // Load the modules properties
        modules.stream().filter(AbstractModule::hasProperties).forEach(module -> {
            for (final String s : module.getPropertyKeys()) {
                module.setProperty(s, p.getProperty(s, ""));
            }
        });

        // Twitter extra properties
        twitterHandle = p.getProperty(Constants.TWITTER_HANDLE_PROP, "");
        isTwitterAutoPost =
                Boolean.parseBoolean(p.getProperty(Constants.TWITTER_AUTOPOST_PROP, "false"))
                && twitterModule.isEnabled();

        // Sort the command & module names
        Collections.sort(commandsNames);
        Collections.sort(opsCommandsNames);
        Collections.sort(modulesNames);

        // Save the entries
        UrlMgr.saveEntries(this, true);
    }

    /**
     * The Truth Is Out There...
     *
     * @param args The command line arguments.
     */
    @SuppressFBWarnings(
            { "INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE",
              "DM_DEFAULT_ENCODING",
              "IOI_USE_OF_FILE_STREAM_CONSTRUCTORS" })
    @SuppressWarnings({ "PMD.SystemPrintln", "PMD.AvoidFileStream", "PMD.CloseResource" })
    public static void main(final String[] args) {
        // Setup the command line options
        final Options options = new Options()
                                        .addOption(Constants.HELP_ARG.substring(0, 1),
                                                   Constants.HELP_ARG,
                                                   false,
                                                   "print this help message")
                                        .addOption(Constants.DEBUG_ARG.substring(0, 1), Constants.DEBUG_ARG, false,
                                                   "print debug & logging data directly to the console")
                                        .addOption(Option.builder(Constants.PROPS_ARG.substring(0, 1)).hasArg()
                                                         .argName("file")
                                                         .desc("use " + "alternate properties file")
                                                         .longOpt(Constants.PROPS_ARG).build())
                                        .addOption(Constants.VERSION_ARG.substring(0, 1),
                                                   Constants.VERSION_ARG,
                                                   false,
                                                   "print version info");

        // Parse the command line
        final CommandLineParser parser = new DefaultParser();
        CommandLine line = null;

        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("CLI Parsing failed.  Reason: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        if (line.hasOption(Constants.HELP_ARG.charAt(0))) {
            // Output the usage
            new HelpFormatter().printHelp(Mobibot.class.getName(), options);
        } else if (line.hasOption(Constants.VERSION_ARG.charAt(0))) {
            for (final String s : INFO) {
                System.out.println(s);
            }
        } else {
            final Properties p = new Properties();

            try (final InputStream fis = Files.newInputStream(
                    Paths.get(line.getOptionValue(Constants.PROPS_ARG.charAt(0), "./mobibot.properties")))) {
                // Load the properties files
                p.load(fis);
            } catch (FileNotFoundException e) {
                System.err.println("Unable to find properties file.");
                e.printStackTrace(System.err);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to open properties file.");
                e.printStackTrace(System.err);
                System.exit(1);
            }

            final String nickname = p.getProperty("nick", lowerCase(Mobibot.class.getName()));
            final String channel = p.getProperty("channel");
            final String logsDir = Utils.ensureDir(p.getProperty("logs", "."), false);

            // Redirect the stdout and stderr
            if (!line.hasOption(Constants.DEBUG_ARG.charAt(0))) {
                try {
                    final PrintStream stdout = new PrintStream(
                            new FileOutputStream(logsDir + channel.substring(1) + '.' + Utils.today() + ".log", true));
                    System.setOut(stdout);
                } catch (IOException e) {
                    System.err.println("Unable to open output (stdout) log file.");
                    e.printStackTrace(System.err);
                    System.exit(1);
                }

                try {
                    final PrintStream stderr = new PrintStream(new FileOutputStream(logsDir + nickname + ".err", true));
                    System.setErr(stderr);
                } catch (IOException e) {
                    System.err.println("Unable to open error (stderr) log file.");
                    e.printStackTrace(System.err);
                    System.exit(1);
                }
            }

            // Create the bot
            final Mobibot bot = new Mobibot(nickname, channel, logsDir, p);

            // Connect
            bot.connect();
        }
    }

    /**
     * Sends an action to the current channel.
     *
     * @param action The action.
     */
    public final void action(final String action) {
        action(ircChannel, action);
    }

    /**
     * Sends an action to the channel.
     *
     * @param channel The channel.
     * @param action  The action.
     */
    private void action(final String channel, final String action) {
        if (isNotBlank(channel) && isNotBlank(action)) {
            sendAction(channel, action);
        }
    }

    /**
     * Adds a command.
     *
     * @param command The command to add.
     */
    private void addCommand(final AbstractCommand command) {
        commands.add(command);
        if (command.isVisible()) {
            if (command.isOp()) {
                opsCommandsNames.add(command.getCommand());
            } else {
                commandsNames.add(command.getCommand());
            }
        }
    }

    /**
     * Adds a module.
     *
     * @param module The module to add.
     */
    private void addModule(final AbstractModule module) {
        modules.add(module);
        modulesNames.add(module.getClass().getSimpleName());
        commandsNames.addAll(module.getCommands());
    }

    /**
     * Adds pin on pinboard.
     *
     * @param entry The entry to add.
     */
    public final void addPin(final EntryLink entry) {
        if (pinboard != null) {
            pinboard.addPost(entry);
        }
    }

    /**
     * Connects to the server and joins the channel.
     */
    @SuppressFBWarnings({ "DM_EXIT", "INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE" })
    public final void connect() {
        try {
            connect(ircServer, ircPort);
        } catch (Exception e) {
            int retries = 0;

            while ((retries++ < MAX_RECONNECT) && !isConnected()) {
                sleep(10);

                try {
                    connect(ircServer, ircPort);
                } catch (Exception ex) {
                    if (retries == MAX_RECONNECT) {
                        LOGGER.debug("Unable to reconnect to {} after {} retries.", ircServer, MAX_RECONNECT, ex);
                        e.printStackTrace(System.err);
                        System.exit(1);
                    }
                }
            }
        }
        setVersion(INFO.get(0));
        identify();
        joinChannel();
    }

    /**
     * Deletes pin on pinboard.
     *
     * @param entry The entry to delete.
     */
    public final void deletePin(final EntryLink entry) {
        if (pinboard != null) {
            pinboard.deletePost(entry);
        }
    }

    /**
     * Returns the backlogs URL.
     *
     * @return The backlogs URL.
     */
    public final String getBacklogsUrl() {
        return backLogsUrl;
    }

    /**
     * Returns the current channel.
     *
     * @return The current channel.
     */
    public final String getChannel() {
        return ircChannel;
    }

    /**
     * Returns the current channel name.
     *
     * @return The current channel name.
     */
    @SuppressFBWarnings("STT_STRING_PARSING_A_FIELD")
    public final String getChannelName() {
        return ircChannel.substring(1);
    }

    /**
     * Returns the irc server.
     *
     * @return The irc server.
     */
    public final String getIrcServer() {
        return ircServer;
    }

    /**
     * Returns the bot's logger.
     *
     * @return The bot's logger.
     */
    public final Logger getLogger() {
        return LOGGER;
    }

    /**
     * Returns the log directory.
     *
     * @return the log directory.
     */
    public final String getLogsDir() {
        return logsDir;
    }

    /**
     * Returns the enabled modules names.
     *
     * @return The modules names.
     */
    public final List<String> getModulesNames() {
        return modulesNames;
    }

    /**
     * Returns the bot's nickname regexp pattern.
     *
     * @return The nickname regexp pattern.
     */
    private String getNickPattern() {
        final StringBuilder buff = new StringBuilder(0);

        for (final char c : getNick().toCharArray()) {
            if (Character.isLetter(c)) {
                buff.append('[').append(lowerCase(String.valueOf(c))).append(StringUtils.upperCase(String.valueOf(c)))
                    .append(']');
            } else {
                buff.append(c);
            }
        }

        return buff.toString();
    }

    /**
     * Returns the bot's timer.
     *
     * @return The timer.
     */
    public final Timer getTimer() {
        return TIMER;
    }

    /**
     * Get today's date for the feed.
     *
     * @return Today's date.
     */
    public String getToday() {
        return today;
    }

    /**
     * Returns the weblog URL.
     *
     * @return The weblog URL.
     */
    public final String getWeblogUrl() {
        return weblogUrl;
    }

    /**
     * Responds with the commands help, if any.
     *
     * @param sender    The nick of the person requesting Constants.
     * @param topic     The help topic.
     * @param isPrivate The private flag.
     * @return {@code true} if the topic was found, {@code false} otherwise.
     */
    private boolean helpCommands(final String sender, final String topic, final boolean isPrivate) {
        for (final AbstractCommand command : commands) {
            if (command.isVisible() && command.getCommand().startsWith(topic)) {
                return command.helpResponse(this, topic, sender, isOp(sender), isPrivate);
            }
        }
        return false;
    }

    /**
     * Responds with the default Constants.
     *
     * @param sender    The nick of the person requesting Constants.
     * @param isOp      The channel operator flag.
     * @param isPrivate The private flag.
     */
    public void helpDefault(final String sender, final boolean isOp, final boolean isPrivate) {
        send(sender, "Type a URL on " + ircChannel + " to post it.", isPrivate);
        send(sender, "For more information on a specific command, type:", isPrivate);
        send(sender,
             Utils.helpIndent(Utils.helpFormat("%c " + Constants.HELP_CMD + " <command>", getNick(), isPrivate)),
             isPrivate);
        send(sender, "The commands are:", isPrivate);
        sendList(sender, commandsNames, 8, isPrivate, true);
        if (isOp) {
            send(sender, "The op commands are:", isPrivate);
            sendList(sender, opsCommandsNames, 8, isPrivate, true);
        }
    }

    /**
     * Responds with the modules help, if any.
     *
     * @param sender    The nick of the person requesting Constants.
     * @param topic     The help topic.
     * @param isPrivate The private flag.
     * @return {@code true} if the topic was found, {@code false} otherwise.
     */
    private boolean helpModules(final String sender, final String topic, final boolean isPrivate) {
        for (final AbstractModule module : modules) {
            if (module.isEnabled()) {
                for (final String cmd : module.getCommands()) {
                    if (topic.equals(cmd)) {
                        module.helpResponse(this, sender, isPrivate);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Responds with the bot's Constants.
     *
     * @param sender    The nick of the person who sent the private message.
     * @param topic     The help topic, if any.
     * @param isPrivate The private flag.
     */
    private void helpResponse(final String sender, final String topic, final boolean isPrivate) {
        final boolean isOp = isOp(sender);
        if (StringUtils.isBlank(topic)) {
            helpDefault(sender, isOp, isPrivate);
        } else {
            // Command, Modules or Default
            if (!helpCommands(sender, topic, isPrivate) && !helpModules(sender, lowerCase(topic).trim(), isPrivate)) {
                helpDefault(sender, isOp, isPrivate);
            }
        }
    }

    /**
     * Identifies the bot.
     */
    private void identify() {
        // Identify with NickServ
        if (isNotBlank(identPwd)) {
            identify(identPwd);
        }

        // Identify with a specified nick
        if (isNotBlank(identNick) && isNotBlank(identMsg)) {
            sendMessage(identNick, identMsg);
        }
    }

    /**
     * Returns <code>true</code> if the specified sender is an Op on the {@link #ircChannel channel}.
     *
     * @param sender The sender.
     * @return true, if the sender is an Op.
     */
    public boolean isOp(final String sender) {
        final User[] users = getUsers(ircChannel);

        for (final User user : users) {
            if (user.getNick().equals(sender)) {
                return user.isOp();
            }
        }

        return false;
    }

    /**
     * Joins the bot's channel.
     */
    public final void joinChannel() {
        joinChannel(ircChannel);
        twitterNotification("has joined " + ircChannel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onDisconnect() {
        if (isNotBlank(weblogUrl)) {
            setVersion(weblogUrl);
        }
        sleep(5);
        connect();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings(value = "CC_CYCLOMATIC_COMPLEXITY",
                        justification = "Working on it.")
    @Override
    protected final void onMessage(final String channel, final String sender, final String login, final String hostname,
                                   final String message) {
        LOGGER.debug(">>> {} : {}", sender, message);

        if (tell.isEnabled()) {
            tell.send(sender, true);
        }

        if (message.matches(getNickPattern() + ":.*")) { // mobibot: <command>
            final String[] cmds = message.substring(message.indexOf(':') + 1).trim().split(" ", 2);
            final String cmd = lowerCase(cmds[0]);

            String args = "";

            if (cmds.length > 1) {
                args = cmds[1].trim();
            }

            if (cmd.startsWith(Constants.HELP_CMD)) { // mobibot: help
                helpResponse(sender, args, false);
            } else {
                // Commands
                for (final AbstractCommand command : commands) {
                    if (command.isPublic() && command.getCommand().startsWith(cmd)) {
                        command.commandResponse(this, sender, login, args, isOp(sender), false);
                        return;
                    }
                }
                // Modules
                for (final AbstractModule module : modules) { // modules
                    for (final String c : module.getCommands()) {
                        if (cmd.startsWith(c)) {
                            module.commandResponse(this, sender, cmd, args, false);
                            return;
                        }
                    }
                }
            }
        } else {
            // Commands, e.g.: https://www.example.com/
            for (final AbstractCommand command : commands) {
                if (command.matches(message)) {
                    command.commandResponse(this, sender, login, message, isOp(sender), false);
                    return;
                }
            }
        }

        Recap.storeRecap(sender, message, false);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings(value = { "DM_EXIT", "CC_CYCLOMATIC_COMPLEXITY" },
                        justification = "Yes, we want to bail out.")
    @Override
    protected final void onPrivateMessage(final String sender, final String login, final String hostname,
                                          final String message) {
        LOGGER.debug(">>> {} : {}", sender, message);

        final String[] cmds = message.split(" ", 2);
        final String cmd = lowerCase(cmds[0]);
        String args = "";

        if (cmds.length > 1) {
            args = cmds[1].trim();
        }

        final boolean isOp = isOp(sender);

        if (cmd.startsWith(Constants.HELP_CMD)) { // help
            helpResponse(sender, args, true);
        } else if (isOp && "kill".equals(cmd)) { // kill
            sendRawLine("QUIT : Poof!");
            System.exit(0);
        } else if (isOp && Constants.DEBUG_CMD.equals(cmd)) { // debug
            if (LOGGER.isDebugEnabled()) {
                Configurator.setLevel(LOGGER.getName(), loggerLevel);
            } else {
                Configurator.setLevel(LOGGER.getName(), Level.DEBUG);
            }
            send(sender, "Debug logging is " + (LOGGER.isDebugEnabled() ? "enabled." : "disabled."), true);
        } else if (isOp && Constants.DIE_CMD.equals(cmd)) { // die
            send(sender + " has just signed my death sentence.");
            TIMER.cancel();
            twitterShutdown();
            twitterNotification("killed by  " + sender + " on " + ircChannel);
            sleep(3);
            quitServer("The Bot Is Out There!");
            System.exit(0);
        } else {
            for (final AbstractCommand command : commands) {
                if (command.getCommand().startsWith(cmd)) {
                    command.commandResponse(this, sender, login, args, isOp, true);
                    return;
                }
            }
            for (final AbstractModule module : modules) {
                if (module.isPrivateMsgEnabled()) {
                    for (final String c : module.getCommands()) {
                        if (cmd.equals(c)) {
                            module.commandResponse(this, sender, cmd, args, true);
                            return;
                        }
                    }
                }
            }
            helpDefault(sender, isOp, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onAction(final String sender, final String login, final String hostname, final String target,
                                  final String action) {
        if (ircChannel.equals(target)) {
            Recap.storeRecap(sender, action, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onJoin(final String channel, final String sender, final String login, final String hostname) {
        if (tell.isEnabled()) {
            tell.send(sender);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onNickChange(final String oldNick, final String login, final String hostname, final String newNick) {
        if (tell.isEnabled()) {
            tell.send(newNick);
        }
    }

    /**
     * Sends a private message or notice.
     *
     * @param sender    The channel or nick of the person who sent the message.
     * @param message   The actual message.
     * @param isPrivate Set to <code>true</code> if the response should be a private message, otherwise a notice is
     *                  sent.
     */
    public final void send(final String sender, final String message, final boolean isPrivate) {
        if (isNotBlank(message) && isNotBlank(sender)) {
            if (isPrivate) {
                LOGGER.debug("Sending message to {} : {}", sender, message);
                sendMessage(sender, message);
            } else {
                LOGGER.debug("Sending notice to {} : {}", sender, message);
                sendNotice(sender, message);
            }
        }
    }

    /**
     * Sends a notice to the channel.
     *
     * @param notice The notice message.
     */
    public final void send(final String notice) {
        send(getChannel(), notice, false);

    }

    /**
     * Sends a message.
     *
     * @param who     The channel or nick of the person who sent the command.
     * @param message The message.
     */
    public final void send(final String who, final Message message) {
        send(message.isNotice() ? who : getChannel(), message.getText(), message.getColor(), message.isPrivate());
    }

    /**
     * Sends a message.
     *
     * @param who       The channel or nick of the person who sent the command.
     * @param message   The actual message.
     * @param color     The message's color.
     * @param isPrivate The private flag.
     */
    public final void send(final String who, final String message, final String color, final boolean isPrivate) {
        send(who, Utils.colorize(message, color), isPrivate);
    }

    /**
     * Send a formatted commands/modules, etc. list.
     *
     * @param nick      The nick to send the list to.
     * @param list      The list to format.
     * @param size      The number of items per line.
     * @param isPrivate The private flag.
     * @param isBold    The bold flag
     */
    public final void sendList(final String nick,
                               final List<String> list,
                               final int size,
                               final boolean isPrivate,
                               final boolean isBold) {
        for (int i = 0; i < list.size(); i += size) {
            send(nick, Utils.helpIndent(
                    String.join(" ", list.subList(i, Math.min(list.size(), i + size))), isBold), isPrivate);
        }
    }

    /**
     * Sets the backlogs URL.
     *
     * @param url The backlogs URL.
     */
    final void setBacklogsUrl(final String url) {
        backLogsUrl = url;
    }

    /**
     * Sets the bot's identification.
     *
     * @param pwd  The password for NickServ, if any.
     * @param nick The ident nick name.
     * @param msg  The ident message.
     */
    final void setIdentity(final String pwd, final String nick, final String msg) {
        identPwd = pwd;
        identNick = nick;
        identMsg = msg;
    }

    /**
     * Sets the pinboard authentication.
     *
     * @param apiToken The API token
     */
    final void setPinboardAuth(final String apiToken) {
        if (isNotBlank(apiToken)) {
            pinboard = new Pinboard(this, apiToken);
        }
    }

    /**
     * Sets the weblog URL.
     *
     * @param url The weblog URL.
     */
    final void setWeblogUrl(final String url) {
        weblogUrl = url;
    }

    /**
     * Sleeps for the specified number of seconds.
     *
     * @param secs The number of seconds to sleep for.
     */
    public final void sleep(final int secs) {
        try {
            Thread.sleep(secs * 1000L);
        } catch (InterruptedException ignore) {
            // Do nothing
        }
    }

    /**
     * Add an entry to be posted on Twitter.
     *
     * @param index The entry index.
     */
    public void twitterAddEntry(final int index) {
        twitterEntries.add(index);
    }

    /**
     * Post an entry to twitter.
     *
     * @param index The post entry index.
     */
    @SuppressFBWarnings("SUI_CONTAINS_BEFORE_REMOVE")
    public final void twitterEntryPost(final int index) {
        if (isTwitterAutoPost && twitterEntries.contains(index) && UrlMgr.getEntriesCount() >= index) {
            final EntryLink entry = UrlMgr.getEntry(index);
            final String msg =
                    entry.getTitle() + ' ' + entry.getLink() + " via " + entry.getNick() + " on " + getChannel();
            new Thread(() -> {
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Posting {}{} to Twitter.", Constants.LINK_CMD, index + 1);
                    }
                    twitterModule.post(twitterHandle, msg, false);
                } catch (ModuleException e) {
                    LOGGER.warn("Failed to post entry on Twitter.", e);
                }
            }).start();
            twitterEntries.remove(index);
        }
    }

    /**
     * Return the total count of links to be posted to twitter.
     *
     * @return The count of twitter links.
     */
    public final int twitterLinksCount() {
        return twitterEntries.size();
    }

    /**
     * Send a notification to the registered Twitter handle.
     *
     * @param msg The twitter message.
     */
    final void twitterNotification(final String msg) {
        if (twitterModule.isEnabled() && isNotBlank(twitterHandle)) {
            new Thread(() -> {
                try {
                    twitterModule.post(
                            twitterHandle,
                            getName() + ' ' + ReleaseInfo.VERSION + " " + msg,
                            true);
                } catch (ModuleException e) {
                    LOGGER.warn("Failed to notify @{}: {}", twitterHandle, msg, e);
                }
            }).start();
        }
    }

    /**
     * Removes entry from Twitter auto-post.
     *
     * @param index The entry's index.
     */
    public final void twitterRemoveEntry(final int index) {
        twitterEntries.remove(index);
    }


    /**
     * Post all the links on twitter on shutdown.
     */
    final void twitterShutdown() {
        if (twitterModule.isEnabled() && isNotBlank(twitterHandle)) {
            LOGGER.debug("Twitter shutdown.");
            for (final int i : twitterEntries) {
                twitterEntryPost(i);
            }
        }
    }

    /**
     * Updates pin on pinboard.
     *
     * @param oldUrl The old pin url.
     * @param entry  The entry to update.
     */
    public final void updatePin(final String oldUrl, final EntryLink entry) {
        if (pinboard != null) {
            pinboard.updatePost(oldUrl, entry);
        }
    }
}
