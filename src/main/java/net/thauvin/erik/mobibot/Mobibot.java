/*
 * Mobibot.java
 *
 * Copyright (c) 2004-2018, Erik C. Thauvin (erik@thauvin.net)
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

import com.rometools.rome.io.FeedException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.modules.*;
import net.thauvin.erik.semver.Version;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implements the #mobitopia bot.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Jan 31, 2004
 * @since 1.0
 */
@Version(properties = "version.properties", className = "ReleaseInfo")
public class Mobibot extends PircBot {
    /**
     * The connect/read timeout in ms.
     */
    public static final int CONNECT_TIMEOUT = 5000;

    // The empty title string.
    static final String NO_TITLE = "No Title";

    // The default port.
    private static final int DEFAULT_PORT = 6667;

    // The default server.
    private static final String DEFAULT_SERVER = "irc.freenode.net";

    // The info strings.
    private static final String[] INFO_STRS = {
            ReleaseInfo.PROJECT + " v" + ReleaseInfo.VERSION + " by Erik C. Thauvin (erik@thauvin.net)",
            "https://www.mobitopia.org/mobibot/"
    };

    // The link match string.
    private static final String LINK_MATCH = "^[hH][tT][tT][pP](|[sS])://.*";

    // The default maximum number of entries to display.
    private static final int MAX_ENTRIES = 8;

    // The default maximum recap entries.
    private static final int MAX_RECAP = 10;

    // The maximum number of times the bot will try to reconnect, if disconnected.
    private static final int MAX_RECONNECT = 10;

    // The number of milliseconds to delay between consecutive messages.
    private static final long MESSAGE_DELAY = 1000L;

    // The modules.
    private static final List<AbstractModule> MODULES = new ArrayList<>(0);

    // The start time.
    private static final long START_TIME = System.currentTimeMillis();

    // The tags/categories marker.
    private static final String TAGS_MARKER = "tags:";

    // The version strings.
    private static final String[] VERSION_STRS = {
            "Version: "
                    + ReleaseInfo.VERSION
                    + " ("
                    + Utils.isoLocalDate(ReleaseInfo.BUILDDATE) + ')',
            "Platform: "
                    + System.getProperty("os.name")
                    + " ("
                    + System.getProperty("os.version")
                    + ", "
                    + System.getProperty("os.arch")
                    + ", "
                    + System.getProperty("user.country") + ')',
            "Runtime: "
                    + System.getProperty("java.runtime.name")
                    + " (build "
                    + System.getProperty("java.runtime.version")
                    + ')',
            "VM: "
                    + System.getProperty("java.vm.name")
                    + " (build "
                    + System.getProperty("java.vm.version")
                    + ", "
                    + System.getProperty("java.vm.info")
                    + ')'
    };

    // The tell object.
    private static Tell tell;
    // The commands list.
    private final List<String> commandsList = new ArrayList<>();
    // The entries array.
    private final List<EntryLink> entries = new ArrayList<>(0);
    // The history/backlogs array.
    private final List<String> history = new ArrayList<>(0);
    // The ignored nicks array.
    private final List<String> ignoredNicks = new ArrayList<>(0);
    // The main channel.
    private final String ircChannel;
    // The IRC port.
    private final int ircPort;

    // The IRC server.
    private final String ircServer;

    // The logger.
    private final Logger logger = LogManager.getLogger(Mobibot.class);

    // The logger default level.
    private final Level loggerLevel;

    // The log directory.
    private final String logsDir;

    // The recap array.
    private final List<String> recap = new ArrayList<>(0);

    // The backlogs URL.
    private String backLogsUrl = "";

    // The default tags/categories.
    private String defaultTags = "";

    // The feed URL.
    private String feedURL = "";

    // The ident message.
    private String identMsg = "";

    // The ident nick.
    private String identNick = "";

    // The NickServ ident password.
    private String identPwd = "";

    // The pinboard posts handler.
    private Pinboard pinboard = null;

    // Today's date.
    private String today = Utils.today();

    // The weblog URL.
    private String weblogUrl = "";

    /**
     * Creates a new {@link Mobibot} instance.
     *
     * @param channel     The irc channel.
     * @param nickname    The bot's nickname.
     * @param logsDirPath The path to the logs directory.
     * @param p           The bot's properties.
     */
    @SuppressWarnings("WeakerAccess")
    public Mobibot(final String nickname, final String channel, final String logsDirPath, final Properties p) {
        System.getProperties().setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(CONNECT_TIMEOUT));
        System.getProperties().setProperty("sun.net.client.defaultReadTimeout", String.valueOf(CONNECT_TIMEOUT));

        setName(nickname);

        ircServer = p.getProperty("server", DEFAULT_SERVER);
        ircPort = Utils.getIntProperty(p.getProperty("port"), DEFAULT_PORT);
        ircChannel = channel;
        logsDir = logsDirPath;

        // Set the logger level
        loggerLevel = logger.getLevel();

        // Load the current entries, if any.
        try {
            today = EntriesMgr.loadEntries(logsDir + EntriesMgr.CURRENT_XML, ircChannel, entries);

            if (logger.isDebugEnabled()) {
                logger.debug("Last feed: " + today);
            }

            if (!Utils.today().equals(today)) {
                entries.clear();
                today = Utils.today();
            }
        } catch (IOException ignore) {
            ; // Do nothing.
        } catch (FeedException e) {
            logger.error("An error occurred while parsing the '" + EntriesMgr.CURRENT_XML + "' file.", e);
        }

