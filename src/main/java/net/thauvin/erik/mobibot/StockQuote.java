/*
 * StockQuote.java
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

import com.Ostermiller.util.CSVParser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;

/**
 * Processes the {@link Commands#STOCK_CMD} command.
 *
 * @author Erik C. Thauvin
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
	private final Mobibot bot;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String sender;

	/**
	 * The stock symbol.
	 */
	private final String symbol;

	/**
	 * Creates a new {@link StockQuote} instance.
	 *
	 * @param bot The bot's instance.
	 * @param sender The nick of the person who sent the message.
	 * @param symbol The stock symbol.
	 */
	public StockQuote(Mobibot bot, String sender, String symbol)
	{
		this.bot = bot;
		this.sender = sender;
		this.symbol = symbol;
	}

	/**
	 * Returns the specified stock quote from Yahoo!
	 */
	public final void run()
	{
		try
		{
			final HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(Mobibot.CONNECT_TIMEOUT);
			client.getHttpConnectionManager().getParams().setSoTimeout(Mobibot.CONNECT_TIMEOUT);

			final GetMethod getMethod = new GetMethod(YAHOO_URL + symbol.toUpperCase());
			client.executeMethod(getMethod);

			final String[][] lines = CSVParser.parse(getMethod.getResponseBodyAsString());

			if (lines.length > 0)
			{
				final String[] quote = lines[0];

				if (quote.length > 0)
				{
					if ((quote.length > 3) && (!"N/A".equalsIgnoreCase(quote[3])))
					{
						bot.send(bot.getChannel(), "Symbol: " + quote[0] + " [" + quote[1] + ']');

						if (quote.length > 5)
						{
							bot.send(bot.getChannel(), "Last Trade: " + quote[2] + " (" + quote[5] + ')');
						}
						else
						{
							bot.send(bot.getChannel(), "Last Trade: " + quote[2]);
						}

						if (quote.length > 4)
						{
							bot.send(sender, "Time: " + quote[3] + ' ' + quote[4]);
						}

						if (quote.length > 6 && !"N/A".equalsIgnoreCase(quote[6]))
						{
							bot.send(sender, "Open: " + quote[6]);
						}

						if (quote.length > 7 && !"N/A".equalsIgnoreCase(quote[7]) && !"N/A".equalsIgnoreCase(quote[8]))
						{
							bot.send(sender, "Day's Range: " + quote[7] + " - " + quote[8]);
						}

						if (quote.length > 9 && !"0".equalsIgnoreCase(quote[9]))
						{
							bot.send(sender, "Volume: " + quote[9]);
						}
					}
					else
					{
						bot.send(sender, "Invalid ticker symbol.");
					}
				}
				else
				{
					bot.send(sender, "No values returned.");
				}
			}
			else
			{
				bot.send(sender, "No data returned.");
			}
		}
		catch (IOException e)
		{
			bot.getLogger().debug("Unable to retrieve stock quote for: " + symbol, e);
			bot.send(sender, "An error has occurred: " + e.getMessage());
		}
	}
}
