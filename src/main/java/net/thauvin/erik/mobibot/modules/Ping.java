/*
 * Ping.java
 *
 * Copyright (c) 2004-2020, Erik C. Thauvin (erik@thauvin.net)
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

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * The Ping module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2016-07-02
 * @since 1.0
 */
public class Ping extends AbstractModule {
    /**
     * The ping responses.
     */
    static final List<String> PINGS =
            Arrays.asList(
                    "is barely alive.",
                    "is trying to stay awake.",
                    "has gone fishing.",
                    "is somewhere over the rainbow.",
                    "has fallen and can't get up.",
                    "is running. You better go chase it.",
                    "has just spontaneously combusted.",
                    "is talking to itself... don't interrupt. That's rude.",
                    "is bartending at an AA meeting.",
                    "is hibernating.",
                    "is saving energy: apathetic mode activated.",
                    "is busy. Go away!");
    /**
     * The ping command.
     */
    private static final String PING_CMD = "ping";

    /**
     * The default constructor.
     */
    public Ping() {
        super();

        commands.add(PING_CMD);

        help.add("To ping the bot:");
        help.add(Utils.helpIndent("%c " + PING_CMD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commandResponse(final Mobibot bot,
                                final String sender,
                                final String cmd,
                                final String args,
                                final boolean isPrivate) {
        final SecureRandom r = new SecureRandom();
        bot.action(PINGS.get(r.nextInt(PINGS.size())));
    }
}