        // Load the backlogs, if any.
        try {
            EntriesMgr.loadBacklogs(logsDir + EntriesMgr.NAV_XML, history);
        } catch (IOException ignore) {
            ; // Do nothing.
        } catch (FeedException e) {
            logger.error("An error occurred while parsing the '" + EntriesMgr.NAV_XML + "' file.", e);
        }

        // Initialize the bot
        setVerbose(true);
        setAutoNickChange(true);
        setLogin(p.getProperty("login", getName()));
        setVersion(p.getProperty("weblog", ""));
        setMessageDelay(MESSAGE_DELAY);
        setIdentity(p.getProperty("ident", ""), p.getProperty("ident-nick", ""),
                p.getProperty("ident-msg", ""));

        // Set the URLs
        setWeblogUrl(getVersion());
        setFeedURL(p.getProperty("feed", ""));
        setBacklogsUrl(Utils.ensureDir(p.getProperty("backlogs", weblogUrl), true));

        // Set the pinboard authentication
        setPinboardAuth(p.getProperty("pinboard-api-token"));

        // Load the modules
        MODULES.add(new Calc());
        MODULES.add(new CurrencyConverter());
        MODULES.add(new Dice());
        MODULES.add(new GoogleSearch());
        MODULES.add(new Joke());
        MODULES.add(new Lookup());
        MODULES.add(new Ping());
        MODULES.add(new StockQuote());
        MODULES.add(new Twitter());
        MODULES.add(new War());
        MODULES.add(new Weather2());
        MODULES.add(new WorldTime());

        // Load the modules properties
        MODULES.stream().filter(AbstractModule::hasProperties).forEach(
                module -> {
                    for (final String s : module.getPropertyKeys()) {
                        module.setProperty(s, p.getProperty(s, ""));
                    }
                });

        // Get the tell command settings
        tell = new Tell(this, p.getProperty("tell-max-days"), p.getProperty("tell-max-size"));

        // Set the tags
        setTags(p.getProperty("tags", ""));

        // Set the ignored nicks
        setIgnoredNicks(p.getProperty("ignore", ""));

