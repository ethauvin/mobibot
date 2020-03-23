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

import com.rometools.rome.io.FeedException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.entries.EntriesMgr;
import net.thauvin.erik.mobibot.entries.EntriesUtils;
import net.thauvin.erik.mobibot.entries.EntryComment;
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
import net.thauvin.erik.mobibot.tell.Tell;
import net.thauvin.erik.semver.Version;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import static net.thauvin.erik.mobibot.Utils.bold;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

    // The default port.
    private static final int DEFAULT_PORT = 6667;

    // The default server.
    private static final String DEFAULT_SERVER = "irc.freenode.net";

    // The info strings.
    @SuppressWarnings("indentation")
    private static final String[] INFO_STRS = {
            ReleaseInfo.PROJECT + " v" + ReleaseInfo.VERSION + " by Erik C. Thauvin (erik@thauvin.net)",
            "https://www.mobitopia.org/mobibot/" };

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

    // The tags/categories marker.
    private static final String TAGS_MARKER = "tags:";

    /* The version strings.*/
    @SuppressWarnings("indentation")
    private static final String[] VERSION_STRS =
            { "Version: " + ReleaseInfo.VERSION + " (" + Utils.isoLocalDate(ReleaseInfo.BUILDDATE) + ')',
              "Platform: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", "
              + System.getProperty("os.arch") + ", " + System.getProperty("user.country") + ')',
              "Runtime: " + System.getProperty("java.runtime.name") + " (build " + System.getProperty(
                      "java.runtime.version") + ')',
              "VM: " + System.getProperty("java.vm.name") + " (build " + System.getProperty("java.vm.version") + ", "
              + System.getProperty("java.vm.info") + ')' };
    // The logger.
    private static final Logger logger = LogManager.getLogger(Mobibot.class);
    // The commands list.
    private final List<String> commandsList = new ArrayList<>();
    // The entries array.
    private final List<EntryLink> entries = new ArrayList<>(0);
    // The history/backlogs array.
    private final List<String> history = new ArrayList<>(0);
    // The ignored nicks array.
    private final Set<String> ignoredNicks = new HashSet<>(0);
    // The main channel.
    private final String ircChannel;
    // The IRC port.
    private final int ircPort;
    // The IRC server.
    private final String ircServer;
    // The logger default level.
    private final Level loggerLevel;
    // The log directory.
    private final String logsDir;
    // The recap array.
    private final List<String> recap = new ArrayList<>(0);
    // The tell object.
    private final Tell tell;
    // The Twitter handle for channel join notifications.
    private final String twitterHandle;
    // The Twitter module.
    private final Twitter twitterModule;
    // The backlogs URL.
    private String backLogsUrl = "";
    // The default tags/categories.
    private String defaultTags = "";
    // The feed URL.
    private String feedUrl = "";
    // The ident message.
    private String identMsg = "";
    // The ident nick.
    private String identNick = "";
    // The NickServ ident password.
    private String identPwd = "";
    // The pinboard posts handler.
    private Pinboard pinboard;
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
    public Mobibot(final String nickname, final String channel, final String logsDirPath, final Properties p) {
        super();
        System.getProperties().setProperty("sun.net.client.defaultConnectTimeout",
                                           String.valueOf(Constants.CONNECT_TIMEOUT));
        System.getProperties().setProperty("sun.net.client.defaultReadTimeout",
                                           String.valueOf(Constants.CONNECT_TIMEOUT));

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
                logger.debug("Last feed: {}", today);
            }

            if (!Utils.today().equals(today)) {
                entries.clear();
                today = Utils.today();
            }
        } catch (IOException ignore) {
            // Do nothing.
        } catch (FeedException e) {
            if (logger.isErrorEnabled()) {
                logger.error("An error occurred while parsing the '" + EntriesMgr.CURRENT_XML + "' file.", e);
            }
        }

        // Load the backlogs, if any.
        try {
            EntriesMgr.loadBacklogs(logsDir + EntriesMgr.NAV_XML, history);
        } catch (IOException ignore) {
            // Do nothing.
        } catch (FeedException e) {
            if (logger.isErrorEnabled()) {
                logger.error("An error occurred while parsing the '" + EntriesMgr.NAV_XML + "' file.", e);
            }
        }

        // Initialize the bot
        setVerbose(true);
        setAutoNickChange(true);
        setLogin(p.getProperty("login", getName()));
        setVersion(p.getProperty("weblog", ""));
        setMessageDelay(MESSAGE_DELAY);
        setIdentity(p.getProperty("ident", ""), p.getProperty("ident-nick", ""), p.getProperty("ident-msg", ""));

        // Set the URLs
        setWeblogUrl(getVersion());
        setFeedUrl(p.getProperty("feed", ""));
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
        MODULES.add(new RockPaperScissors());
        MODULES.add(new StockQuote());

        twitterModule = new Twitter();
        MODULES.add(twitterModule);
        twitterHandle = p.getProperty(Constants.TWITTER_HANDLE_PROP, "");

        MODULES.add(new War());
        MODULES.add(new Weather2());
        MODULES.add(new WorldTime());

        // Load the modules properties
        MODULES.stream().filter(AbstractModule::hasProperties).forEach(module -> {
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
    @SuppressFBWarnings(
            {
                    "INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE",
                    "DM_DEFAULT_ENCODING",
                    "IOI_USE_OF_FILE_STREAM_CONSTRUCTORS"
            })
    @SuppressWarnings({ "PMD.SystemPrintln", "PMD.AvoidFileStream", "PMD.CloseResource" })
    public static void main(final String[] args) {
        // Setup the command line options
        final Options options = new Options()
                                        .addOption(Commands.HELP_ARG.substring(0, 1),
                                                   Commands.HELP_ARG,
                                                   false,
                                                   "print this help message")
                                        .addOption(Commands.DEBUG_ARG.substring(0, 1), Commands.DEBUG_ARG, false,
                                                   "print debug & logging data directly to the console")
                                        .addOption(Option.builder(Commands.PROPS_ARG.substring(0, 1)).hasArg()
                                                         .argName("file")
                                                         .desc("use " + "alternate properties file")
                                                         .longOpt(Commands.PROPS_ARG).build())
                                        .addOption(Commands.VERSION_ARG.substring(0, 1),
                                                   Commands.VERSION_ARG,
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

        if (line.hasOption(Commands.HELP_ARG.charAt(0))) {
            // Output the usage
            new HelpFormatter().printHelp(Mobibot.class.getName(), options);
        } else if (line.hasOption(Commands.VERSION_ARG.charAt(0))) {
            for (final String s : INFO_STRS) {
                System.out.println(s);
            }
        } else {
            final Properties p = new Properties();

            try (final InputStream fis = Files.newInputStream(
                    Paths.get(line.getOptionValue(Commands.PROPS_ARG.charAt(0), "./mobibot.properties")))) {
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

            final String nickname = p.getProperty("nick", Mobibot.class.getName().toLowerCase(Constants.LOCALE));
            final String channel = p.getProperty("channel");
            final String logsDir = Utils.ensureDir(p.getProperty("logs", "."), false);

            // Redirect the stdout and stderr
            if (!line.hasOption(Commands.DEBUG_ARG.charAt(0))) {
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
     * Sleeps for the specified number of seconds.
     *
     * @param secs The number of seconds to sleep for.
     */
    private static void sleep(final int secs) {
        try {
            Thread.sleep(secs * 1000L);
        } catch (InterruptedException ignore) {
            // Do nothing.
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
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Unable to reconnect to {} after {} retries.", ircServer, MAX_RECONNECT, ex);
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
        if (isNotBlank(feedUrl)) {
            new Thread(new FeedReader(this, sender, feedUrl)).start();
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
                buff.append('[').append(String.valueOf(c).toLowerCase(Constants.LOCALE)).append(
                        String.valueOf(c).toUpperCase(Constants.LOCALE)).append(']');
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
     * Returns indented and bold help string.
     *
     * @param help The help string.
     * @return The indented help string.
     */
    public final String helpIndent(final String help) {
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
        return "        " + (isBold ? bold(help) : help);
    }

    /**
     * Responds with the bot's help.
     *
     * @param sender The nick of the person who sent the private message.
     * @param topic  The help topic, if any.
     */
    private void helpResponse(final String sender, final String topic) {
        final String lcTopic = topic.toLowerCase(Constants.LOCALE).trim();

        if (Commands.HELP_POSTING_KEYWORD.equals(lcTopic)) {
            send(sender, bold("Post a URL, by saying it on a line on its own:"));
            send(sender, helpIndent("<url> [<title>] [" + TAGS_MARKER + "<+tag> [...]]"));
            send(sender, "I will reply with a label, for example: " + bold(Commands.LINK_CMD + '1'));
            send(sender, "To add a title, use a its label and a pipe:");
            send(sender, helpIndent(Commands.LINK_CMD + "1:|This is the title"));
            send(sender, "To add a comment: ");
            send(sender, helpIndent(Commands.LINK_CMD + "1:This is a comment"));
            send(sender, "I will reply with a label, for example: " + bold(Commands.LINK_CMD + "1.1"));
            send(sender, "To edit a comment, use its label: ");
            send(sender, helpIndent(Commands.LINK_CMD + "1.1:This is an edited comment"));
            send(sender, "To delete a comment, use its label and a minus sign: ");
            send(sender, helpIndent(Commands.LINK_CMD + "1.1:-"));
            send(sender, "You can also view a posting by saying its label.");
        } else if (Commands.HELP_TAGS_KEYWORD.equals(lcTopic)) {
            send(sender, bold("To categorize or tag a URL, use its label and a T:"));
            send(sender, helpIndent(Commands.LINK_CMD + "1T:<+tag|-tag> [...]"));
        } else if (Commands.VIEW_CMD.equals(lcTopic)) {
            send(sender, "To list or search the current URL posts:");
            send(sender, helpIndent(getNick() + ": " + Commands.VIEW_CMD) + " [<start>] [<query>]");
        } else if (lcTopic.equalsIgnoreCase(getChannelName())) {
            send(sender, "To list the last 5 posts from the channel's weblog:");
            send(sender, helpIndent(getNick() + ": " + getChannelName()));
        } else if (Commands.RECAP_CMD.equals(lcTopic)) {
            send(sender, "To list the last 10 public channel messages:");
            send(sender, helpIndent(getNick() + ": " + Commands.RECAP_CMD));
        } else if (Commands.USERS_CMD.equals(lcTopic)) {
            send(sender, "To list the users present on the channel:");
            send(sender, helpIndent(getNick() + ": " + Commands.USERS_CMD));
        } else if (Commands.INFO_CMD.equals(lcTopic)) {
            send(sender, "To view information about the bot:");
            send(sender, helpIndent(getNick() + ": " + Commands.INFO_CMD));
        } else {
            final String msg = "/msg ";
            if (Commands.CYCLE_CMD.equals(lcTopic) && isOp(sender)) {
                send(sender, "To have the bot leave the channel and come back:");
                send(sender, helpIndent(msg + getNick() + ' ' + Commands.CYCLE_CMD));
            } else if (Commands.ME_CMD.equals(lcTopic) && isOp(sender)) {
                send(sender, "To have the bot perform an action:");
                send(sender, helpIndent(msg + getNick() + ' ' + Commands.ME_CMD + " <action>"));
            } else if (Commands.SAY_CMD.equals(lcTopic) && isOp(sender)) {
                send(sender, "To have the bot say something on the channel:");
                send(sender, helpIndent(msg + getNick() + ' ' + Commands.SAY_CMD + " <text>"));
            } else if (Commands.VERSION_CMD.equals(lcTopic) && isOp(sender)) {
                send(sender, "To view the version data (bot, java, etc.):");
                send(sender, helpIndent(msg + getNick() + ' ' + Commands.VERSION_CMD));
            } else if (Commands.MSG_CMD.equals(lcTopic) && isOp(sender)) {
                send(sender, "To have the bot send a private message to someone:");
                send(sender, helpIndent(msg + getNick() + ' ' + Commands.MSG_CMD + " <nick> <text>"));
            } else if (Commands.IGNORE_CMD.equals(lcTopic)) {
                send(sender, "To check your ignore status:");
                send(sender, helpIndent(getNick() + ": " + Commands.IGNORE_CMD));

                send(sender, "To toggle your ignore status:");
                send(sender, helpIndent(getNick() + ": " + Commands.IGNORE_CMD + ' ' + Commands.IGNORE_ME_KEYWORD));
            } else if (Tell.TELL_CMD.equals(lcTopic) && tell.isEnabled()) {
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

                send(sender, bold("Type a URL on " + ircChannel + " to post it."));
                send(sender, "For more information on a specific command, type:");
                send(sender, helpIndent(getNick() + ": " + Commands.HELP_CMD + " <command>"));
                send(sender, "The commands are:");

                if (commandsList.isEmpty()) {
                    commandsList.add(Commands.IGNORE_CMD);
                    commandsList.add(Commands.INFO_CMD);
                    commandsList.add(getChannelName());
                    commandsList.add(Commands.HELP_POSTING_KEYWORD);
                    commandsList.add(Commands.HELP_TAGS_KEYWORD);
                    commandsList.add(Commands.RECAP_CMD);
                    commandsList.add(Commands.USERS_CMD);
                    commandsList.add(Commands.VIEW_CMD);

                    MODULES.stream().filter(AbstractModule::isEnabled)
                           .forEach(module -> commandsList.addAll(module.getCommands()));

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
                    send(sender,
                         helpIndent(Commands.CYCLE_CMD + "  " + Commands.ME_CMD + "  " + Commands.MSG_CMD + "  "
                                    + Commands.SAY_CMD + "  " + Commands.VERSION_CMD));
                }
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
     * Processes the {@link net.thauvin.erik.mobibot.Commands#IGNORE_CMD} command.
     *
     * @param sender The sender.
     * @param args   The command arguments.
     */
    private void ignoreResponse(final String sender, final String args) {
        if (!isOp(sender)) {
            final String nick = sender.toLowerCase(Constants.LOCALE);
            final boolean isMe = args.toLowerCase(Constants.LOCALE).startsWith(Commands.IGNORE_ME_KEYWORD);
            if (isMe) {
                if (ignoredNicks.remove(nick)) {
                    send(sender, "You are no longer ignored.");
                } else {
                    ignoredNicks.add(nick);
                    send(sender, "You are now ignored.");
                }
            } else {
                if (ignoredNicks.contains(nick)) {
                    send(sender, "You are currently ignored.");
                } else {
                    send(sender, "You are not currently ignored.");
                }
            }
        } else {
            if (args.length() > 0) {
                final String[] nicks = args.toLowerCase(Constants.LOCALE).split(" ");

                for (final String nick : nicks) {
                    final String ignore;

                    if (Commands.IGNORE_ME_KEYWORD.equals(nick)) {
                        ignore = sender.toLowerCase(Constants.LOCALE);
                    } else {
                        ignore = nick;
                    }

                    if (!ignoredNicks.remove(ignore)) {
                        ignoredNicks.add(ignore);
                    }
                }
            }

            send(sender, "The following nicks are ignored: " + ignoredNicks);
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
                send(sender, info, Colors.DARK_GREEN, isPrivate);
            } else {
                send(sender, info, isPrivate);
            }
        }

        final StringBuilder info = new StringBuilder(29);

        info.append("Uptime: ").append(Utils.uptime(ManagementFactory.getRuntimeMXBean().getUptime())).append(
                " [Entries: ").append(entries.size());

        if (tell.isEnabled() && isOp(sender)) {
            info.append(", Messages: ").append(tell.size());
        }

        info.append(", Recap: ").append(recap.size()).append(']');

        send(sender, info.toString(), isPrivate);
    }

    /**
     * Determines whether the specified nick should be ignored.
     *
     * @param nick The nick.
     * @return <code>true</code> if the nick should be ignored, <code>false</code> otherwise.
     */
    private boolean isIgnoredNick(final String nick) {
        return isNotBlank(nick) && ignoredNicks.contains(nick.toLowerCase(Constants.LOCALE));
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
        if (logger.isDebugEnabled()) {
            logger.debug(">>> {} : {}", sender, message);
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
                    String title = Constants.NO_TITLE;

                    if (cmds.length == 2) {
                        final String[] data = cmds[1].trim().split(TAGS_MARKER, 2);

                        if (data.length == 1) {
                            title = data[0].trim();
                        } else {
                            if (isNotBlank(data[0])) {
                                title = data[0].trim();
                            }

                            tags.append(' ').append(data[1].trim());
                        }
                    }

                    if (Constants.NO_TITLE.equals(title)) {
                        try {
                            final Document html = Jsoup.connect(link).userAgent("Mozilla").get();
                            final String htmlTitle = html.title();

                            if (isNotBlank(htmlTitle)) {
                                title = htmlTitle;
                            }
                        } catch (IOException ignore) {
                            // Do nothing
                        }
                    }

                    entries.add(new EntryLink(link, title, sender, login, channel, tags.toString()));

                    final int index = entries.size() - 1;
                    final EntryLink entry = entries.get(index);
                    send(channel, EntriesUtils.buildLink(index, entry));

                    if (pinboard != null) {
                        pinboard.addPost(entry);
                    }

                    saveEntries(isBackup);

                    if (Constants.NO_TITLE.equals(entry.getTitle())) {
                        send(sender, "Please specify a title, by typing:", true);
                        send(sender, helpIndent(Commands.LINK_CMD + (index + 1) + ":|This is the title"), true);
                    }
                } else {
                    final EntryLink entry = entries.get(dupIndex);
                    send(sender, bold("Duplicate") + " >> " + EntriesUtils.buildLink(dupIndex, entry));
                }
            }
        } else if (message.matches(getNickPattern() + ":.*")) { // mobibot: <command>
            isCommand = true;

            final String[] cmds = message.substring(message.indexOf(':') + 1).trim().split(" ", 2);
            final String cmd = cmds[0].toLowerCase(Constants.LOCALE);

            String args = "";

            if (cmds.length > 1) {
                args = cmds[1].trim();
            }


            if (cmd.startsWith(Commands.HELP_CMD)) { // mobibot: help
                helpResponse(sender, args);
            } else if (Commands.RECAP_CMD.equals(cmd)) { // mobibot: recap
                recapResponse(sender, false);
            } else if (Commands.USERS_CMD.equals(cmd)) { // mobibot: users
                usersResponse(sender, false);
            } else if (Commands.INFO_CMD.equals(cmd)) { // mobibot: info
                infoResponse(sender, false);
            } else if (Commands.VERSION_CMD.equals(cmd)) { // mobbiot: version
                versionResponse(sender, false);
            } else if (cmd.equalsIgnoreCase(getChannelName())) { // mobibot: <channel>
                feedResponse(sender);
            } else if (cmd.startsWith(Commands.VIEW_CMD)) { // mobibot: view
                viewResponse(sender, args, false);
            } else if (cmd.startsWith(Tell.TELL_CMD) && tell.isEnabled()) { // mobibot: tell
                tell.response(sender, args);
            } else if (cmd.startsWith(Commands.IGNORE_CMD)) { // mobibot: ignore
                ignoreResponse(sender, args);
            } else {
                for (final AbstractModule module : MODULES) { // modules
                    for (final String c : module.getCommands()) {
                        if (cmd.startsWith(c)) {
                            module.commandResponse(this, sender, cmd, args, false);
                        }
                    }
                }
            }
        } else if (message.matches(Commands.LINK_CMD + "[0-9]+:.*")) { // L1:<comment>, L1:-, L1:|<title>, etc.
            isCommand = true;

            final String[] cmds = message.substring(1).split(":", 2);
            final int index = Integer.parseInt(cmds[0]) - 1;

            // L1:<comment>
            if (index < entries.size()) {
                final String cmd = cmds[1].trim();

                if (cmd.length() == 0) {
                    final EntryLink entry = entries.get(index);
                    send(channel, EntriesUtils.buildLink(index, entry));

                    if (entry.hasTags()) {
                        send(channel, EntriesUtils.buildTags(index, entry));
                    }

                    if (entry.hasComments()) {
                        final EntryComment[] comments = entry.getComments();

                        for (int i = 0; i < comments.length; i++) {
                            send(channel, EntriesUtils.buildComment(index, i, comments[i]));
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
                    } else if (cmd.charAt(0) == '|') { // L1:|<title>
                        if (cmd.length() > 1) {
                            final EntryLink entry = entries.get(index);
                            entry.setTitle(cmd.substring(1).trim());

                            if (pinboard != null) {
                                pinboard.updatePost(entry.getLink(), entry);
                            }

                            send(channel, EntriesUtils.buildLink(index, entry));
                            saveEntries(false);
                        }
                    } else if (cmd.charAt(0) == '=') { // L1:=<url>
                        final EntryLink entry = entries.get(index);

                        if (entry.getLogin().equals(login) || isOp(sender)) {
                            final String link = cmd.substring(1);

                            if (link.matches(LINK_MATCH)) {
                                final String oldLink = entry.getLink();

                                entry.setLink(link);

                                if (pinboard != null) {
                                    pinboard.updatePost(oldLink, entry);
                                }

                                send(channel, EntriesUtils.buildLink(index, entry));
                                saveEntries(false);
                            }
                        } else {
                            send(sender, "Please ask a channel op to change this link for you.");
                        }
                    } else if (cmd.charAt(0) == '?') { // L1:?<author>
                        if (isOp(sender)) {
                            if (cmd.length() > 1) {
                                final EntryLink entry = entries.get(index);
                                entry.setNick(cmd.substring(1));
                                send(channel, EntriesUtils.buildLink(index, entry));
                                saveEntries(false);
                            }
                        } else {
                            send(sender, "Please ask a channel op to change the author of this link for you.");
                        }
                    } else {
                        final EntryLink entry = entries.get(index);
                        final int cindex = entry.addComment(cmd, sender);

                        final EntryComment comment = entry.getComment(cindex);
                        send(sender, EntriesUtils.buildComment(index, cindex, comment));
                        saveEntries(false);
                    }
                }
            }
        } else if (message.matches(Commands.LINK_CMD + "[0-9]+T:.*")) { // L1T:<+-tag>
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

                        send(channel, EntriesUtils.buildTags(index, entry));
                        saveEntries(false);
                    } else {
                        send(sender, "Please ask a channel op to change the tags for you.");
                    }
                } else {
                    if (entry.hasTags()) {
                        send(channel, EntriesUtils.buildTags(index, entry));
                    } else {
                        send(sender, "The entry has no tags. Why don't add some?");
                    }
                }
            }
        } else if (message.matches(Commands.LINK_CMD + "[0-9]+\\.[0-9]+:.*")) { // L1.1:<command>
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
                        send(channel, EntriesUtils.buildComment(index, cindex, comment));
                    } else if ("-".equals(cmd)) { // L1.1:-
                        entry.deleteComment(cindex);
                        send(channel, "Comment " + Commands.LINK_CMD + (index + 1) + '.' + (cindex + 1) + " removed.");
                        saveEntries(false);
                    } else if (cmd.charAt(0) == '?') { // L1.1:?<author>
                        if (isOp(sender)) {
                            if (cmd.length() > 1) {
                                final EntryComment comment = entry.getComment(cindex);
                                comment.setNick(cmd.substring(1));
                                send(channel, EntriesUtils.buildComment(index, cindex, comment));
                                saveEntries(false);
                            }
                        } else {
                            send(sender, "Please ask a channel op to change the author of this comment for you.");
                        }
                    } else {
                        entry.setComment(cindex, cmd, sender);

                        final EntryComment comment = entry.getComment(cindex);
                        send(sender, EntriesUtils.buildComment(index, cindex, comment));
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
    @SuppressFBWarnings(value = { "DM_EXIT", "CC_CYCLOMATIC_COMPLEXITY" },
                        justification = "Yes, we want to bail out.")
    @Override
    protected final void onPrivateMessage(final String sender, final String login, final String hostname,
                                          final String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> {} : {}", sender, message);
        }

        final String[] cmds = message.split(" ", 2);
        final String cmd = cmds[0].toLowerCase(Constants.LOCALE);
        String args = "";

        if (cmds.length > 1) {
            args = cmds[1].trim();
        }

        if (cmd.startsWith(Commands.HELP_CMD)) {
            helpResponse(sender, args);
        } else if ("kill".equals(cmd) && isOp(sender)) {
            sendRawLine("QUIT : Poof!");
            System.exit(0);
        } else if (Commands.DIE_CMD.equals(cmd) && isOp(sender)) {
            send(ircChannel, sender + " has just signed my death sentence.");
            twitterNotification("killed by  " + sender + " on " + ircChannel);
            saveEntries(true);
            sleep(3);
            quitServer("The Bot Is Out There!");
            System.exit(0);
        } else if (Commands.CYCLE_CMD.equals(cmd)) {
            send(ircChannel, sender + " has just asked me to leave. I'll be back!");
            sleep(0);
            partChannel(ircChannel);
            sleep(10);
            joinChannel(ircChannel);
        } else if (Commands.RECAP_CMD.equals(cmd)) {
            recapResponse(sender, true);
        } else if (Commands.USERS_CMD.equals(cmd)) {
            usersResponse(sender, true);
        } else if ((cmds.length > 1) && isOp(sender) && Commands.ADDLOG_CMD.equals(cmd)) {
            // e.g. 2014-04-01
            final File backlog = new File(logsDir + args + EntriesMgr.XML_EXT);
            if (backlog.exists()) {
                history.add(0, args);
                send(sender, history.toString(), true);
            } else {
                send(sender, "The specified log could not be found.");
            }
        } else if (Commands.ME_CMD.equals(cmd) && isOp(sender)) {
            if (args.length() > 1) {
                action(args);
            } else {
                helpResponse(sender, Commands.ME_CMD);
            }
        } else if ((cmds.length > 1) && isOp(sender) && Commands.NICK_CMD.equals(cmd)) {
            changeNick(args);
        } else if (Commands.SAY_CMD.equals(cmd) && isOp(sender)) {
            if (cmds.length > 1) {
                send(ircChannel, args, true);
            } else {
                helpResponse(sender, Commands.SAY_CMD);
            }
        } else if (Commands.MSG_CMD.equals(cmd) && isOp(sender)) {
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
        } else if (Commands.VIEW_CMD.equals(cmd)) {
            viewResponse(sender, args, true);
        } else if (Tell.TELL_CMD.equals(cmd) && tell.isEnabled()) {
            tell.response(sender, args);
        } else if (Commands.INFO_CMD.equals(cmd)) {
            infoResponse(sender, true);
        } else if (Commands.VERSION_CMD.equals(cmd)) {
            versionResponse(sender, true);
        } else if (Commands.DEBUG_CMD.equals(cmd) && isOp(sender)) {
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
                            module.commandResponse(this, sender, cmd, args, true);
                            return;
                        }
                    }
                }
            }

            helpResponse(sender, "");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void onAction(final String sender, final String login, final String hostname, final String target,
                                  final String action) {
        if (target != null && target.equals(ircChannel)) {
            storeRecap(sender, action, true);
        }
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
    protected void onNickChange(final String oldNick, final String login, final String hostname, final String newNick) {
        tell.send(newNick);
    }

    /**
     * Responds with the last 10 public messages.
     *
     * @param sender    The nick of the person who sent the private message.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    private void recapResponse(final String sender, final boolean isPrivate) {
        if (!recap.isEmpty()) {
            for (final String r : recap) {
                send(sender, r, isPrivate);
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
    final void saveEntries(final boolean isDayBackup) {
        EntriesMgr.saveEntries(this, entries, history, isDayBackup);
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
                if (logger.isDebugEnabled()) {
                    logger.debug("Sending message to {} : {}", sender, message);
                }

                sendMessage(sender, message);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sending notice to {} : {}", sender, message);
                }

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
        send(getChannel(), notice);

    }

    /**
     * Sends a message.
     *
     * @param who     The channel or nick of the person who sent the command.
     * @param message The actual message.
     */
    public final void send(final String who, final String message) {
        send(who, message, false);
    }

    /**
     * Sends a message.
     *
     * @param who     The channel or nick of the person who sent the command.
     * @param message The message.
     */
    public final void send(final String who, final Message message) {
        send(message.isNotice() ? who : getChannel(), message.getMessage(), message.getColor(), message.isPrivate());
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
     * Sends a message.
     *
     * @param who     The channel or nick of the person who sent the command.
     * @param message The actual message.
     * @param color   The message's color.
     */
    @SuppressWarnings("unused")
    public final void send(final String who, final String message, final String color) {
        send(who, Utils.colorize(message, color), false);
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
     * Sets the feed URL.
     *
     * @param feedUrl The feed URL.
     */
    final void setFeedUrl(final String feedUrl) {
        this.feedUrl = feedUrl;
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
     * Sets the Ignored nicks.
     *
     * @param nicks The nicks to ignore
     */
    final void setIgnoredNicks(final String nicks) {
        if (isNotBlank(nicks)) {
            final StringTokenizer st = new StringTokenizer(nicks, ",");

            while (st.hasMoreTokens()) {
                ignoredNicks.add(st.nextToken().trim().toLowerCase(Constants.LOCALE));
            }
        }
    }

    /**
     * Sets the pinboard authentication.
     *
     * @param apiToken The API token
     */
    final void setPinboardAuth(final String apiToken) {
        if (isNotBlank(apiToken)) {
            pinboard = new Pinboard(this, apiToken, ircServer);
        }
    }

    /**
     * Sets the default tags/categories.
     *
     * @param tags The tags.
     */
    final void setTags(final String tags) {
        defaultTags = tags;
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
     * Send a notification to the registered Twitter handle.
     *
     * @param msg The twitter message.
     */
    final void twitterNotification(final String msg) {
        if (twitterModule.isEnabled() && isNotBlank(twitterHandle)) {
            new Thread(() -> {
                try {
                    twitterModule.post(twitterHandle, getName() + ' ' + ReleaseInfo.VERSION + " " + msg, true);
                } catch (ModuleException e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to notify @{}: {}", twitterHandle, msg, e);
                    }
                }
            }).start();
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
        if (!entries.isEmpty()) {
            final int max = entries.size();
            String lcArgs = args.toLowerCase(Constants.LOCALE);
            int i = 0;

            if ((lcArgs.length() <= 0) && (max > MAX_ENTRIES)) {
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
                    // Do nothing.
                }
            }

            EntryLink entry;
            int sent = 0;

            for (; i < max; i++) {
                entry = entries.get(i);

                if (lcArgs.length() > 0) {
                    if ((entry.getLink().toLowerCase(Constants.LOCALE).contains(lcArgs))
                        || (entry.getTitle().toLowerCase(Constants.LOCALE).contains(lcArgs))
                        || (entry.getNick().toLowerCase(Constants.LOCALE).contains(lcArgs))) {
                        if (sent > MAX_ENTRIES) {
                            send(sender,
                                 "To view more, try: "
                                 + bold(getNick() + ": " + Commands.VIEW_CMD + ' ' + (i + 1) + ' ' + lcArgs),
                                 isPrivate);

                            break;
                        }

                        send(sender, EntriesUtils.buildLink(i, entry, true), isPrivate);
                        sent++;
                    }
                } else {
                    if (sent > MAX_ENTRIES) {
                        send(sender,
                             "To view more, try: " + bold(getNick() + ": " + Commands.VIEW_CMD + ' ' + (i + 1)),
                             isPrivate);

                        break;
                    }

                    send(sender, EntriesUtils.buildLink(i, entry, true), isPrivate);
                    sent++;
                }
            }
        } else {
            send(sender, "There is currently nothing to view. Why don't you post something?", isPrivate);
        }
    }
}
