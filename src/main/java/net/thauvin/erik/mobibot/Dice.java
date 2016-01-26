/*
 * Dice.java
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

import java.util.Random;

/**
 * Processes the {@link Commands#DICE_CMD} command.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created 2014-04-28
 * @since 1.0
 */
final class Dice
{
	/**
	 * Disables the default constructor.
	 *
	 * @throws UnsupportedOperationException If the constructor is called.
	 */
	private Dice()
			throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Illegal constructor call.");
	}

	/**
	 * Rolls the dice.
	 *
	 * @param bot The bot's instance.
	 * @param sender The sender's nickname.
	 */
	public static void roll(final Mobibot bot, final String sender)
	{
		final Random r = new Random();

		int i = r.nextInt(6) + 1;
		int y = r.nextInt(6) + 1;
		final int playerTotal = i + y;

		bot.send(bot.getChannel(),
		         sender + " rolled two dice: " + Utils.reverseColor(i) + " and " + Utils.reverseColor(y) + " for a total of " + Utils
				         .bold(playerTotal));

		i = r.nextInt(6) + 1;
		y = r.nextInt(6) + 1;
		final int total = i + y;

		bot.action(
				"rolled two dice: " + Utils.reverseColor(i) + " and " + Utils.reverseColor(y) + " for a total of " + Utils.bold(total));

		if (playerTotal < total)
		{
			bot.action("wins.");
		}
		else if (playerTotal > total)
		{
			bot.action("lost.");
		}
		else
		{
			bot.action("tied.");
		}
	}
}