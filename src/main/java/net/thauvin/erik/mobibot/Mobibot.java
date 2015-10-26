/*
 * Mobibot.java
 *
 * Copyright (c) 2004-2015, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.thauvin.erik.mobibot;

import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.io.FeedException;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.cli.*;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;
import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implements the #mobitopia bot.
 *
 * @author Erik C. Thauvin
 * @created Jan 31, 2004
 * @since 1.0
 */
public class Mobibot extends PircBot
{
	/**
	 * The connect/read timeout in ms.
	 */
	public static final int CONNECT_TIMEOUT = 5000;

	/**
	 * The empty title string.
	 */
	public static final String NO_TITLE = "No Title";

	/**
	 * The default port.
	 */
	private static final int DEFAULT_PORT = 6667;

	/**
	 * The default maximum number of days to keep {@link Commands#TELL_CMD} messages.
	 */

	private static final int DEFAULT_TELL_MAX_DAYS = 7;

	/**
	 * The default {@link Commands#TELL_CMD) message max queue size.
	 */
	private static final int DEFAULT_TELL_MAX_SIZE = 50;

	/**
	 * The info strings.
	 */
	private static final String[] INFO_STRS = {
			ReleaseInfo.getProject() + " v" + ReleaseInfo.getVersion() + '.' + ReleaseInfo.getBuildNumber()
			+ " by Erik C. Thauvin (erik@thauvin.net)", "http://www.mobitopia.org/mobibot/"
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
	 * The serialized object file extension.
	 */
	private static final String SER_EXT = ".ser";

	/**
	 * Shall we play a game?
	 */
	private static final String SHALL_WE_PLAY_A_GAME = "Shall we play a game?";

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
			"Version: " + ReleaseInfo.getVersion() + '.' + ReleaseInfo.getBuildNumber() + " (" + Utils.ISO_SDF
					.format(ReleaseInfo.getBuildDate()) + ')',
			"Platform: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " + System
					.getProperty("os.arch") + ", " + System.getProperty("user.country") + ')',
			"Runtime: " + System.getProperty("java.runtime.name") + " (build " + System
					.getProperty("java.runtime.version") + ')',
			"VM: " + System.getProperty("java.vm.name") + " (build " + System.getProperty("java.vm.version") + ", "
			+ System.getProperty("java.vm.info") + ')'
	};

	/**
	 * The main channel.
	 */
	private final String channel;

	/**
	 * The commands list.
	 */
	private final List<String> commandsList = new ArrayList<String>();

	/**
	 * The currency converter.
	 */
	private final CurrencyConverter currencyConverter;

	/**
	 * The entries array.
	 */
	private final List<EntryLink> entries = new ArrayList<EntryLink>(0);

	/**
	 * The feed info cache.
	 */
	private final FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();

	/**
	 * The history/backlogs array.
	 */
	private final List<String> history = new ArrayList<String>(0);

	/**
	 * The ignored nicks array.
	 */
	private final List<String> ignoredNicks = new ArrayList<String>(0);

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
	private final List<String> recap = new ArrayList<String>(0);

	/**
	 * The serialized object file.
	 */
	private final String serializedObject;

	/**
	 * Processes the {@link Commands#TELL_CMD} messages queue.
	 */
	private final List<TellMessage> tellMessages = new CopyOnWriteArrayList<TellMessage>();

	/**
	 * Time command.
	 */
	private final WorldTime worldTime = new WorldTime();

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
	 * The number of days message are kept.
	 */
	private int tellMaxDays = DEFAULT_TELL_MAX_DAYS;

	/**
	 * The maximum number of  {@link Commands#TELL_CMD} messages allowed.
	 */
	private int tellMaxSize = DEFAULT_TELL_MAX_SIZE;

	/**
	 * Today's date.
	 */
	private String today = Utils.today();

	/**
	 * The Twitter consumer key.
	 */
	private String twitterConsumerKey = "";

	/**
	 * The Twitter consumer secret.
	 */
	private String twitterConsumerSecret = "";

	/**
	 * The Twitter token.
	 */
	private String twitterToken = "";

	/**
	 * The Twitter token secret.
	 */
	private String twitterTokenSecret = "";

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
	public Mobibot(String server, int port, String nickname, String channel, String logsDir)
	{
		System.getProperties().setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(CONNECT_TIMEOUT));
		System.getProperties().setProperty("sun.net.client.defaultReadTimeout", String.valueOf(CONNECT_TIMEOUT));

		setName(nickname);

		ircServer = server;
		ircPort = port;
		this.channel = channel;
		this.logsDir = logsDir;
		this.serializedObject = logsDir + getName() + SER_EXT;

		// Set the logger level
		loggerLevel = logger.getLogger().getLevel();

