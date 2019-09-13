/*
 * Joke.java
 *
 * Copyright (c) 2004-2019, Erik C. Thauvin (erik@thauvin.net)
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
import net.thauvin.erik.mobibot.msg.Message;
import net.thauvin.erik.mobibot.msg.PublicMessage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * The Joke module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2014-04-20
 * @since 1.0
 */
public final class Joke extends ThreadedModule {
    // The joke command.
    private static final String JOKE_CMD = "joke";
    // The ICNDB URL.
    private static final String JOKE_URL =
        "http://api.icndb.com/jokes/random?escape=javascript&exclude=[explicit]&limitTo=[nerdy]";

    /**
     * Creates a new {@link Joke} instance.
     */
    public Joke() {
        super();
        commands.add(JOKE_CMD);
    }

    /**
     * Retrieves a random joke.
     *
     * @return The {@link Message} containing the new joke.
     * @throws ModuleException If an error occurs while retrieving a new joke.
     */
    static Message randomJoke() throws ModuleException {
        try {
            final URL url = new URL(JOKE_URL);
            final URLConnection conn = url.openConnection();

            final StringBuilder sb = new StringBuilder();
            try (final BufferedReader reader =
                     new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                final JSONObject json = new JSONObject(sb.toString());

                //noinspection RegExpRedundantEscape
                return new PublicMessage(
                    json.getJSONObject("value").get("joke").toString().replace("\\'", "'")
                        .replace("\\\"", "\""));
            }
        } catch (Exception e) {
            throw new ModuleException("randomJoke()", "An error has occurred retrieving a random joke.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commandResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        new Thread(() -> run(bot, sender, args)).start();
    }

    /**
     * Returns a random joke from <a href="http://www.icndb.com/">The Internet Chuck Norris Database</a>.
     */
    @Override
    void run(final Mobibot bot, final String sender, final String arg) {
        try {
            bot.send(Utils.cyan(randomJoke().getMessage()));
        } catch (ModuleException e) {
            bot.getLogger().warn(e.getDebugMessage(), e);
            bot.send(sender, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        bot.send(sender, "To retrieve a random joke:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + JOKE_CMD));
    }
}
