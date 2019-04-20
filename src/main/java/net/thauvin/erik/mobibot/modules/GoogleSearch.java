/*
 * GoogleSearch.java
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.Utils;
import net.thauvin.erik.mobibot.msg.Message;
import net.thauvin.erik.mobibot.msg.NoticeMessage;
import org.jibble.pircbot.Colors;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * The GoogleSearch module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Feb 7, 2004
 * @since 1.0
 */
public final class GoogleSearch extends ThreadedModule {
    // The Google API Key property.
    static final String GOOGLE_API_KEY_PROP = "google-api-key";
    // The Google Custom Search Engine ID property.
    static final String GOOGLE_CSE_KEY_PROP = "google-cse-cx";
    // The Google command
    private static final String GOOGLE_CMD = "google";
    // The tab indent (4 spaces).
    private static final String TAB_INDENT = "    ";

    /**
     * Creates a new {@link GoogleSearch} instance.
     */
    public GoogleSearch() {
        commands.add(GOOGLE_CMD);
        properties.put(GOOGLE_API_KEY_PROP, "");
        properties.put(GOOGLE_CSE_KEY_PROP, "");
    }

    /**
     * Performs a search on Google.
     *
     * @param query  The search query.
     * @param apiKey The Google API key.
     * @param cseKey The Google CSE key.
     * @return The {@link Message} array containing the search results.
     * @throws ModuleException If an error occurs while searching.
     */
    @SuppressFBWarnings(value = {"URLCONNECTION_SSRF_FD", "REC_CATCH_EXCEPTION"})
    static ArrayList<Message> searchGoogle(final String query, final String apiKey, final String cseKey)
        throws ModuleException {
        if (!Utils.isValidString(apiKey) || !Utils.isValidString(cseKey)) {
            throw new ModuleException(Utils.capitalize(GOOGLE_CMD) + " is disabled. The API keys are missing.");
        }

        if (Utils.isValidString(query)) {
            final ArrayList<Message> results = new ArrayList<>();
            try {
                final String q = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());

                final URL url =
                    new URL("https://www.googleapis.com/customsearch/v1?key="
                        + apiKey
                        + "&cx="
                        + cseKey
                        + "&q="
                        + q
                        + "&filter=1&num=5&alt=json");
                final URLConnection conn = url.openConnection();

                final StringBuilder sb = new StringBuilder();
                try (final BufferedReader reader =
                         new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    final JSONObject json = new JSONObject(sb.toString());
                    final JSONArray ja = json.getJSONArray("items");

                    for (int i = 0; i < ja.length(); i++) {
                        final JSONObject j = ja.getJSONObject(i);
                        results.add(new NoticeMessage(Utils.unescapeXml(j.getString("title"))));
                        results.add(
                            new NoticeMessage(TAB_INDENT + j.getString("link"), Colors.DARK_GREEN));
                    }
                }
            } catch (IOException e) {
                throw new ModuleException("searchGoogle(" + query + ')', "An error has occurred searching Google.", e);
            }

            return results;
        } else {
            throw new ModuleException("Invalid query.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        if (isEnabled()) {
            bot.send(sender, "To search Google:");
            bot.send(sender, bot.helpIndent(bot.getNick() + ": " + GOOGLE_CMD + " <query>"));
        } else {
            bot.send(sender, "The Google search module is disabled.");
        }
    }

    /**
     * Searches Google.
     */
    void run(final Mobibot bot, final String sender, final String query) {
        if (Utils.isValidString(query)) {
            try {
                final ArrayList<Message> results = searchGoogle(query, properties.get(GOOGLE_API_KEY_PROP),
                    properties.get(GOOGLE_CSE_KEY_PROP));
                for (final Message msg : results) {
                    bot.send(sender, msg);
                }
            } catch (ModuleException e) {
                bot.getLogger().warn(e.getDebugMessage(), e);
                bot.send(sender, e.getMessage());
            }
        } else {
            helpResponse(bot, sender, query, true);
        }
    }
}