		// Initialization
		Utils.UTC_SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
		currencyConverter = new CurrencyConverter(this);

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
	}

	/**
	 * The Truth Is Out There...
	 *
	 * @param args The command line arguments.
	 */
	public static void main(String[] args)
	{
		// Setup the command line options
		final Options options = new Options();
		options.addOption(Commands.HELP_ARG.substring(0, 1), Commands.HELP_ARG, false, "print this help message");
		options.addOption(Commands.DEBUG_ARG.substring(0, 1),
		                  Commands.DEBUG_ARG,
		                  false,
		                  "print debug & logging data directly to the console");
		//noinspection AccessStaticViaInstance
		options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("use alternate properties file")
				                  .withLongOpt(Commands.PROPS_ARG).create(Commands.PROPS_ARG.substring(0, 1)));
		options.addOption(Commands.VERSION_ARG.substring(0, 1), Commands.VERSION_ARG, false, "print version info");

		// Parse the command line
		final CommandLineParser parser = new PosixParser();
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

			// Get the Twitter properties
			final String tconsumerKey = p.getProperty("twitter-consumerKey");
			final String tconsumerSecret = p.getProperty("twitter-consumerSecret");
			final String ttoken = p.getProperty("twitter-token", "");
			final String ttokenSecret = p.getProperty("twitter-tokenSecret", "");

			// Get the tell command settings
			final int tellMaxDays = Utils.getIntProperty(p.getProperty("tell-max-days"), DEFAULT_TELL_MAX_DAYS);
			final int tellMaxSize = Utils.getIntProperty(p.getProperty("tell-max-size"), DEFAULT_TELL_MAX_SIZE);

			// Create the bot
			final Mobibot bot = new Mobibot(server, port, nickname, channel, logsDir);

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

			if (Utils.isValidString(tconsumerKey) && Utils.isValidString(tconsumerSecret) && Utils.isValidString(ttoken)
			    && Utils.isValidString(ttokenSecret))
			{
				// Set the Twitter authentication
				bot.setTwitterAuth(tconsumerKey, tconsumerSecret, ttoken, ttokenSecret);
			}

			// Set the tags
			bot.setTags(tags);

			// Set the ignored nicks
			bot.setIgnoredNicks(ignoredNicks);

			// Set the tell command
			bot.setTell(tellMaxDays, tellMaxSize);

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

			// Load the messages queue
			bot.tellMessages.addAll(TellMessagesMgr.load(bot.getSerializedObject(), bot.getLogger()));
			if (bot.cleanTellMessages())
			{
				bot.saveTellMessages();
			}
		}
	}

	/**
	 * Sets the ident password.
	 *
	 * @param pwd The password.
	 */
	private void setIdent(String pwd)
	{
		ident = pwd;
	}

	/**
	 * Sets the ident nickname.
	 *
	 * @param nick The nickname.
	 */
	private void setIdentNick(String nick)
	{
		identNick = nick;
	}

	/**
	 * Sets the ident message.
	 *
	 * @param msg The message.
	 */
	private void setIdentMsg(String msg)
	{
		identMsg = msg;
	}

	/**
	 * Sets the feed URL.
	 *
	 * @param feedURL The feed URL.
	 */
	private void setFeedURL(String feedURL)
	{
		this.feedURL = feedURL;
	}

	/**
	 * Sets the del.icio.us authentication.
	 *
	 * @param username The del.icio.us username.
	 * @param password The del.icio.us password.
	 */
	private void setDeliciousAuth(String username, String password)
	{
		delicious = new DeliciousPoster(username, password, ircServer);
	}

	/**
	 * Sets the Twitter consumerSecret and password..
	 *
	 * @param consumerKey The Twitter consumer key.
	 * @param consumerSecret The Twitter consumer secret.
	 * @param token The Twitter token.
	 * @param tokenSecret The Twitter token secret.
	 */
	private void setTwitterAuth(String consumerKey, String consumerSecret, String token, String tokenSecret)
	{
		twitterConsumerKey = consumerKey;
		twitterConsumerSecret = consumerSecret;
		twitterToken = token;
		twitterTokenSecret = tokenSecret;
	}

	/**
	 * Sets the default tags/categories.
	 *
	 * @param tags The tags.
	 */
	private void setTags(String tags)
	{
		defaultTags = tags;
	}

	/**
	 * Sets the Ignored nicks.
	 *
	 * @param nicks The nicks to ignore
	 */
	private void setIgnoredNicks(String nicks)
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
	 * Set the {@link Commands#TELL_CMD} parameters
	 *
	 * @param tellMaxDays The max number of days to hold messages for.
	 * @param tellMaxSize The maximmm number of messages to hold
	 */
	private void setTell(int tellMaxDays, int tellMaxSize)
	{
		this.tellMaxDays = tellMaxDays;
		this.tellMaxSize = tellMaxSize;
	}

	/**
	 * Saves the entries.
	 *
	 * @param isDayBackup Set the true if the daily backup file should also be created.
	 */
	private void saveEntries(boolean isDayBackup)
	{
		EntriesMgr.saveEntries(this, entries, history, isDayBackup);
	}

	/**
	 * Sleeps for the specified number of seconds.
	 *
	 * @param secs The number of seconds to sleep for.
	 */
	private static void sleep(int secs)
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
	 * Reruns the serialized object file.
	 *
	 * @return The file location.
	 */
	private String getSerializedObject()
	{
		return serializedObject;
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
	 * Cleans the {@link #tellMessages} messages queue.
	 *
	 * @return <code>True</code> if the queue was cleaned.
	 */
	private boolean cleanTellMessages()
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("Cleaning the messages.");
		}

		return TellMessagesMgr.cleanTellMessages(tellMessages, tellMaxDays);
	}

	/**
	 * Saves the {@link #tellMessages} messages queue.
	 */
	private void saveTellMessages()
	{
		TellMessagesMgr.save(getSerializedObject(), tellMessages, logger);
	}

	/**
	 * Sends an action to the current channel.
	 *
	 * @param action The action.
	 */
	final void action(String action)
	{
		action(channel, action);
	}

	/**
	 * Sends an action to the channel.
	 *
	 * @param channel The channel.
	 * @param action The action.
	 */
	private void action(String channel, String action)
	{
		if (Utils.isValidString(channel) && Utils.isValidString(action))
		{
			sendAction(channel, action);
		}
	}

	/**
	 * Processes the {@link Commands#CALC_CMD} command.
	 *
	 * @param sender The nick of the person who sent the message
	 * @param args The command arguments.
	 * @param message The actual message.
	 */
	private void calcResponse(String sender, String args, String message)
	{
		if (Utils.isValidString(args))
		{
			final DecimalFormat decimalFormat = new DecimalFormat("#.##");

			try
			{
				final Expression calc = new ExpressionBuilder(args).build();
				send(channel, args.replaceAll(" ", "") + " = " + decimalFormat.format(calc.evaluate()));
			}
			catch (Exception e)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Unable to calculate: " + message, e);
				}

				send(channel, "No idea. This is the kind of math I don't get.");
			}
		}
		else
		{
			helpResponse(sender, Commands.CALC_CMD);
		}
	}

	/**
	 * Responds with the title and links from the RSS feed.
	 *
	 * @param sender The nick of the person who sent the private message.
	 */
	private void feedResponse(String sender)
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
	private int findDupEntry(String link)
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
	private void setBacklogsUrl(String backLogsUrl)
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
	 * Returns the {@link FeedFetcherCache feed info cache}.
	 *
	 * @return The feed info cache.
	 */
	public final FeedFetcherCache getFeedInfoCache()
	{
		return this.feedInfoCache;
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
	private void setWeblogUrl(String weblogUrl)
	{
		this.weblogUrl = weblogUrl;
	}

	/**
	 * Responds with the Google search results for the specified query.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param query The Google query to execute.
	 */
	private void googleResponse(String sender, String query)
	{
		if (query.length() > 0)
		{
			new Thread(new GoogleSearch(this, sender, query)).start();
		}
		else
		{
			helpResponse(sender, Commands.GOOGLE_CMD);
		}
	}

	/**
	 * Returns indented and bold help string.
	 *
	 * @param help The help string.
	 *
	 * @return The indented help string.
	 */
	private String helpIndent(String help)
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
	private String helpIndent(String help, boolean isBold)
	{
		return "        " + (isBold ? Utils.bold(help) : help);
	}

	/**
	 * Responds with the bot's help.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param topic The help topic, if any.
	 */
	public final void helpResponse(String sender, String topic)
	{
		final String lcTopic = topic.toLowerCase();

		if (lcTopic.endsWith(Commands.HELP_POSTING_KEYWORD))
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
		else if (lcTopic.endsWith(Commands.HELP_TAGS_KEYWORD))
		{
			send(sender, Utils.bold("To categorize or tag a URL, use its label and a T:"));
			send(sender, helpIndent(Commands.LINK_CMD + "1T:<+tag|-tag> [...]"));
		}
		else if (lcTopic.endsWith(Commands.VIEW_CMD))
		{
			send(sender, "To list or search the current URL posts:");
			send(sender, helpIndent(getNick() + ": " + Commands.VIEW_CMD) + " [<start>] [<query>]");
		}
		else if (lcTopic.endsWith(channel.substring(1).toLowerCase()))
		{
			send(sender, "To list the last 5 posts from the channel's weblog:");
			send(sender, helpIndent(getNick() + ": " + channel.substring(1)));
		}
		else if (lcTopic.endsWith(Commands.GOOGLE_CMD))
		{
			send(sender, "To search Google:");
			send(sender, helpIndent(getNick() + ": " + Commands.GOOGLE_CMD + " <query>"));
		}
		else if (lcTopic.endsWith(Commands.TWITTER_CMD) && isTwitterEnabled())
		{
			send(sender, "To post to Twitter:");
			send(sender, helpIndent(getNick() + ": " + Commands.TWITTER_CMD + " <message>"));
		}
		else if (lcTopic.endsWith(Commands.RECAP_CMD))
		{
			send(sender, "To list the last 10 public channel messages:");
			send(sender, helpIndent(getNick() + ": " + Commands.RECAP_CMD));
		}
		else if (lcTopic.endsWith(Commands.CALC_CMD))
		{
			send(sender, "To solve a mathematical calculation:");
			send(sender, helpIndent(getNick() + ": " + Commands.CALC_CMD + " <calculation>"));
		}
		else if (lcTopic.endsWith(Commands.LOOKUP_CMD))
		{
			send(sender, "To perform a DNS lookup query:");
			send(sender, helpIndent(getNick() + ": " + Commands.LOOKUP_CMD + " <ip address or hostname>"));
		}
		else if (lcTopic.endsWith(Commands.TIME_CMD))
		{
			send(sender, "To display a country's current date/time:");
			send(sender, helpIndent(getNick() + ": " + Commands.TIME_CMD) + " [<country code>]");

			send(sender, "For a listing of the supported countries:");
			send(sender, helpIndent(getNick() + ": " + Commands.TIME_CMD));
		}
		else if (lcTopic.endsWith(Commands.JOKE_CMD))
		{
			send(sender, "To retrieve a random joke:");
			send(sender, helpIndent(getNick() + ": " + Commands.JOKE_CMD));
		}
		else if (lcTopic.endsWith(Commands.STOCK_CMD))
		{
			send(sender, "To retrieve a stock quote:");
			send(sender, helpIndent(getNick() + ": " + Commands.STOCK_CMD + " <symbol[.country code]>"));
		}
		else if (lcTopic.endsWith(Commands.DICE_CMD))
		{
			send(sender, "To roll the dice:");
			send(sender, helpIndent(getNick() + ": " + Commands.DICE_CMD));
		}
		else if (lcTopic.endsWith(Commands.WAR_CMD))
		{
			send(sender, "To play war:");
			send(sender, helpIndent(getNick() + ": " + Commands.WAR_CMD));
		}
		else if (lcTopic.endsWith(Commands.WEATHER_CMD))
		{
			send(sender, "To display weather information:");
			send(sender, helpIndent(getNick() + ": " + Commands.WEATHER_CMD + " <station id>"));
			send(sender, "For a listing of the ICAO station IDs, please visit: " + Weather.STATIONS_URL);
		}
		else if (lcTopic.endsWith(Commands.USERS_CMD))
		{
			send(sender, "To list the users present on the channel:");
			send(sender, helpIndent(getNick() + ": " + Commands.USERS_CMD));
		}
		else if (lcTopic.endsWith(Commands.INFO_CMD))
		{
			send(sender, "To view information about the bot:");
			send(sender, helpIndent(getNick() + ": " + Commands.INFO_CMD));
		}
		else if (lcTopic.endsWith(Commands.CYCLE_CMD))
		{
			if (isOp(sender))
			{
				send(sender, "To have the bot leave the channel and come back:");
				send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.CYCLE_CMD));
			}
		}
		else if (lcTopic.endsWith(Commands.ME_CMD))
		{
			if (isOp(sender))
			{
				send(sender, "To have the bot perform an action:");
				send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.ME_CMD + " <action>"));
			}
		}
		else if (lcTopic.endsWith(Commands.SAY_CMD))
		{
			if (isOp(sender))
			{
				send(sender, "To have the bot say something on the channel:");
				send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.SAY_CMD + " <text>"));
			}
		}
		else if (lcTopic.endsWith(Commands.VERSION_CMD))
		{
			if (isOp(sender))
			{
				send(sender, "To view the version data (bot, java, etc.):");
				send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.VERSION_CMD));
			}
		}
		else if (lcTopic.endsWith(Commands.MSG_CMD))
		{
			if (isOp(sender))
			{
				send(sender, "To have the bot send a private message to someone:");
				send(sender, helpIndent("/msg " + getNick() + ' ' + Commands.MSG_CMD + " <nick> <text>"));
			}
		}
		else if (lcTopic.startsWith(Commands.CURRENCY_CMD))
		{
			send(sender, "To convert from one currency to another:");
			send(sender, helpIndent(getNick() + ": " + Commands.CURRENCY_CMD + " [100 USD to EUR]"));

			if (lcTopic.endsWith(Commands.CURRENCY_CMD))
			{
				send(sender, "For a listing of currency rates:");
				send(sender,
				     helpIndent(getNick() + ": " + Commands.CURRENCY_CMD) + ' ' + Commands.CURRENCY_RATES_KEYWORD);
				send(sender, "For a listing of supported currencies:");
				send(sender, helpIndent(getNick() + ": " + Commands.CURRENCY_CMD));
			}
		}
		else if (lcTopic.startsWith(Commands.IGNORE_CMD))
		{
			send(sender, "To check your ignore status:");
			send(sender, helpIndent(getNick() + ": " + Commands.IGNORE_CMD));

			send(sender, "To toggle your ignore status:");
			send(sender, helpIndent(getNick() + ": " + Commands.IGNORE_CMD + ' ' + Commands.IGNORE_ME_KEYWORD));

		}
		else if (lcTopic.startsWith(Commands.TELL_CMD))
		{
			send(sender, "To send a message to someone when they join the channel:");
			send(sender, helpIndent(getNick() + ": " + Commands.TELL_CMD + " <nick> <message>"));

			send(sender, "To view queued and sent messages:");
			send(sender, helpIndent(getNick() + ": " + Commands.TELL_CMD + ' ' + Commands.VIEW_CMD));

			send(sender, "Messages are kept for " + Utils.bold(tellMaxDays) + " days.");
		}
		else
		{
			send(sender, Utils.bold("Type a URL on " + channel + " to post it."));
			send(sender, "For more information on specific command, type:");
			send(sender, helpIndent(getNick() + ": " + Commands.HELP_CMD + " <command>"));
			send(sender, "The commands are:");

			if (commandsList.isEmpty())
			{
				commandsList.add(Commands.CALC_CMD);
				commandsList.add(Commands.CURRENCY_CMD);
				commandsList.add(Commands.DICE_CMD);
				commandsList.add(Commands.GOOGLE_CMD);
				commandsList.add(Commands.IGNORE_CMD);
				commandsList.add(Commands.INFO_CMD);
				commandsList.add(Commands.JOKE_CMD);
				commandsList.add(Commands.LOOKUP_CMD);
				commandsList.add(channel.substring(1));
				commandsList.add(Commands.HELP_POSTING_KEYWORD);
				commandsList.add(Commands.RECAP_CMD);
				commandsList.add(Commands.STOCK_CMD);
				commandsList.add(Commands.HELP_TAGS_KEYWORD);
				commandsList.add(Commands.TIME_CMD);
				commandsList.add(Commands.USERS_CMD);
				commandsList.add(Commands.VIEW_CMD);
				commandsList.add(Commands.WAR_CMD);
				commandsList.add(Commands.WEATHER_CMD);

				if (isTellEnabled())
				{
					commandsList.add(Commands.TELL_CMD);
				}

				if (isTwitterEnabled())
				{
					commandsList.add(Commands.TWITTER_CMD);
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

				// 5 commands per line or last command
				if (sb.length() > 0 && (cmdCount == 5 || i == (commandsList.size() - 1)))
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
	private void ignoreResponse(String sender, String args)
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
	private void infoResponse(String sender, boolean isPrivate)
	{
		for (final String info : INFO_STRS)
		{
			send(sender, info, isPrivate);
		}

		long timeInSeconds = (System.currentTimeMillis() - START_TIME) / 1000L;

		final long days = timeInSeconds / 86400L;
		timeInSeconds -= (days * 86400L);

		final long hours = timeInSeconds / 3600L;
		timeInSeconds -= (hours * 3600L);

		final long minutes = timeInSeconds / 60L;

		send(sender,
		     "Uptime: " + days + " day(s) " + hours + " hour(s) " + minutes + " minute(s)  [Entries: " + entries.size()
		     + (isTellEnabled() && isOp(sender) ? ", Messages: " + tellMessages.size() : "") + ']',
		     isPrivate);
	}

	/**
	 * Determines whether the specified nick should be ignored.
	 *
	 * @param nick The nick.
	 *
	 * @return <code>true</code> if the nick should be ignored, <code>false</code> otherwise.
	 */
	private boolean isIgnoredNick(String nick)
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
	private boolean isOp(String sender)
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

	/**
	 * Returns <code>true</code> if twitter posting is enabled.
	 *
	 * @return <code>true</code> or <code>false</code>
	 */
	private boolean isTwitterEnabled()
	{
		return Utils.isValidString(twitterConsumerKey) && Utils.isValidString(twitterConsumerSecret) && Utils
				.isValidString(twitterToken) && Utils.isValidString(twitterTokenSecret);
	}

	/**
	 * Responds with the results of a DNS query.
	 *
	 * @param sender The nick of the person who sent the message
	 * @param query The hostname or IP address.
	 */
	private void lookupResponse(String sender, String query)
	{
		if (query.matches("(\\S.)+(\\S)+"))
		{
			try
			{
				send(channel, Lookup.lookup(query));
			}
			catch (UnknownHostException ignore)
			{
				if (query.matches(
						"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"))
				{
					try
					{
						final String[] lines = Lookup.whois(query);

						if ((lines != null) && (lines.length > 0))
						{
							String line;

							for (final String rawLine : lines)
							{
								line = rawLine.trim();

								if ((line.length() > 0) && (line.charAt(0) != '#'))
								{
									send(channel, line);
								}
							}
						}
						else
						{
							send(channel, "Unknown host.");
						}
					}
					catch (IOException ioe)
					{
						if (logger.isDebugEnabled())
						{
							logger.debug("Unable to perform whois IP lookup: " + query, ioe);
						}

						send(channel, "Unable to perform whois IP lookup: " + ioe.getMessage());
					}
				}
				else
				{
					send(channel, "Unknown host.");
				}
			}
		}
		else
		{
			helpResponse(sender, Commands.LOOKUP_CMD);
		}
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
	protected final void onMessage(String channel, String sender, String login, String hostname, String message)
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
			// mobibot: ping
			else if (cmd.equals(Commands.PING_CMD))
			{
				final String[] pings = {
						"is barely alive.",
						"is trying to stay awake.",
						"has gone fishing.",
						"is somewhere over the rainbow.",
						"has fallen and can't get up.",
						"is running. You better go chase it.",
						"has just spontaneously combusted.",
						"is talking to itself... don't interrupt. That's rude.",
						"is bartending at an AA meeting.",
						"is hibernating.",
						"is saving energy: apathetic mode activated.",
						"is busy. Go away!"
				};

				final Random r = new Random();

				action(channel, pings[r.nextInt(pings.length)]);
			}
			// mobibot: pong
			else if (cmd.equals(Commands.PONG_CMD))
			{
				send(channel, Commands.PING_CMD, true);
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
			// mobibot: dice
			else if (cmd.equals(Commands.DICE_CMD))
			{
				send(channel, SHALL_WE_PLAY_A_GAME);

				Dice.roll(this, sender);
			}
			// mobibot: war
			else if (cmd.equals(Commands.WAR_CMD))
			{
				send(channel, SHALL_WE_PLAY_A_GAME);

				War.play(this, sender);
			}
			// mobibot: <channel>
			else if (cmd.equalsIgnoreCase(channel.substring(1)))
			{
				feedResponse(sender);
			}
			// mobibot: currency
			else if (cmd.startsWith(Commands.CURRENCY_CMD))
			{
				currencyConverter.setQuery(sender, args);
				new Thread(currencyConverter).start();
			}
			// mobibot: lookup
			else if (cmd.startsWith(Commands.LOOKUP_CMD))
			{
				lookupResponse(sender, args);
			}
			// mobibot: view
			else if (cmd.startsWith(Commands.VIEW_CMD))
			{
				viewResponse(sender, args, false);
			}
			// mobibot: google
			else if (cmd.startsWith(Commands.GOOGLE_CMD))
			{
				googleResponse(sender, args);
			}
			// mobibot: twitter
			else if (cmd.startsWith(Commands.TWITTER_CMD) && isTwitterEnabled())
			{
				twitterResponse(sender, args);
			}
			// mobibot: stock
			else if (cmd.startsWith(Commands.STOCK_CMD))
			{
				stockResponse(sender, args);
			}
			// mobibot: joke
			else if (cmd.startsWith(Commands.JOKE_CMD))
			{
				new Thread(new Joke(this, sender)).start();
			}
			// mobibot: calc
			else if (cmd.startsWith(Commands.CALC_CMD))
			{
				calcResponse(sender, args, message);
			}
			// mobibot: time
			else if (cmd.startsWith(Commands.TIME_CMD))
			{
				worldTime.timeResponse(this, sender, args, false);
			}
			// mobibot: tell
			else if (cmd.startsWith(Commands.TELL_CMD) && isTellEnabled())
			{
				tellResponse(sender, args);
			}
			// mobibot: weather
			else if (cmd.startsWith(Commands.WEATHER_CMD))
			{
				weatherResponse(sender, args, false);
			}
			// mobibot: ignore
			else if (cmd.startsWith(Commands.IGNORE_CMD))
			{
				ignoreResponse(sender, args);
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

		tellSendMessages(sender, true);
	}

	@Override
	protected final void onPrivateMessage(String sender, String login, String hostname, String message)
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
		else if ("kill".equals(cmd))
		{
			if (isOp(sender))
			{
				sendRawLine("QUIT : Poof!");
				System.exit(0);
			}
		}
		else if (cmd.equals(Commands.DIE_CMD))
		{
			if (isOp(sender))
			{
				send(channel, sender + " has just signed my death sentence.");
				saveEntries(true);
				sleep(3);
				quitServer("The Bot Is Out There!");
				System.exit(0);
			}
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
		else if (cmd.startsWith(Commands.ADDLOG_CMD) && (cmds.length > 1))
		{
			if (isOp(sender))
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
		}
		else if (cmd.startsWith(Commands.ME_CMD))
		{
			if (isOp(sender))
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
		}
		else if (cmd.startsWith(Commands.NICK_CMD) && (cmds.length > 1))
		{
			if (isOp(sender))
			{
				changeNick(args);
			}
		}
		else if (cmd.startsWith(Commands.SAY_CMD))
		{
			if (isOp(sender))
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
		}
		else if (cmd.startsWith(Commands.MSG_CMD))
		{
			if (isOp(sender))
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
		}
		else if (cmd.startsWith(Commands.VIEW_CMD))
		{
			viewResponse(sender, args, true);
		}
		else if (cmd.startsWith(Commands.TIME_CMD))
		{
			worldTime.timeResponse(this, sender, args, true);
		}
		else if (cmd.startsWith(Commands.TELL_CMD) && isTellEnabled())
		{
			tellResponse(sender, args);
		}
		else if (cmd.startsWith(Commands.WEATHER_CMD))
		{
			weatherResponse(sender, args, true);
		}
		else if (cmd.equals(Commands.INFO_CMD))
		{
			infoResponse(sender, true);
		}
		else if (cmd.equals(Commands.VERSION_CMD))
		{
			versionResponse(sender, true);
		}
		else if (cmd.equals(Commands.DEBUG_CMD))
		{
			if (isOp(sender))
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
		}
		else
		{
			helpResponse(sender, "");
		}
	}

	@Override
	protected final void onAction(String sender, String login, String hostname, String target, String action)
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
	private void recap(String sender, String message, boolean isAction)
	{
		recap.add(Utils.UTC_SDF.format(Calendar.getInstance().getTime()) + " -> " + sender + (isAction ? " " : ": ")
		          + message);

		if (recap.size() > MAX_RECAP)
		{
			recap.remove(0);
		}
	}

	@Override
	protected void onJoin(String channel, String sender, String login, String hostname)
	{
		tellSendMessages(sender);
	}

	@Override
	protected void onNickChange(String oldNick, String login, String hostname, String newNick)
	{
		tellSendMessages(newNick);
	}

	/**
	 * Checks and sends {@link Commands#TELL_CMD} messages.
	 *
	 * @param nickname The user's nickname.
	 */
	private void tellSendMessages(String nickname)
	{
		tellSendMessages(nickname, false);
	}

	/**
	 * Checks and sends {@link Commands#TELL_CMD} messages.
	 *
	 * @param nickname The user's nickname.
	 * @param isMessage The message flag.
	 */
	private void tellSendMessages(String nickname, boolean isMessage)
	{
		if (!nickname.equals(getNick()) && isTellEnabled())
		{
			for (final TellMessage message : tellMessages)
			{
				if (message.isMatch(nickname))
				{
					if (message.getRecipient().equalsIgnoreCase(nickname) && !message.isReceived())
					{
						if (message.getSender().equals(nickname))
						{
							if (!isMessage)
							{
								send(nickname,
								     Utils.bold("You") + " wanted me to remind you: " + Colors.REVERSE + message
										     .getMessage() + Colors.REVERSE, true);

								message.setIsReceived();
								message.setIsNotified();

								saveTellMessages();
							}
						}
						else
						{
							send(nickname,
							     message.getSender() + " wanted me to tell you: " + Colors.REVERSE + message
									     .getMessage() + Colors.REVERSE,
							     true);

							message.setIsReceived();

							saveTellMessages();
						}
					}
					else if (message.getSender().equalsIgnoreCase(nickname) && message.isReceived() && !message
							.isNotified())
					{
						send(nickname,
						     "Your message " + Colors.REVERSE + "[ID " + message.getId() + ']' + Colors.REVERSE
						     + " was sent to " + Utils.bold(message.getRecipient()) + " on " + Utils.UTC_SDF
								     .format(message.getReceived()),
						     true);

						message.setIsNotified();

						saveTellMessages();
					}
				}
			}
		}
	}

	/**
	 * Returns <code>true</code> if {@link Commands#TELL_CMD} is enabled.
	 *
	 * @return <code>true</code> or <code>false</code>
	 */

	private boolean isTellEnabled()
	{
		return tellMaxSize > 0 && tellMaxDays > 0;
	}

	/**
	 * Sends a private message or notice.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param message The actual message.
	 * @param isPrivate Set to true if the response should be a private message, otherwise a notice is sent.
	 */
	public final void send(String sender, String message, boolean isPrivate)
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
	 * Responds with the last 10 public messages.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void recapResponse(String sender, boolean isPrivate)
	{
		for (final String recap : this.recap)
		{
			send(sender, recap, isPrivate);
		}
	}

	/**
	 * Sends a private notice.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param message The actual message.
	 */
	public final void send(String sender, String message)
	{
		send(sender, message, false);
	}

	/**
	 * Responds with the specified stock quote.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param symbol The stock symbol to lookup.
	 */
	private void stockResponse(String sender, String symbol)
	{
		if (symbol.length() > 0)
		{
			new Thread(new StockQuote(this, sender, symbol)).start();
		}
		else
		{
			helpResponse(sender, Commands.STOCK_CMD);
		}
	}

	/**
	 * Processes the {@link Commands#TELL_CMD} commands.
	 *
	 * @param sender The sender's nick.
	 * @param cmds The commands string.
	 */
	private void tellResponse(String sender, String cmds)
	{
		if (!Utils.isValidString(cmds))
		{
			helpResponse(sender, Commands.TELL_CMD);
		}
		else if (cmds.startsWith(Commands.VIEW_CMD))
		{
			if (isOp(sender) && cmds.equals(Commands.VIEW_CMD + ' ' + Commands.TELL_ALL_CMD))
			{
				if (tellMessages.size() > 0)
				{
					for (final TellMessage message : tellMessages)
					{
						send(sender,
						     Utils.bold(message.getSender()) + " --> " + Utils.bold(message.getRecipient()) + " [ID: "
						     + message.getId() + ", " + (message.isReceived() ? "DELIVERED" : "QUEUED") + ']',
						     true);
					}
				}
				else
				{
					send(sender, "There are no messages in the queue.", true);
				}
			}
			else
			{
				boolean hasMessage = false;

				for (final TellMessage message : tellMessages)
				{
					if (message.isMatch(sender))
					{
						if (!hasMessage)
						{
							hasMessage = true;
							send(sender, "Here are your messages: ", true);
						}

						if (message.isReceived())
						{
							send(sender,
							     Utils.bold(message.getSender()) + " --> " + Utils.bold(message.getRecipient()) + " ["
							     + Utils.UTC_SDF.format(message.getReceived()) + ", ID: " + message.getId()
							     + ", DELIVERED]",
							     true);

						}
						else
						{
							send(sender,
							     Utils.bold(message.getSender()) + " --> " + Utils.bold(message.getRecipient()) + " ["
							     + Utils.UTC_SDF.format(message.getQueued()) + ", ID: " + message.getId() + ", QUEUED]",
							     true);
						}

						send(sender, helpIndent(message.getMessage(), false), true);
					}
				}

				if (!hasMessage)
				{
					send(sender, "You have no messages in the queue.", true);
				}
				else
				{
					send(sender, "To delete one or all delivered messages:");
					send(sender,
					     helpIndent(getNick() + ": " + Commands.TELL_CMD + ' ' + Commands.TELL_DEL_CMD + " <id|"
					                + Commands.TELL_ALL_CMD + '>'));
					send(sender, "Messages are kept for " + Utils.bold(tellMaxDays) + " days.");
				}
			}
		}
		else if (cmds.startsWith(Commands.TELL_DEL_CMD + ' '))
		{
			final String[] split = cmds.split(" ");

			if (split.length == 2)
			{
				final String id = split[1];
				boolean deleted = false;

				if (id.equalsIgnoreCase(Commands.TELL_ALL_CMD))
				{
					for (final TellMessage message : tellMessages)
					{
						if (message.getSender().equalsIgnoreCase(sender) && message.isReceived())
						{
							tellMessages.remove(message);
							deleted = true;
						}
					}

					if (deleted)
					{
						saveTellMessages();
						send(sender, "Delivered messages have been deleted.", true);
					}
					else
					{
						send(sender, "No delivered messages were found.", true);
					}

				}
				else
				{
					boolean found = false;

					for (final TellMessage message : tellMessages)
					{
						found = message.isMatchId(id);

						if (found && (message.getSender().equalsIgnoreCase(sender) || isOp(sender)))
						{
							tellMessages.remove(message);

							saveTellMessages();
							send(sender, "Your message was deleted from the queue.", true);
							deleted = true;
							break;
						}
					}

					if (!deleted)
					{
						if (found)
						{
							send(sender, "Only messages that you sent can be deleted.", true);
						}
						else
						{
							send(sender, "The specified message [ID " + id + "] could not be found.", true);
						}
					}
				}
			}
			else
			{
				helpResponse(sender, Commands.TELL_CMD);
			}
		}
		else
		{
			final String[] split = cmds.split(" ", 2);

			if (split.length == 2 && (Utils.isValidString(split[1]) && split[1].contains(" ")))
			{
				if (tellMessages.size() < tellMaxSize)
				{
					final TellMessage message = new TellMessage(sender, split[0], split[1].trim());

					tellMessages.add(message);

					saveTellMessages();

					send(sender,
					     "Message [ID " + message.getId() + "] was queued for " + Utils.bold(message.getRecipient()),
					     true);
				}
				else
				{
					send(sender, "Sorry, the messages queue is currently full.", true);
				}
			}
			else
			{
				helpResponse(sender, Commands.TELL_CMD);
			}
		}

		if (cleanTellMessages())
		{
			saveTellMessages();
		}
	}

	/**
	 * Posts a message to Twitter.
	 *
	 * @param sender The sender's nick.
	 * @param message The message.
	 */
	private void twitterResponse(String sender, String message)
	{
		if (isTwitterEnabled())
		{
			if (message.length() > 0)
			{
				new Thread(new Twitter(this,
				                       sender,
				                       twitterConsumerKey,
				                       twitterConsumerSecret,
				                       twitterToken,
				                       twitterTokenSecret,
				                       message)).start();
			}
			else
			{
				helpResponse(sender, Commands.TWITTER_CMD);
			}
		}
		else
		{
			send(sender, "The Twitter posting facility is disabled.");
		}
	}

	/**
	 * Responds with the users on a channel.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void usersResponse(String sender, boolean isPrivate)
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
	private void versionResponse(String sender, boolean isPrivate)
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
	private void viewResponse(String sender, String args, boolean isPrivate)
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

	/**
	 * Responds with weather from the specified station ID.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param id The station's ID.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void weatherResponse(String sender, String id, boolean isPrivate)
	{
		new Thread(new Weather(this, sender, id, isPrivate)).start();
	}
}
