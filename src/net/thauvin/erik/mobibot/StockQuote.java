/*
 * @(#)StockQuote.java
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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;


/**
 * Retrieves a stock quote from Yahoo!.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 *
 * @created Feb 7, 2004
 * @since 1.0
 */
public class StockQuote implements Runnable
{
	/**
	 * The Yahoo! stock quote URL.
	 */
	private static final String YAHOO_URL = "http://finance.yahoo.com/d/quotes.csv?&f=snl1d1t1c1oghv&e=.csv&s=";

	/**
	 * The bot.
	 */
	private final Mobibot _bot;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String _sender;

	/**
	 * The stock symbol.
	 */
	private final String _symbol;

	/**
	 * Creates a new StockQuote object.
	 *
	 * @param bot The bot.
	 * @param sender The nick of the person who sent the message.
	 * @param symbol The stock symbol.
	 */
	public StockQuote(Mobibot bot, String sender, String symbol)
	{
		_bot = bot;
		_sender = sender;
		_symbol = symbol;
	}

	/**
	 * Returns the specified stock quote.
	 */
	public void run()
	{
		try
		{
			final HttpClient client = new HttpClient();
			client.setConnectionTimeout(Mobibot.CONNECT_TIMEOUT);
			client.setTimeout(Mobibot.CONNECT_TIMEOUT);

			final GetMethod getMethod = new GetMethod(YAHOO_URL + _symbol.toUpperCase());
			client.executeMethod(getMethod);

			final String[] quote = getMethod.getResponseBodyAsString().split(",");

			if (quote.length > 0)
			{
				if ((quote.length > 3) && (!"\"N/A\"".equalsIgnoreCase(quote[3])))
				{
					_bot.sendNotice(_bot.getChannel(),
									"Symbol: " + quote[0].replaceAll("\"", "") + " [" + quote[1].replaceAll("\"", "") +
									']');

					if (quote.length > 5)
					{
						_bot.sendNotice(_bot.getChannel(), "Last Trade: " + quote[2] + " (" + quote[5] + ')');
					}
					else
					{
						_bot.sendNotice(_bot.getChannel(), "Last Trade: " + quote[2]);
					}

					if (quote.length > 4)
					{
						_bot.sendNotice(_sender,
										"Time: " + quote[3].replaceAll("\"", "") + ' ' + quote[4].replaceAll("\"", ""));
					}

					if (quote.length > 6)
					{
						_bot.sendNotice(_sender, "Open: " + quote[6]);
					}

					if (quote.length > 7)
					{
						_bot.sendNotice(_sender, "Day's Range: " + quote[7] + " - " + quote[8]);
					}

					if (quote.length > 9)
					{
						_bot.sendNotice(_sender, "Volume: " + quote[9]);
					}
				}
				else
				{
					_bot.sendNotice(_sender, "Invalid ticker symbol.");
				}
			}
			else
			{
				_bot.sendNotice(_sender, "No data returned.");
			}
		}
		catch (IOException e)
		{
			_bot.getLogger().debug("Unable to retrieve stock quote for: " + _symbol, e);
			_bot.sendNotice(_sender, "An error has occurred: " + e.getMessage());
		}
	}
}
