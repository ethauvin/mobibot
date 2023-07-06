/*
 * War.java
 *
 * Copyright 2004-2023 Erik C. Thauvin (erik@thauvin.net)
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
    private static final String[] CLUBS =
            {"🃑", "🃞", "🃝", "🃛", "🃚", "🃙", "🃘", "🃗", "🃖", "🃕", "🃔", "🃓", "🃒"};
    private static final String[] DIAMONDS =
            {"🃁", "🃎", "🃍", "🃋", "🃊", "🃉", "🃈", "🃇", "🃆", "🃅", "🃄", "🃃", "🃂"};
    private static final String[] HEARTS =
            {"🂱", "🂾", "🂽", "🂻", "🂺", "🂹", "🂸", "🂷", "🂶", "🂵", "🂴", "🂳", "🂲"};
    // Random
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String[] SPADES =
            {"🂡", "🂮", "🂭", "🂫", "🂪", "🂩", "🂨", "🂧", "🂦", "🂥", "🂤", "🂣", "🂢"};
    private static final String[][] DECK = {HEARTS, SPADES, DIAMONDS, CLUBS};
    // War command
    private static final String WAR_CMD = "war";

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
            i = RANDOM.nextInt(HEARTS.length);
            y = RANDOM.nextInt(HEARTS.length);

            final String result;
            if (i < y) {
                result = bold("win");
            } else if (i > y) {
                result = bold("lose");
            } else {
                result = bold("tie") + ". This means " + bold("WAR");
            }

            event.respond(DECK[RANDOM.nextInt(DECK.length)][i] + "  " + DECK[RANDOM.nextInt(DECK.length)][y] +
                    "  » You " + result + '!');

        } while (i == y);
    }
}
