/*
 * War.java
 *
 * Copyright (c) 2004-2021, Erik C. Thauvin (erik@thauvin.net)
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
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

import static net.thauvin.erik.mobibot.Utils.bold;

/**
 * The War module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2014-04-28
 * @since 1.0
 */
public final class War extends AbstractModule {
    // Random
    private static final SecureRandom RANDOM = new SecureRandom();
    // War command
    private static final String WAR_CMD = "war";
    // Deck of card
    private static final String[] WAR_DECK =
            new String[]{ "Ace", "King", "Queen", "Jack", "10", "9", "8", "7", "6", "5", "4", "3", "2" };
    // Suits for the deck of card
    private static final String[] WAR_SUITS = new String[]{ "Hearts", "Spades", "Diamonds", "Clubs" };

    /**
     * The default constructor.
     */
    public War(final Mobibot bot) {
        super(bot);

        commands.add(WAR_CMD);

        help.add("To play war:");
        help.add(Utils.helpFormat("%c " + WAR_CMD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commandResponse(@NotNull final String sender,
                                @NotNull final String cmd,
                                @NotNull final String args,
                                final boolean isPrivate) {
        int i;
        int y;

        while (true) {
            i = RANDOM.nextInt(WAR_DECK.length);
            y = RANDOM.nextInt(WAR_DECK.length);

            getBot().send(sender + " drew the " + bold(WAR_DECK[i]) + " of "
                          + bold(WAR_SUITS[RANDOM.nextInt(WAR_SUITS.length)]));
            getBot().action("drew the " + bold(WAR_DECK[y]) + " of "
                            + bold(WAR_SUITS[RANDOM.nextInt(WAR_SUITS.length)]));

            if (i != y) {
                break;
            }

            getBot().send("This means " + bold("WAR") + '!');
        }

        if (i < y) {
            getBot().action("lost.");
        } else {
            getBot().action("wins.");
        }
    }
}
