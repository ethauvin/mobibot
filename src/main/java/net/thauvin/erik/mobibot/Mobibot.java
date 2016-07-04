/*
 * Mobibot.java
 *
 * Copyright (c) 2004-2016, Erik C. Thauvin (erik@thauvin.net)
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
import net.thauvin.erik.mobibot.modules.*;
import net.thauvin.erik.semver.Version;
import org.apache.commons.cli.*;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.*;

/**
 * Implements the #mobitopia bot.
 *
 * @author Erik C. Thauvin
 * @created Jan 31, 2004
 * @since 1.0
 */
@Version(properties = "version.properties", className = "ReleaseInfo")
public class Mobibot extends PircBot
{
	/**
	 * The connect/read timeout in ms.
	 */
	public static final int CONNECT_TIMEOUT = 5000;

	/**
	 * The empty title string.
	 */
	static final String NO_TITLE = "No Title";

	/**
	 * The default port.
	 */
	private static final int DEFAULT_PORT = 6667;

	/**
	 * The info strings.
	 */
	private static final String[] INFO_STRS = {
			ReleaseInfo.getProject() + " v" + ReleaseInfo.getVersion() + " by Erik C. Thauvin (erik@thauvin.net)",
			"http://www.mobitopia.org/mobibot/"
	};

	/**
	 * The link match string.
	 */
	private static final String LINK_MATCH = "^[hH][tT][tT][pP](|[sS])://.*";

	/**
	 * The default maximum number of entries to display.
	 */
	private static final int MAX_ENTRIES = 8;

	/**
	 * The default maximum recap entries.
	 */
	private static final int MAX_RECAP = 10;

	/**
	 * The maximum number of times the bot will try to reconnect, if disconnected.
	 */
	private static final int MAX_RECONNECT = 10;

	/**
	 * The number of milliseconds to delay between consecutive messages.
	 */
	private static final long MESSAGE_DELAY = 1000L;

	/**
	 * The modules.
	 */
	private static final List<AbstractModule> MODULES = new ArrayList<>(0);

	/**
	 * The start time.
	 */
	private static final long START_TIME = System.currentTimeMillis();

	/**
	 * The tags/categories marker.
	 */
	private static final String TAGS_MARKER = "tags:";

	/**
	 * The version strings.
	 */
	private static final String[] VERSION_STRS = {
			"Version: " + ReleaseInfo.getVersion() + " (" + Utils.ISO_SDF.format(ReleaseInfo.getBuildDate()) + ')',
			"Platform: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " + System
					.getProperty("os.arch") + ", " + System.getProperty("user.country") + ')',
			"Runtime: " + System.getProperty("java.runtime.name") + " (build " + System
					.getProperty("java.runtime.version") + ')',
			"VM: " + System.getProperty("java.vm.name") + " (build " + System.getProperty("java.vm.version") + ", "
			+ System.getProperty("java.vm.info") + ')'
	};

	/**
	 * The tell object.
	 */
	private static Tell tell;

	/**
	 * The main channel.
	 */
	private final String channel;

	/**
	 * The commands list.
	 */
	private final List<String> commandsList = new ArrayList<>();

	/**
	 * The entries array.
	 */
	private final List<EntryLink> entries = new ArrayList<>(0);

	/**
	 * The history/backlogs array.
	 */
	private final List<String> history = new ArrayList<>(0);

	/**
	 * The ignored nicks array.
	 */
	private final List<String> ignoredNicks = new ArrayList<>(0);

	/**
	 * The IRC port.
	 */
	private final int ircPort;

	/**
	 * The IRC server.
	 */
	private final String ircServer;

	/**
	 * The logger.
	 */
	private final Log4JLogger logger = new Log4JLogger(Mobibot.class.getPackage().getName());

	/**
	 * The logger default level.
	 */
	private final Level loggerLevel;

	/**
	 * The log directory.
	 */
	private final String logsDir;

	/**
	 * The recap array.
	 */
	private final List<String> recap = new ArrayList<>(0);

	/**
	 * The backlogs URL.
	 */
	private String backLogsUrl = "";

	/**
	 * The default tags/categories.
	 */
	private String defaultTags = "";

	/**
	 * The del.icio.us posts handler.
	 */
	private DeliciousPoster delicious = null;

	/**
	 * The feed URL.
	 */
	private String feedURL = "";

	/**
	 * The NickServ ident password.
	 */
	private String ident = "";

	/**
	 * The ident message.
	 */
	private String identMsg = "";

	/**
	 * The ident nick.
	 */
	private String identNick = "";

	/**
	 * Today's date.
	 */
	private String today = Utils.today();

	/**
	 * The weblog URL.
	 */
	private String weblogUrl = "";

	/**
	 * Creates a new {@link Mobibot} instance.
	 *
	 * @param server The server.
	 * @param port The port.
	 * @param nickname The nickname.
	 * @param channel The channel.
	 * @param logsDir The logs directory.
	 */
	@SuppressWarnings("WeakerAccess")
	public Mobibot(final String server, final int port, final String nickname, final String channel,
	               final String logsDir)
	{
		System.getProperties().setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(CONNECT_TIMEOUT));
		System.getProperties().setProperty("sun.net.client.defaultReadTimeout", String.valueOf(CONNECT_TIMEOUT));

		setName(nickname);

		ircServer = server;
		ircPort = port;
		this.channel = channel;
		this.logsDir = logsDir;

		// Set the logger level
		loggerLevel = logger.getLogger().getLevel();

		// Initialization
		Utils.UTC_SDF.setTimeZone(TimeZone.getTimeZone("UTC"));

		// Load the current entries, if any.
		try
		{
			today = EntriesMgr.loadEntries(this.logsDir + EntriesMgr.CURRENT_XML, this.channel, entries);

			if (logger.isDebugEnabled())
			{
				logger.debug("Last feed: " + today);
			}

			if (!Utils.today().equals(today))
			{
				entries.clear();
				today = Utils.today();
			}
		}
		catch (FileNotFoundException ignore)
		{
			; // Do nothing.
		}
		catch (FeedException e)
		{
			logger.error("An error occurred while parsing the '" + EntriesMgr.CURRENT_XML + "' file.", e);
		}

