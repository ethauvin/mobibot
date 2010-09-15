/*
 * @(#)Identica.java
 *
 * Copyright (C) 2010 Erik C. Thauvin
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

import twitter4j.internal.http.BASE64Encoder;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.internal.org.json.XML;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * The <code>Identica</code> class.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @version $Revision$, $Date$
 * @created Sep 14, 2010
 * @since 1.0
 */
public class Identica implements Runnable
{
	/**
	 * The bot.
	 */
	private final Mobibot _bot;

	/**
	 * The identi.ca user.
	 */
	private final String _user;

	/**
	 * The identi.ca password.
	 */
	private final String _pwd;
	/**
	 * The identi.ca message.
	 */
	private final String _message;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String _sender;

	/**
	 * Creates a new identi.ca object.
	 *
	 * @param bot The bot.
	 * @param sender The nick of the person who sent the message.
	 * @param user The identi.ca user.
	 * @param pwd The identi.ca passwword.
	 * @param message The identi.ca message.
	 */
	public Identica(Mobibot bot, String sender, String user, String pwd, String message)
	{
		_bot = bot;
		_sender = sender;
		_user = user;
		_pwd = pwd;
		_message = message;
	}

	public final void run()
	{
		try
		{
			final String auth = _user + ':' + _pwd;

			final URL url = new URL("http://identi.ca/api/statuses/update.xml");
			final URLConnection conn = url.openConnection();
			conn.setRequestProperty("Authorization", "Basic " + BASE64Encoder.encode(auth.getBytes()));
			conn.setDoOutput(true);

			final OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

			writer.write("status=" + URLEncoder.encode(_message + " (" + _sender + ')', "UTF-8"));
			writer.flush();

			final StringBuffer sb = new StringBuffer();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
			}

			final JSONObject response = XML.toJSONObject(sb.toString());
			final int id = response.getJSONObject("status").getInt("id");

			_bot.send(_sender, "You message was posted to http://identi.ca/notice/" + id);

			writer.close();
			reader.close();
		}
		catch (Exception e)
		{
			_bot.getLogger().warn("Unable to post to identi.ca: " + _message, e);
			_bot.send(_sender, "An error has occurred: " + e.getMessage());
		}
	}
}