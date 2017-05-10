/*
 * Dice.java
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
 * The Dice module.
 *
 * @author <a href="http://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2014-04-28
 * @since 1.0
 */
final public class Dice extends AbstractModule {
    /**
     * The dice command.
     */
    public static final String DICE_CMD = "dice";

    /**
     * The default constructor.
     */
    public Dice() {
        commands.add(DICE_CMD);
    }

    /**
     * Rolls the dice.
     *
     * @param bot       The bot's instance.
     * @param sender    The sender.
     * @param args      The command arguments.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    @Override
    public void commandResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        final Random r = new Random();

        int i = r.nextInt(6) + 1;
        int y = r.nextInt(6) + 1;
        final int playerTotal = i + y;

        bot.send(bot.getChannel(),
                sender + " rolled two dice: " + Utils.bold(i) + " and " + Utils.bold(y) + " for a total of " + Utils
                        .bold(playerTotal));

        i = r.nextInt(6) + 1;
        y = r.nextInt(6) + 1;
        final int total = i + y;

        bot.action(
                "rolled two dice: " + Utils.bold(i) + " and " + Utils.bold(y) + " for a total of " + Utils.bold(total));

        if (playerTotal < total) {
            bot.action("wins.");
        } else if (playerTotal > total) {
            bot.action("lost.");
        } else {
            bot.action("tied.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        bot.send(sender, "To roll the dice:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + DICE_CMD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
