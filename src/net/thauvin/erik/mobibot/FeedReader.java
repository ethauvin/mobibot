/*
 * @(#)FeedReader.java
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

import org.crazybob.rss.Channel;
import org.crazybob.rss.Item;
import org.crazybob.rss.Parser;
import org.crazybob.rss.UrlLoader;
import org.crazybob.rss.UrlLoader.Response;

import org.jdom.JDOMException;

import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;

import java.util.Iterator;
import java.util.List;


/**
 * Reads a RSS feed.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 *
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
	private final Mobibot _bot;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String _sender;

	/**
	 * The URL to fetch.
	 */
	private final String _url;

	/**
	 * Creates a new FeedReader object.
	 *
	 * @param bot The bot.
	 * @param sender The nick of the person who sent the message.
	 * @param url The URL to fetch.
	 */
	public FeedReader(Mobibot bot, String sender, String url)
	{
		_bot = bot;
		_sender = sender;
		_url = url;
	}

	/**
	 * Fetches the Feed's items.
	 */
	public void run()
	{
		List items = null;

		try
		{
			final Response response = new UrlLoader().load(_url, _bot.getFeedLastMod());

			if (response != null)
			{
				_bot.setFeedLastMod(response.getLastModified());

				final Channel chan = new Parser().parse(new SAXBuilder().build(new StringReader(response.getBody())));
				items = chan.getItems();
				_bot.setFeedItems(items);
			}
		}
		catch (JDOMException e)
		{
			_bot.getLogger().debug("Unable to parse the feed.", e);
			_bot.sendNotice(_sender, "An error has occurred while parsing the feed.");
		}
		catch (IOException e)
		{
			_bot.getLogger().debug("Unable to fetch the feed.", e);
			_bot.sendNotice(_sender, "An error has occurred while fetching the feed: " + e.getMessage());
		}

		if (items == null)
		{
			items = _bot.getFeedItems();
		}

		if ((items != null) && (!items.isEmpty()))
		{
			Item item;
			int i = 0;
			final Iterator it = items.iterator();

			while (it.hasNext() && (i < MAX_ITEMS))
			{
				item = (Item) it.next();
				_bot.sendNotice(_sender, item.getTitle());
				_bot.sendNotice(_sender, TAB_INDENT + '<' + item.getLink() + '>');

				i++;
			}

			if (_bot.getFeedLastMod().length() > 0)
			{
				_bot.sendNotice(_sender, "Last Updated: " + _bot.getFeedLastMod());
			}
		}
	}
}