        // Save the entries
        saveEntries(true);
    }

    /**
     * The Truth Is Out There...
     *
     * @param args The command line arguments.
     */
    @SuppressFBWarnings(value = {"INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE", "DM_DEFAULT_ENCODING"})
    public static void main(final String[] args) {
        // Setup the command line options
        final Options options = new Options();
        options.addOption(Commands.HELP_ARG.substring(0, 1), Commands.HELP_ARG, false, "print this help message");
        options.addOption(Commands.DEBUG_ARG.substring(0, 1), Commands.DEBUG_ARG, false,
            "print debug & logging data directly to the console");
        options.addOption(Option.builder(
            Commands.PROPS_ARG.substring(0, 1)).hasArg().argName("file").desc("use " + "alternate properties file")
            .longOpt(Commands.PROPS_ARG).build());
        options.addOption(Commands.VERSION_ARG.substring(0, 1), Commands.VERSION_ARG, false, "print version info");

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

        if (line.hasOption(Commands.HELP_ARG.charAt(0))) {
            // Output the usage
            new HelpFormatter().printHelp(Mobibot.class.getName(), options);
        } else if (line.hasOption(Commands.VERSION_ARG.charAt(0))) {
            for (final String s : INFO_STRS) {
                System.out.println(s);
            }
        } else {
            final Properties p = new Properties();

            try (final FileInputStream fis = new FileInputStream(
                    new File(line.getOptionValue(Commands.PROPS_ARG.charAt(0), "./mobibot.properties")))) {
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

            final String nickname = p.getProperty("nick", Mobibot.class.getName().toLowerCase());
            final String channel = p.getProperty("channel");
            final String logsDir = Utils.ensureDir(p.getProperty("logs", "."), false);

            // Redirect the stdout and stderr
            if (!line.hasOption(Commands.DEBUG_ARG.charAt(0))) {
                PrintStream stdout = null;

                try {
                    stdout = new PrintStream(new FileOutputStream(
                            logsDir + channel.substring(1) + '.' + Utils.today() + ".log", true));
                } catch (IOException e) {
                    System.err.println("Unable to open output (stdout) log file.");
                    e.printStackTrace(System.err);
                    System.exit(1);
                }

                PrintStream stderr = null;

                try {
                    stderr = new PrintStream(
                            new FileOutputStream(logsDir + nickname + ".err", true));
                } catch (IOException e) {
                    System.err.println("Unable to open error (stderr) log file.");
                    e.printStackTrace(System.err);
                    System.exit(1);
                }

                System.setOut(stdout);
                System.setErr(stderr);
            }

            // Create the bot
            final Mobibot bot = new Mobibot(nickname, channel, logsDir, p);

            // Connect
            bot.connect();
        }
    }

    /**
     * Sleeps for the specified number of seconds.
     *
     * @param secs The number of seconds to sleep for.
     */
    private static void sleep(final int secs) {
        try {
            Thread.sleep((long) (secs * 1000));
        } catch (InterruptedException ignore) {
            ; // Do nothing
        }
    }

    /**
     * Sends an action to the current channel.
     *
     * @param action The action.
     */
    final public void action(final String action) {
        action(ircChannel, action);
    }

    /**
     * Sends an action to the channel.
     *
     * @param channel The channel.
     * @param action  The action.
     */
    private void action(final String channel, final String action) {
        if (Utils.isValidString(channel) && Utils.isValidString(action)) {
            sendAction(channel, action);
        }
    }

    /**
     * Connects to the server and joins the channel.
     */
    @SuppressFBWarnings(value = {"DM_EXIT", "INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE"})
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
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Unable to reconnect to " + ircServer + " after " + MAX_RECONNECT + " retries.",
                                    ex);
                        }

                        e.printStackTrace(System.err);
                        System.exit(1);
                    }
                }
            }
        }

        setVersion(INFO_STRS[0]);

        identify();

        joinChannel();
    }

    /**
     * Responds with the title and links from the RSS feed.
     *
     * @param sender The nick of the person who sent the private message.
     */
    private void feedResponse(final String sender) {
        if (Utils.isValidString(feedURL)) {
            new Thread(new FeedReader(this, sender, feedURL)).start();
        } else {
            send(sender, "There is no weblog setup for this channel.");
        }
    }

    /**
     * Returns the index of the specified duplicate entry, if any.
     *
     * @param link The link.
     * @return The index or -1 if none.
     */
    private int findDupEntry(final String link) {
        synchronized (entries) {
            for (int i = 0; i < entries.size(); i++) {
                if (link.equals(entries.get(i).getLink())) {
                    return i;
                }
            }
        }

        return -1;
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
     * Sets the backlogs URL.
     *
     * @param url The backlogs URL.
     */
    private void setBacklogsUrl(final String url) {
        backLogsUrl = url;
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
        return logger;
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
     * Returns the bot's nickname regexp pattern.
     *
     * @return The nickname regexp pattern.
     */
    private String getNickPattern() {
        final StringBuilder buff = new StringBuilder(0);

        for (final char c : getNick().toCharArray()) {
            if (Character.isLetter(c)) {
                buff.append('[')
                        .append(String.valueOf(c).toLowerCase())
                        .append(String.valueOf(c).toUpperCase())
                        .append(']');
            } else {
                buff.append(c);
            }
        }

        return buff.toString();
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
     * Sets the weblog URL.
     *
     * @param url The weblog URL.
     */
    private void setWeblogUrl(final String url) {
        weblogUrl = url;
    }

    /**
     * Returns indented and bold help string.
     *
     * @param help The help string.
     * @return The indented help string.
     */
    final public String helpIndent(final String help) {
        return helpIndent(help, true);
    }

    /**
     * Returns indented help string.
     *
     * @param help   The help string.
     * @param isBold The bold flag.
     * @return The indented help string.
     */
    public String helpIndent(final String help, final boolean isBold) {
        return "        " + (isBold ? Utils.bold(help) : help);
    }

    /**
     * Responds with the bot's help.
     *
     * @param sender The nick of the person who sent the private message.
     * @param topic  The help topic, if any.
     */
    private void helpResponse(final String sender, final String topic) {
        final String lcTopic = topic.toLowerCase().trim();

        if (lcTopic.equals(Commands.HELP_POSTING_KEYWORD)) {
            send(sender, Utils.bold("Post a URL, by saying it on a line on its own:"));
            send(sender, helpIndent("<url> [<title>] [" + TAGS_MARKER + "<+tag> [...]]"));
            send(sender, "I will reply with a label, for example: " + Utils.bold(Commands.LINK_CMD + '1'));
            send(sender, "To add a title, use a its label and a pipe:");
            send(sender, helpIndent(Commands.LINK_CMD + "1:|This is the title"));
            send(sender, "To add a comment: ");
            send(sender, helpIndent(Commands.LINK_CMD + "1:This is a comment"));
            send(sender, "I will reply with a label, for example: " + Utils.bold(Commands.LINK_CMD + "1.1"));
            send(sender, "To edit a comment, use its label: ");
            send(sender, helpIndent(Commands.LINK_CMD + "1.1:This is an edited comment"));
            send(sender, "To delete a comment, use its label and a minus sign: ");
            send(sender, helpIndent(Commands.LINK_CMD + "1.1:-"));
            send(sender, "You can also view a posting by saying its label.");
        } else if (lcTopic.equals(Commands.HELP_TAGS_KEYWORD)) {
            send(sender, Utils.bold("To categorize or tag a URL, use its label and a T:"));
            send(sender, helpIndent(Commands.LINK_CMD + "1T:<+tag|-tag> [...]"));
        } else if (lcTopic.equals(Commands.VIEW_CMD)) {
            send(sender, "To list or search the current URL posts:");
            send(sender, helpIndent(getNick() + ": " + Commands.VIEW_CMD) + " [<start>] [<query>]");
        } else if (lcTopic.equals(ircChannel.substring(1).toLowerCase())) {
            send(sender, "To list the last 5 posts from the channel's weblog:");
            send(sender, helpIndent(getNick() + ": " + ircChannel.substring(1)));
        } else if (lcTopic.equals(Commands.RECAP_CMD)) {
            send(sender, "To list the last 10 public channel messages:");
            send(sender, helpIndent(getNick() + ": " + Commands.RECAP_CMD));
        } else if (lcTopic.equals(Commands.USERS_CMD)) {
            send(sender, "To list the users present on the channel:");
            send(sender, helpIndent(getNick() + ": " + Commands.USERS_CMD));
        } else if (lcTopic.equals(Commands.INFO_CMD)) {
            send(sender, "To view information about the bot:");
            send(sender, helpIndent(getNick() + ": " + Commands.INFO_CMD));
        } else if (lcTopic.equals(Commands.CYCLE_CMD) && isOp(sender)) {
            send(sender, "To have the bot leave the channel and come back:");
            send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.CYCLE_CMD));
        } else if (lcTopic.equals(Commands.ME_CMD) && isOp(sender)) {
            send(sender, "To have the bot perform an action:");
            send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.ME_CMD + " <action>"));
        } else if (lcTopic.equals(Commands.SAY_CMD) && isOp(sender)) {
            send(sender, "To have the bot say something on the channel:");
            send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.SAY_CMD + " <text>"));
        } else if (lcTopic.equals(Commands.VERSION_CMD) && isOp(sender)) {
            send(sender, "To view the version data (bot, java, etc.):");
            send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.VERSION_CMD));
        } else if (lcTopic.equals(Commands.MSG_CMD) && isOp(sender)) {
            send(sender, "To have the bot send a private message to someone:");
            send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.MSG_CMD + " <nick> <text>"));
        } else if (lcTopic.equals(Commands.IGNORE_CMD)) {
            send(sender, "To check your ignore status:");
            send(sender, helpIndent(getNick() + ": " + Commands.IGNORE_CMD));

            send(sender, "To toggle your ignore status:");
            send(sender, helpIndent(getNick() + ": " + Commands.IGNORE_CMD + ' ' + Commands.IGNORE_ME_KEYWORD));
        } else if (lcTopic.equals(Tell.TELL_CMD) && tell.isEnabled()) {
            tell.helpResponse(sender);
        } else {
            for (final AbstractModule module : MODULES) {
                for (final String cmd : module.getCommands()) {
                    if (lcTopic.equals(cmd)) {
                        module.helpResponse(this, sender, topic, true);
                        return;
                    }
                }
            }

            send(sender, Utils.bold("Type a URL on " + ircChannel + " to post it."));
            send(sender, "For more information on a specific command, type:");
            send(sender, helpIndent(getNick() + ": " + Commands.HELP_CMD + " <command>"));
            send(sender, "The commands are:");

            if (commandsList.isEmpty()) {
                commandsList.add(Commands.IGNORE_CMD);
                commandsList.add(Commands.INFO_CMD);
                commandsList.add(ircChannel.substring(1));
                commandsList.add(Commands.HELP_POSTING_KEYWORD);
                commandsList.add(Commands.HELP_TAGS_KEYWORD);
                commandsList.add(Commands.RECAP_CMD);
                commandsList.add(Commands.USERS_CMD);
                commandsList.add(Commands.VIEW_CMD);

                MODULES.stream().filter(AbstractModule::isEnabled).forEach(
                        module -> commandsList.addAll(module.getCommands()));

                if (tell.isEnabled()) {
                    commandsList.add(Tell.TELL_CMD);
                }

                Collections.sort(commandsList);
            }

            final StringBuilder sb = new StringBuilder(0);

            for (int i = 0, cmdCount = 1; i < commandsList.size(); i++, cmdCount++) {
                if (sb.length() > 0) {
                    sb.append("  ");
                }

                sb.append(commandsList.get(i));

                // 6 commands per line or last command
                if (sb.length() > 0 && (cmdCount == 6 || i == (commandsList.size() - 1))) {
                    send(sender, helpIndent(sb.toString()));

                    sb.setLength(0);
                    cmdCount = 0;
                }
            }

            if (isOp(sender)) {
                send(sender, "The op commands are:");
                send(sender, helpIndent(
                        Commands.CYCLE_CMD + "  "
                                + Commands.ME_CMD + "  "
                                + Commands.MSG_CMD + "  "
                                + Commands.SAY_CMD + "  "
                                + Commands.VERSION_CMD));
            }
        }
    }

    /**
     * Identifies the bot.
     */
    private void identify() {
        // Identify with NickServ
        if (Utils.isValidString(identPwd)) {
            identify(identPwd);
        }

        // Identify with a specified nick
        if (Utils.isValidString(identNick) && Utils.isValidString(identMsg)) {
            sendMessage(identNick, identMsg);
        }
    }

    /**
     * Processes the {@link net.thauvin.erik.mobibot.Commands#IGNORE_CMD} command.
     *
     * @param sender The sender.
     * @param args   The command arguments.
     */
    private void ignoreResponse(final String sender, final String args) {
        if (!isOp(sender)) {
            final String nick = sender.toLowerCase();
            final boolean isMe = args.toLowerCase().startsWith(Commands.IGNORE_ME_KEYWORD);

            if (ignoredNicks.contains(nick)) {
                if (isMe) {
                    ignoredNicks.remove(nick);

                    send(sender, "You are no longer ignored.");
                } else {
                    send(sender, "You are currently ignored.");
                }
            } else {
                if (isMe) {
                    ignoredNicks.add(nick);

                    send(sender, "You are now ignored.");
                } else {
                    send(sender, "You are not currently ignored.");
                }
            }
        } else {
            if (args.length() > 0) {
                final String[] nicks = args.toLowerCase().split(" ");

                for (final String nick : nicks) {
                    final String ignore;

                    if (Commands.IGNORE_ME_KEYWORD.equals(nick)) {
                        ignore = sender.toLowerCase();
                    } else {
                        ignore = nick;
                    }

                    if (ignoredNicks.contains(ignore)) {
                        ignoredNicks.remove(ignore);
                    } else {
                        ignoredNicks.add(ignore);
                    }
                }
            }

            send(sender, "The following nicks are ignored: " + ignoredNicks.toString());
        }
    }

    /**
     * Responds with the bot's information.
     *
     * @param sender    The nick of the person who sent the message.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    private void infoResponse(final String sender, final boolean isPrivate) {
        for (final String info : INFO_STRS) {
            if (info.startsWith("https://")) {
                send(sender, Utils.green(info), isPrivate);
            } else {
                send(sender, info, isPrivate);
            }
        }

        final StringBuilder info = new StringBuilder("Uptime: ");

        long timeInSeconds = (System.currentTimeMillis() - START_TIME) / 1000L;

        final long years = timeInSeconds / 31540000L;

        if (years > 0) {
            info.append(years).append(Utils.plural(years, " year ", " years "));
            timeInSeconds -= (years * 31540000L);
        }

        final long weeks = timeInSeconds / 604800L;

        if (weeks > 0) {
            info.append(weeks).append(Utils.plural(weeks, " week ", " weeks "));
            timeInSeconds -= (weeks * 604800L);
        }

        final long days = timeInSeconds / 86400L;

        if (days > 0) {
            info.append(days).append(Utils.plural(days, " day ", " days "));
            timeInSeconds -= (days * 86400L);
        }

        final long hours = timeInSeconds / 3600L;

        if (hours > 0) {
            info.append(hours).append(Utils.plural(hours, " hour ", " hours "));
            timeInSeconds -= (hours * 3600L);
        }

        final long minutes = timeInSeconds / 60L;

        info.append(minutes).append(Utils.plural(minutes, " minute ", " minutes "));

        info.append("[Entries: ").append(entries.size());

        if (tell.isEnabled() && isOp(sender)) {
            info.append(", Messages: ").append(tell.size());
        }

        info.append(']');

        send(sender, info.toString(), isPrivate);
    }

    /**
     * Determines whether the specified nick should be ignored.
     *
     * @param nick The nick.
     * @return <code>true</code> if the nick should be ignored, <code>false</code> otherwise.
     */
    private boolean isIgnoredNick(final String nick) {
        return Utils.isValidString(nick) && ignoredNicks.contains(nick.toLowerCase());
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onAction(final String sender,
                                  final String login,
                                  final String hostname,
                                  final String target,
                                  final String action) {
        if (target.equals(ircChannel)) {
            storeRecap(sender, action, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onDisconnect() {
        if (Utils.isValidString(weblogUrl)) {
            setVersion(weblogUrl);
        }

        sleep(5);

        connect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onJoin(final String channel, final String sender, final String login, final String hostname) {
        tell.send(sender);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onMessage(final String channel,
                                   final String sender,
                                   final String login,
                                   final String hostname,
                                   final String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> " + sender + ": " + message);
        }

        boolean isCommand = false;

        // Capture URLs posted on the channel
        if (message.matches(LINK_MATCH) && !isIgnoredNick(sender)) {
            isCommand = true;

            final String[] cmds = message.split(" ", 2);

            if (cmds.length == 1 || (!cmds[1].contains(getNick()))) {
                final String link = cmds[0].trim();
                boolean isBackup = false;

                final int dupIndex = findDupEntry(link);

                if (dupIndex == -1) {
                    if (!Utils.today().equals(today)) {
                        isBackup = true;
                        saveEntries(true);

                        entries.clear();
                        today = Utils.today();
                    }

                    final StringBuilder tags = new StringBuilder(defaultTags);
                    String title = NO_TITLE;

                    if (cmds.length == 2) {
                        final String[] data = cmds[1].trim().split(TAGS_MARKER, 2);

                        if (data.length == 1) {
                            title = data[0].trim();
                        } else {
                            if (Utils.isValidString(data[0])) {
                                title = data[0].trim();
                            }

                            tags.append(' ').append(data[1].trim());
                        }
                    }

                    if (NO_TITLE.equals(title)) {
                        try {
                            final Document html = Jsoup.connect(link).userAgent("Mozilla").get();
                            final String htmlTitle = html.title();

                            if (Utils.isValidString(htmlTitle)) {
                                title = htmlTitle;
                            }
                        } catch (IOException ignore) {
                            // Do nothing
                        }
                    }

                    entries.add(new EntryLink(link, title, sender, login, channel, tags.toString()));

                    final int index = entries.size() - 1;
                    final EntryLink entry = entries.get(index);
                    send(channel, Utils.buildLink(index, entry));

                    if (pinboard != null) {
                        pinboard.addPost(entry);
                    }

                    saveEntries(isBackup);

                    if (NO_TITLE.equals(entry.getTitle())) {
                        send(sender, "Please specify a title, by typing:", true);
                        send(sender, helpIndent(Commands.LINK_CMD + (index + 1) + ":|This is the title"), true);
                    }
                } else {
                    final EntryLink entry = entries.get(dupIndex);
                    send(sender, Utils.bold("Duplicate") + " >> " + Utils.buildLink(dupIndex, entry));
                }
            }
        }
        // mobibot: <command>
        else if (message.matches(getNickPattern() + ":.*")) {
            isCommand = true;

            final String[] cmds = message.substring(message.indexOf(':') + 1).trim().split(" ", 2);
            final String cmd = cmds[0].toLowerCase();

            String args = "";

            if (cmds.length > 1) {
                args = cmds[1].trim();
            }

            // mobibot: help
            if (cmd.startsWith(Commands.HELP_CMD)) {
                helpResponse(sender, args);
            }
            // mobibot: recap
            else if (cmd.equals(Commands.RECAP_CMD)) {
                recapResponse(sender, false);
            }
            // mobibot: users
            else if (cmd.equals(Commands.USERS_CMD)) {
                usersResponse(sender, false);
            }
            // mobibot: info
            else if (cmd.equals(Commands.INFO_CMD)) {
                infoResponse(sender, false);
            }
            // mobbiot: version
            else if (cmd.equals(Commands.VERSION_CMD)) {
                versionResponse(sender, false);
            }
            // mobibot: <channel>
            else if (cmd.equalsIgnoreCase(channel.substring(1))) {
                feedResponse(sender);
            }
            // mobibot: view
            else if (cmd.startsWith(Commands.VIEW_CMD)) {
                viewResponse(sender, args, false);
            }
            // mobibot: tell
            else if (cmd.startsWith(Tell.TELL_CMD) && tell.isEnabled()) {
                tell.response(sender, args);
            }
            // mobibot: ignore
            else if (cmd.startsWith(Commands.IGNORE_CMD)) {
                ignoreResponse(sender, args);
            }
            // modules
            else {
                for (final AbstractModule module : MODULES) {
                    for (final String c : module.getCommands()) {
                        if (cmd.startsWith(c)) {
                            module.commandResponse(this, sender, args, false);
                        }
                    }
                }
            }
        }
        // L1:<comment>, L1:-, L1:|<title>, etc.
        else if (message.matches(Commands.LINK_CMD + "[0-9]+:.*")) {
            isCommand = true;

            final String[] cmds = message.substring(1).split(":", 2);
            final int index = Integer.parseInt(cmds[0]) - 1;

            // L1:<comment>
            if (index < entries.size()) {
                final String cmd = cmds[1].trim();

                if (cmd.length() == 0) {
                    final EntryLink entry = entries.get(index);
                    send(channel, Utils.buildLink(index, entry));

                    if (entry.hasTags()) {
                        send(channel, Utils.buildTags(index, entry));
                    }

                    if (entry.hasComments()) {
                        final EntryComment[] comments = entry.getComments();

                        for (int i = 0; i < comments.length; i++) {
                            send(channel, Utils.buildComment(index, i, comments[i]));
                        }
                    }
                } else {
                    // L1:-
                    if ("-".equals(cmd)) {
                        final EntryLink entry = entries.get(index);

                        if (entry.getLogin().equals(login) || isOp(sender)) {
                            if (pinboard != null) {
                                pinboard.deletePost(entry);
                            }

                            entries.remove(index);
                            send(channel, "Entry " + Commands.LINK_CMD + (index + 1) + " removed.");
                            saveEntries(false);
                        } else {
                            send(sender, "Please ask a channel op to remove this entry for you.");
                        }
                    }
                    // L1:|<title>
                    else if (cmd.charAt(0) == '|') {
                        if (cmd.length() > 1) {
                            final EntryLink entry = entries.get(index);
                            entry.setTitle(cmd.substring(1).trim());

                            if (pinboard != null) {
                                pinboard.updatePost(entry.getLink(), entry);
                            }

                            send(channel, Utils.buildLink(index, entry));
                            saveEntries(false);
                        }
                    }
                    // L1:=<url>
                    else if (cmd.charAt(0) == '=') {
                        final EntryLink entry = entries.get(index);

                        if (entry.getLogin().equals(login) || isOp(sender)) {
                            final String link = cmd.substring(1);

                            if (link.matches(LINK_MATCH)) {
                                final String oldLink = entry.getLink();

                                entry.setLink(link);

                                if (pinboard != null) {
                                    pinboard.updatePost(oldLink, entry);
                                }

                                send(channel, Utils.buildLink(index, entry));
                                saveEntries(false);
                            }
                        } else {
                            send(sender, "Please ask a channel op to change this link for you.");
                        }
                    }
                    // L1:?<author>
                    else if (cmd.charAt(0) == '?') {
                        if (isOp(sender)) {
                            if (cmd.length() > 1) {
                                final EntryLink entry = entries.get(index);
                                entry.setNick(cmd.substring(1));
                                send(channel, Utils.buildLink(index, entry));
                                saveEntries(false);
                            }
                        } else {
                            send(sender, "Please ask a channel op to change the author of this link for you.");
                        }
                    } else {
                        final EntryLink entry = entries.get(index);
                        final int cindex = entry.addComment(cmd, sender);

                        final EntryComment comment = entry.getComment(cindex);
                        send(sender, Utils.buildComment(index, cindex, comment));
                        saveEntries(false);
                    }
                }
            }
        }
        // L1T:<+-tag>
        else if (message.matches(Commands.LINK_CMD + "[0-9]+T:.*")) {
            isCommand = true;

            final String[] cmds = message.substring(1).split("T:", 2);
            final int index = Integer.parseInt(cmds[0]) - 1;

            if (index < entries.size()) {
                final String cmd = cmds[1].trim();

                final EntryLink entry = entries.get(index);

                if (cmd.length() != 0) {
                    if (entry.getLogin().equals(login) || isOp(sender)) {
                        entry.setTags(cmd);

                        if (pinboard != null) {
                            pinboard.updatePost(entry.getLink(), entry);
                        }

                        send(channel, Utils.buildTags(index, entry));
                        saveEntries(false);
                    } else {
                        send(sender, "Please ask a channel op to change the tags for you.");
                    }
                } else {
                    if (entry.hasTags()) {
                        send(channel, Utils.buildTags(index, entry));
                    } else {
                        send(sender, "The entry has no tags. Why don't add some?");
                    }
                }
            }
        }
        // L1.1:<command>
        else if (message.matches(Commands.LINK_CMD + "[0-9]+\\.[0-9]+:.*")) {
            isCommand = true;

            final String[] cmds = message.substring(1).split("[.:]", 3);
            final int index = Integer.parseInt(cmds[0]) - 1;

            if (index < entries.size()) {
                final EntryLink entry = entries.get(index);
                final int cindex = Integer.parseInt(cmds[1]) - 1;

                if (cindex < entry.getCommentsCount()) {
                    final String cmd = cmds[2].trim();

                    // L1.1:
                    if (cmd.length() == 0) {
                        final EntryComment comment = entry.getComment(cindex);
                        send(channel, Utils.buildComment(index, cindex, comment));
                    }
                    // L1.1:-
                    else if ("-".equals(cmd)) {
                        entry.deleteComment(cindex);
                        send(channel, "Comment " + Commands.LINK_CMD + (index + 1) + '.' + (cindex + 1) + " removed.");
                        saveEntries(false);
                    }
                    // L1.1:?<author>
                    else if (cmd.charAt(0) == '?') {
                        if (isOp(sender)) {
                            if (cmd.length() > 1) {
                                final EntryComment comment = entry.getComment(cindex);
                                comment.setNick(cmd.substring(1));
                                send(channel, Utils.buildComment(index, cindex, comment));
                                saveEntries(false);
                            }
                        } else {
                            send(sender, "Please ask a channel op to change the author of this comment for you.");
                        }
                    } else {
                        entry.setComment(cindex, cmd, sender);

                        final EntryComment comment = entry.getComment(cindex);
                        send(sender, Utils.buildComment(index, cindex, comment));
                        saveEntries(false);
                    }
                }
            }
        }

        if (!isCommand) {
            storeRecap(sender, message, false);
        }

        tell.send(sender, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onNickChange(final String oldNick, final String login, final String hostname, final String newNick) {
        tell.send(newNick);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings(value = "DM_EXIT", justification = "Yes, we want to bail out.")
    @Override
    protected final void onPrivateMessage(final String sender,
                                          final String login,
                                          final String hostname,
                                          final String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> " + sender + ": " + message);
        }

        final String[] cmds = message.split(" ", 2);
        final String cmd = cmds[0].toLowerCase();
        String args = "";

        if (cmds.length > 1) {
            args = cmds[1].trim();
        }

        if (cmd.startsWith(Commands.HELP_CMD)) {
            helpResponse(sender, args);
        } else if ("kill".equals(cmd) && isOp(sender)) {
            sendRawLine("QUIT : Poof!");
            System.exit(0);
        } else if (cmd.equals(Commands.DIE_CMD) && isOp(sender)) {
            send(ircChannel, sender + " has just signed my death sentence.");
            saveEntries(true);
            sleep(3);
            quitServer("The Bot Is Out There!");
            System.exit(0);
        } else if (cmd.equals(Commands.CYCLE_CMD)) {
            send(ircChannel, sender + " has just asked me to leave. I'll be back!");
            sleep(0);
            partChannel(ircChannel);
            sleep(10);
            joinChannel(ircChannel);
        } else if (cmd.equals(Commands.RECAP_CMD)) {
            recapResponse(sender, true);
        } else if (cmd.equals(Commands.USERS_CMD)) {
            usersResponse(sender, true);
        } else if (cmd.equals(Commands.ADDLOG_CMD) && (cmds.length > 1) && isOp(sender)) {
            // e.g. 2014-04-01
            final File backlog = new File(logsDir + args + EntriesMgr.XML_EXT);
            if (backlog.exists()) {
                history.add(0, args);
                send(sender, history.toString(), true);
            } else {
                send(sender, "The specified log could not be found.");
            }
        } else if (cmd.equals(Commands.ME_CMD) && isOp(sender)) {
            if (args.length() > 1) {
                action(args);
            } else {
                helpResponse(sender, Commands.ME_CMD);
            }
        } else if (cmd.equals(Commands.NICK_CMD) && (cmds.length > 1) && isOp(sender)) {
            changeNick(args);
        } else if (cmd.equals(Commands.SAY_CMD) && isOp(sender)) {
            if (cmds.length > 1) {
                send(ircChannel, args, true);
            } else {
                helpResponse(sender, Commands.SAY_CMD);
            }
        } else if (cmd.equals(Commands.MSG_CMD) && isOp(sender)) {
            if (cmds.length > 1) {
                final String[] msg = args.split(" ", 2);

                if (args.length() > 2) {
                    send(msg[0], msg[1], true);
                } else {
                    helpResponse(sender, Commands.MSG_CMD);
                }
            } else {
                helpResponse(sender, Commands.MSG_CMD);
            }
        } else if (cmd.equals(Commands.VIEW_CMD)) {
            viewResponse(sender, args, true);
        } else if (cmd.equals(Tell.TELL_CMD) && tell.isEnabled()) {
            tell.response(sender, args);
        } else if (cmd.equals(Commands.INFO_CMD)) {
            infoResponse(sender, true);
        } else if (cmd.equals(Commands.VERSION_CMD)) {
            versionResponse(sender, true);
        } else if (cmd.equals(Commands.DEBUG_CMD) && isOp(sender)) {
            if (logger.isDebugEnabled()) {
                Configurator.setLevel(logger.getName(), loggerLevel);
            } else {
                Configurator.setLevel(logger.getName(), Level.DEBUG);

            }

            send(sender, "Debug logging is " + (logger.isDebugEnabled() ? "enabled." : "disabled."), true);
        } else {
            for (final AbstractModule module : MODULES) {
                if (module.isPrivateMsgEnabled()) {
                    for (final String c : module.getCommands()) {
                        if (cmd.equals(c)) {
                            module.commandResponse(this, sender, args, true);
                            return;
                        }
                    }
                }
            }

            helpResponse(sender, "");
        }
    }

    /**
     * Responds with the last 10 public messages.
     *
     * @param sender    The nick of the person who sent the private message.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    private void recapResponse(final String sender, final boolean isPrivate) {
        if (recap.size() > 0) {
            for (final String recap : recap) {
                send(sender, recap, isPrivate);
            }
        } else {
            send(sender, "Sorry, nothing to recap.", true);
        }
    }

    /**
     * Saves the entries.
     *
     * @param isDayBackup Set the <code>true</code> if the daily backup file should also be created.
     */
    private void saveEntries(final boolean isDayBackup) {
        EntriesMgr.saveEntries(this, entries, history, isDayBackup);
    }

    /**
     * Sends a private message or notice.
     *
     * @param sender    The nick of the person who sent the message.
     * @param message   The actual message.
     * @param isPrivate Set to <code>true</code> if the response should be a private message, otherwise a notice is
     *                  sent.
     */
    public final void send(final String sender, final String message, final boolean isPrivate) {
        if (Utils.isValidString(message) && Utils.isValidString(sender)) {
            if (isPrivate) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sending message to " + sender + ": " + message);
                }

                sendMessage(sender, message);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sending notice to " + sender + ": " + message);
                }

                sendNotice(sender, message);
            }
        }
    }

    /**
     * Sends a private notice.
     *
     * @param sender  The nick of the person who sent the message.
     * @param message The actual message.
     */
    public final void send(final String sender, final String message) {
        send(sender, message, false);
    }

    /**
     * Sets the feed URL.
     *
     * @param feedURL The feed URL.
     */
    private void setFeedURL(final String feedURL) {
        this.feedURL = feedURL;
    }

    /**
     * Sets the bot's identification.
     *
     * @param pwd  The password for NickServ, if any.
     * @param nick The ident nick name.
     * @param msg  The ident message.
     */
    private void setIdentity(final String pwd, final String nick, final String msg) {
        identPwd = pwd;
        identNick = nick;
        identMsg = msg;
    }

    /**
     * Sets the Ignored nicks.
     *
     * @param nicks The nicks to ignore
     */
    private void setIgnoredNicks(final String nicks) {
        if (Utils.isValidString(nicks)) {
            final StringTokenizer st = new StringTokenizer(nicks, ",");

            while (st.hasMoreTokens()) {
                ignoredNicks.add(st.nextToken().trim().toLowerCase());
            }
        }
    }

    /**
     * Sets the pinboard authentication.
     *
     * @param apiToken The API token
     */
    private void setPinboardAuth(final String apiToken) {
        if (Utils.isValidString(apiToken)) {
            pinboard = new Pinboard(this, apiToken, ircServer);
        }
    }

    /**
     * Sets the default tags/categories.
     *
     * @param tags The tags.
     */
    private void setTags(final String tags) {
        defaultTags = tags;
    }

    /**
     * Stores the last 10 public messages and actions.
     *
     * @param sender   The nick of the person who sent the private message.
     * @param message  The actual message sent.
     * @param isAction Set to <code>true</code> if the message is an action.
     */
    private void storeRecap(final String sender, final String message, final boolean isAction) {
        recap.add(Utils.utcDateTime(LocalDateTime.now(Clock.systemUTC())) + " -> " + sender + (isAction ? " " : ": ")
                + message);

        if (recap.size() > MAX_RECAP) {
            recap.remove(0);
        }
    }

    /**
     * Responds with the users on a channel.
     *
     * @param sender    The nick of the person who sent the message.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    private void usersResponse(final String sender, final boolean isPrivate) {
        final User[] users = getUsers(ircChannel);
        final String[] nicks = new String[users.length];

        for (int i = 0; i < users.length; i++) {
            nicks[i] = users[i].getNick();
        }

        Arrays.sort(nicks, String.CASE_INSENSITIVE_ORDER);

        final StringBuilder buff = new StringBuilder(0);

        for (final String nick : nicks) {
            if (isOp(nick)) {
                buff.append('@');
            }

            buff.append(nick).append(' ');
        }

        send(sender, buff.toString(), isPrivate);
    }

    /**
     * Responds with the bot's version info.
     *
     * @param sender    The nick of the person who sent the message.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    private void versionResponse(final String sender, final boolean isPrivate) {
        if (isOp(sender)) {
            for (final String version : VERSION_STRS) {
                send(sender, version, isPrivate);
            }
        }
    }

    /**
     * Responds with the stored links.
     *
     * @param sender    The nick of the person who sent the message.
     * @param args      The view command arguments.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    private void viewResponse(final String sender, final String args, final boolean isPrivate) {
        String lcArgs = args.toLowerCase();

        if (!entries.isEmpty()) {
            final int max = entries.size();
            int i = 0;

            if (!(lcArgs.length() > 0) && (max > MAX_ENTRIES)) {
                i = max - MAX_ENTRIES;
            }

            if (lcArgs.matches("^\\d+(| .*)")) {
                final String[] split = lcArgs.split(" ", 2);

                try {
                    i = Integer.parseInt(split[0]);

                    if (i > 0) {
                        i--;
                    }

                    if (split.length == 2) {
                        lcArgs = split[1].trim();
                    } else {
                        lcArgs = "";
                    }

                    if (i > max) {
                        i = 0;
                    }
                } catch (NumberFormatException ignore) {
                    ; // Do nothing
                }
            }

            EntryLink entry;
            int sent = 0;

            for (; i < max; i++) {
                entry = entries.get(i);

                if (lcArgs.length() > 0) {
                    if ((entry.getLink().toLowerCase().contains(lcArgs)) ||
                            (entry.getTitle().toLowerCase().contains(lcArgs)) ||
                            (entry.getNick().toLowerCase().contains(lcArgs))) {
                        if (sent > MAX_ENTRIES) {
                            send(sender,
                                    "To view more, try: " + Utils
                                            .bold(getNick() + ": " + Commands.VIEW_CMD + ' ' + (i + 1) + ' ' + lcArgs),
                                    isPrivate);

                            break;
                        }

                        send(sender, Utils.buildLink(i, entry, true), isPrivate);
                        sent++;
                    }
                } else {
                    if (sent > MAX_ENTRIES) {
                        send(sender,
                                "To view more, try: " + Utils
                                        .bold(getNick() + ": " + Commands.VIEW_CMD + ' ' + (i + 1)),
                                isPrivate);

                        break;
                    }

                    send(sender, Utils.buildLink(i, entry, true), isPrivate);
                    sent++;
                }
            }
        } else {
            send(sender, "There is currently nothing to view. Why don't you post something?", isPrivate);
        }
    }
}
