/*
 * @(#)Mobibot.java
 *
 * Copyright (c) 2004, Erik C. Thauvin (erik@thauvin.net)
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
 *
 * $Id$
 *
 */
package net.thauvin.erik.mobibot;

import com.primalworld.math.MathEvaluator;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import org.apache.commons.cli.*;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.commons.net.WhoisClient;
import org.apache.log4j.Level;
import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implements the #mobitopia bot.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
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
	 * The HH:MM timestamp simple date format.
	 */
	private static final SimpleDateFormat HHMM_SDF = new SimpleDateFormat("HH:mm");

	/**
	 * The ISO (YYYY-MM-DD) simple date format.
	 */
	private static final SimpleDateFormat ISO_SDF = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * The info strings.
	 */
	private static final String[] INFO_STRS =
			{"Mobibot v" + ReleaseInfo.getVersion() + '.' + ReleaseInfo.getBuildNumber()
			 + " by Erik C. Thauvin (erik@thauvin.net)", "http://www.mobitopia.org/mobibot/"};

	/**
	 * The version strings.
	 */
	private static final String[] VERSION_STRS =
			{"Version: " + ReleaseInfo.getVersion() + '.' + ReleaseInfo.getBuildNumber() + " ("
			 + ISO_SDF.format(ReleaseInfo.getBuildDate()) + ')',
			 "Platform: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", "
			 + System.getProperty("os.arch") + ", " + System.getProperty("user.country") + ')',
			 "Runtime: " + System.getProperty("java.runtime.name") + " (build "
			 + System.getProperty("java.runtime.version") + ')',
			 "VM: " + System.getProperty("java.vm.name") + " (build " + System.getProperty("java.vm.version") + ", "
			 + System.getProperty("java.vm.info") + ')'};

	/**
	 * Debug command line argument.
	 */
	private static final String DEBUG_ARG = "debug";

	/**
	 * Help command line argument.
	 */
	private static final String HELP_ARG = "help";

	/**
	 * Properties command line argument.
	 */
	private static final String PROPS_ARG = "properties";

	/**
	 * The maximum number of times the bot will try to reconnect, if disconnected.
	 */
	private static final int MAX_RECONNECT = 10;

	/**
	 * The default maximum number of entries to display.
	 */
	private static final int MAX_ENTRIES = 8;

	/**
	 * The default maximum recap entries.
	 */
	private static final int MAX_RECAP = 10;

	/**
	 * The maximum number of backlogs to keep.
	 */
	private static final int MAX_BACKLOGS = 10;

	/**
	 * The double tab indent (8 spaces).
	 */
	private static final String DOUBLE_INDENT = "        ";

	/**
	 * The add (back)log command.
	 */
	private static final String ADDLOG_CMD = "addlog";

	/**
	 * The debug command.
	 */
	private static final String DEBUG_CMD = "debug";

	/**
	 * The dices command.
	 */
	private static final String DICE_CMD = "dice";

	/**
	 * The say command.
	 */
	private static final String SAY_CMD = "say";

	/**
	 * The die command.
	 */
	private static final String DIE_CMD = "die";

	/**
	 * The cycle command.
	 */
	private static final String CYCLE_CMD = "cycle";

	/**
	 * The msg command.
	 */
	private static final String MSG_CMD = "msg";

	/**
	 * The ignore command.
	 */
	private static final String IGNORE_CMD = "ignore";

	/**
	 * The ignore <code>me</code> keyword.
	 */
	private static final String IGNORE_ME_KEYWORD = "me";

	/**
	 * The help command.
	 */
	private static final String HELP_CMD = "help";

	/**
	 * The help on posting keyword.
	 */
	private static final String HELP_POSTING_KEYWORD = "posting";

	/**
	 * The help on tags keyword.
	 */
	private static final String HELP_TAGS_KEYWORD = "tags";

	/**
	 * The Google command.
	 */
	private static final String GOOGLE_CMD = "google";

	/**
	 * The Jaiku command.
	 */
	private static final String JAIKU_CMD = "jaiku";

	/**
	 * The Twitter command.
	 */
	private static final String TWITTER_CMD = "twitter";
	/**
	 * The math command.
	 */
	private static final String CALC_CMD = "calc";

	/**
	 * The me command.
	 */
	private static final String ME_CMD = "me";

	/**
	 * The nick command.
	 */
	private static final String NICK_CMD = "nick";

	/**
	 * The link command.
	 */
	private static final String LINK_CMD = "L";

	/**
	 * The link match string.
	 */
	private static final String LINK_MATCH = "^[hH][tT][tT][pP](|[sS])://.*";

	/**
	 * The lookup command.
	 */
	private static final String LOOKUP_CMD = "lookup";

	/**
	 * The ping command.
	 */
	private static final String PING_CMD = "ping";

	/**
	 * The pong command.
	 */
	private static final String PONG_CMD = "pong";

	/**
	 * The recap command.
	 */
	private static final String RECAP_CMD = "recap";

	/**
	 * The spell command.
	 */
	private static final String SPELL_CMD = "spell";

	/**
	 * The stock command.
	 */
	private static final String STOCK_CMD = "stock";

	/**
	 * The time command.
	 */
	private static final String TIME_CMD = "time";

	/**
	 * The empty title string.
	 */
	private static final String NO_TITLE = "No Title";

	/**
	 * The tags/categories marker.
	 */
	private static final String TAGS_MARKER = "tags:";

	/**
	 * The countries supporte by the {@link #TIME_CMD time} command.
	 */
	private static final Map COUNTRIES_MAP = new TreeMap();

	/**
	 * The date/time format for the {@link #TIME_CMD time} command.
	 */
	private static final SimpleDateFormat TIME_SDF =
			new SimpleDateFormat("'The time is 'HH:mm' on 'EEE, d MMM yyyy' in '");

	/**
	 * The beats (Internet Time) keyword.
	 */
	private static final String BEATS_KEYWORD = ".beats";

	/**
	 * The currency command.
	 */
	public static final String CURRENCY_CMD = "currency";

	/**
	 * The users command.
	 */
	private static final String USERS_CMD = "users";

	/**
	 * The info command.
	 */
	private static final String INFO_CMD = "info";

	/**
	 * The version command.
	 */
	private static final String VERSION_CMD = "version";

	/**
	 * The view command.
	 */
	private static final String VIEW_CMD = "view";

	/**
	 * The weather command.
	 */
	public static final String WEATHER_CMD = "weather";

	/**
	 * The start time.
	 */
	private static final long START_TIME = System.currentTimeMillis();

	/**
	 * The recap array.
	 */
	private static final List RECAP_ARRAY = new ArrayList(MAX_RECAP);

	/**
	 * The default port.
	 */
	private static final int DEFAULT_PORT = 6667;

	// Initialize the countries.

	static
	{
		COUNTRIES_MAP.put("AU", "Australia/Sydney");
		COUNTRIES_MAP.put("BE", "Europe/Brussels");
		COUNTRIES_MAP.put("CA", "America/Montreal");
		COUNTRIES_MAP.put("CH", "Europe/Zurich");
		COUNTRIES_MAP.put("CN", "Asia/Shanghai");
		COUNTRIES_MAP.put("DE", "Europe/Berlin");
		COUNTRIES_MAP.put("DK", "Europe/Copenhagen");
		COUNTRIES_MAP.put("ES", "Europe/Madrid");
		COUNTRIES_MAP.put("FI", "Europe/Helsinki");
		COUNTRIES_MAP.put("FR", "Europe/Paris");
		COUNTRIES_MAP.put("GB", "Europe/London");
		COUNTRIES_MAP.put("HK", "Asia/Hong_Kong");
		COUNTRIES_MAP.put("IE", "Europe/Dublin");
		COUNTRIES_MAP.put("IL", "Israel");
		COUNTRIES_MAP.put("IN", "Asia/Calcutta");
		COUNTRIES_MAP.put("IS", "Iceland");
		COUNTRIES_MAP.put("IT", "Europe/Rome");
		COUNTRIES_MAP.put("JP", "Asia/Tokyo");
		COUNTRIES_MAP.put("MX", "Mexico/Mexico_City");
		COUNTRIES_MAP.put("NL", "Europe/Amsterdam");
		COUNTRIES_MAP.put("NO", "Europe/Oslo");
		COUNTRIES_MAP.put("NZ", "Pacific/Auckland");
		COUNTRIES_MAP.put("PK", "Asia/Karachi");
		COUNTRIES_MAP.put("RU", "Europe/Moscow");
		COUNTRIES_MAP.put("SE", "Europe/Stockholm");
		COUNTRIES_MAP.put("SG", "Asia/Singapore");
		COUNTRIES_MAP.put("SU", "Europe/Moscow");
		COUNTRIES_MAP.put("TH", "Asia/Bangkok");
		COUNTRIES_MAP.put("TW", "Asia/Taipei");
		COUNTRIES_MAP.put("UK", "Europe/London");
		COUNTRIES_MAP.put("US", "America/New_York");
		COUNTRIES_MAP.put("EST", "America/New_York");
		COUNTRIES_MAP.put("CST", "America/Chicago");
		COUNTRIES_MAP.put("MST", "America/Denver");
		COUNTRIES_MAP.put("PST", "America/Los_Angeles");
		COUNTRIES_MAP.put("EDT", "America/New_York");
		COUNTRIES_MAP.put("CDT", "America/Chicago");
		COUNTRIES_MAP.put("MDT", "America/Denver");
		COUNTRIES_MAP.put("PDT", "America/Los_Angeles");
		COUNTRIES_MAP.put("CET", "CET");
		COUNTRIES_MAP.put("GMT", "GMT");
		COUNTRIES_MAP.put("HST", "HST");
		COUNTRIES_MAP.put("UTC", "UTC");
		COUNTRIES_MAP.put("INTERNET", BEATS_KEYWORD);
		COUNTRIES_MAP.put("BEATS", BEATS_KEYWORD);
	}

	/**
	 * The whois host.
	 */
	private static final String WHOIS_HOST = "whois.arin.net";

	/**
	 * The number of milliseconds to delay between consecutive messages.
	 */
	private static final long MESSAGE_DELAY = 1000L;

	/**
	 * The name of the file containing the current entries.
	 */
	private static final String CURRENT_XML = "current.xml";

	/**
	 * The name of the file containing the backlog entries.
	 */
	private static final String NAV_XML = "nav.xml";

	/**
	 * The backlogs URL.
	 */
	private String _backlogsURL = "";

	/**
	 * The main channel.
	 */
	private final String _channel;

	/**
	 * The default tags/categories.
	 */
	private String _defaultTags = "";

	/**
	 * The del.icio.us posts handler.
	 */
	private DeliciousPoster _delicious = null;

	/**
	 * The entries array.
	 */
	private final List _entries = new ArrayList(0);

	/**
	 * The feed info cache.
	 */
	private final FeedFetcherCache _feedInfoCache = HashMapFeedInfoCache.getInstance();

	/**
	 * The feed URL.
	 */
	private String _feedURL = "";

	/**
	 * The Google API key.
	 */

	private String _googleKey = "";

	/**
	 * The Jaiku API key.
	 */
	private String _jaikuKey = "";

	/**
	 * The Jaiku user.
	 */
	private String _jaikuUser = "";

	/**
	 * The Twitter consumer key.
	 */
	private String _twitterConsumerKey = "";

	/**
	 * The Twitter consumer secret.
	 */
	private String _twitterConsumerSecret = "";

	/**
	 * The Twitter token.
	 */
	private String _twitterToken = "";

	/**
	 * The Twitter token secret.
	 */
	private String _twitterTokenSecret = "";

	/**
	 * The history/backlogs array.
	 */
	private final List _history = new ArrayList(0);

	/**
	 * The ident message.
	 */
	private String _identMsg = "";

	/**
	 * The ident nick.
	 */
	private String _identNick = "";

	/**
	 * The NickServ ident password.
	 */
	private String _ident = "";

	/**
	 * The ignored nicks array.
	 */
	private final List _ignoredNicks = new ArrayList(0);

	/**
	 * The IRC port.
	 */
	private final int _ircPort;

	/**
	 * The IRC server.
	 */
	private final String _ircServer;

	/**
	 * The logger.
	 */
	private final Log4JLogger _logger;

	/**
	 * The logger default level.
	 */
	private final Level _loggerLevel;

	/**
	 * The log directory.
	 */
	private final String _logsDir;

	/**
	 * Today's date.
	 */
	private String _today = today();

	/**
	 * The weblog URL.
	 */
	private String _weblogURL = "";

	/**
	 * Creates a new Mobibot object.
	 *
	 * @param server The server.
	 * @param port The port.
	 * @param channel The channel.
	 * @param logsDir The logs directory.
	 */
	public Mobibot(String server, int port, String channel, String logsDir)
	{
		System.getProperties().setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(CONNECT_TIMEOUT));
		System.getProperties().setProperty("sun.net.client.defaultReadTimeout", String.valueOf(CONNECT_TIMEOUT));

		_ircServer = server;
		_ircPort = port;
		_channel = channel;
		_logsDir = logsDir;

		// Set the logger
		_logger = new Log4JLogger(Mobibot.class.getPackage().getName());
		_loggerLevel = _logger.getLogger().getLevel();

		// Load the current entries, if any.
		try
		{
			loadEntries(_logsDir + CURRENT_XML);

			if (!today().equals(_today))
			{
				_entries.clear();
				_today = today();
			}
		}
		catch (FileNotFoundException ignore)
		{
			; // Do nothing.
		}
		catch (FeedException e)
		{
			_logger.error("An error occurred while parsing the '" + CURRENT_XML + "' file.", e);
		}

		// Load the backlogs, if any.
		try
		{
			loadBacklogs(_logsDir + NAV_XML);
		}
		catch (FileNotFoundException ignore)
		{
			; // Do nothing.
		}
		catch (FeedException e)
		{
			_logger.error("An error occurred while parsing the '" + NAV_XML + "' file.", e);
		}
	}

	/**
	 * Returns true if the given string is valid.
	 *
	 * @param s The string to validate.
	 *
	 * @return true if the string is non-empty and not null, false otherwise.
	 */
	public static boolean isValidString(String s)
	{
		return (s != null) && (s.trim().length() > 0);
	}

	/**
	 * The Truth Is Out There...
	 *
	 * @param args The command line arguments.
	 *
	 * @noinspection UseOfSystemOutOrSystemErr,ACCESS_STATIC_VIA_INSTANCE
	 */
	public static void main(String[] args)
	{
		// Setup the command line options
		final Options options = new Options();
		options.addOption(HELP_ARG.substring(0, 1), HELP_ARG, false, "print this help message");
		options.addOption(DEBUG_ARG.substring(0, 1),
		                  DEBUG_ARG,
		                  false,
		                  "print debug & logging data directly to the console");
		options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("use alternate properties file")
				.withLongOpt(PROPS_ARG).create(PROPS_ARG.substring(0, 1)));

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

		if (line.hasOption(HELP_ARG.charAt(0)))
		{
			// Output the usage
			new HelpFormatter().printHelp(Mobibot.class.getName(), options);
		}
		else
		{
			FileInputStream fis = null;
			final Properties p = new Properties();

			try
			{
				fis = new FileInputStream(new File(line.getOptionValue(PROPS_ARG.charAt(0), "./mobibot.properties")));

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
			final int port = getPort(p.getProperty("port", String.valueOf(DEFAULT_PORT)), DEFAULT_PORT);
			final String nickname = p.getProperty("nick", Mobibot.class.getName().toLowerCase());
			final String logsDir = ensureDir(p.getProperty("logs", "."), false);

			if (!line.hasOption(DEBUG_ARG.charAt(0)))
			{
				// Redirect the stdout and stderr
				PrintStream stdout = null;

				try
				{
					stdout = new PrintStream(new FileOutputStream(
							logsDir + channel.substring(1) + '.' + today() + ".log", true));
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
			final String backlogsURL = ensureDir(p.getProperty("backlogs", weblogURL), true);
			final String googleKey = p.getProperty("google", "");
			final String ignoredNicks = p.getProperty("ignore", "");
			final String identNick = p.getProperty("ident-nick", "");
			final String identMsg = p.getProperty("ident-msg", "");
			final String ident = p.getProperty("ident", "");
			final String tags = p.getProperty("tags", "");

			// Get the del.icio.us properties
			final String dname = p.getProperty("delicious-user");
			final String dpwd = p.getProperty("delicious-pwd");

			// Get the Jaiku properties
			final String jname = p.getProperty("jaiku-user");
			final String jkey = p.getProperty("jaiku-key");

			// Get the Twitter properties
			final String tconsumerKey = p.getProperty("twitter-consumerKey");
			final String tconsumerSecret = p.getProperty("twitter-consumerSecret");
			final String ttoken = p.getProperty("twitter-token", "");
			final String ttokenSecret = p.getProperty("twitter-tokenSecret", "");

			// Create the bot
			final Mobibot bot = new Mobibot(server, port, channel, logsDir);

			// Initialize the bot
			bot.setVerbose(true);
			bot.setAutoNickChange(true);
			bot.setName(nickname);
			bot.setLogin(login);
			bot.setVersion(weblogURL);
			bot.setMessageDelay(MESSAGE_DELAY);

			// Set the ident password
			bot.setIdent(ident);

			// Set the ident nick and message
			bot.setIdentNick(identNick);
			bot.setIdentMsg(identMsg);

			// Set the URLs
			bot.setWeblogURL(weblogURL);
			bot.setFeedURL(feedURL);
			bot.setBacklogsURL(backlogsURL);

			// Set the Google key
			bot.setGoogleKey(googleKey);

			if (isValidString(dname) && isValidString(dpwd))
			{
				// Set the del.icio.us authentication
				bot.setDeliciousAuth(dname, dpwd);
			}

			if (isValidString(jname) && isValidString(jkey))
			{
				// Set the Jaiku authentication
				bot.setJaikuAuth(jname, jkey);
			}

			if (isValidString(tconsumerKey) && isValidString(tconsumerSecret) && isValidString(ttoken) && isValidString(
					ttokenSecret))
			{
				// Set the Twitter authentication
				bot.setTwitterAuth(tconsumerKey, tconsumerSecret, ttoken, ttokenSecret);
			}

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
			if (isValidString(ident))
			{
				bot.identify(ident);
			}

			// Identify with a specified nick
			if (isValidString(identNick) && isValidString(identMsg))
			{
				bot.sendMessage(identNick, identMsg);
			}

			bot.joinChannel(channel);
		}
	}

	/**
	 * Converts XML/XHTML entities to plain text.
	 *
	 * @param str The string to unescape.
	 *
	 * @return The unescaped string.
	 */
	public static String unescapeXml(String str)
	{
		String s = str.replaceAll("&amp;", "&");
		s = s.replaceAll("&lt;", "<");
		s = s.replaceAll("&gt;", ">");
		s = s.replaceAll("&quot;", "\"");
		s = s.replaceAll("&apos;", "'");
		s = s.replaceAll("&#39;", "'");

		return s;
	}

	/**
	 * Sends an action to the current channel.
	 *
	 * @param action The action.
	 */
	public final void action(String action)
	{
		action(getChannel(), action);
	}

	/**
	 * Sends an action to the channel.
	 *
	 * @param channel The channel.
	 * @param action The action.
	 */
	public final void action(String channel, String action)
	{
		if (isValidString(channel) && isValidString(action))
		{
			sendAction(channel, action);
		}
	}

	/**
	 * Returns the current channel.
	 *
	 * @return The current channel.
	 */
	public final String getChannel()
	{
		return _channel;
	}

	/**
	 * Returns the {@link FeedFetcherCache feed info cache}.
	 *
	 * @return The feed info cache.
	 */
	public final synchronized FeedFetcherCache getFeedInfoCache()
	{
		return _feedInfoCache;
	}

	/**
	 * Returns the bot's logger.
	 *
	 * @return The bot's logger.
	 */
	public final Log4JLogger getLogger()
	{
		return _logger;
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

		if (lcTopic.endsWith(HELP_POSTING_KEYWORD))
		{
			send(sender, bold("Post a URL, by saying it on a line on its own:"));
			send(sender, DOUBLE_INDENT + bold("<url> [<title>] [" + TAGS_MARKER + "<+tag> [...]]"));
			send(sender, "I will reply with a label, for example: " + bold(LINK_CMD + '1'));
			send(sender, "To add a title, use a its label and a pipe:");
			send(sender, DOUBLE_INDENT + bold(LINK_CMD + "1:|This is the title"));
			send(sender, "To add a comment: ");
			send(sender, DOUBLE_INDENT + bold(LINK_CMD + "1:This is a comment"));
			send(sender, "I will reply with a label, for example: " + bold(LINK_CMD + "1.1"));
			send(sender, "To edit a comment, use its label: ");
			send(sender, DOUBLE_INDENT + bold(LINK_CMD + "1.1:This is an edited comment"));
			send(sender, "To delete a comment, use its label and a minus sign: ");
			send(sender, DOUBLE_INDENT + bold(LINK_CMD + "1.1:-"));
			send(sender, "You can also view a posting by saying its label.");
		}
		else if (lcTopic.endsWith(HELP_TAGS_KEYWORD))
		{
			send(sender, bold("To categorize or tag a URL, use its label and a T:"));
			send(sender, DOUBLE_INDENT + bold(LINK_CMD + "1T:<+tag|-tag> [...]"));
		}
		else if (lcTopic.endsWith(VIEW_CMD))
		{
			send(sender, "To list or search the current URL posts:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + VIEW_CMD) + " [<start>] [<query>]");
		}
		else if (lcTopic.endsWith(getChannel().substring(1).toLowerCase()))
		{
			send(sender, "To list the last 5 posts from the channel's weblog:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + getChannel().substring(1)));
		}
		else if (lcTopic.endsWith(GOOGLE_CMD) && isGoogleEnabled())
		{
			send(sender, "To search Google:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + GOOGLE_CMD + " <query>"));
		}
		else if (lcTopic.endsWith(JAIKU_CMD) && isJaikuEnabled())
		{
			send(sender, "To post to Jaiku:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + JAIKU_CMD + " <message>"));
		}
		else if (lcTopic.endsWith(TWITTER_CMD) && isTwitterEnabled())
		{
			send(sender, "To post to Twitter:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + TWITTER_CMD + " <message>"));
		}
		else if (lcTopic.endsWith(RECAP_CMD))
		{
			send(sender, "To list the last 10 public channel messages:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + RECAP_CMD));
		}
		else if (lcTopic.endsWith(CALC_CMD))
		{
			send(sender, "To solve a mathematical calculation:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + CALC_CMD + " <calculation>"));
		}
		else if (lcTopic.endsWith(LOOKUP_CMD))
		{
			send(sender, "To perform a DNS lookup query:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + LOOKUP_CMD + " <ip address or hostname>"));
		}
		else if (lcTopic.endsWith(TIME_CMD))
		{
			send(sender, "To display a country's current date/time:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + TIME_CMD) + " [<country code>]");

			send(sender, "For a listing of the supported countries:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + TIME_CMD));
		}
		else if (lcTopic.endsWith(SPELL_CMD) && isGoogleEnabled())
		{
			send(sender, "To have Google try to correctly spell a sentence:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + SPELL_CMD + " <sentence>"));
		}
		else if (lcTopic.endsWith(STOCK_CMD))
		{
			send(sender, "To retrieve a stock quote:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + STOCK_CMD + " <symbol[.country code]>"));
		}
		else if (lcTopic.endsWith(DICE_CMD))
		{
			send(sender, "To roll the dice:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + DICE_CMD));
		}
		else if (lcTopic.endsWith(WEATHER_CMD))
		{
			send(sender, "To display weather information:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + WEATHER_CMD + " <station id>"));
			send(sender, "For a listing of the ICAO station IDs, please visit: " + Weather.STATIONS_URL);
		}
		else if (lcTopic.endsWith(USERS_CMD))
		{
			send(sender, "To list the users present on the channel:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + USERS_CMD));
		}
		else if (lcTopic.endsWith(INFO_CMD))
		{
			send(sender, "To view information about the bot:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + INFO_CMD));
		}
		else if (lcTopic.endsWith(CYCLE_CMD))
		{
			if (isOp(sender))
			{
				send(sender, "To have the bot leave the channel and come back:");
				send(sender, DOUBLE_INDENT + bold("/msg " + getNick() + ' ' + CYCLE_CMD));
			}
		}
		else if (lcTopic.endsWith(ME_CMD))
		{
			if (isOp(sender))
			{
				send(sender, "To have the bot perform an action:");
				send(sender, DOUBLE_INDENT + bold("/msg " + getNick() + ' ' + ME_CMD + " <action>"));
			}
		}
		else if (lcTopic.endsWith(SAY_CMD))
		{
			if (isOp(sender))
			{
				send(sender, "To have the bot say something on the channel:");
				send(sender, DOUBLE_INDENT + bold("/msg " + getNick() + ' ' + SAY_CMD + " <text>"));
			}
		}
		else if (lcTopic.endsWith(VERSION_CMD))
		{
			if (isOp(sender))
			{
				send(sender, "To view the version data (bot, java, etc.):");
				send(sender, DOUBLE_INDENT + bold("/msg " + getNick() + ' ' + VERSION_CMD));
			}
		}
		else if (lcTopic.endsWith(MSG_CMD))
		{
			if (isOp(sender))
			{
				send(sender, "To have the bot send a private message to someone:");
				send(sender, DOUBLE_INDENT + bold("/msg " + getNick() + ' ' + MSG_CMD + " <nick> <text>"));
			}
		}
		else if (lcTopic.startsWith(CURRENCY_CMD))
		{
			send(sender, "To convert from one currency to another:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + CURRENCY_CMD + " [100 USD to EUR]"));

			if (lcTopic.endsWith(CURRENCY_CMD))
			{
				send(sender, "For a listing of supported currencies:");
				send(sender, DOUBLE_INDENT + bold(getNick() + ": " + CURRENCY_CMD));
			}
		}
		else if (lcTopic.startsWith(IGNORE_CMD))
		{
			send(sender, "To check your ignore status:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + IGNORE_CMD));

			send(sender, "To toggle your ignore status:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + IGNORE_CMD + ' ' + IGNORE_ME_KEYWORD));
		}
		else
		{
			send(sender, bold("Type a URL on " + getChannel() + " to post it."));
			send(sender, "For more information on specific command, type:");
			send(sender, DOUBLE_INDENT + bold(getNick() + ": " + HELP_CMD + " <command>"));
			send(sender, "The commands are:");

			final String[] cmds = {CALC_CMD,
			                       CURRENCY_CMD,
			                       DICE_CMD,
			                       GOOGLE_CMD,
			                       IGNORE_CMD,
			                       INFO_CMD,
			                       JAIKU_CMD,
			                       LOOKUP_CMD,
			                       getChannel().substring(1),
			                       HELP_POSTING_KEYWORD,
			                       RECAP_CMD,
			                       SPELL_CMD,
			                       STOCK_CMD,
			                       HELP_TAGS_KEYWORD,
			                       TIME_CMD,
			                       TWITTER_CMD,
			                       USERS_CMD,
			                       VIEW_CMD,
			                       WEATHER_CMD};

			Arrays.sort(cmds);

			final StringBuffer sb = new StringBuffer(0);
			boolean isValidCmd = true;

			for (int i = 0, cmdCount = 1; i < cmds.length; i++, cmdCount++)
			{
				if (cmds[i].equals(GOOGLE_CMD) || cmds[i].equals(SPELL_CMD))
				{
					isValidCmd = isGoogleEnabled();
				}

				if (cmds[i].equals(JAIKU_CMD))
				{
					isValidCmd = isJaikuEnabled();
				}

				if (cmds[i].equals(TWITTER_CMD))
				{
					isValidCmd = isTwitterEnabled();
				}

				if (isValidCmd)
				{
					if (sb.length() > 0)
					{
						sb.append("  ");
					}

					sb.append(cmds[i]);
				}
				else
				{
					cmdCount--;
				}

				// 5 commands per line or last command
				if (sb.length() > 0 && (cmdCount == 5 || i == (cmds.length - 1)))
				{
					send(sender, DOUBLE_INDENT + bold(sb.toString()));

					sb.setLength(0);
					cmdCount = 0;
				}

				isValidCmd = true;
			}

			if (isOp(sender))
			{
				send(sender, "The op commands are:");
				send(sender,
				     DOUBLE_INDENT + bold(
						     CYCLE_CMD + "  " + ME_CMD + "  " + MSG_CMD + "  " + SAY_CMD + "  " + VERSION_CMD));
			}
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
	 * Sends a private message or notice.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param message The actual message.
	 * @param isPrivate Set to true if the response should be a private message, otherwise a notice is sent.
	 */
	public final void send(String sender, String message, boolean isPrivate)
	{
		if (isValidString(message) && isValidString(sender))
		{
			if (isPrivate)
			{
				if (_logger.isDebugEnabled())
				{
					_logger.debug("Sending message to " + sender + ": " + message);
				}

				sendMessage(sender, message);
			}
			else
			{
				if (_logger.isDebugEnabled())
				{
					_logger.debug("Sending notice to " + sender + ": " + message);
				}

				sendNotice(sender, message);
			}
		}
	}

	/**
	 * This method is called whenever an ACTION is sent from a user.
	 *
	 * @param sender The nick of the person who sent the action.
	 * @param login The login of the person who sent the action.
	 * @param hostname The hostname of the person who sent the action.
	 * @param target The target of the action, be it a channel or our nick.
	 * @param action The action carried out by the user.
	 */
	protected final void onAction(String sender, String login, String hostname, String target, String action)
	{
		if (target.equals(getChannel()))
		{
			recap(sender, action, true);
		}
	}

	/**
	 * This method carries out the actions to be performed when the PircBot gets disconnected.
	 *
	 * @noinspection UseOfSystemOutOrSystemErr
	 */
	protected final void onDisconnect()
	{
		if (isValidString(_weblogURL))
		{
			setVersion(_weblogURL);
		}

		sleep(5);

		// Connect
		try
		{
			connect(_ircServer, _ircPort);
		}
		catch (Exception e)
		{
			int retries = 0;

			while ((retries++ < MAX_RECONNECT) && !isConnected())
			{
				sleep(10);

				try
				{
					connect(_ircServer, _ircPort);
				}
				catch (Exception ex)
				{
					if (retries == MAX_RECONNECT)
					{
						if (_logger.isDebugEnabled())
						{
							_logger.debug(
									"Unable to reconnect to " + _ircServer + " after " + MAX_RECONNECT + " retries.",
									ex);
						}

						e.printStackTrace(System.err);
						System.exit(1);
					}
				}
			}
		}

		setVersion(INFO_STRS[0]);

		if (isValidString(_ident))
		{
			identify(_ident);
		}

		if (isValidString(_identNick) && isValidString(_identMsg))
		{
			sendMessage(_identNick, _identMsg);
		}

		joinChannel(getChannel());
	}

	/**
	 * This method is called whenever a message is sent to a channel.
	 *
	 * @param channel The channel to which the message was sent.
	 * @param sender The nick of the person who sent the message.
	 * @param login The login of the person who sent the message.
	 * @param hostname The hostname of the person who sent the message.
	 * @param message The actual message sent.
	 */
	protected final void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		if (_logger.isDebugEnabled())
		{
			_logger.debug(">>> " + sender + ": " + message);
		}

		boolean isCommand = false;

		if (message.matches(LINK_MATCH) && !isIgnoredNick(sender))
		{
			isCommand = true;

			final String[] cmds = message.split(" ", 2);
			final String cmd = cmds[0].trim();
			boolean isBackup = false;

			final int dupIndex = findDupEntry(cmd);

			if (dupIndex == -1)
			{
				if (!today().equals(getToday()))
				{
					isBackup = true;
					saveEntries(isBackup);

					_entries.clear();
					setToday(today());
				}

				final boolean hasTitle = (cmds.length > 1) && (cmds[1].trim().length() > 0);

				if (hasTitle)
				{
					final String title = cmds[1].trim();

					if (title.indexOf(getNick()) == -1)
					{
						final int tagSep = title.lastIndexOf(TAGS_MARKER);

						if (tagSep != -1)
						{
							_entries.add(new EntryLink(cmd,
							                           title.substring(0, tagSep),
							                           sender,
							                           login,
							                           channel,
							                           (_defaultTags + ' ' + title
									                           .substring(tagSep + TAGS_MARKER.length()))));
						}
						else
						{
							_entries.add(new EntryLink(cmd, title, sender, login, channel, _defaultTags));
						}
					}
					else
					{
						isCommand = false;
					}
				}
				else
				{
					_entries.add(new EntryLink(cmd, NO_TITLE, sender, login, channel, _defaultTags));
				}

				if (isCommand)
				{
					final int index = _entries.size() - 1;
					final EntryLink entry = (EntryLink) _entries.get(index);
					send(channel, buildLink(index, entry));

					if (_delicious != null)
					{
						_delicious.addPost(entry);
					}

					saveEntries(isBackup);

					if (!hasTitle)
					{
						send(sender, "Please specify a title, by typing:", true);
						send(sender, DOUBLE_INDENT + bold(LINK_CMD + (index + 1) + ":|This is the title"), true);
					}
				}
			}
			else
			{
				final EntryLink entry = (EntryLink) _entries.get(dupIndex);
				send(sender, "Duplicate >> " + buildLink(dupIndex, entry));
			}
		}
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

			if (cmd.startsWith(HELP_CMD))
			{
				helpResponse(sender, args);
			}
			else if (cmd.equals(PING_CMD))
			{
				final String[] pings = {"is barely alive.",
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
				                        "is busy. Go away!"};

				final Random r = new Random();

				action(channel, pings[r.nextInt(pings.length)]);
			}
			else if (cmd.equals(PONG_CMD))
			{
				send(channel, PING_CMD, true);
			}
			else if (cmd.equals(RECAP_CMD))
			{
				recapResponse(sender, false);
			}
			else if (cmd.equals(USERS_CMD))
			{
				usersResponse(sender, false);
			}
			else if (cmd.equals(INFO_CMD))
			{
				infoResponse(sender, false);
			}
			else if (cmd.equals(VERSION_CMD))
			{
				versionResponse(sender, false);
			}
			else if (cmd.equals(DICE_CMD))
			{
				final Random r = new Random();
				int i = r.nextInt(6) + 1;
				int y = r.nextInt(6) + 1;
				final int total = i + y;

				send(getChannel(), sender + " rolled two dice: " + i + " and " + y + " for a total of " + total);

				i = r.nextInt(6) + 1;
				y = r.nextInt(6) + 1;
				action("rolled two dice: " + i + " and " + y + " for a total of " + (i + y));

				if (total < (i + y))
				{
					action("wins.");
				}
				else if (total > (i + y))
				{
					action("lost.");
				}
				else
				{
					action("tied.");
				}
			}
			else if (cmd.equalsIgnoreCase(getChannel().substring(1)))
			{
				feedResponse(sender);
			}
			else if (cmd.startsWith(CURRENCY_CMD))
			{
				new Thread(new CurrencyConverter(this, sender, args, today())).start();
			}
			else if (cmd.startsWith(LOOKUP_CMD))
			{
				lookupResponse(sender, args);
			}
			else if (cmd.startsWith(VIEW_CMD))
			{
				viewResponse(sender, args, false);
			}
			else if (cmd.startsWith(GOOGLE_CMD))
			{
				googleResponse(sender, args);
			}
			else if (cmd.startsWith(JAIKU_CMD))
			{
				jaikuResponse(sender, args);
			}
			else if (cmd.startsWith(TWITTER_CMD))
			{
				twitterResponse(sender, args);
			}
			else if (cmd.startsWith(SPELL_CMD))
			{
				spellResponse(sender, args);
			}
			else if (cmd.startsWith(STOCK_CMD))
			{
				stockResponse(sender, args);
			}
			else if (cmd.startsWith(CALC_CMD))
			{
				if (cmds.length > 1)
				{
					final MathEvaluator me = new MathEvaluator(args);

					try
					{
						me.trace();
						send(getChannel(), String.valueOf(me.getValue()));
					}
					catch (Exception e)
					{
						if (_logger.isDebugEnabled())
						{
							_logger.debug("Unable to calculate: " + message, e);
						}
					}
				}
				else
				{
					helpResponse(sender, CALC_CMD);
				}
			}
			else if (cmd.startsWith(TIME_CMD))
			{
				timeResponse(sender, args, false);
			}
			else if (cmd.startsWith(WEATHER_CMD))
			{
				weatherResponse(sender, args, false);
			}
			else if (cmd.startsWith(IGNORE_CMD))
			{
				if (!isOp(sender))
				{
					final String nick = sender.toLowerCase();
					final boolean isMe = args.toLowerCase().startsWith(IGNORE_ME_KEYWORD);

					if (_ignoredNicks.contains(nick))
					{
						if (isMe)
						{
							_ignoredNicks.remove(nick);

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
							_ignoredNicks.add(nick);

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

						String nick;

						for (int i = 0; i < nicks.length; i++)
						{
							nick = nicks[i];

							if (IGNORE_ME_KEYWORD.equals(nick))
							{
								nick = sender.toLowerCase();
							}

							if (_ignoredNicks.contains(nick))
							{
								_ignoredNicks.remove(nick);
							}
							else
							{
								_ignoredNicks.add(nick);
							}
						}
					}

					send(sender, "The following nicks are ignored: " + _ignoredNicks.toString());
				}
			}
		}
		else if (message.matches(LINK_CMD + "[0-9]+:.*"))
		{
			isCommand = true;

			final String[] cmds = message.substring(1).split(":", 2);
			final int index = Integer.parseInt(cmds[0]) - 1;

			if (index < _entries.size())
			{
				final String cmd = cmds[1].trim();

				if (cmd.length() == 0)
				{
					final EntryLink entry = (EntryLink) _entries.get(index);
					send(getChannel(), buildLink(index, entry));

					if (entry.hasTags())
					{
						send(getChannel(), buildTags(index, entry));
					}

					if (entry.hasComments())
					{
						final EntryComment[] comments = entry.getComments();

						for (int i = 0; i < comments.length; i++)
						{
							send(getChannel(), buildComment(index, i, comments[i]));
						}
					}
				}
				else
				{
					if ("-".equals(cmd))
					{
						final EntryLink entry = (EntryLink) _entries.get(index);

						if (entry.getLogin().equals(login) || isOp(sender))
						{
							if (_delicious != null)
							{
								_delicious.deletePost(entry);
							}

							_entries.remove(index);
							send(getChannel(), "Entry " + LINK_CMD + (index + 1) + " removed.");
							saveEntries(false);
						}
						else
						{
							send(sender, "Please ask a channel op to remove this entry for you.");
						}
					}
					else if (cmd.charAt(0) == '|')
					{
						if (cmd.length() > 1)
						{
							final EntryLink entry = (EntryLink) _entries.get(index);
							entry.setTitle(cmd.substring(1).trim());

							if (_delicious != null)
							{
								_delicious.updatePost(entry.getLink(), entry);
							}

							send(getChannel(), buildLink(index, entry));
							saveEntries(false);
						}
					}
					else if (cmd.charAt(0) == '=')
					{
						final EntryLink entry = (EntryLink) _entries.get(index);

						if (entry.getLogin().equals(login) || isOp(sender))
						{
							final String link = cmd.substring(1);

							if (link.matches(LINK_MATCH))
							{
								final String oldLink = entry.getLink();

								entry.setLink(link);

								if (_delicious != null)
								{
									_delicious.updatePost(oldLink, entry);
								}

								send(getChannel(), buildLink(index, entry));
								saveEntries(false);
							}
						}
						else
						{
							send(sender, "Please ask a channel op to change this link for you.");
						}
					}
					else if (cmd.charAt(0) == '?')
					{
						if (isOp(sender))
						{
							if (cmd.length() > 1)
							{
								final EntryLink entry = (EntryLink) _entries.get(index);
								entry.setNick(cmd.substring(1));
								send(getChannel(), buildLink(index, entry));
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
						final EntryLink entry = (EntryLink) _entries.get(index);
						final int cindex = entry.addComment(cmd, sender);

						final EntryComment comment = entry.getComment(cindex);
						send(sender, buildComment(index, cindex, comment));
						saveEntries(false);
					}
				}
			}
		}
		else if (message.matches(LINK_CMD + "[0-9]+T:.*"))
		{
			isCommand = true;

			final String[] cmds = message.substring(1).split("T:", 2);
			final int index = Integer.parseInt(cmds[0]) - 1;

			if (index < _entries.size())
			{
				final String cmd = cmds[1].trim();

				final EntryLink entry = (EntryLink) _entries.get(index);

				if (cmd.length() != 0)
				{
					if (entry.getLogin().equals(login) || isOp(sender))
					{
						entry.setTags(cmd);

						if (_delicious != null)
						{
							_delicious.updatePost(entry.getLink(), entry);
						}

						send(getChannel(), buildTags(index, entry));
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
						send(getChannel(), buildTags(index, entry));
					}
					else
					{
						send(sender, "The entry has no tags. Why don't add some?");
					}
				}
			}
		}
		else if (message.matches(LINK_CMD + "[0-9]+\\.[0-9]+:.*"))
		{
			isCommand = true;

			final String[] cmds = message.substring(1).split("[.:]", 3);
			final int index = Integer.parseInt(cmds[0]) - 1;

			if (index < _entries.size())
			{
				final EntryLink entry = (EntryLink) _entries.get(index);
				final int cindex = Integer.parseInt(cmds[1]) - 1;

				if (cindex < entry.getCommentsCount())
				{
					final String cmd = cmds[2].trim();

					if (cmd.length() == 0)
					{
						final EntryComment comment = entry.getComment(cindex);
						send(getChannel(), buildComment(index, cindex, comment));
					}
					else if ("-".equals(cmd))
					{
						entry.deleteComment(cindex);
						send(getChannel(), "Comment " + LINK_CMD + (index + 1) + '.' + (cindex + 1) + " removed.");
						saveEntries(false);
					}
					else if (cmd.charAt(0) == '?')
					{
						if (isOp(sender))
						{
							if (cmd.length() > 1)
							{
								final EntryComment comment = entry.getComment(cindex);
								comment.setNick(cmd.substring(1));
								send(getChannel(), buildComment(index, cindex, comment));
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
						send(sender, buildComment(index, cindex, comment));
						saveEntries(false);
					}
				}
			}
		}

		if (!isCommand)
		{
			recap(sender, message, false);
		}
	}

	/**
	 * This method is called whenever a private message is sent to the bot.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param login The login of the person who sent the private message.
	 * @param hostname The hostname of the person who sent the private message.
	 * @param message The actual message sent.
	 *
	 * @noinspection UseOfSystemOutOrSystemErr
	 */
	protected final void onPrivateMessage(String sender, String login, String hostname, String message)
	{
		if (_logger.isDebugEnabled())
		{
			_logger.debug(">>> " + sender + ": " + message);
		}

		final String[] cmds = message.split(" ", 2);
		final String cmd = cmds[0].toLowerCase();
		String args = "";

		if (cmds.length > 1)
		{
			args = cmds[1].trim();
		}

		if (cmd.startsWith(HELP_CMD))
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
		else if (cmd.equals(DIE_CMD))
		{
			if (isOp(sender))
			{
				send(getChannel(), sender + " has just signed my death sentence.");
				saveEntries(true);
				sleep(3);
				quitServer("The Bot Is Out There!");
				System.exit(0);
			}
		}
		else if (cmd.equals(CYCLE_CMD))
		{
			send(getChannel(), sender + " has just asked me to leave. I'll be back!");
			sleep(0);
			partChannel(getChannel());
			sleep(5);
			joinChannel(getChannel());
		}
		else if (cmd.equals(RECAP_CMD))
		{
			recapResponse(sender, true);
		}
		else if (cmd.equals(USERS_CMD))
		{
			usersResponse(sender, true);
		}
		else if (cmd.startsWith(ADDLOG_CMD) && (cmds.length > 1))
		{
			if (isOp(sender))
			{
				_history.add(0, args);
				send(sender, _history.toString(), true);
			}
		}
		else if (cmd.startsWith(ME_CMD))
		{
			if (isOp(sender))
			{
				if (args.length() > 1)
				{
					action(args);
				}
				else
				{
					helpResponse(sender, ME_CMD);
				}
			}
		}
		else if (cmd.startsWith(NICK_CMD) && (cmds.length > 1))
		{
			if (isOp(sender))
			{
				changeNick(args);
			}
		}
		else if (cmd.startsWith(SAY_CMD))
		{
			if (isOp(sender))
			{
				if (cmds.length > 1)
				{
					send(getChannel(), args, true);
				}
				else
				{
					helpResponse(sender, SAY_CMD);
				}
			}
		}
		else if (cmd.startsWith(MSG_CMD))
		{
			if (isOp(sender))
			{
				if (cmds.length > 1)
				{
					final String[] msg = args.split(" ", 2);

					if (args.length() > 2)
					{
						System.out.println(msg[0] + ' ' + msg[1]);
						send(msg[0], msg[1], true);
					}
					else
					{
						helpResponse(sender, MSG_CMD);
					}
				}
				else
				{
					helpResponse(sender, MSG_CMD);
				}
			}
		}
		else if (cmd.startsWith(VIEW_CMD))
		{
			viewResponse(sender, args, true);
		}
		else if (cmd.startsWith(TIME_CMD))
		{
			timeResponse(sender, args, true);
		}
		else if (cmd.startsWith(WEATHER_CMD))
		{
			weatherResponse(sender, args, true);
		}
		else if (cmd.equals(INFO_CMD))
		{
			infoResponse(sender, true);
		}
		else if (cmd.equals(VERSION_CMD))
		{
			versionResponse(sender, true);
		}
		else if (cmd.equals(DEBUG_CMD))
		{
			if (isOp(sender))
			{
				if (_logger.isDebugEnabled())
				{
					_logger.getLogger().setLevel(_loggerLevel);
				}
				else
				{
					_logger.getLogger().setLevel(Level.DEBUG);
				}

				send(sender, "Debug logging is " + (_logger.isDebugEnabled() ? "enabled." : "disabled."), true);
			}
		}
		else
		{
			helpResponse(sender, "");
		}
	}

	/**
	 * Makes the given string bold.
	 *
	 * @param s The string.
	 *
	 * @return The bold string.
	 */
	private static String bold(String s)
	{
		return Colors.BOLD + s + Colors.BOLD;
	}

	/**
	 * Builds an entry's comment for display on the channel.
	 *
	 * @param entryIndex The entry's index.
	 * @param commentIndex The comment's index.
	 * @param comment The {@link EntryComment comment} object.
	 *
	 * @return The entry's comment.
	 */
	private static String buildComment(int entryIndex, int commentIndex, EntryComment comment)
	{
		return (LINK_CMD + (entryIndex + 1) + '.' + (commentIndex + 1) + ": [" + comment.getNick() + "] " + comment
				.getComment());
	}

	/**
	 * Builds an entry's link for display on the channel.
	 *
	 * @param index The entry's index.
	 * @param entry The {@link EntryLink entry} object.
	 *
	 * @return The entry's link.
	 *
	 * @see #buildLink(int,EntryLink,boolean)
	 */
	private static String buildLink(int index, EntryLink entry)
	{
		return buildLink(index, entry, false);
	}

	/**
	 * Builds an entry's link for display on the channel.
	 *
	 * @param index The entry's index.
	 * @param entry The {@link EntryLink entry} object.
	 * @param isView Set to true to display the number of comments.
	 *
	 * @return The entry's link.
	 */
	private static String buildLink(int index, EntryLink entry, boolean isView)
	{
		final StringBuffer buff = new StringBuffer(LINK_CMD + (index + 1) + ": ");

		buff.append('[').append(entry.getNick()).append(']');

		if (isView && entry.hasComments())
		{
			buff.append("[+").append(entry.getCommentsCount()).append(']');
		}

		buff.append(' ');

		if (NO_TITLE.equals(entry.getTitle()))
		{
			buff.append(bold(entry.getTitle()));
		}
		else
		{
			buff.append(entry.getTitle());
		}

		buff.append(" ( ").append(entry.getLink()).append(" )");

		return buff.toString();
	}

	/**
	 * Build an entry's tags/categories for diplay on the channel.
	 *
	 * @param entryIndex The entry's index.
	 * @param entry The {@link EntryLink entry} object.
	 *
	 * @return The entry's tags.
	 */
	private static String buildTags(int entryIndex, EntryLink entry)
	{
		return (LINK_CMD + (entryIndex + 1) + "T: " + entry.getDeliciousTags());
	}

	/**
	 * Copies a file.
	 *
	 * @param in The source file.
	 * @param out The destination file.
	 *
	 * @throws IOException If the file could not be copied.
	 */
	private static void copyFile(File in, File out)
			throws IOException
	{
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		FileInputStream input = null;
		FileOutputStream output = null;

		try
		{
			input = new FileInputStream(in);
			output = new FileOutputStream(out);

			inChannel = input.getChannel();
			outChannel = output.getChannel();

			inChannel.transferTo(0L, inChannel.size(), outChannel);
		}
		finally
		{
			try
			{
				if (inChannel != null)
				{
					inChannel.close();
				}

				if (input != null)
				{
					input.close();
				}
			}
			catch (Exception ignore)
			{
				; // Do nothing
			}

			try
			{
				if (outChannel != null)
				{
					outChannel.close();
				}

				if (output != null)
				{
					output.close();
				}
			}
			catch (Exception ignore)
			{
				; // Do nothing
			}
		}
	}

	/**
	 * Ensures that the given location (File/URL) has a trailing slash (<code>/</code>) to indicate a directory.
	 *
	 * @param location The File or URL location.
	 * @param isUrl Set to true if the location is a URL
	 *
	 * @return The location ending with a slash.
	 */
	private static String ensureDir(String location, boolean isUrl)
	{
		if (isUrl)
		{
			if (location.charAt(location.length() - 1) == '/')
			{
				return location;
			}
			else
			{
				return location + '/';
			}
		}
		else
		{
			if (location.charAt(location.length() - 1) == File.separatorChar)
			{
				return location;
			}
			else
			{
				return location + File.separatorChar;
			}
		}
	}

	/**
	 * Returns the port.
	 *
	 * @param property The port property value.
	 * @param defaultValue The default value.
	 *
	 * @return The port or default value if invalid.
	 */
	private static int getPort(String property, int defaultValue)
	{
		int port;

		try
		{
			port = Integer.parseInt(property);
		}
		catch (NumberFormatException ignore)
		{
			port = defaultValue;
		}

		return port;
	}

	/**
	 * Returns the current Internet (beat) Time.
	 *
	 * @return The Internet Time string.
	 */
	private static String internetTime()
	{
		final Calendar gc = Calendar.getInstance();

		final int offset = (gc.get(Calendar.ZONE_OFFSET) / (60 * 60 * 1000));
		int hh = gc.get(Calendar.HOUR_OF_DAY);
		final int mm = gc.get(Calendar.MINUTE);
		final int ss = gc.get(Calendar.SECOND);

		hh -= offset; // GMT
		hh += 1; // BMT

		long beats = Math.round(Math.floor((double) ((((hh * 3600) + (mm * 60) + ss) * 1000) / 86400)));

		if (beats >= 1000)
		{
			beats -= (long) 1000;
		}
		else if (beats < 0)
		{
			beats += (long) 1000;
		}

		if (beats < 10)
		{
			return ("@00" + String.valueOf(beats));
		}
		else if (beats < 100)
		{
			return ("@0" + String.valueOf(beats));
		}

		return ('@' + String.valueOf(beats));
	}

	/**
	 * Performs a DNS lookup on the specified query.
	 *
	 * @param query The IP address or hostname.
	 *
	 * @return The lookup query result string.
	 *
	 * @throws UnknownHostException If the host is unknown.
	 */
	private static String lookup(String query)
			throws UnknownHostException
	{
		final StringBuffer buffer = new StringBuffer("");

		final InetAddress[] result = InetAddress.getAllByName(query);
		String hostInfo;

		for (int i = 0; i < result.length; i++)
		{
			if (result[i].getHostAddress().equals(query))
			{
				hostInfo = result[i].getHostName();

				if (hostInfo.equals(query))
				{
					throw new UnknownHostException();
				}
			}
			else
			{
				hostInfo = result[i].getHostAddress();
			}

			if (buffer.length() > 0)
			{
				buffer.append(", ");
			}

			buffer.append(hostInfo);
		}

		return buffer.toString();
	}

	/**
	 * Stores the last 10 public messages and actions.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param message The actual message sent.
	 * @param isAction Set to true if the message is an action.
	 */
	private static void recap(String sender, String message, boolean isAction)
	{
		RECAP_ARRAY.add(HHMM_SDF.format(Calendar.getInstance().getTime()) + " -> " + sender + (isAction ? " " : ": ")
		                + message);

		if (RECAP_ARRAY.size() > MAX_RECAP)
		{
			RECAP_ARRAY.remove(0);
		}
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
	 * Returns today's date.
	 *
	 * @return Today's date in {@link #ISO_SDF ISO} format.
	 */
	private static String today()
	{
		return ISO_SDF.format(Calendar.getInstance().getTime());
	}

	/**
	 * Performs a whois IP query.
	 *
	 * @param query The IP address.
	 *
	 * @return The IP whois data, if any.
	 *
	 * @throws IOException If a connection error occurs.
	 */
	private static String[] whois(String query)
			throws IOException
	{
		final WhoisClient whois = new WhoisClient();
		String[] lines;

		try
		{
			whois.setDefaultTimeout(CONNECT_TIMEOUT);
			whois.connect(WHOIS_HOST);
			whois.setSoTimeout(CONNECT_TIMEOUT);
			whois.setSoLinger(false, 0);

			lines = whois.query('-' + query).split("\n");
		}
		finally
		{
			whois.disconnect();
		}

		return lines;
	}

	/**
	 * Responds the title and links from the RSS feed.
	 *
	 * @param sender The nick of the person who sent the private message.
	 */
	private void feedResponse(String sender)
	{
		if (isValidString(_feedURL))
		{
			new Thread(new FeedReader(this, sender, _feedURL)).start();
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

		for (int i = 0; i < _entries.size(); i++)
		{
			entry = (EntryLink) _entries.get(i);

			if (link.equals(entry.getLink()))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * Returns the bot's nickname regexp pattern.
	 *
	 * @return The nickname regexp pattern.
	 */
	private String getNickPattern()
	{
		final StringBuffer buff = new StringBuffer(0);
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
	 * Get today's date.
	 *
	 * @return Today's date.
	 */
	private synchronized String getToday()
	{
		return _today;
	}

	/**
	 * Returns <code>true</code> if Google services are enabled.
	 *
	 * @return <code>true</code> or <code>false</code>
	 */
	private boolean isGoogleEnabled()
	{
		return isValidString(_googleKey);
	}

	/**
	 * Responds with the Google search results for the specified query.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param query The Google query to execute.
	 */
	private void googleResponse(String sender, String query)
	{
		if (isGoogleEnabled())
		{
			if (query.length() > 0)
			{
				new Thread(new GoogleSearch(this, _googleKey, sender, query, false)).start();
			}
			else
			{
				helpResponse(sender, GOOGLE_CMD);
			}
		}
		else
		{
			send(sender, "The Google search facility is disabled.");
		}
	}

	/**
	 * Returns <code>true</code> if jaiku posting is enabled.
	 *
	 * @return <code>true</code> or <code>false</code>
	 */
	private boolean isJaikuEnabled()
	{
		return isValidString(_jaikuKey) && isValidString(_jaikuUser);
	}

	/**
	 * Posts a message to Jaiku.
	 *
	 * @param sender The sender's nick.
	 * @param message The message.
	 */
	private void jaikuResponse(String sender, String message)
	{
		if (isJaikuEnabled())
		{
			if (message.length() > 0)
			{
				new Thread(new Jaiku(this, sender, _jaikuUser, _jaikuKey, message)).start();
			}
			else
			{
				helpResponse(sender, JAIKU_CMD);
			}
		}
		else
		{
			send(sender, "The Jaiku posting facility is disabled.");
		}
	}

	/**
	 * Returns <code>true</code> if twitter posting is enabled.
	 *
	 * @return <code>true</code> or <code>false</code>
	 */
	private boolean isTwitterEnabled()
	{
		return isValidString(_twitterConsumerKey) && isValidString(_twitterConsumerSecret)
		       && isValidString(_twitterToken) && isValidString(_twitterTokenSecret);
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
				                       _twitterConsumerKey,
				                       _twitterConsumerSecret,
				                       _twitterToken,
				                       _twitterTokenSecret,
				                       message)).start();
			}
			else
			{
				helpResponse(sender, TWITTER_CMD);
			}
		}
		else
		{
			send(sender, "The Twitter posting facility is disabled.");
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
		for (int i = 0; i < INFO_STRS.length; i++)
		{
			send(sender, INFO_STRS[i], isPrivate);
		}

		long timeInSeconds = (System.currentTimeMillis() - START_TIME) / 1000L;

		final long days = timeInSeconds / 86400L;
		timeInSeconds -= (days * 86400L);

		final long hours = timeInSeconds / 3600L;
		timeInSeconds -= (hours * 3600L);

		final long minutes = timeInSeconds / 60L;
		send(sender,
		     "Uptime: " + days + " day(s) " + hours + " hour(s) " + minutes + " minute(s)  [Entries: " + _entries.size()
		     + ']',
		     isPrivate);
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
			for (int i = 0; i < VERSION_STRS.length; i++)
			{
				send(sender, VERSION_STRS[i], isPrivate);
			}
		}
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
		return isValidString(nick) && _ignoredNicks.contains(nick.toLowerCase());

	}

	/**
	 * Returns true is the specified sender is an Op on the {@link #_channel channel}.
	 *
	 * @param sender The sender.
	 *
	 * @return true, if the sender is an Op.
	 */
	private boolean isOp(String sender)
	{
		final User[] users = getUsers(getChannel());

		User user;

		for (int i = 0; i < users.length; i++)
		{
			user = users[i];

			if (user.getNick().equals(sender))
			{
				return user.isOp();
			}
		}

		return false;
	}

	/**
	 * Loads the backlogs.
	 *
	 * @param file The file containing the backlogs.
	 *
	 * @throws FileNotFoundException If the file was not found.
	 * @throws FeedException If an error occurred while reading the feed.
	 */
	private void loadBacklogs(String file)
			throws FileNotFoundException, FeedException
	{
		_history.clear();

		final SyndFeedInput input = new SyndFeedInput();

		InputStreamReader reader = null;

		try
		{
			reader = new InputStreamReader(new FileInputStream(new File(file)));

			final SyndFeed feed = input.build(reader);

			final List items = feed.getEntries();
			SyndEntry item;

			for (int i = items.size() - 1; i >= 0; i--)
			{
				item = (SyndEntryImpl) items.get(i);
				_history.add(item.getTitle());
			}
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException ignore)
				{
					; // Do nothing
				}
			}
		}

	}

	/**
	 * Loads the current entries.
	 *
	 * @param file The file containing the current entries.
	 *
	 * @throws FileNotFoundException If the file was not found.
	 * @throws FeedException If an error occurred while reading the feed.
	 */
	private void loadEntries(String file)
			throws FileNotFoundException, FeedException
	{
		_entries.clear();

		final SyndFeedInput input = new SyndFeedInput();

		InputStreamReader reader = null;

		try
		{
			reader = new InputStreamReader(new FileInputStream(new File(file)));

			final SyndFeed feed = input.build(reader);

			setToday(ISO_SDF.format(feed.getPublishedDate()));

			final List items = feed.getEntries();
			SyndEntry item;
			SyndContent description;
			String[] comments;
			String[] comment;
			String author;
			EntryLink entry;

			for (int i = items.size() - 1; i >= 0; i--)
			{
				item = (SyndEntryImpl) items.get(i);
				author = item.getAuthor()
						.substring(item.getAuthor().lastIndexOf('(') + 1, item.getAuthor().length() - 1);
				entry = new EntryLink(item.getLink(),
				                      item.getTitle(),
				                      author,
				                      getChannel(),
				                      item.getPublishedDate(),
				                      item.getCategories());
				description = item.getDescription();
				comments = description.getValue().split("<br/>");

				for (int j = 0; j < comments.length; j++)
				{
					comment = comments[j].split(":");

					if (comment.length == 2)
					{
						entry.addComment(comment[1].trim(), comment[0]);
					}
				}

				_entries.add(entry);
			}
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException ignore)
				{
					; // Do nothing
				}
			}
		}
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
				send(getChannel(), lookup(query));
			}
			catch (UnknownHostException ignore)
			{
				if (query.matches(
						"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"))
				{
					try
					{
						final String[] lines = whois(query);

						if ((lines != null) && (lines.length > 0))
						{
							String line;

							for (int i = 0; i < lines.length; i++)
							{
								line = lines[i].trim();

								if ((line.length() > 0) && (line.charAt(0) != '#'))
								{
									send(getChannel(), line);
								}
							}
						}
						else
						{
							send(getChannel(), "Unknown host.");
						}
					}
					catch (IOException ioe)
					{
						if (_logger.isDebugEnabled())
						{
							_logger.debug("Unable to perform whois IP lookup: " + query, ioe);
						}

						send(getChannel(), "Unable to perform whois IP lookup: " + ioe.getMessage());
					}
				}
				else
				{
					send(getChannel(), "Unknown host.");
				}
			}
		}
		else
		{
			helpResponse(sender, LOOKUP_CMD);
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
		for (int i = 0; i < RECAP_ARRAY.size(); i++)
		{
			send(sender, (String) RECAP_ARRAY.get(i), isPrivate);
		}
	}

	/**
	 * Saves the entries.
	 *
	 * @param isDayBackup Set the true if the daily backup file should also be created.
	 */
	private void saveEntries(boolean isDayBackup)
	{
		if (_logger.isDebugEnabled())
		{
			_logger.debug("Saving...");
		}

		if (isValidString(_logsDir) && isValidString(_weblogURL))
		{
			FileWriter fw = null;

			try
			{
				fw = new FileWriter(new File(_logsDir + CURRENT_XML));

				SyndFeed rss = new SyndFeedImpl();
				rss.setFeedType("rss_2.0");
				rss.setTitle(getChannel() + " IRC Links");
				rss.setDescription("Links from " + _ircServer + " on " + getChannel());
				rss.setLink(_weblogURL);
				rss.setPublishedDate(Calendar.getInstance().getTime());
				rss.setLanguage("en");

				EntryLink entry;
				StringBuffer buff;
				EntryComment comment;
				final List items = new ArrayList(0);
				SyndEntry item;
				SyndContent description;

				for (int i = (_entries.size() - 1); i >= 0; --i)
				{
					entry = (EntryLink) _entries.get(i);

					buff = new StringBuffer(0);

					if (entry.getCommentsCount() > 0)
					{
						final EntryComment[] comments = entry.getComments();

						for (int j = 0; j < comments.length; j++)
						{
							comment = comments[j];

							if (j > 0)
							{
								buff.append("<br/>");
							}

							buff.append(comment.getNick()).append(": ").append(comment.getComment());
						}
					}

					item = new SyndEntryImpl();
					item.setLink(entry.getLink());
					description = new SyndContentImpl();
					description.setValue(buff.toString());
					item.setDescription(description);
					item.setTitle(entry.getTitle());
					item.setPublishedDate(entry.getDate());
					item.setAuthor(getChannel().substring(1) + '@' + _ircServer + " (" + entry.getNick() + ')');
					item.setCategories(entry.getTags());

					items.add(item);
				}

				rss.setEntries(items);

				if (_logger.isDebugEnabled())
				{
					_logger.debug("Writing the entries feed.");
				}

				final SyndFeedOutput output = new SyndFeedOutput();
				output.output(rss, fw);
				fw.close();

				fw = new FileWriter(new File(_logsDir + getToday() + ".xml"));
				output.output(rss, fw);

				if (isDayBackup)
				{
					if (isValidString(_backlogsURL))
					{
						if (_history.indexOf(getToday()) == -1)
						{
							_history.add(getToday());

							while (_history.size() > MAX_BACKLOGS)
							{
								_history.remove(0);
							}
						}

						fw.close();
						fw = new FileWriter(new File(_logsDir + NAV_XML));
						rss = new SyndFeedImpl();
						rss.setFeedType("rss_2.0");
						rss.setTitle(getChannel() + " IRC Links Backlogs");
						rss.setDescription("Backlogs of Links from " + _ircServer + " on " + getChannel());
						rss.setLink(_backlogsURL);
						rss.setPublishedDate(Calendar.getInstance().getTime());

						String date;
						items.clear();

						for (int i = (_history.size() - 1); i >= 0; --i)
						{
							date = (String) _history.get(i);

							item = new SyndEntryImpl();
							item.setLink(_backlogsURL + date + ".xml");
							item.setTitle(date);
							description = new SyndContentImpl();
							description.setValue("Links for " + date);
							item.setDescription(description);

							items.add(item);
						}

						rss.setEntries(items);

						if (_logger.isDebugEnabled())
						{
							_logger.debug("Writing the backlog feed.");
						}

						output.output(rss, fw);
					}
					else
					{
						_logger.warn("Unable to generate the backlogs feed. No property configured.");
					}
				}
			}
			catch (Exception e)
			{
				_logger.warn("Unable to generate the feed.", e);
			}
			finally
			{
				try
				{
					fw.close();
				}
				catch (Exception ignore)
				{
					; // Do nothing
				}
			}
		}
		else
		{
			_logger.warn("Unable to generate the feed. At least one of the required property is missing.");
		}
	}

	/**
	 * Sets the backlogs URL.
	 *
	 * @param backlogsURL The backlogs URL.
	 */
	private void setBacklogsURL(String backlogsURL)
	{
		_backlogsURL = backlogsURL;
	}

	/**
	 * Sets the del.icio.us authentication.
	 *
	 * @param username The del.icio.us username.
	 * @param password The del.icio.us password.
	 */
	private void setDeliciousAuth(String username, String password)
	{
		_delicious = new DeliciousPoster(username, password, _ircServer);
	}

	/**
	 * Sets the feed URL.
	 *
	 * @param feedURL The feed URL.
	 */
	private void setFeedURL(String feedURL)
	{
		_feedURL = feedURL;
	}

	/**
	 * Sets the Google API key.
	 *
	 * @param googleKey The Google API key.
	 */
	private void setGoogleKey(String googleKey)
	{
		_googleKey = googleKey;
	}

	/**
	 * Sets the Jaiku user and API key..
	 *
	 * @param user The Jaiku user.
	 * @param key The Jaiku API key.
	 */
	private void setJaikuAuth(String user, String key)
	{
		_jaikuKey = key;
		_jaikuUser = user;
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
		_twitterConsumerKey = consumerKey;
		_twitterConsumerSecret = consumerSecret;
		_twitterToken = token;
		_twitterTokenSecret = tokenSecret;
	}

	/**
	 * Sets the ident password.
	 *
	 * @param pwd The password.
	 */
	private void setIdent(String pwd)
	{
		_ident = pwd;
	}

	/**
	 * Sets the ident message.
	 *
	 * @param msg The message.
	 */
	private void setIdentMsg(String msg)
	{
		_identMsg = msg;
	}

	/**
	 * Sets the ident nickname.
	 *
	 * @param nick The nickname.
	 */
	private void setIdentNick(String nick)
	{
		_identNick = nick;
	}

	/**
	 * Sets the Ignored nicks.
	 *
	 * @param nicks The nicks to ignore
	 */
	private void setIgnoredNicks(String nicks)
	{
		if (isValidString(nicks))
		{
			final StringTokenizer st = new StringTokenizer(nicks, ",");

			while (st.hasMoreTokens())
			{
				_ignoredNicks.add(st.nextToken().trim().toLowerCase());
			}
		}
	}

	/**
	 * Sets the default tags/categories.
	 *
	 * @param tags The tags.
	 */
	private void setTags(String tags)
	{
		_defaultTags = tags;
	}

	/**
	 * Set today's date.
	 *
	 * @param today Today's date.
	 */
	private synchronized void setToday(String today)
	{
		_today = today;
	}

	/**
	 * Sets the weblog URL.
	 *
	 * @param weblogURL The weblog URL.
	 */
	private void setWeblogURL(String weblogURL)
	{
		_weblogURL = weblogURL;
	}

	/**
	 * Uses Google to correctly spell a sentence.
	 *
	 * @param sender The nick of the person who sent the message
	 * @param spell The sentence to spell.
	 */
	private void spellResponse(String sender, String spell)
	{
		if (isGoogleEnabled())
		{
			if (spell.length() > 0)
			{
				new Thread(new GoogleSearch(this, _googleKey, getChannel(), spell, true)).start();
			}
			else
			{
				helpResponse(sender, SPELL_CMD);
			}
		}
		else
		{
			send(getChannel(), "The Google spelling facility is disabled.");
		}
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
			helpResponse(sender, STOCK_CMD);
		}
	}

	/**
	 * Responds with the current time.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param args The time command arguments.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void timeResponse(String sender, String args, boolean isPrivate)
	{
		boolean isInvalidTz = false;
		final String tz = ((String) COUNTRIES_MAP.get((args.substring(args.indexOf(' ') + 1).trim().toUpperCase())));
		final String response;

		if (tz != null)
		{
			if (tz.equals(BEATS_KEYWORD))
			{
				response = ("The current Internet Time is: " + internetTime() + ' ' + BEATS_KEYWORD);
			}
			else
			{
				TIME_SDF.setTimeZone(TimeZone.getTimeZone(tz));
				response = TIME_SDF.format(Calendar.getInstance().getTime()) + tz.substring(tz.indexOf('/') + 1)
						.replace('_', ' ');
			}
		}
		else
		{
			isInvalidTz = true;
			response = "The supported time zones/countries are: " + COUNTRIES_MAP.keySet().toString();
		}

		if (isPrivate)
		{
			send(sender, response, isPrivate);
		}
		else
		{
			if (isInvalidTz)
			{
				send(sender, response);
			}
			else
			{
				send(getChannel(), response);
			}
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
		final User[] users = getUsers(getChannel());
		final String[] nicks = new String[users.length];

		for (int i = 0; i < users.length; i++)
		{
			nicks[i] = users[i].getNick();
		}

		Arrays.sort(nicks, String.CASE_INSENSITIVE_ORDER);

		final StringBuffer buff = new StringBuffer(0);

		for (int i = 0; i < nicks.length; i++)
		{
			buff.append(nicks[i]).append(' ');
		}

		send(sender, buff.toString(), isPrivate);
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

		if (!_entries.isEmpty())
		{
			final int max = _entries.size();
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
				entry = (EntryLink) _entries.get(i);

				if (lcArgs.length() > 0)
				{
					if ((entry.getLink().toLowerCase().indexOf(lcArgs) != -1)
					    || (entry.getTitle().toLowerCase().indexOf(lcArgs) != -1) || (
							entry.getNick().toLowerCase().indexOf(lcArgs) != -1))
					{
						if (sent > MAX_ENTRIES)
						{
							send(sender,
							     "To view more, try: " + bold(
									     getNick() + ": " + VIEW_CMD + ' ' + (i + 1) + ' ' + lcArgs),
							     isPrivate);

							break;
						}

						send(sender, buildLink(i, entry, true), isPrivate);
						sent++;
					}
				}
				else
				{
					if (sent > MAX_ENTRIES)
					{
						send(sender,
						     "To view more, try: " + bold(getNick() + ": " + VIEW_CMD + ' ' + (i + 1)),
						     isPrivate);

						break;
					}

					send(sender, buildLink(i, entry, true), isPrivate);
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
