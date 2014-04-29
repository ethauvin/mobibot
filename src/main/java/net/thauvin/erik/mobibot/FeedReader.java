/*
 * @(#)FeedReader.java
 *
 * Copyright (c) 2004-2014, Erik C. Thauvin (erik@thauvin.net)
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

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Reads a RSS feed.
 *
 * @author Erik C. Thauvin
 * @created Feb 1, 2004
 * @since 1.0
 */
public class FeedReader implements Runnable
{
	/**
	 * The maximum number of feed items to display.
	 */
	private static final int MAX_ITEMS = 5;

	/**
	 * The tab indent (4 spaces).
	 */
	private static final String TAB_INDENT = "    ";

	/**
	 * The bot.
	 */
	private final Mobibot bot;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String sender;

	/**
	 * The URL to fetch.
	 */
	private final String url;

	/**
	 * Creates a new FeedReader object.
	 *
	 * @param bot The bot.
	 * @param sender The nick of the person who sent the message.
	 * @param url The URL to fetch.
	 */
	public FeedReader(Mobibot bot, String sender, String url)
	{
		this.bot = bot;
		this.sender = sender;
		this.url = url;
	}

	/**
	 * Fetches the Feed's items.
	 */
	public final void run()
	{
		final FeedFetcher fetcher = new HttpURLFeedFetcher(bot.getFeedInfoCache());

		try
		{
			final SyndFeed feed = fetcher.retrieveFeed(new URL(url));
			SyndEntry item;
			final List items = feed.getEntries();

			for (int i = 0; (i < items.size()) && (i < MAX_ITEMS); i++)
			{
				item = (SyndEntryImpl) items.get(i);
				bot.send(sender, item.getTitle());
				bot.send(sender, TAB_INDENT + item.getLink());
			}
		}
		catch (MalformedURLException e)
		{
			bot.getLogger().debug("Invalid feed URL.", e);
			bot.send(sender, "The feed URL is invalid.");
		}
		catch (Exception e)
		{
			bot.getLogger().debug("Unable to fetch the feed.", e);
			bot.send(sender, "An error has occurred while fetching the feed: " + e.getMessage());
		}
	}
}
