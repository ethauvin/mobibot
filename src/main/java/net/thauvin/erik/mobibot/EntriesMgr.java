/*
 * EntriesMgr.java
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

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Manages the feed entries.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created 2014-04-28
 * @since 1.0
 */
public class EntriesMgr
{
	/**
	 * The name of the file containing the current entries.
	 */
	public static final String CURRENT_XML = "current.xml";

	/**
	 * The name of the file containing the backlog entries.
	 */
	public static final String NAV_XML = "nav.xml";

	/**
	 * The .xml extension
	 */
	public static final String XML_EXT = ".xml";

	/**
	 * The maximum number of backlogs to keep.
	 */
	private static final int MAX_BACKLOGS = 10;

	/**
	 * Disables the default constructor.
	 *
	 * @throws UnsupportedOperationException If the constructor is called.
	 */
	private EntriesMgr()
			throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Illegal constructor call.");
	}

	/**
	 * Loads the backlogs.
	 *
	 * @param file The file containing the backlogs.
	 * @param history The history list.
	 *
	 * @throws FileNotFoundException If the file was not found.
	 * @throws FeedException If an error occurred while reading the feed.
	 */
	public static void loadBacklogs(String file, List<String> history)
			throws FileNotFoundException, FeedException
	{
		history.clear();

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
				history.add(item.getTitle());
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
	 * @param channel The channel
	 * @param entries The entries.
	 *
	 * @return The feed's last published date.
	 *
	 * @throws java.io.FileNotFoundException If the file was not found.
	 * @throws com.sun.syndication.io.FeedException If an error occurred while reading the feed.
	 */
	@SuppressWarnings("unchecked")
	public static String loadEntries(String file, String channel, List<EntryLink> entries)
			throws FileNotFoundException, FeedException
	{
		entries.clear();

		final SyndFeedInput input = new SyndFeedInput();

		String today;
		InputStreamReader reader = null;

		try
		{
			reader = new InputStreamReader(new FileInputStream(new File(file)));

			final SyndFeed feed = input.build(reader);

			today = Utils.ISO_SDF.format(feed.getPublishedDate());

			final List items = feed.getEntries();
			SyndEntry item;
			SyndContent description;
			String[] comments;
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
				                      channel,
				                      item.getPublishedDate(),
				                      item.getCategories());
				description = item.getDescription();
				comments = description.getValue().split("<br/>");

				int split;
				for (final String comment : comments)
				{
					split = comment.indexOf(": ");

					if (split != -1)
					{
						entry.addComment(comment.substring(split + 2).trim(), comment.substring(0, split).trim());
					}
				}

				entries.add(entry);
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

		return today;
	}

	/**
	 * Saves the entries.
	 *
	 * @param bot The bot object.
	 * @param entries The entries array.
	 * @param history The history array.
	 * @param isDayBackup Set the true if the daily backup file should also be created.
	 */
	public static void saveEntries(Mobibot bot, List<EntryLink> entries, List<String> history, boolean isDayBackup)
	{
		if (bot.getLogger().isDebugEnabled())
		{
			bot.getLogger().debug("Saving the feeds...");
		}

		if (Utils.isValidString(bot.getLogsDir()) && Utils.isValidString(bot.getWeblogUrl()))
		{
			FileWriter fw = null;

			try
			{
				fw = new FileWriter(new File(bot.getLogsDir() + CURRENT_XML));

				SyndFeed rss = new SyndFeedImpl();
				rss.setFeedType("rss_2.0");
				rss.setTitle(bot.getChannel() + " IRC Links");
				rss.setDescription("Links from " + bot.getIrcServer() + " on " + bot.getChannel());
				rss.setLink(bot.getWeblogUrl());
				rss.setPublishedDate(Calendar.getInstance().getTime());
				rss.setLanguage("en");

				EntryLink entry;
				StringBuffer buff;
				EntryComment comment;
				final List<SyndEntry> items = new ArrayList<SyndEntry>(0);
				SyndEntry item;
				SyndContent description;

				for (int i = (entries.size() - 1); i >= 0; --i)
				{
					entry = entries.get(i);

					buff = new StringBuffer(
							"Posted by <b>" + entry.getNick() + "</b> on <a href=\"irc://" + bot.getIrcServer() + '/'
							+ entry.getChannel() + "\"><b>" + entry.getChannel() + "</b></a>");

					if (entry.getCommentsCount() > 0)
					{
						buff.append(" <br/><br/>");

						final EntryComment[] comments = entry.getComments();

						for (int j = 0; j < comments.length; j++)
						{
							comment = comments[j];

							if (j > 0)
							{
								buff.append(" <br/>");
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
					item.setAuthor(
							bot.getChannel().substring(1) + '@' + bot.getIrcServer() + " (" + entry.getNick() + ')');
					item.setCategories(entry.getTags());

					items.add(item);
				}

				rss.setEntries(items);

				if (bot.getLogger().isDebugEnabled())
				{
					bot.getLogger().debug("Writing the entries feed.");
				}

				final SyndFeedOutput output = new SyndFeedOutput();
				output.output(rss, fw);
				fw.close();

				fw = new FileWriter(new File(bot.getLogsDir() + bot.getToday() + XML_EXT));
				output.output(rss, fw);

				if (isDayBackup)
				{
					if (Utils.isValidString(bot.getBacklogsUrl()))
					{
						if (history.indexOf(bot.getToday()) == -1)
						{
							history.add(bot.getToday());

							while (history.size() > MAX_BACKLOGS)
							{
								history.remove(0);
							}
						}

						fw.close();
						fw = new FileWriter(new File(bot.getLogsDir() + NAV_XML));
						rss = new SyndFeedImpl();
						rss.setFeedType("rss_2.0");
						rss.setTitle(bot.getChannel() + " IRC Links Backlogs");
						rss.setDescription("Backlogs of Links from " + bot.getIrcServer() + " on " + bot.getChannel());
						rss.setLink(bot.getBacklogsUrl());
						rss.setPublishedDate(Calendar.getInstance().getTime());

						String date;
						items.clear();

						for (int i = (history.size() - 1); i >= 0; --i)
						{
							date = history.get(i);

							item = new SyndEntryImpl();
							item.setLink(bot.getBacklogsUrl() + date + ".xml");
							item.setTitle(date);
							description = new SyndContentImpl();
							description.setValue("Links for " + date);
							item.setDescription(description);

							items.add(item);
						}

						rss.setEntries(items);

						if (bot.getLogger().isDebugEnabled())
						{
							bot.getLogger().debug("Writing the backlog feed.");
						}

						output.output(rss, fw);
					}
					else
					{
						bot.getLogger().warn("Unable to generate the backlogs feed. No property configured.");
					}
				}
			}
			catch (Exception e)
			{
				bot.getLogger().warn("Unable to generate the entries feed.", e);
			}
			finally
			{
				try
				{
					if (fw != null)
					{
						fw.close();
					}
				}
				catch (Exception ignore)
				{
					; // Do nothing
				}
			}
		}
		else
		{
			bot.getLogger()
					.warn("Unable to generate the entries feed. At least one of the required property is missing.");
		}
	}
}