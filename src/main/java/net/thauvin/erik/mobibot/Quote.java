/*
 * @(#)Quote.java
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

import org.jibble.pircbot.Colors;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Processes the {@link Commands#QUOTE_CMD} command.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created 2014-04-20
 * @since 1.0
 */
public class Quote implements Runnable
{

	/**
	 * The I Heart Quotes URL.
	 */
	private static final String QUOTE_URL =
			"http://www.iheartquotes.com/api/v1/random?format=json&max_lines=1&source=esr+humorix_misc+humorix_stories+joel_on_software+macintosh+math+mav_flame+osp_rules+paul_graham+prog_style+subversion";

	/**
	 * The bot's instance.
	 */
	private final Mobibot bot;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String sender;

	/**
	 * Creates a new {@link StockQuote} instance.
	 *
	 * @param bot The bot's instance.
	 * @param sender The nick of the person who sent the message.
	 */
	public Quote(Mobibot bot, String sender)
	{
		this.bot = bot;
		this.sender = sender;
	}

	/**
	 * Returns a random quote from <a href="http://iheartquotes.com/">I Heart Quote</a>
	 */
	public final void run()
	{
		try
		{
			final URL url = new URL(QUOTE_URL);
			final URLConnection conn = url.openConnection();

			final StringBuilder sb = new StringBuilder();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
			}

			final JSONObject json = new JSONObject(sb.toString());

			bot.send(bot.getChannel(), Colors.CYAN + json.getString("quote") + Colors.CYAN);

			reader.close();
		}
		catch (Exception e)
		{
			bot.getLogger().warn("Unable to retrieve random quote.", e);
			bot.send(sender, "An error has occurred: " + e.getMessage());
		}
	}
}