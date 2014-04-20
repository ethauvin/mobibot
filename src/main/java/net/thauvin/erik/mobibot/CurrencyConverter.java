/*
 * @(#)CurrencyConverter.java
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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

/**
 * Converts various currencies.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 * @created Feb 11, 2004
 * @since 1.0
 */
public class CurrencyConverter implements Runnable
{
	/**
	 * The exchange rates table URL.
	 */
	private static final String EXCHANGE_TABLE_URL = "http://www.ecb.int/stats/eurofxref/eurofxref-daily.xml";

	/**
	 * The exchange rates.
	 */
	private static final Map EXCHANGE_RATES = new TreeMap();

	/**
	 * The rates keyword.
	 */
	private static final String RATES_KEYWORD = "rates";

	/**
	 * The bot.
	 */
	private final Mobibot _bot;

	/**
	 * The actual currency _query.
	 */
	private final String _query;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String _sender;

	/**
	 * The last exchange rates table publication date.
	 */
	private String s_date = "";

	/**
	 * Creates a new CurrencyConverter object.
	 *
	 * @param bot The bot.
	 * @param sender The nick of the person who sent the message.
	 * @param query The currency query.
	 * @param date The current date.
	 */
	public CurrencyConverter(Mobibot bot, String sender, String query, String date)
	{
		_bot = bot;
		_sender = sender;
		_query = query.toLowerCase();

		if (!s_date.equals(date))
		{
			EXCHANGE_RATES.clear();
		}
	}

	// Converts specified currencies.
	public final void run()
	{
		if (EXCHANGE_RATES.isEmpty())
		{
			try
			{
				final SAXBuilder builder = new SAXBuilder();
				builder.setIgnoringElementContentWhitespace(true);

				final Document doc = builder.build(new URL(EXCHANGE_TABLE_URL));
				final Element root = doc.getRootElement();
				final Namespace ns = root.getNamespace("");
				final Element cubeRoot = root.getChild("Cube", ns);
				final Element cubeTime = cubeRoot.getChild("Cube", ns);

				s_date = cubeTime.getAttribute("time").getValue();

				final List cubes = cubeTime.getChildren();
				Element cube;

				for (int i = 0; i < cubes.size(); i++)
				{
					cube = (Element) cubes.get(i);
					EXCHANGE_RATES.put(cube.getAttribute("currency").getValue(), cube.getAttribute("rate").getValue());
				}

				EXCHANGE_RATES.put("EUR", "1");
			}
			catch (JDOMException e)
			{
				_bot.getLogger().debug("Unable to parse the exchange rates table.", e);
				_bot.send(_sender, "An error has occurred while parsing the exchange rates table.");
			}
			catch (IOException e)
			{
				_bot.getLogger().debug("Unable to fetch the exchange rates table.", e);
				_bot.send(_sender, "An error has occurred while fetching the exchange rates table:  " + e.getMessage());
			}
		}

		if (!EXCHANGE_RATES.isEmpty())
		{
			if (_query.matches("\\d+([,\\d]+)?(\\.\\d+)? [a-z]{3}+ to [a-z]{3}+"))
			{
				final String[] cmds = _query.split(" ");

				if (cmds.length == 4)
				{
					if (cmds[3].equals(cmds[1]))
					{
						_bot.send(_sender, "You're kidding, right?");
					}
					else
					{
						try
						{
							final double amt = Double.parseDouble(cmds[0].replaceAll(",", ""));
							final double from = Double.parseDouble((String) EXCHANGE_RATES.get(cmds[1].toUpperCase()));
							final double to = Double.parseDouble((String) EXCHANGE_RATES.get(cmds[3].toUpperCase()));

							_bot.send(_bot.getChannel(),
							          NumberFormat.getCurrencyInstance(Locale.US).format(amt).substring(1) + ' ' +
							          cmds[1].toUpperCase() + " = " +
							          NumberFormat.getCurrencyInstance(Locale.US).format((amt * to) / from).substring(1)
							          + ' ' + cmds[3].toUpperCase()
							);
						}
						catch (NullPointerException ignored)
						{
							_bot.send(_sender, "The supported currencies are: " + EXCHANGE_RATES.keySet().toString());
						}
					}
				}
			}
			else if (_query.equals(RATES_KEYWORD))
			{
				_bot.send(_sender, "Last Update: " + s_date);

				final Iterator it = EXCHANGE_RATES.keySet().iterator();
				String rate;

				final StringBuffer buff = new StringBuffer(0);

				while (it.hasNext())
				{
					rate = (String) it.next();
					if (buff.length() > 0)
					{
						buff.append(", ");
					}
					buff.append(rate).append(": ").append(EXCHANGE_RATES.get(rate));
				}

				_bot.send(_sender, buff.toString());

			}
			else
			{
				_bot.helpResponse(_sender, Mobibot.CURRENCY_CMD + ' ' + _query);
				_bot.send(_sender, "The supported currencies are: " + EXCHANGE_RATES.keySet().toString());
			}
		}
		else
		{
			_bot.getLogger().debug("The exchange rate table is empty.");
			_bot.send(_sender, "Sorry, but the exchange rate table is empty.");
		}
	}
}
