/*
 * War.java
 *
 * Copyright (c) 2004-2022, Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.Utils;
import org.jetbrains.annotations.NotNull;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.security.SecureRandom;

import static net.thauvin.erik.mobibot.Utils.bold;

/**
 * The War module.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class War extends AbstractModule {
    // Random
    private static final SecureRandom RANDOM = new SecureRandom();
    // War command
    private static final String WAR_CMD = "war";

    private static final String[] DECK = {"A", "K", "Q", "J", "10", "9", "8", "7", "6", "5", "4", "3", "2"};
    private static final String[] SUITS = {"♥", "♠", "♦", "♣"};

    /**
     * The default constructor.
     */
    public War() {
        super();

        commands.add(WAR_CMD);

        help.add("To play war:");
        help.add(Utils.helpFormat("%c " + WAR_CMD));
    }

    @NotNull
    @Override
    public String getName() {
        return "War";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commandResponse(@NotNull final String channel, @NotNull final String cmd, @NotNull final String args,
                                @NotNull final GenericMessageEvent event) {
        int i;
        int y;

        do {
            i = RANDOM.nextInt(DECK.length);
            y = RANDOM.nextInt(DECK.length);

            final String result;
            if (i < y) {
                result = bold("lost") + '.';
            } else if (i > y) {
                result = bold("wins") + '.';
            } else {
                result = bold("tied") + ". This means " + bold("WAR!");
            }

            event.respond("you drew " + bold(DECK[i]) + SUITS[RANDOM.nextInt(SUITS.length)]);
            event.getBot().sendIRC().action(channel, "drew " + bold(DECK[y]) + SUITS[RANDOM.nextInt(SUITS.length)] +
                    " and " + result);
        } while (i == y);
    }
}