		// Load the backlogs, if any.
		try
		{
			EntriesMgr.loadBacklogs(this.logsDir + EntriesMgr.NAV_XML, history);
		}
		catch (FileNotFoundException ignore)
		{
			; // Do nothing.
		}
		catch (FeedException e)
		{
			logger.error("An error occurred while parsing the '" + EntriesMgr.NAV_XML + "' file.", e);
		}

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
		MODULES.add(new Weather());
		MODULES.add(new WorldTime());
	}

	/**
	 * The Truth Is Out There...
	 *
	 * @param args The command line arguments.
	 */
	public static void main(final String[] args)
	{
		// Setup the command line options
		final Options options = new Options();
		options.addOption(Commands.HELP_ARG.substring(0, 1), Commands.HELP_ARG, false, "print this help message");
		options.addOption(Commands.DEBUG_ARG.substring(0, 1),
		                  Commands.DEBUG_ARG,
		                  false,
		                  "print debug & logging data directly to the console");
		options.addOption(Option.builder(Commands.PROPS_ARG.substring(0, 1)).hasArg().argName("file")
				                  .desc("use alternate properties file").longOpt(Commands.PROPS_ARG).build());
		options.addOption(Commands.VERSION_ARG.substring(0, 1), Commands.VERSION_ARG, false, "print version info");

		// Parse the command line
		final CommandLineParser parser = new DefaultParser();
		CommandLine line = null;

		try
		{
			line = parser.parse(options, args);
		}
		catch (ParseException e)
		{
			System.err.println("CLI Parsing failed.  Reason: " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}

		if (line.hasOption(Commands.HELP_ARG.charAt(0)))
		{
			// Output the usage
			new HelpFormatter().printHelp(Mobibot.class.getName(), options);
		}
		else if (line.hasOption(Commands.VERSION_ARG.charAt(0)))
		{
			for (final String s : INFO_STRS)
			{
				System.out.println(s);
			}
		}
		else
		{
			FileInputStream fis = null;
			final Properties p = new Properties();

			try
			{
				fis = new FileInputStream(new File(line.getOptionValue(Commands.PROPS_ARG.charAt(0),
				                                                       "./mobibot.properties")));

				// Load the properties files
				p.load(fis);
			}
			catch (FileNotFoundException e)
			{
				System.err.println("Unable to find properties file.");
				e.printStackTrace(System.err);
				System.exit(1);
			}
			catch (IOException e)
			{
				System.err.println("Unable to open properties file.");
				e.printStackTrace(System.err);
				System.exit(1);
			}
			finally
			{
				if (fis != null)
				{
					try
					{
						fis.close();
					}
					catch (IOException ignore)
					{
						; // Do nothing
					}
				}
			}

			// Get the main properties
			final String channel = p.getProperty("channel");
			final String server = p.getProperty("server");
			final int port = Utils.getIntProperty(p.getProperty("port"), DEFAULT_PORT);
			final String nickname = p.getProperty("nick", Mobibot.class.getName().toLowerCase());
			final String logsDir = Utils.ensureDir(p.getProperty("logs", "."), false);

			if (!line.hasOption(Commands.DEBUG_ARG.charAt(0)))
			{
				// Redirect the stdout and stderr
				PrintStream stdout = null;

				try
				{
					stdout = new PrintStream(new FileOutputStream(
							logsDir + channel.substring(1) + '.' + Utils.today() + ".log", true));
				}
				catch (IOException e)
				{
					System.err.println("Unable to open output (stdout) log file.");
					e.printStackTrace(System.err);
					System.exit(1);
				}

				PrintStream stderr = null;

				try
				{
					stderr = new PrintStream(new FileOutputStream(logsDir + nickname + ".err", true));
				}
				catch (IOException e)
				{
					System.err.println("Unable to open error (stderr) log file.");
					e.printStackTrace(System.err);
					System.exit(1);
				}

				System.setOut(stdout);
				System.setErr(stderr);
			}

			// Get the bot's properties
			final String login = p.getProperty("login", nickname);
			final String weblogURL = p.getProperty("weblog", "");
			final String feedURL = p.getProperty("feed", "");
			final String backlogsURL = Utils.ensureDir(p.getProperty("backlogs", weblogURL), true);
			final String ignoredNicks = p.getProperty("ignore", "");
			final String identNick = p.getProperty("ident-nick", "");
			final String identMsg = p.getProperty("ident-msg", "");
			final String ident = p.getProperty("ident", "");
			final String tags = p.getProperty("tags", "");

			// Get the del.icio.us properties
			final String dname = p.getProperty("delicious-user");
			final String dpwd = p.getProperty("delicious-pwd");

			// Create the bot
			final Mobibot bot = new Mobibot(server, port, nickname, channel, logsDir);

			// Get the tell command settings
			tell = new Tell(bot, p.getProperty("tell-max-days"), p.getProperty("tell-max-size"));

			// Initialize the bot
			bot.setVerbose(true);
			bot.setAutoNickChange(true);
			bot.setLogin(login);
			bot.setVersion(weblogURL);
			bot.setMessageDelay(MESSAGE_DELAY);

			// Set the ident password
			bot.setIdent(ident);

			// Set the ident nick and message
			bot.setIdentNick(identNick);
			bot.setIdentMsg(identMsg);

			// Set the URLs
			bot.setWeblogUrl(weblogURL);
			bot.setFeedURL(feedURL);
			bot.setBacklogsUrl(backlogsURL);

			if (Utils.isValidString(dname) && Utils.isValidString(dpwd))
			{
				// Set the del.icio.us authentication
				bot.setDeliciousAuth(dname, dpwd);
			}

			// Load the modules properties
			MODULES.stream().filter(AbstractModule::hasProperties).forEach(module -> {
				for (final String s : module.getPropertyKeys())
				{
					module.setProperty(s, p.getProperty(s, ""));
				}
			});

			// Set the tags
			bot.setTags(tags);

			// Set the ignored nicks
			bot.setIgnoredNicks(ignoredNicks);

			// Save the entries
			bot.saveEntries(true);

			// Connect
			try
			{
				bot.connect(server, port);
			}
			catch (Exception e)
			{
				int retries = 0;

				while ((retries++ < MAX_RECONNECT) && !bot.isConnected())
				{
					sleep(10);

					try
					{
						bot.connect(server, port);
					}
					catch (Exception ignore)
					{
						if (retries == MAX_RECONNECT)
						{
							System.err.println(
									"Unable to connect to " + server + " after " + MAX_RECONNECT + " retries.");
							e.printStackTrace(System.err);
							System.exit(1);
						}
					}
				}
			}

			bot.setVersion(INFO_STRS[0]);

			// Identify with NickServ
			if (Utils.isValidString(ident))
			{
				bot.identify(ident);
			}

			// Identify with a specified nick
			if (Utils.isValidString(identNick) && Utils.isValidString(identMsg))
			{
				bot.sendMessage(identNick, identMsg);
			}

			bot.joinChannel(channel);
		}
	}

	/**
	 * Sets the ident password.
	 *
	 * @param pwd The password.
	 */
	private void setIdent(final String pwd)
	{
		ident = pwd;
	}

	/**
	 * Sets the ident nickname.
	 *
	 * @param nick The nickname.
	 */
	private void setIdentNick(final String nick)
	{
		identNick = nick;
	}

	/**
	 * Sets the ident message.
	 *
	 * @param msg The message.
	 */
	private void setIdentMsg(final String msg)
	{
		identMsg = msg;
	}

	/**
	 * Sets the feed URL.
	 *
	 * @param feedURL The feed URL.
	 */
	private void setFeedURL(final String feedURL)
	{
		this.feedURL = feedURL;
	}

	/**
	 * Sets the del.icio.us authentication.
	 *
	 * @param username The del.icio.us user name.
	 * @param password The del.icio.us password.
	 */
	private void setDeliciousAuth(final String username, final String password)
	{
		delicious = new DeliciousPoster(username, password, ircServer);
	}

	/**
	 * Sets the default tags/categories.
	 *
	 * @param tags The tags.
	 */
	private void setTags(final String tags)
	{
		defaultTags = tags;
	}

	/**
	 * Sets the Ignored nicks.
	 *
	 * @param nicks The nicks to ignore
	 */
	private void setIgnoredNicks(final String nicks)
	{
		if (Utils.isValidString(nicks))
		{
			final StringTokenizer st = new StringTokenizer(nicks, ",");

			while (st.hasMoreTokens())
			{
				ignoredNicks.add(st.nextToken().trim().toLowerCase());
			}
		}
	}

	/**
	 * Saves the entries.
	 *
	 * @param isDayBackup Set the true if the daily backup file should also be created.
	 */
	private void saveEntries(final boolean isDayBackup)
	{
		EntriesMgr.saveEntries(this, entries, history, isDayBackup);
	}

	/**
	 * Sleeps for the specified number of seconds.
	 *
	 * @param secs The number of seconds to sleep for.
	 */
	private static void sleep(final int secs)
	{
		try
		{
			Thread.sleep((long) (secs * 1000));
		}
		catch (InterruptedException ignore)
		{
			; // Do nothing
		}
	}

	/**
	 * Sends an action to the current channel.
	 *
	 * @param action The action.
	 */
	final public void action(final String action)
	{
		action(channel, action);
	}

	/**
	 * Sends an action to the channel.
	 *
	 * @param channel The channel.
	 * @param action The action.
	 */
	private void action(final String channel, final String action)
	{
		if (Utils.isValidString(channel) && Utils.isValidString(action))
		{
			sendAction(channel, action);
		}
	}

	/**
	 * Responds with the title and links from the RSS feed.
	 *
	 * @param sender The nick of the person who sent the private message.
	 */
	private void feedResponse(final String sender)
	{
		if (Utils.isValidString(feedURL))
		{
			new Thread(new FeedReader(this, sender, feedURL)).start();
		}
		else
		{
			send(sender, "There is no weblog setup for this channel.");
		}
	}

	/**
	 * Returns the index of the specified duplicate entry, if any.
	 *
	 * @param link The link.
	 *
	 * @return The index or -1 if none.
	 */
	private int findDupEntry(final String link)
	{
		EntryLink entry;

		synchronized (entries)
		{
			for (int i = 0; i < entries.size(); i++)
			{
				entry = entries.get(i);

				if (link.equals(entry.getLink()))
				{
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
	public final String getBacklogsUrl()
	{
		return this.backLogsUrl;
	}

	/**
	 * Sets the backlogs URL.
	 *
	 * @param backLogsUrl The backlogs URL.
	 */
	private void setBacklogsUrl(final String backLogsUrl)
	{
		this.backLogsUrl = backLogsUrl;
	}

	/**
	 * Returns the current channel.
	 *
	 * @return The current channel.
	 */
	public final String getChannel()
	{
		return channel;
	}

	/**
	 * Returns the irc server.
	 *
	 * @return The irc server.
	 */
	public final String getIrcServer()
	{
		return this.ircServer;
	}

	/**
	 * Returns the bot's logger.
	 *
	 * @return The bot's logger.
	 */
	public final Log4JLogger getLogger()
	{
		return logger;
	}

	/**
	 * Returns the log directory.
	 *
	 * @return the log directory.
	 */
	public final String getLogsDir()
	{
		return this.logsDir;
	}

	/**
	 * Returns the bot's nickname regexp pattern.
	 *
	 * @return The nickname regexp pattern.
	 */
	private String getNickPattern()
	{
		final StringBuilder buff = new StringBuilder(0);
		final String nick = getNick();
		char c;

		for (int i = 0; i < nick.length(); i++)
		{
			c = nick.charAt(i);

			if (Character.isLetter(c))
			{
				buff.append('[').append(String.valueOf(c).toLowerCase()).append(String.valueOf(c).toUpperCase())
						.append(']');
			}
			else
			{
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
	public String getToday()
	{
		return this.today;
	}

	/**
	 * Returns the weblog URL.
	 *
	 * @return The weblog URL.
	 */
	public final String getWeblogUrl()
	{
		return this.weblogUrl;
	}

	/**
	 * Sets the weblog URL.
	 *
	 * @param weblogUrl The weblog URL.
	 */
	private void setWeblogUrl(final String weblogUrl)
	{
		this.weblogUrl = weblogUrl;
	}

	/**
	 * Returns indented and bold help string.
	 *
	 * @param help The help string.
	 *
	 * @return The indented help string.
	 */
	final public String helpIndent(final String help)
	{
		return helpIndent(help, true);
	}

	/**
	 * Returns indented help string.
	 *
	 * @param help The help string.
	 * @param isBold The bold flag.
	 *
	 * @return The indented help string.
	 */
	public String helpIndent(final String help, final boolean isBold)
	{
		return "        " + (isBold ? Utils.bold(help) : help);
	}

	/**
	 * Responds with the bot's help.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param topic The help topic, if any.
	 */
	private void helpResponse(final String sender, final String topic)
	{
		final String lcTopic = topic.toLowerCase().trim();

		if (lcTopic.equals(Commands.HELP_POSTING_KEYWORD))
		{
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
		}
		else if (lcTopic.equals(Commands.HELP_TAGS_KEYWORD))
		{
			send(sender, Utils.bold("To categorize or tag a URL, use its label and a T:"));
			send(sender, helpIndent(Commands.LINK_CMD + "1T:<+tag|-tag> [...]"));
		}
		else if (lcTopic.equals(Commands.VIEW_CMD))
		{
			send(sender, "To list or search the current URL posts:");
			send(sender, helpIndent(getNick() + ": " + Commands.VIEW_CMD) + " [<start>] [<query>]");
		}
		else if (lcTopic.equals(channel.substring(1).toLowerCase()))
		{
			send(sender, "To list the last 5 posts from the channel's weblog:");
			send(sender, helpIndent(getNick() + ": " + channel.substring(1)));
		}
		else if (lcTopic.equals(Commands.RECAP_CMD))
		{
			send(sender, "To list the last 10 public channel messages:");
			send(sender, helpIndent(getNick() + ": " + Commands.RECAP_CMD));
		}
		else if (lcTopic.equals(Commands.USERS_CMD))
		{
			send(sender, "To list the users present on the channel:");
			send(sender, helpIndent(getNick() + ": " + Commands.USERS_CMD));
		}
		else if (lcTopic.equals(Commands.INFO_CMD))
		{
			send(sender, "To view information about the bot:");
			send(sender, helpIndent(getNick() + ": " + Commands.INFO_CMD));
		}
		else if (lcTopic.equals(Commands.CYCLE_CMD) && isOp(sender))
		{
			send(sender, "To have the bot leave the channel and come back:");
			send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.CYCLE_CMD));
		}
		else if (lcTopic.equals(Commands.ME_CMD) && isOp(sender))
		{
			send(sender, "To have the bot perform an action:");
			send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.ME_CMD + " <action>"));
		}
		else if (lcTopic.equals(Commands.SAY_CMD) && isOp(sender))
		{
			send(sender, "To have the bot say something on the channel:");
			send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.SAY_CMD + " <text>"));
		}
		else if (lcTopic.equals(Commands.VERSION_CMD) && isOp(sender))
		{
			send(sender, "To view the version data (bot, java, etc.):");
			send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.VERSION_CMD));
		}
		else if (lcTopic.equals(Commands.MSG_CMD) && isOp(sender))
		{
			send(sender, "To have the bot send a private message to someone:");
			send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.MSG_CMD + " <nick> <text>"));
		}
		else if (lcTopic.equals(Commands.IGNORE_CMD))
		{
			send(sender, "To check your ignore status:");
			send(sender, helpIndent(getNick() + ": " + Commands.IGNORE_CMD));

			send(sender, "To toggle your ignore status:");
			send(sender, helpIndent(getNick() + ": " + Commands.IGNORE_CMD + ' ' + Commands.IGNORE_ME_KEYWORD));
		}
		else if (lcTopic.equals(Tell.TELL_CMD) && tell.isEnabled())
		{
			tell.helpResponse(sender);
		}
		else
		{
			for (final AbstractModule module : MODULES)
			{
				for (final String cmd : module.getCommands())
				{
					if (lcTopic.equals(cmd))
					{
						module.helpResponse(this, sender, topic, true);
						return;
					}
				}
			}

			send(sender, Utils.bold("Type a URL on " + channel + " to post it."));
			send(sender, "For more information on a specific command, type:");
			send(sender, helpIndent(getNick() + ": " + Commands.HELP_CMD + " <command>"));
			send(sender, "The commands are:");

			if (commandsList.isEmpty())
			{
				commandsList.add(Commands.IGNORE_CMD);
				commandsList.add(Commands.INFO_CMD);
				commandsList.add(channel.substring(1));
				commandsList.add(Commands.HELP_POSTING_KEYWORD);
				commandsList.add(Commands.HELP_TAGS_KEYWORD);
				commandsList.add(Commands.RECAP_CMD);
				commandsList.add(Commands.USERS_CMD);
				commandsList.add(Commands.VIEW_CMD);

				MODULES.stream().filter(AbstractModule::isEnabled)
						.forEach(module -> commandsList.addAll(module.getCommands()));

				if (tell.isEnabled())
				{
					commandsList.add(Tell.TELL_CMD);
				}

				Collections.sort(commandsList);
			}

			final StringBuilder sb = new StringBuilder(0);

			for (int i = 0, cmdCount = 1; i < commandsList.size(); i++, cmdCount++)
			{
				if (sb.length() > 0)
				{
					sb.append("  ");
				}

				sb.append(commandsList.get(i));

				// 6 commands per line or last command
				if (sb.length() > 0 && (cmdCount == 6 || i == (commandsList.size() - 1)))
				{
					send(sender, helpIndent(sb.toString()));

					sb.setLength(0);
					cmdCount = 0;
				}
			}

			if (isOp(sender))
			{
				send(sender, "The op commands are:");
				send(sender,
				     helpIndent(Commands.CYCLE_CMD + "  " + Commands.ME_CMD + "  " + Commands.MSG_CMD + "  "
				                + Commands.SAY_CMD + "  " + Commands.VERSION_CMD));
			}
		}
	}

	/**
	 * Processes the {@link net.thauvin.erik.mobibot.Commands#IGNORE_CMD} command.
	 *
	 * @param sender The sender.
	 * @param args The command arguments.
	 */
	private void ignoreResponse(final String sender, final String args)
	{
		if (!isOp(sender))
		{
			final String nick = sender.toLowerCase();
			final boolean isMe = args.toLowerCase().startsWith(Commands.IGNORE_ME_KEYWORD);

			if (ignoredNicks.contains(nick))
			{
				if (isMe)
				{
					ignoredNicks.remove(nick);

					send(sender, "You are no longer ignored.");
				}
				else
				{
					send(sender, "You are currently ignored.");
				}
			}
			else
			{
				if (isMe)
				{
					ignoredNicks.add(nick);

					send(sender, "You are now ignored.");
				}
				else
				{
					send(sender, "You are not currently ignored.");
				}
			}
		}
		else
		{
			if (args.length() > 0)
			{
				final String[] nicks = args.toLowerCase().split(" ");

				for (String nick : nicks)
				{
					if (Commands.IGNORE_ME_KEYWORD.equals(nick))
					{
						nick = sender.toLowerCase();
					}

					if (ignoredNicks.contains(nick))
					{
						ignoredNicks.remove(nick);
					}
					else
					{
						ignoredNicks.add(nick);
					}
				}
			}

			send(sender, "The following nicks are ignored: " + ignoredNicks.toString());
		}
	}

	/**
	 * Responds with the bot's information.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void infoResponse(final String sender, final boolean isPrivate)
	{
		for (final String info : INFO_STRS)
		{
			if (info.startsWith("http://"))
			{
				send(sender, Utils.green(info), isPrivate);
			}
			else
			{
				send(sender, info, isPrivate);
			}
		}

		final StringBuilder info = new StringBuilder("Uptime: ");

		long timeInSeconds = (System.currentTimeMillis() - START_TIME) / 1000L;

		final long years = timeInSeconds / 31540000L;

		if (years > 0)
		{
			info.append(years).append(Utils.plural(years, " year ", " years "));
			timeInSeconds -= (years * 31540000L);
		}


		final long weeks = timeInSeconds / 604800L;
		
		if (weeks > 0)
		{
			info.append(weeks).append(Utils.plural(weeks, " week ", " weeks "));
			timeInSeconds -= (weeks * 604800L);
		}


		final long days = timeInSeconds / 86400L;

		if (days > 0)
		{
			info.append(days).append(Utils.plural(days, " day ", " days "));
			timeInSeconds -= (days * 86400L);
		}


		final long hours = timeInSeconds / 3600L;

		if (hours > 0)
		{
			info.append(hours).append(Utils.plural(hours, " hour ", " hours "));
			timeInSeconds -= (hours * 3600L);
		}


		final long minutes = timeInSeconds / 60L;

		info.append(minutes).append(Utils.plural(minutes, " minute ", " minutes "));

		info.append("[Entries: ").append(entries.size());

		if (tell.isEnabled() && isOp(sender))
		{
			info.append(", Messages: ").append(tell.size());
		}

		info.append(']');

		send(sender, info.toString(), isPrivate);
	}

	/**
	 * Determines whether the specified nick should be ignored.
	 *
	 * @param nick The nick.
	 *
	 * @return <code>true</code> if the nick should be ignored, <code>false</code> otherwise.
	 */
	private boolean isIgnoredNick(final String nick)
	{
		return Utils.isValidString(nick) && ignoredNicks.contains(nick.toLowerCase());
	}

	/**
	 * Returns true is the specified sender is an Op on the {@link #channel channel}.
	 *
	 * @param sender The sender.
	 *
	 * @return true, if the sender is an Op.
	 */
	public boolean isOp(final String sender)
	{
		final User[] users = getUsers(channel);

		for (final User user : users)
		{
			if (user.getNick().equals(sender))
			{
				return user.isOp();
			}
		}

		return false;
	}

	@Override
	protected final void onDisconnect()
	{
		if (Utils.isValidString(weblogUrl))
		{
			setVersion(weblogUrl);
		}

		sleep(5);

		// Connect
		try
		{
			connect(ircServer, ircPort);
		}
		catch (Exception e)
		{
			int retries = 0;

			while ((retries++ < MAX_RECONNECT) && !isConnected())
			{
				sleep(10);

				try
				{
					connect(ircServer, ircPort);
				}
				catch (Exception ex)
				{
					if (retries == MAX_RECONNECT)
					{
						if (logger.isDebugEnabled())
						{
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

		if (Utils.isValidString(ident))
		{
			identify(ident);
		}

		if (Utils.isValidString(identNick) && Utils.isValidString(identMsg))
		{
			sendMessage(identNick, identMsg);
		}

		joinChannel(channel);
	}

	@Override
	protected final void onMessage(final String channel, final String sender, final String login, final String hostname,
	                               final String message)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug(">>> " + sender + ": " + message);
		}

		boolean isCommand = false;

		// Capture URLs posted on the channel
		if (message.matches(LINK_MATCH) && !isIgnoredNick(sender))
		{
			isCommand = true;

			final String[] cmds = message.split(" ", 2);

			if (cmds.length == 1 || (!cmds[1].contains(getNick())))
			{
				final String link = cmds[0].trim();
				boolean isBackup = false;

				final int dupIndex = findDupEntry(link);

				if (dupIndex == -1)
				{
					if (!Utils.today().equals(today))
					{
						isBackup = true;
						saveEntries(true);

						entries.clear();
						today = Utils.today();
					}

					final StringBuilder tags = new StringBuilder(defaultTags);
					String title = NO_TITLE;

					if (cmds.length == 2)
					{
						final String[] data = cmds[1].trim().split(TAGS_MARKER, 2);

						if (data.length == 1)
						{
							title = data[0].trim();
						}
						else
						{
							if (Utils.isValidString(data[0]))
							{
								title = data[0].trim();
							}

							tags.append(' ').append(data[1].trim());
						}
					}

					if (NO_TITLE.equals(title))
					{
						try
						{
							final Document html = Jsoup.connect(link).userAgent("Mozilla").get();
							final String htmlTitle = html.title();

							if (Utils.isValidString(htmlTitle))
							{
								title = htmlTitle;
							}
						}
						catch (IOException ignore)
						{
							// Do nothing
						}
					}

					entries.add(new EntryLink(link, title, sender, login, channel, tags.toString()));

					final int index = entries.size() - 1;
					final EntryLink entry = entries.get(index);
					send(channel, Utils.buildLink(index, entry));

					if (delicious != null)
					{
						delicious.addPost(entry);
					}

					saveEntries(isBackup);

					if (NO_TITLE.equals(entry.getTitle()))
					{
						send(sender, "Please specify a title, by typing:", true);
						send(sender, helpIndent(Commands.LINK_CMD + (index + 1) + ":|This is the title"), true);
					}
				}
				else
				{
					final EntryLink entry = entries.get(dupIndex);
					send(sender, "Duplicate >> " + Utils.buildLink(dupIndex, entry));
				}
			}
		}
		// mobibot: <command>
		else if (message.matches(getNickPattern() + ":.*"))
		{
			isCommand = true;

			final String[] cmds = message.substring(message.indexOf(':') + 1).trim().split(" ", 2);
			final String cmd = cmds[0].toLowerCase();

			String args = "";

			if (cmds.length > 1)
			{
				args = cmds[1].trim();
			}

			// mobibot: help
			if (cmd.startsWith(Commands.HELP_CMD))
			{
				helpResponse(sender, args);
			}
			// mobibot: recap
			else if (cmd.equals(Commands.RECAP_CMD))
			{
				recapResponse(sender, false);
			}
			// mobibot: users
			else if (cmd.equals(Commands.USERS_CMD))
			{
				usersResponse(sender, false);
			}
			// mobibot: info
			else if (cmd.equals(Commands.INFO_CMD))
			{
				infoResponse(sender, false);
			}
			// mobbiot: version
			else if (cmd.equals(Commands.VERSION_CMD))
			{
				versionResponse(sender, false);
			}
			// mobibot: <channel>
			else if (cmd.equalsIgnoreCase(channel.substring(1)))
			{
				feedResponse(sender);
			}
			// mobibot: view
			else if (cmd.startsWith(Commands.VIEW_CMD))
			{
				viewResponse(sender, args, false);
			}
			// mobibot: tell
			else if (cmd.startsWith(Tell.TELL_CMD) && tell.isEnabled())
			{
				tell.response(sender, args);
			}
			// mobibot: ignore
			else if (cmd.startsWith(Commands.IGNORE_CMD))
			{
				ignoreResponse(sender, args);
			}
			// modules
			else
			{
				for (final AbstractModule module : MODULES)
				{
					for (final String c : module.getCommands())
					{
						if (cmd.startsWith(c))
						{
							module.commandResponse(this, sender, args, false);
						}
					}
				}
			}
		}
		// L1:<comment>, L1:-, L1:|<title>, etc.
		else if (message.matches(Commands.LINK_CMD + "[0-9]+:.*"))
		{
			isCommand = true;

			final String[] cmds = message.substring(1).split(":", 2);
			final int index = Integer.parseInt(cmds[0]) - 1;

			// L1:<comment>
			if (index < entries.size())
			{
				final String cmd = cmds[1].trim();

				if (cmd.length() == 0)
				{
					final EntryLink entry = entries.get(index);
					send(channel, Utils.buildLink(index, entry));

					if (entry.hasTags())
					{
						send(channel, Utils.buildTags(index, entry));
					}

					if (entry.hasComments())
					{
						final EntryComment[] comments = entry.getComments();

						for (int i = 0; i < comments.length; i++)
						{
							send(channel, Utils.buildComment(index, i, comments[i]));
						}
					}
				}
				else
				{
					// L1:-
					if ("-".equals(cmd))
					{
						final EntryLink entry = entries.get(index);

						if (entry.getLogin().equals(login) || isOp(sender))
						{
							if (delicious != null)
							{
								delicious.deletePost(entry);
							}

							entries.remove(index);
							send(channel, "Entry " + Commands.LINK_CMD + (index + 1) + " removed.");
							saveEntries(false);
						}
						else
						{
							send(sender, "Please ask a channel op to remove this entry for you.");
						}
					}
					// L1:|<title>
					else if (cmd.charAt(0) == '|')
					{
						if (cmd.length() > 1)
						{
							final EntryLink entry = entries.get(index);
							entry.setTitle(cmd.substring(1).trim());

							if (delicious != null)
							{
								delicious.updatePost(entry.getLink(), entry);
							}

							send(channel, Utils.buildLink(index, entry));
							saveEntries(false);
						}
					}
					// L1:=<url>
					else if (cmd.charAt(0) == '=')
					{
						final EntryLink entry = entries.get(index);

						if (entry.getLogin().equals(login) || isOp(sender))
						{
							final String link = cmd.substring(1);

							if (link.matches(LINK_MATCH))
							{
								final String oldLink = entry.getLink();

								entry.setLink(link);

								if (delicious != null)
								{
									delicious.updatePost(oldLink, entry);
								}

								send(channel, Utils.buildLink(index, entry));
								saveEntries(false);
							}
						}
						else
						{
							send(sender, "Please ask a channel op to change this link for you.");
						}
					}
					// L1:?<author>
					else if (cmd.charAt(0) == '?')
					{
						if (isOp(sender))
						{
							if (cmd.length() > 1)
							{
								final EntryLink entry = entries.get(index);
								entry.setNick(cmd.substring(1));
								send(channel, Utils.buildLink(index, entry));
								saveEntries(false);
							}
						}
						else
						{
							send(sender, "Please ask a channel op to change the author of this link for you.");
						}
					}
					else
					{
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
		else if (message.matches(Commands.LINK_CMD + "[0-9]+T:.*"))
		{
			isCommand = true;

			final String[] cmds = message.substring(1).split("T:", 2);
			final int index = Integer.parseInt(cmds[0]) - 1;

			if (index < entries.size())
			{
				final String cmd = cmds[1].trim();

				final EntryLink entry = entries.get(index);

				if (cmd.length() != 0)
				{
					if (entry.getLogin().equals(login) || isOp(sender))
					{
						entry.setTags(cmd);

						if (delicious != null)
						{
							delicious.updatePost(entry.getLink(), entry);
						}

						send(channel, Utils.buildTags(index, entry));
						saveEntries(false);
					}
					else
					{
						send(sender, "Please ask a channel op to change the tags for you.");
					}
				}
				else
				{
					if (entry.hasTags())
					{
						send(channel, Utils.buildTags(index, entry));
					}
					else
					{
						send(sender, "The entry has no tags. Why don't add some?");
					}
				}
			}
		}
		// L1.1:<command>
		else if (message.matches(Commands.LINK_CMD + "[0-9]+\\.[0-9]+:.*"))
		{
			isCommand = true;

			final String[] cmds = message.substring(1).split("[.:]", 3);
			final int index = Integer.parseInt(cmds[0]) - 1;

			if (index < entries.size())
			{
				final EntryLink entry = entries.get(index);
				final int cindex = Integer.parseInt(cmds[1]) - 1;

				if (cindex < entry.getCommentsCount())
				{
					final String cmd = cmds[2].trim();

					// L1.1:
					if (cmd.length() == 0)
					{
						final EntryComment comment = entry.getComment(cindex);
						send(channel, Utils.buildComment(index, cindex, comment));
					}
					// L1.1:-
					else if ("-".equals(cmd))
					{
						entry.deleteComment(cindex);
						send(channel, "Comment " + Commands.LINK_CMD + (index + 1) + '.' + (cindex + 1) + " removed.");
						saveEntries(false);
					}
					// L1.1:?<author>
					else if (cmd.charAt(0) == '?')
					{
						if (isOp(sender))
						{
							if (cmd.length() > 1)
							{
								final EntryComment comment = entry.getComment(cindex);
								comment.setNick(cmd.substring(1));
								send(channel, Utils.buildComment(index, cindex, comment));
								saveEntries(false);
							}
						}
						else
						{
							send(sender, "Please ask a channel op to change the author of this comment for you.");
						}
					}
					else
					{
						entry.setComment(cindex, cmd, sender);

						final EntryComment comment = entry.getComment(cindex);
						send(sender, Utils.buildComment(index, cindex, comment));
						saveEntries(false);
					}
				}
			}
		}

		if (!isCommand)
		{
			recap(sender, message, false);
		}

		tell.send(sender, true);
	}

	@Override
	protected final void onPrivateMessage(final String sender, final String login, final String hostname,
	                                      final String message)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug(">>> " + sender + ": " + message);
		}

		final String[] cmds = message.split(" ", 2);
		final String cmd = cmds[0].toLowerCase();
		String args = "";

		if (cmds.length > 1)
		{
			args = cmds[1].trim();
		}

		if (cmd.startsWith(Commands.HELP_CMD))
		{
			helpResponse(sender, args);
		}
		else if ("kill".equals(cmd) && isOp(sender))
		{
			sendRawLine("QUIT : Poof!");
			System.exit(0);
		}
		else if (cmd.equals(Commands.DIE_CMD) && isOp(sender))
		{
			send(channel, sender + " has just signed my death sentence.");
			saveEntries(true);
			sleep(3);
			quitServer("The Bot Is Out There!");
			System.exit(0);
		}
		else if (cmd.equals(Commands.CYCLE_CMD))
		{
			send(channel, sender + " has just asked me to leave. I'll be back!");
			sleep(0);
			partChannel(channel);
			sleep(10);
			joinChannel(channel);
		}
		else if (cmd.equals(Commands.RECAP_CMD))
		{
			recapResponse(sender, true);
		}
		else if (cmd.equals(Commands.USERS_CMD))
		{
			usersResponse(sender, true);
		}
		else if (cmd.equals(Commands.ADDLOG_CMD) && (cmds.length > 1) && isOp(sender))
		{
			// e.g. 2014-04-01
			final File backlog = new File(logsDir + args + EntriesMgr.XML_EXT);
			if (backlog.exists())
			{
				history.add(0, args);
				send(sender, history.toString(), true);
			}
			else
			{
				send(sender, "The specified log could not be found.");
			}
		}
		else if (cmd.equals(Commands.ME_CMD) && isOp(sender))
		{
			if (args.length() > 1)
			{
				action(args);
			}
			else
			{
				helpResponse(sender, Commands.ME_CMD);
			}
		}
		else if (cmd.equals(Commands.NICK_CMD) && (cmds.length > 1))
		{
			if (isOp(sender))
			{
				changeNick(args);
			}
		}
		else if (cmd.equals(Commands.SAY_CMD) && isOp(sender))
		{
			if (cmds.length > 1)
			{
				send(channel, args, true);
			}
			else
			{
				helpResponse(sender, Commands.SAY_CMD);
			}
		}
		else if (cmd.equals(Commands.MSG_CMD) && isOp(sender))
		{
			if (cmds.length > 1)
			{
				final String[] msg = args.split(" ", 2);

				if (args.length() > 2)
				{
					send(msg[0], msg[1], true);
				}
				else
				{
					helpResponse(sender, Commands.MSG_CMD);
				}
			}
			else
			{
				helpResponse(sender, Commands.MSG_CMD);
			}
		}
		else if (cmd.equals(Commands.VIEW_CMD))
		{
			viewResponse(sender, args, true);
		}
		else if (cmd.equals(Tell.TELL_CMD) && tell.isEnabled())
		{
			tell.response(sender, args);
		}
		else if (cmd.equals(Commands.INFO_CMD))
		{
			infoResponse(sender, true);
		}
		else if (cmd.equals(Commands.VERSION_CMD))
		{
			versionResponse(sender, true);
		}
		else if (cmd.equals(Commands.DEBUG_CMD) && isOp(sender))
		{
			if (logger.isDebugEnabled())
			{
				logger.getLogger().setLevel(loggerLevel);
			}
			else
			{
				logger.getLogger().setLevel(Level.DEBUG);
			}

			send(sender, "Debug logging is " + (logger.isDebugEnabled() ? "enabled." : "disabled."), true);
		}
		else
		{
			for (final AbstractModule module : MODULES)
			{
				if (module.isPrivateMsgEnabled())
				{
					for (final String c : module.getCommands())
					{
						if (cmd.equals(c))
						{
							module.commandResponse(this, sender, args, true);
							return;
						}
					}
				}
			}

			helpResponse(sender, "");
		}
	}

	@Override
	protected final void onAction(final String sender, final String login, final String hostname, final String target,
	                              final String action)
	{
		if (target.equals(channel))
		{
			recap(sender, action, true);
		}
	}

	/**
	 * Stores the last 10 public messages and actions.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param message The actual message sent.
	 * @param isAction Set to true if the message is an action.
	 */
	private void recap(final String sender, final String message, final boolean isAction)
	{
		recap.add(Utils.UTC_SDF.format(Calendar.getInstance().getTime()) + " -> " + sender + (isAction ? " " : ": ")
		          + message);

		if (recap.size() > MAX_RECAP)
		{
			recap.remove(0);
		}
	}

	@Override
	protected void onJoin(final String channel, final String sender, final String login, final String hostname)
	{
		tell.send(sender);
	}

	@Override
	protected void onNickChange(final String oldNick, final String login, final String hostname, final String newNick)
	{
		tell.send(newNick);
	}

	/**
	 * Responds with the last 10 public messages.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void recapResponse(final String sender, final boolean isPrivate)
	{
		for (final String recap : this.recap)
		{
			send(sender, recap, isPrivate);
		}
	}

	/**
	 * Sends a private message or notice.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param message The actual message.
	 * @param isPrivate Set to true if the response should be a private message, otherwise a notice is sent.
	 */
	public final void send(final String sender, final String message, final boolean isPrivate)
	{
		if (Utils.isValidString(message) && Utils.isValidString(sender))
		{
			if (isPrivate)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Sending message to " + sender + ": " + message);
				}

				sendMessage(sender, message);
			}
			else
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Sending notice to " + sender + ": " + message);
				}

				sendNotice(sender, message);
			}
		}
	}

	/**
	 * Sends a private notice.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param message The actual message.
	 */
	public final void send(final String sender, final String message)
	{
		send(sender, message, false);
	}

	/**
	 * Responds with the users on a channel.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void usersResponse(final String sender, final boolean isPrivate)
	{
		final User[] users = getUsers(channel);
		final String[] nicks = new String[users.length];

		for (int i = 0; i < users.length; i++)
		{
			nicks[i] = users[i].getNick();
		}

		Arrays.sort(nicks, String.CASE_INSENSITIVE_ORDER);

		final StringBuilder buff = new StringBuilder(0);

		for (final String nick : nicks)
		{
			if (isOp(nick))
			{
				buff.append('@');
			}

			buff.append(nick).append(' ');
		}

		send(sender, buff.toString(), isPrivate);
	}

	/**
	 * Responds with the bot's version info.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void versionResponse(final String sender, final boolean isPrivate)
	{
		if (isOp(sender))
		{
			for (final String version : VERSION_STRS)
			{
				send(sender, version, isPrivate);
			}
		}
	}

	/**
	 * Responds with the stored links.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param args The view command arguments.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void viewResponse(final String sender, final String args, final boolean isPrivate)
	{
		String lcArgs = args.toLowerCase();

		if (!entries.isEmpty())
		{
			final int max = entries.size();
			int i = 0;

			if (!(lcArgs.length() > 0) && (max > MAX_ENTRIES))
			{
				i = max - MAX_ENTRIES;
			}

			if (lcArgs.matches("^\\d+(| .*)"))
			{
				final String[] split = lcArgs.split(" ", 2);

				try
				{
					i = Integer.parseInt(split[0]);

					if (i > 0)
					{
						i--;
					}

					if (split.length == 2)
					{
						lcArgs = split[1].trim();
					}
					else
					{
						lcArgs = "";
					}

					if (i > max)
					{
						i = 0;
					}
				}
				catch (NumberFormatException ignore)
				{
					; // Do nothing
				}
			}

			EntryLink entry;
			int sent = 0;

			for (; i < max; i++)
			{
				entry = entries.get(i);

				if (lcArgs.length() > 0)
				{
					if ((entry.getLink().toLowerCase().contains(lcArgs)) ||
					    (entry.getTitle().toLowerCase().contains(lcArgs)) ||
					    (entry.getNick().toLowerCase().contains(lcArgs)))
					{
						if (sent > MAX_ENTRIES)
						{
							send(sender,
							     "To view more, try: " + Utils
									     .bold(getNick() + ": " + Commands.VIEW_CMD + ' ' + (i + 1) + ' ' + lcArgs),
							     isPrivate);

							break;
						}

						send(sender, Utils.buildLink(i, entry, true), isPrivate);
						sent++;
					}
				}
				else
				{
					if (sent > MAX_ENTRIES)
					{
						send(sender,
						     "To view more, try: " + Utils.bold(getNick() + ": " + Commands.VIEW_CMD + ' ' + (i + 1)),
						     isPrivate);

						break;
					}

					send(sender, Utils.buildLink(i, entry, true), isPrivate);
					sent++;
				}
			}
		}
		else
		{
			send(sender, "There is currently nothing to view. Why don't you post something?", isPrivate);
		}
	}
}
