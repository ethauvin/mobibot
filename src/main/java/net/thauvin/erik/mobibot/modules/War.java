/*
 * War.java
 *
 * Copyright (c) 2004-2017, Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot.modules;

import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.Utils;

import java.util.Random;

/**
 * The War module.
 *
 * @author <a href="http://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2014-04-28
 * @since 1.0
 */
final public class War extends AbstractModule {
    /**
     * The war command
     */
    public static final String WAR_CMD = "war";

    // The deck of card.
    private static final String[] WAR_DECK =
            new String[]{"Ace", "King", "Queen", "Jack", "10", "9", "8", "7", "6", "5", "4", "3", "2"};

    // The suits for the deck of card.
    private static final String[] WAR_SUITS = new String[]{"Hearts", "Spades", "Diamonds", "Clubs"};

    /**
     * The default constructor.
     */
    public War() {
        commands.add(WAR_CMD);
    }

    /**
     * Plays war.
     *
     * @param bot       The bot's instance.
     * @param sender    The sender.
     * @param args      The command arguments.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    @Override
    public void commandResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        final Random r = new Random();

        int i;
        int y;

        while (true) {
            i = r.nextInt(WAR_DECK.length);
            y = r.nextInt(WAR_DECK.length);

            bot.send(bot.getChannel(),
                    sender + " drew the " + Utils.bold(WAR_DECK[i]) + " of " + WAR_SUITS[r.nextInt(WAR_SUITS.length)]);
            bot.action("drew the " + Utils.bold(WAR_DECK[y]) + " of " + WAR_SUITS[r.nextInt(WAR_SUITS.length)]);

            if (i != y) {
                break;
            }

            bot.send(bot.getChannel(), "This means " + Utils.bold("WAR") + '!');
        }

        if (i < y) {
            bot.action("lost.");
        } else if (i > y) {
            bot.action("wins.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        bot.send(sender, "To play war:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + WAR_CMD));
    }
}
