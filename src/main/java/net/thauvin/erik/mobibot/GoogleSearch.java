/*
 * @(#)GoogleSearch.java
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


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Performs a Google search or spell checking query.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 * @created Feb 7, 2004
 * @since 1.0
 */
public class GoogleSearch implements Runnable
{
	/**
	 * The tab indent (4 spaces).
	 */
	private static final String TAB_INDENT = "    ";

	/**
	 * The bot.
	 */
	private final Mobibot _bot;

	/**
	 * The search query.
	 */
	private final String _query;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String _sender;

	/**
	 * Creates a new GoogleSearch object.
	 *
	 * @param bot The bot.
	 * @param sender The nick of the person who sent the message.
	 * @param query The Google query
	 */
	public GoogleSearch(Mobibot bot, String sender, String query)
	{
		_bot = bot;
		_sender = sender;
		_query = query;
	}

	/**
	 * Main processing method.
	 */
	public final void run()
	{

		try
		{
			final String query = URLEncoder.encode(_query, "UTF-8");

			final URL url =
					new URL("http://ajax.googleapis.com/ajax/services/search/web?start=0&rsz=small&v=1.0&q=" + query);
			final URLConnection conn = url.openConnection();

			final StringBuffer sb = new StringBuffer();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
			}

			final JSONObject json = new JSONObject(sb.toString());
			final JSONArray ja = json.getJSONObject("responseData").getJSONArray("results");

			for (int i = 0; i < ja.length(); i++)
			{
				final JSONObject j = ja.getJSONObject(i);
				_bot.send(_sender, Mobibot.unescapeXml(j.getString("titleNoFormatting")));
				_bot.send(_sender, TAB_INDENT + j.getString("url"));
			}

			reader.close();

		}
		catch (Exception e)
		{
			_bot.getLogger().warn("Unable to search in Google for: " + _query, e);
			_bot.send(_sender, "An error has occurred: " + e.getMessage());
		}
	}
}
