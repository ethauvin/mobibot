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

import com.google.soap.search.GoogleSearchFault;

import net.thauvin.google.GoogleSearchBean;

import org.jibble.pircbot.Colors;


/**
 * Performs a Google search or spell checking query.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 *
 * @created Feb 7, 2004
 * @since 1.0
 */
public class GoogleSearch implements Runnable
{
	/**
	 * The maximum number of Google results to display.
	 */
	private static final int MAX_GOOGLE = 5;

	/**
	 * The Google search bean.
	 */
	private static final GoogleSearchBean GOOGLE_BEAN = new GoogleSearchBean();

	/**
	 * The tab indent (4 spaces).
	 */
	private static final String TAB_INDENT = "    ";

	/**
	 * The bot.
	 */
	private final Mobibot _bot;

	/**
	 * The Google API key.
	 */
	private final String _key;

	/**
	 * The search query.
	 */
	private final String _query;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String _sender;

	/**
	 * Spell Checking query flag.
	 */
	private final boolean _isSpellQuery;

	/**
	 * Creates a new GoogleSearch object.
	 *
	 * @param bot The bot.
	 * @param key The Google API key.
	 * @param sender The nick of the person who sent the message.
	 * @param query The Google query
	 * @param isSpellQuery Set to true if the query is a Spell Checking query
	 */
	public GoogleSearch(Mobibot bot, String key, String sender, String query, boolean isSpellQuery)
	{
		_bot = bot;
		_key = key;
		_sender = sender;
		_query = query;
		_isSpellQuery = isSpellQuery;
	}

	/**
	 * Main processing method.
	 */
	public void run()
	{
		GOOGLE_BEAN.setKey(_key);

		if (_isSpellQuery)
		{
			try
			{
				final String r = GOOGLE_BEAN.getSpellingSuggestion(_query);

				if (Mobibot.isValidString(r))
				{
					_bot.sendNotice(_sender, Mobibot.unescapeXml(r));
				}
				else
				{
					_bot.sendNotice(_sender, "You've just won our spelling bee contest.");
				}
			}
			catch (GoogleSearchFault e)
			{
				_bot.getLogger().warn("Unable to spell: " + _query, e);
				_bot.sendNotice(_sender, "An error has occurred: " + e.getMessage());
			}
		}
		else
		{
			try
			{
				GOOGLE_BEAN.getGoogleSearch(_query, GoogleSearchBean.DEFAULT_START, MAX_GOOGLE,
											GoogleSearchBean.DEFAULT_FILTER, GoogleSearchBean.DEFAULT_RESTRICT,
											GoogleSearchBean.DEFAULT_SAFE_SEARCH, GoogleSearchBean.DEFAULT_LR);

				if (GOOGLE_BEAN.isValidResult())
				{
					for (int i = 0; i < GOOGLE_BEAN.getResultElementsCount(); i++)
					{
						_bot.sendNotice(_sender,
										Mobibot.unescapeXml(GOOGLE_BEAN.getResultElementProperty(i, "title").replaceAll("<([bB]|/[bB])>",
																														Colors.BOLD)));
						_bot.sendNotice(_sender, TAB_INDENT + GOOGLE_BEAN.getResultElementProperty(i, "url"));
					}
				}
			}
			catch (GoogleSearchFault e)
			{
				_bot.getLogger().warn("Unable to search in Google for: " + _query, e);
				_bot.sendNotice(_sender, "An error has occurred: " + e.getMessage());
			}
		}
	}
}
