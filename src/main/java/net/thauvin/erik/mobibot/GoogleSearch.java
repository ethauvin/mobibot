/*
 * GoogleSearch.java
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Processes the {@link Commands#GOOGLE_CMD} command.
 *
 * @author Erik C. Thauvin
 * @created Feb 7, 2004
 * @since 1.0
 */
class GoogleSearch implements Runnable
{
	/**
	 * The tab indent (4 spaces).
	 */
	private static final String TAB_INDENT = "    ";

	/**
	 * The bot.
	 */
	private final Mobibot bot;

	/**
	 * The Google Custom Search Engine ID.
	 */
	private final String cseCx;

	/**
	 * The search query.
	 */
	private final String query;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String sender;

	/**
	 * Creates a new {@link GoogleSearch} instance.
	 *
	 * @param bot The bot's instance.
	 * @param cseCx The Google Custom Search Engine ID.
	 * @param sender The nick of the person who sent the message.
	 * @param query The Google query
	 */
	public GoogleSearch(final Mobibot bot, final String cseCx, final String sender, final String query)
	{
		this.bot = bot;
		this.cseCx = cseCx;
		this.sender = sender;
		this.query = query;
	}

	/**
	 * Searches Google.
	 */
	public final void run()
	{
		try
		{
			final String query = URLEncoder.encode(this.query, "UTF-8");

			final URL url =
					new URL("https://www.googleapis.com/customsearch/v1?key=" + bot.getGoogleApiKey() + "&cx=" + cseCx
					        + "&q=" + query + "&filter=1&num=5&alt=json");
			final URLConnection conn = url.openConnection();

			final StringBuilder sb = new StringBuilder();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
			}

			final JSONObject json = new JSONObject(sb.toString());
			final JSONArray ja = json.getJSONArray("items");

			for (int i = 0; i < ja.length(); i++)
			{
				final JSONObject j = ja.getJSONObject(i);
				bot.send(sender, Utils.unescapeXml(j.getString("title")));
				bot.send(sender, TAB_INDENT + Utils.green(j.getString("link")));
			}

			reader.close();
		}
		catch (Exception e)
		{
			bot.getLogger().warn("Unable to search in Google for: " + query, e);
			bot.send(sender, "An error has occurred: " + e.getMessage());
		}
	}
}
