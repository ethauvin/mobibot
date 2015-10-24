/*
 * War.java
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

import java.util.Random;

/**
 * Processes the {@link Commands#WAR_CMD} command.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created 2014-04-28
 * @since 1.0
 */
class War
{
	/**
	 * The deck of card for the {@link net.thauvin.erik.mobibot.Commands#WAR_CMD war} command.
	 */
	private static final String[] WAR_DECK =
			new String[]{"Ace", "King", "Queen", "Jack", "10", "9", "8", "7", "6", "5", "4", "3", "2"};

	/**
	 * The suits for the deck of card for the {@link Commands#WAR_CMD war} command.
	 */
	private static final String[] WAR_SUITS = new String[]{"Hearts", "Spades", "Diamonds", "Clubs"};

	/**
	 * Disables the default constructor.
	 *
	 * @throws UnsupportedOperationException If the constructor is called.
	 */
	private War()
			throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Illegal constructor call.");
	}

	/**
	 * Plays war.
	 *
	 * @param bot The bot's instance.
	 * @param sender The sender's nickname.
	 */
	public static void play(Mobibot bot, String sender)
	{
		final Random r = new Random();

		int i;
		int y;

		while (true)
		{
			i = r.nextInt(WAR_DECK.length);
			y = r.nextInt(WAR_DECK.length);

			bot.send(bot.getChannel(),
			         sender + " drew the " + Utils.reverseColor(WAR_DECK[i]) + " of " + WAR_SUITS[r.nextInt(WAR_SUITS.length)]);
			bot.action("drew the " + Utils.reverseColor(WAR_DECK[y]) + " of " + WAR_SUITS[r.nextInt(WAR_SUITS.length)]);

			if (i != y)
			{
				break;
			}

			bot.send(bot.getChannel(), "This means " + Utils.bold("WAR") + '!');
		}

		if (i < y)
		{
			bot.action("lost.");
		}
		else if (i > y)
		{
			bot.action("wins.");
		}
		else
		{
			bot.action("tied.");
		}
	}
}