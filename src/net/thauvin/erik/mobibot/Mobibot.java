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

import com.rsslibj.elements.Channel;
import com.rsslibj.elements.Item;

import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.commons.net.WhoisClient;

import org.apache.log4j.Level;

import org.jibble.pircbot.*;

import java.io.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.text.SimpleDateFormat;

import java.util.*;


/**
 * Implements the #mobitopia bot.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 *
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
	 * The info strings.
	 */
	private static final String[] INFO_STRS = 
											  {
												  "Mobibot v0.1.2 by Erik C. Thauvin (erik@thauvin.net)",
												  "<http://www.thauvin.net/mobitopia/mobibot/>"
											  };

	/**
	 * Debug command line argument.
	 */
	private static final String DEBUG_ARG = "-debug";

	/**
	 * The object serialization file where data is saved between launches.
	 */
	private static final String DATA_FILE = "./mobibot.ser";

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
	 * The help command.
	 */
	private static final String HELP_CMD = "help";

	/**
	 * The help on posting keyword.
	 */
	private static final String HELP_POSTING_KEYWORD = "posting";

	/**
	 * The Google command.
	 */
	private static final String GOOGLE_CMD = "google";

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
	private static final String LINK_MATCH = "http://";

	/**
	 * The lookup command.
	 */
	private static final String LOOKUP_CMD = "lookup";

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
	 * The countries supporte by the {@link #TIME_CMD time} command.
	 */
	private static final Map COUNTRIES_MAP = new TreeMap();

	/**
	 * The date/time format for the {@link #TIME_CMD time} command.
	 */
	private static final SimpleDateFormat TIME_SDF = new SimpleDateFormat("'The time is 'HH:mm' on 'EEE, d MMM yyyy' in '");

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
	 * The view command.
	 */
	private static final String VIEW_CMD = "view";

	/**
	 * The view "all" keyword.
	 */
	private static final String VIEW_ALL_KEYWORD = "all";

	/**
	 * The weather command.
	 */
	private static final String WEATHER_CMD = "weather";

	/**
	 * The HH:MM timestamp simple date format.
	 */
	private static final SimpleDateFormat HHMM_SDF = new SimpleDateFormat("HH:mm");

	/**
	 * The ISO (YYYY-MM-DD) simple date format.
	 */
	private static final SimpleDateFormat ISO_SDF = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * The start time.
	 */
	private static final long START_TIME = System.currentTimeMillis();

	/**
	 * The recap array.
	 */
	private static final List RECAP_ARRAY = new ArrayList(MAX_RECAP);

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
		COUNTRIES_MAP.put("RU", "Europe/Moscow");
		COUNTRIES_MAP.put("SE", "Europe/Stockholm");
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
		COUNTRIES_MAP.put("UTC", "UTC");
		COUNTRIES_MAP.put("INTERNET", BEATS_KEYWORD);
		COUNTRIES_MAP.put("BEATS", BEATS_KEYWORD);
	}

	/**
	 * The whois host.
	 */
	private static final String WHOIS_HOST = "whois.arin.net";

	/**
	 * The logger default level.
	 */
	private final Level _loggerLevel;

	/**
	 * The feed items.
	 */
	private final List _feedItems = new ArrayList(0);

	/**
	 * The logger.
	 */
	private final Log4JLogger _logger;

	/**
	 * The backlogs URL.
	 */
	private final String _backlogsURL;

	/**
	 * The main channel.
	 */
	private final String _channel;

	/**
	 * The feed URL.
	 */
	private final String _feedURL;

	/**
	 * The IRC server.
	 */
	private final String _ircServer;

	/**
	 * The log directory.
	 */
	private final String _logsDir;

	/**
	 * The weblog URL.
	 */
	private final String _weblogURL;

	/**
	 * The entries array.
	 */
	private final Vector _entries = new Vector(0);

	/**
	 * The history/backlogs array.
	 */
	private final Vector _history = new Vector(0);

	// The feed last modification date.
	private String _feedLastMod = "";

	// The Google API key.
	private String _googleKey = "";

	// Today's date.
	private String _today = today();

	// The Google API key flag.
	private boolean _isGoogleKeySet;

	/**
	 * Creates a new Mobibot object.
	 *
	 * @param server The server.
	 * @param channel The channel.
	 * @param weblogURL The weblog URL.
	 * @param feedURL The feed URL.
	 * @param backlogsURL The backlogs URL.
	 * @param logsDir The logs directory.
	 */
	public Mobibot(String server, String channel, String weblogURL, String feedURL, String backlogsURL, String logsDir)
	{
		System.getProperties().setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(CONNECT_TIMEOUT));
		System.getProperties().setProperty("sun.net.client.defaultReadTimeout", String.valueOf(CONNECT_TIMEOUT));

		_ircServer = server;
		_channel = channel;
		_weblogURL = weblogURL;
		_feedURL = feedURL;
		_backlogsURL = backlogsURL;
		_logsDir = logsDir;

		// Set the logger
		_logger = new Log4JLogger(Mobibot.class.getPackage().getName());
		_loggerLevel = _logger.getLogger().getLevel();

		// Load the saved data, if any
		final File ser = new File(DATA_FILE);

		if (ser.exists())
		{
			ObjectInputStream ois = null;

			try
			{
				ois = new ObjectInputStream(new FileInputStream(ser));

				_entries.addAll((Vector) ois.readObject());
				_today = (String) ois.readObject();
				_history.addAll((Vector) ois.readObject());

				saveEntries(true);

				if (!today().equals(_today))
				{
					_entries.clear();
					_today = today();
				}
			}
			catch (ClassNotFoundException e)
			{
				_logger.fatal("Unable to read objects in data file.", e);
				System.exit(1);
			}
			catch (IOException e)
			{
				_logger.fatal("Unable to open data file.", e);
				System.exit(1);
			}
			finally
			{
				if (ois != null)
				{
					try
					{
						ois.close();
					}
					catch (IOException ignore)
					{
						; // Do nothing
					}
				}
			}
		}
	}

	/**
	 * The Truth Is Out There...
	 *
	 * @param args The command line arguments.
	 */
	public static void main(String[] args)
	{
		FileInputStream fis = null;
		final Properties p = new Properties();

		try
		{
			fis = new FileInputStream(new File("./mobibot.properties"));

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
		final String nickname = p.getProperty("nick", Mobibot.class.getName().toLowerCase());
		final String logsDir = ensureDir(p.getProperty("logs", "."), false);

		if ((args.length == 0) || !DEBUG_ARG.equals(args[0]))
		{
			// Redirect the stdout and stderr
			PrintStream stdout = null;

			try
			{
				stdout = new PrintStream(new FileOutputStream(logsDir + channel.substring(1) + '.' + today() + ".log",
															  true));
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

		// Create the bot
		final Mobibot bot = new Mobibot(server, channel, weblogURL, feedURL, backlogsURL, logsDir);

		// Initialize the bot
		bot.setVerbose(true);
		bot.setName(nickname);
		bot.setLogin(login);
		bot.setVersion(weblogURL);

		// Set the Google key
		bot.setGoogleKey(googleKey);

		// Connect
		try
		{
			bot.connect(server);
		}
		catch (Exception e)
		{
			int retries = 0;

			while ((retries < MAX_RECONNECT) && !bot.isConnected())
			{
				sleep(10);

				if ((retries > 0) && (e instanceof NickAlreadyInUseException))
				{
					bot.setName(nickname + retries);
				}

				retries++;

				try
				{
					bot.connect(server);
				}
				catch (NickAlreadyInUseException ex)
				{
					if (retries == MAX_RECONNECT)
					{
						System.err.println("Unable to connect to " + server + " after " + MAX_RECONNECT +
										   " retries. Nickname already in use.");
						e.printStackTrace(System.err);
						System.exit(1);
					}
				}
				catch (Exception ex)
				{
					if (retries == MAX_RECONNECT)
					{
						System.err.println("Unable to connect to " + server + " after " + MAX_RECONNECT + " retries.");
						e.printStackTrace(System.err);
						System.exit(1);
					}
				}
			}
		}

		bot.setVersion(INFO_STRS[0]);
		bot.joinChannel(channel);
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
	 * Returns the current channel.
	 *
	 * @return The current channel.
	 */
	public final String getChannel()
	{
		return _channel;
	}

	/**
	 * Sets the feed items.
	 *
	 * @param items The feed items.
	 */
	public final void setFeedItems(List items)
	{
		synchronized (_feedItems)
		{
			_feedItems.clear();
			_feedItems.addAll(items);
		}
	}

	/**
	 * Returns the feed items.
	 *
	 * @return The feed items.
	 */
	public final List getFeedItems()
	{
		synchronized (_feedItems)
		{
			return _feedItems;
		}
	}

	/**
	 * Sets the feed last modification date.
	 *
	 * @param feedLastMod The last modification date.
	 */
	public final void setFeedLastMod(String feedLastMod)
	{
		_feedLastMod = feedLastMod;
	}

	/**
	 * Returns the feed last modification date.
	 *
	 * @return The feed modification date, or empty.
	 */
	public final String getFeedLastMod()
	{
		if (_feedLastMod != null)
		{
			return _feedLastMod;
		}

		return "";
	}

	/**
	 * Sets the Google API key.
	 *
	 * @param googleKey The Google API key.
	 */
	public final void setGoogleKey(String googleKey)
	{
		if ((googleKey != null) && (googleKey.length() > 0))
		{
			_googleKey = googleKey;
			_isGoogleKeySet = true;
		}
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
	 * Set today's date.
	 *
	 * @param today Today's date.
	 */
	public final void setToday(String today)
	{
		synchronized (_today)
		{
			_today = today;
		}
	}

	/**
	 * Get today's date.
	 *
	 * @return Today's date.
	 */
	public final String getToday()
	{
		synchronized (_today)
		{
			return _today;
		}
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
		if (isPrivate)
		{
			this.sendMessage(sender, message);
		}
		else
		{
			this.sendNotice(sender, message);
		}
	}

	/**
	 * Responds with the bot's help.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param cmd The actual help command.
	 */
	public void helpResponse(String sender, String cmd)
	{
		final String lcmd = cmd.toLowerCase();

		if (lcmd.endsWith(HELP_POSTING_KEYWORD))
		{
			this.sendNotice(sender, Colors.BOLD + "Post a URL, by saying it on a line on its own." + Colors.BOLD);
			this.sendNotice(sender,
							"I will reply with a label, for example: " + Colors.BOLD + LINK_CMD + '1' + Colors.BOLD);
			this.sendNotice(sender, "To add a title, use a its label and a pipe:");
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + LINK_CMD + "1:|This is the title");
			this.sendNotice(sender, "To add a comment: ");
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + LINK_CMD + "1:This is a comment");
			this.sendNotice(sender,
							"I will reply with a label, for example: " + Colors.BOLD + LINK_CMD + "1.1" + Colors.BOLD);
			this.sendNotice(sender, "To edit a comment, use its label: ");
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + LINK_CMD + "1.1:This is an edited comment");
			this.sendNotice(sender, "To delete a comment, use its label and a minus sign: ");
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + LINK_CMD + "1.1:-" + Colors.BOLD);
			this.sendNotice(sender, "You can also view a posting by saying its label.");
		}
		else if (lcmd.endsWith(VIEW_CMD))
		{
			this.sendNotice(sender, "To list the current URL posts:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + VIEW_CMD + Colors.BOLD + " [" +
							VIEW_ALL_KEYWORD + ']');
		}
		else if (lcmd.endsWith(_channel.substring(1).toLowerCase()))
		{
			this.sendNotice(sender, "To list the last 5 posts from the channel's weblog:");
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + _channel.substring(1) +
							Colors.BOLD);
		}
		else if (lcmd.endsWith(GOOGLE_CMD))
		{
			this.sendNotice(sender, "To search Google:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + GOOGLE_CMD + " <query>" + Colors.BOLD);
		}
		else if (lcmd.endsWith(RECAP_CMD))
		{
			this.sendNotice(sender, "To list the last 10 public channel messages:");
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + RECAP_CMD + Colors.BOLD);
		}
		else if (lcmd.endsWith(CALC_CMD))
		{
			this.sendNotice(sender, "To solve a mathematical calculation:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + CALC_CMD + " <calculation>" + Colors.BOLD);
		}
		else if (lcmd.endsWith(LOOKUP_CMD))
		{
			this.sendNotice(sender, "To perform a DNS lookup query:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + LOOKUP_CMD + " <ip address or hostname>" +
							Colors.BOLD);
		}
		else if (lcmd.endsWith(TIME_CMD))
		{
			this.sendNotice(sender, "To display a country's current date/time:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + TIME_CMD + Colors.BOLD +
							" [<country code>]");

			this.sendNotice(sender, "For a listing of the supported countries:");
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + TIME_CMD + Colors.BOLD);
		}
		else if (lcmd.endsWith(SPELL_CMD))
		{
			this.sendNotice(sender, "To have Google try to correctly spell a sentence:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + SPELL_CMD + " <sentence>" + Colors.BOLD);
		}
		else if (lcmd.endsWith(STOCK_CMD))
		{
			this.sendNotice(sender, "To retrieve a stock quote:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + STOCK_CMD + " <symbol>" + Colors.BOLD);
		}
		else if (lcmd.endsWith(DICE_CMD))
		{
			this.sendNotice(sender, "To roll the dice:");
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + DICE_CMD + Colors.BOLD);
		}
		else if (lcmd.endsWith(WEATHER_CMD))
		{
			this.sendNotice(sender, "To display weather information:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + WEATHER_CMD + Colors.BOLD +
							" [<ICAO station id>]");
			this.sendNotice(sender, "See: <" + Weather.STATIONS_URL + '>');
		}
		else if (lcmd.endsWith(USERS_CMD))
		{
			this.sendNotice(sender, "To list the users present on the channel:");
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + USERS_CMD + Colors.BOLD);
		}
		else if (lcmd.endsWith(INFO_CMD))
		{
			this.sendNotice(sender, "To view information about the bot:");
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + INFO_CMD + Colors.BOLD);
		}
		else if (lcmd.endsWith(CYCLE_CMD))
		{
			if (isOp(sender))
			{
				this.sendNotice(sender, "To have the bot leave the channel and come back:");
				this.sendNotice(sender,
								DOUBLE_INDENT + Colors.BOLD + "/msg " + getNick() + ' ' + CYCLE_CMD + Colors.BOLD);
			}
		}
		else if (lcmd.endsWith(ME_CMD))
		{
			if (isOp(sender))
			{
				this.sendNotice(sender, "To have the bot perform an action:");
				this.sendNotice(sender,
								DOUBLE_INDENT + Colors.BOLD + "/msg " + getNick() + ' ' + ME_CMD + " <action>" +
								Colors.BOLD);
			}
		}
		else if (lcmd.endsWith(SAY_CMD))
		{
			if (isOp(sender))
			{
				this.sendNotice(sender, "To have the bot say something on the channel:");
				this.sendNotice(sender,
								DOUBLE_INDENT + Colors.BOLD + "/msg " + getNick() + ' ' + SAY_CMD + " <text>" +
								Colors.BOLD);
			}
		}
		else if (lcmd.indexOf(CURRENCY_CMD) != -1)
		{
			this.sendNotice(sender, "To convert from one currency to another:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + CURRENCY_CMD + " [100 USD to EUR]" +
							Colors.BOLD);

			if (lcmd.endsWith(CURRENCY_CMD))
			{
				this.sendNotice(sender, "For a listing of supported currencies:");
				this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + CURRENCY_CMD + Colors.BOLD);
			}
		}
		else
		{
			this.sendNotice(sender, Colors.BOLD + "Type a URL on " + _channel + " to post it." + Colors.BOLD);
			this.sendNotice(sender, "For more information on specific command, type:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + getNick() + ": " + HELP_CMD + " <command>" + Colors.BOLD);
			this.sendNotice(sender, "The commands are:");
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + CALC_CMD + ' ' + CURRENCY_CMD + ' ' + DICE_CMD + ' ' +
							GOOGLE_CMD + ' ' + INFO_CMD + ' ' + _channel.substring(1) + ' ' + LOOKUP_CMD + Colors.BOLD);
			this.sendNotice(sender,
							DOUBLE_INDENT + Colors.BOLD + HELP_POSTING_KEYWORD + ' ' + RECAP_CMD + ' ' + SPELL_CMD +
							' ' + STOCK_CMD + ' ' + TIME_CMD + ' ' + USERS_CMD + ' ' + VIEW_CMD + Colors.BOLD);
			this.sendNotice(sender, DOUBLE_INDENT + Colors.BOLD + WEATHER_CMD + Colors.BOLD);

			if (isOp(sender))
			{
				this.sendNotice(sender, "The op commands are:");
				this.sendNotice(sender,
								DOUBLE_INDENT + Colors.BOLD + CYCLE_CMD + ' ' + ME_CMD + ' ' + SAY_CMD + Colors.BOLD);
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
		if (target.equals(_channel))
		{
			recap(sender, action, true);
		}
	}

	/**
	 * This method carries out the actions to be performed when the PircBot gets disconnected.
	 */
	protected final void onDisconnect()
	{
		setVersion(_weblogURL);

		// Connect
		try
		{
			connect(_ircServer);
		}
		catch (Exception e)
		{
			int retries = 0;

			while ((retries < MAX_RECONNECT) && !isConnected())
			{
				sleep(10);

				if ((retries > 0) && (e instanceof NickAlreadyInUseException))
				{
					setName(getNick() + retries);
				}

				retries++;

				try
				{
					connect(_ircServer);
				}
				catch (NickAlreadyInUseException ex)
				{
					if (retries == MAX_RECONNECT)
					{
						_logger.debug("Unable to reconnect to " + _ircServer + " after " + MAX_RECONNECT +
									  " retries. Nickname already in use.", e);
						System.exit(1);
					}
				}
				catch (Exception ex)
				{
					if (retries == MAX_RECONNECT)
					{
						_logger.debug("Unable to reconnect to " + _ircServer + " after " + MAX_RECONNECT + " retries.",
									  ex);
						e.printStackTrace(System.err);
						System.exit(1);
					}
				}
			}
		}

		setVersion(INFO_STRS[0]);
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
		_logger.debug(">>> " + sender + ": " + message);

		final String lcMsg = message.toLowerCase();

		if ((lcMsg.startsWith(LINK_MATCH)) && (lcMsg.length() > LINK_MATCH.length()))
		{
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

				if ((cmds.length > 1) && (cmds[1].trim().length() > 0))
				{
					_entries.add(new EntryLink(cmd, cmds[1].trim(), sender, login));
				}
				else
				{
					_entries.add(new EntryLink(cmd, sender, login));
				}

				final int index = _entries.size() - 1;
				final EntryLink entry = (EntryLink) _entries.get(index);
				this.sendNotice(channel, buildLink(index, entry));

				saveEntries(isBackup);
			}
			else
			{
				final EntryLink entry = (EntryLink) _entries.get(dupIndex);
				this.sendNotice(sender, "Duplicate >> " + buildLink(dupIndex, entry));
			}
		}
		else if (lcMsg.matches(getNick() + ":.*"))
		{
			final String[] cmds = lcMsg.split(":", 2);
			final String cmd = cmds[1].trim();

			if (cmd.startsWith(HELP_CMD))
			{
				helpResponse(sender, cmd);
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
			else if (cmd.equals(DICE_CMD))
			{
				final Random r = new Random();
				int i = r.nextInt(6) + 1;
				int y = r.nextInt(6) + 1;
				final int total = i + y;

				this.sendNotice(_channel, sender + " rolled two dice: " + i + " and " + y + " for a total of " + total);

				i = r.nextInt(6) + 1;
				y = r.nextInt(6) + 1;
				this.sendAction(_channel, "rolled two dice: " + i + " and " + y + " for a total of " + (i + y));

				if (total < (i + y))
				{
					this.sendAction(_channel, "wins.");
				}
				else if (total > (i + y))
				{
					this.sendAction(_channel, "lost.");
				}
				else
				{
					this.sendAction(_channel, "tied.");
				}
			}
			else if (cmd.equalsIgnoreCase(_channel.substring(1)))
			{
				feedResponse(sender);
			}
			else if (cmd.startsWith(CURRENCY_CMD))
			{
				new Thread(new CurrencyConverter(this, sender,
												 cmd.substring(cmd.indexOf(CURRENCY_CMD) + CURRENCY_CMD.length()).trim(),
												 today())).start();
			}
			else if (cmd.startsWith(LOOKUP_CMD))
			{
				lookupResponse(sender, message.substring(lcMsg.indexOf(LOOKUP_CMD) + LOOKUP_CMD.length()).trim());
			}
			else if (cmd.startsWith(VIEW_CMD))
			{
				viewResponse(sender, cmd, false);
			}
			else if (cmd.startsWith(GOOGLE_CMD))
			{
				googleResponse(sender, message.substring(lcMsg.indexOf(GOOGLE_CMD) + GOOGLE_CMD.length()).trim());
			}
			else if (cmd.startsWith(SPELL_CMD))
			{
				spellResponse(sender, message.substring(lcMsg.indexOf(SPELL_CMD) + SPELL_CMD.length()).trim());
			}
			else if (cmd.startsWith(STOCK_CMD))
			{
				stockResponse(sender, message.substring(lcMsg.indexOf(STOCK_CMD) + STOCK_CMD.length()).trim());
			}
			else if (cmd.startsWith(CALC_CMD))
			{
				final MathEvaluator me = new MathEvaluator(message.substring(lcMsg.indexOf(CALC_CMD) +
																			 CALC_CMD.length()).trim());

				try
				{
					this.sendNotice(_channel, String.valueOf(me.getValue()));
				}
				catch (Exception e)
				{
					_logger.debug("Unable to calculate: " + message, e);
				}
			}
			else if (cmd.startsWith(TIME_CMD))
			{
				timeResponse(sender, cmd, false);
			}
			else if (cmd.startsWith(WEATHER_CMD))
			{
				weatherResponse(sender, cmd, false);
			}
		}
		else if (message.matches(LINK_CMD + "[0-9]+:.*"))
		{
			final String[] cmds = message.substring(1).split(":", 2);
			final int index = Integer.parseInt(cmds[0]) - 1;

			if (index < _entries.size())
			{
				final String cmd = cmds[1].trim();

				if (cmd.length() == 0)
				{
					final EntryLink entry = (EntryLink) _entries.get(index);
					this.sendNotice(_channel, buildLink(index, entry));

					if (entry.hasComments())
					{
						final EntryComment[] comments = entry.getComments();

						for (int i = 0; i < comments.length; i++)
						{
							this.sendNotice(_channel, buildComment(index, i, comments[i]));
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
							_entries.remove(index);
							this.sendNotice(_channel, "Entry " + LINK_CMD + (index + 1) + " removed.");
							saveEntries(false);
						}
						else
						{
							this.sendNotice(sender, "Please ask a channel op to remove this entry for you.");
						}
					}
					else if (cmd.charAt(0) == '|')
					{
						if (cmd.length() > 1)
						{
							final EntryLink entry = (EntryLink) _entries.get(index);
							entry.setTitle(cmd.substring(1));
							this.sendNotice(_channel, buildLink(index, entry));
							saveEntries(false);
						}
					}
					else if (cmd.charAt(0) == '=')
					{
						if (isOp(sender))
						{
							if (cmd.length() > 1)
							{
								final EntryLink entry = (EntryLink) _entries.get(index);
								entry.setLink(cmd.substring(1));
								this.sendNotice(_channel, buildLink(index, entry));
								saveEntries(false);
							}
						}
						else
						{
							this.sendNotice(sender, "Please ask a channel op to change this link for you.");
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
								this.sendNotice(_channel, buildLink(index, entry));
								saveEntries(false);
							}
						}
						else
						{
							this.sendNotice(sender, "Please ask a channel op to change the author of this link for you.");
						}
					}
					else
					{
						final EntryLink entry = (EntryLink) _entries.get(index);
						final int cindex = entry.addComment(cmd, sender);

						final EntryComment comment = entry.getComment(cindex);
						this.sendNotice(sender, buildComment(index, cindex, comment));
						saveEntries(false);
					}
				}
			}
		}
		else if (message.matches(LINK_CMD + "[0-9]+\\.[0-9]+:.*"))
		{
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
						this.sendNotice(_channel, buildComment(index, cindex, comment));
					}
					else if ("-".equals(cmd))
					{
						entry.deleteComment(cindex);
						this.sendNotice(_channel, "Comment " + LINK_CMD + (index + 1) + '.' + (cindex + 1) +
										" removed.");
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
								this.sendNotice(_channel, buildComment(index, cindex, comment));
								saveEntries(false);
							}
						}
						else
						{
							this.sendNotice(sender,
											"Please ask a channel op to change the author of this comment for you.");
						}
					}
					else
					{
						entry.setComment(cindex, cmd, sender);

						final EntryComment comment = entry.getComment(cindex);
						this.sendNotice(sender, buildComment(index, cindex, comment));
						saveEntries(false);
					}
				}
			}
		}
		else
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
	 */
	protected final void onPrivateMessage(String sender, String login, String hostname, String message)
	{
		_logger.debug(">>> " + sender + ": " + message);

		final String lcMsg = message.toLowerCase();

		if (lcMsg.startsWith(HELP_CMD))
		{
			helpResponse(sender, message);
		}
		else if (lcMsg.equals(DIE_CMD))
		{
			if (isOp(sender))
			{
				this.sendNotice(_channel, sender + " has just signed my death sentence.");
				saveEntries(true);
				sleep(3);
				this.quitServer("The Bot Is Out There!");
				System.exit(0);
			}
		}
		else if (lcMsg.equals(CYCLE_CMD))
		{
			this.sendNotice(_channel, sender + " has just asked me to leave. I'll be back!");
			sleep(0);
			this.partChannel(_channel);
			sleep(5);
			this.joinChannel(_channel);
		}
		else if (lcMsg.equals(RECAP_CMD))
		{
			recapResponse(sender, true);
		}
		else if (lcMsg.equals(USERS_CMD))
		{
			usersResponse(sender, true);
		}
		else if (lcMsg.startsWith(ADDLOG_CMD) && !lcMsg.endsWith(ADDLOG_CMD))
		{
			if (isOp(sender))
			{
				_history.add(0, message.substring(message.indexOf(ADDLOG_CMD) + ADDLOG_CMD.length()).trim());
				this.sendMessage(sender, _history.toString());
			}
		}
		else if (lcMsg.startsWith(ME_CMD) && !lcMsg.endsWith(ME_CMD))
		{
			if (isOp(sender))
			{
				this.sendAction(_channel, message.substring(message.indexOf(ME_CMD) + ME_CMD.length()).trim());
			}
		}
		else if (lcMsg.startsWith(NICK_CMD) && !lcMsg.endsWith(ME_CMD))
		{
			if (isOp(sender))
			{
				this.changeNick(message.substring(message.indexOf(NICK_CMD) + NICK_CMD.length()).trim());
			}
		}
		else if (lcMsg.startsWith(SAY_CMD) && !lcMsg.endsWith(SAY_CMD))
		{
			if (isOp(sender))
			{
				this.sendMessage(_channel, message.substring(message.indexOf(SAY_CMD) + SAY_CMD.length()).trim());
			}
		}
		else if (lcMsg.startsWith(VIEW_CMD))
		{
			viewResponse(sender, lcMsg, true);
		}
		else if (lcMsg.startsWith(TIME_CMD))
		{
			timeResponse(sender, message, true);
		}
		else if (lcMsg.startsWith(WEATHER_CMD))
		{
			weatherResponse(sender, lcMsg, true);
		}
		else if (lcMsg.equals(INFO_CMD))
		{
			infoResponse(sender, true);
		}
		else if (lcMsg.equals(DEBUG_CMD))
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

				this.sendMessage(sender, "Debug logging is " + (_logger.isDebugEnabled() ? "enabled." : "disabled."));
			}
		}
		else
		{
			helpResponse(sender, "");
		}
	}

	/**
	 * Builds an entry's link for display on the channel.
	 *
	 * @param index The entry's index.
	 * @param entry The {@link EntryLink entry} object.
	 *
	 * @return The entry's link.
	 *
	 * @see #buildLink(int, EntryLink, boolean)
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

		buff.append('[' + entry.getNick() + ']');

		if (isView && entry.hasComments())
		{
			buff.append("[+" + entry.getCommentsCount() + ']');
		}

		buff.append(' ' + entry.getTitle() + " <" + entry.getLink() + '>');

		return buff.toString();
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
	 * Returns true is the specified sender is an Op on the {@link #_channel channel}.
	 *
	 * @param sender The sender.
	 *
	 * @return true, if the sender is an Op.
	 */
	private boolean isOp(String sender)
	{
		final User[] users = this.getUsers(_channel);

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
		return (LINK_CMD + (entryIndex + 1) + '.' + (commentIndex + 1) + ": [" + comment.getNick() + "] " +
			   comment.getComment());
	}

	/**
	 * Responds the title and links from the RSS feed.
	 *
	 * @param sender The nick of the person who sent the private message.
	 */
	private void feedResponse(String sender)
	{
		if (_feedURL.length() > 0)
		{
			new Thread(new FeedReader(this, sender, _feedURL)).start();
		}
		else
		{
			this.sendNotice(sender, "There is no weblog setup for this channel.");
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
	 * Responds with the Google search results for the specified query.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param query The Google query to execute.
	 */
	private void googleResponse(String sender, String query)
	{
		if (_isGoogleKeySet)
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
			this.sendNotice(sender, "The Google search facility is disabled.");
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
			 "Uptime: " + days + " day(s) " + hours + " hour(s) " + minutes + " minute(s)  [Entries: " +
			 _entries.size() + ']', isPrivate);
	}

	/**
	 * Performs a DNS lookup on the specified query.
	 *
	 * @param query The IP address or hostname.
	 *
	 * @return The lookup query result string.
	 *
	 * @exception UnknownHostException If the host is unknown.
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
	 * Stores the last 10 public messages and actions.
	 *
	 * @param sender The nick of the person who sent the private message.
	 * @param message The actual message sent.
	 * @param isAction Set to true if the message is an action.
	 */
	private void recap(String sender, String message, boolean isAction)
	{
		RECAP_ARRAY.add(HHMM_SDF.format(Calendar.getInstance().getTime()) + " -> " + sender + (isAction ? " " : ": ") +
						message);

		if (RECAP_ARRAY.size() > MAX_RECAP)
		{
			RECAP_ARRAY.remove(0);
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
	private synchronized void saveEntries(boolean isDayBackup)
	{
		if ((_logsDir.length() > 0) && (_weblogURL.length() > 0))
		{
			FileWriter fw = null;

			try
			{
				fw = new FileWriter(new File(_logsDir + "current.xml"));

				Channel rss = new Channel();
				rss.setTitle(_channel + " IRC Links");
				rss.setDescription("Links from " + _ircServer + " on " + _channel);
				rss.setLink(_weblogURL);
				rss.setPubDate(Calendar.getInstance().getTime());

				EntryLink entry;
				StringBuffer buff;
				EntryComment comment;
				Item item;

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

					item = new Item();
					item.setLink(entry.getLink());
					item.setDescription(buff.toString());
					item.setTitle(entry.getTitle());
					item.setPubDate(entry.getDate());
					item.setAuthor(_channel.substring(1) + '@' + _ircServer + " (" + entry.getNick() + ')');

					rss.addItem(item);
				}

				fw.write(rss.getFeed("2.0"));

				if (isDayBackup)
				{
					if (_backlogsURL.length() > 0)
					{
						fw.close();
						fw = new FileWriter(new File(_logsDir + getToday() + ".xml"));
						fw.write(rss.getFeed("2.0"));

						if (_history.indexOf(getToday()) == -1)
						{
							_history.add(getToday());

							while (_history.size() > MAX_BACKLOGS)
							{
								_history.remove(0);
							}
						}

						fw.close();
						fw = new FileWriter(new File(_logsDir + "nav.xml"));
						rss = new Channel();
						rss.setTitle(_channel + " IRC Links Backlogs");
						rss.setDescription("Backlogs of Links from " + _ircServer + " on " + _channel);
						rss.setLink(_backlogsURL);
						rss.setPubDate(Calendar.getInstance().getTime());

						String date;

						for (int i = (_history.size() - 1); i >= 0; --i)
						{
							date = (String) _history.get(i);
							rss.addItem(_backlogsURL + date + ".xml", "Links for " + date, date);
						}

						fw.write(rss.getFeed("2.0"));
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

		// Save the data
		ObjectOutputStream oos = null;

		try
		{
			oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE));

			oos.writeObject(_entries.clone());
			oos.writeObject(getToday());
			oos.writeObject(_history.clone());
		}
		catch (IOException e)
		{
			_logger.warn("Unable to save the data file.", e);
		}
		finally
		{
			try
			{
				if (oos != null)
				{
					oos.close();
				}
			}
			catch (IOException e)
			{
				_logger.debug("Unable to close the data file stream.", e);
			}
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
				this.sendNotice(_channel, lookup(query));
			}
			catch (UnknownHostException e)
			{
				if (query.matches("(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"))
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
									this.sendNotice(_channel, line);
								}
							}
						}
						else
						{
							this.sendNotice(_channel, "Unknown host.");
						}
					}
					catch (IOException ioe)
					{
						_logger.debug("Unable to perform whois IP lookup: " + query, ioe);
						this.sendNotice(_channel, "Unable to perform whois IP lookup: " + ioe.getMessage());
					}
				}
				else
				{
					this.sendNotice(_channel, "Unknown host.");
				}
			}
		}
		else
		{
			helpResponse(sender, LOOKUP_CMD);
		}
	}

	/**
	 * Uses Google to correctly spell a sentence.
	 *
	 * @param sender The nick of the person who sent the message
	 * @param spell The sentence to spell.
	 */
	private void spellResponse(String sender, String spell)
	{
		if (_isGoogleKeySet)
		{
			if (spell.length() > 0)
			{
				new Thread(new GoogleSearch(this, _googleKey, _channel, spell, true)).start();
			}
			else
			{
				helpResponse(sender, SPELL_CMD);
			}
		}
		else
		{
			this.sendNotice(_channel, "The Google spelling facility is disabled.");
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
	 * @param cmd The actual time command.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void timeResponse(String sender, String cmd, boolean isPrivate)
	{
		boolean isInvalidTz = false;
		final String tz = ((String) COUNTRIES_MAP.get((cmd.substring(cmd.indexOf(' ') + 1).trim().toUpperCase())));
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
				response = TIME_SDF.format(Calendar.getInstance().getTime()) +
						   tz.substring(tz.indexOf('/') + 1).replace('_', ' ');
			}
		}
		else
		{
			isInvalidTz = true;
			response = "The supported time zones/countries are: " + COUNTRIES_MAP.keySet().toString();
		}

		if (isPrivate)
		{
			this.sendMessage(sender, response);
		}
		else
		{
			if (isInvalidTz)
			{
				this.sendNotice(sender, response);
			}
			else
			{
				this.sendNotice(_channel, response);
			}
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
	 * Responds with the users on a channel.
	 *
	 * @param sender The nick of the person who sent the message.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void usersResponse(String sender, boolean isPrivate)
	{
		final User[] users = this.getUsers(_channel);
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
	 * @param cmd The actual view command.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void viewResponse(String sender, String cmd, boolean isPrivate)
	{
		if (!_entries.isEmpty())
		{
			final int max = _entries.size();
			int i = 0;

			if (!cmd.endsWith(VIEW_ALL_KEYWORD) && (max > MAX_ENTRIES))
			{
				i = max - MAX_ENTRIES;
			}

			for (; i < max; i++)
			{
				send(sender, buildLink(i, (EntryLink) _entries.get(i), true), isPrivate);
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
	 * @param cmd The actual weather command.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	private void weatherResponse(String sender, String cmd, boolean isPrivate)
	{
		new Thread(new Weather(this, sender, cmd.substring(WEATHER_CMD.length()).trim().toUpperCase(), isPrivate)).start();
	}
}
