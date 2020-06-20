/*
 * GoogleSearch.java
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.Utils;
import net.thauvin.erik.mobibot.msg.Message;
import net.thauvin.erik.mobibot.msg.NoticeMessage;
import org.apache.commons.lang3.StringUtils;
import org.jibble.pircbot.Colors;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The GoogleSearch module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Feb 7, 2004
 * @since 1.0
 */
public final class GoogleSearch extends ThreadedModule {
    // Google API Key property
    static final String GOOGLE_API_KEY_PROP = "google-api-key";
    // Google Custom Search Engine ID property
    static final String GOOGLE_CSE_KEY_PROP = "google-cse-cx";
    // Google command
    private static final String GOOGLE_CMD = "google";

    /**
     * Creates a new {@link GoogleSearch} instance.
     */
    public GoogleSearch(final Mobibot bot) {
        super(bot);

        commands.add(GOOGLE_CMD);

        help.add("To search Google:");
        help.add(Utils.helpIndent("%c " + GOOGLE_CMD + " <query>"));

        initProperties(GOOGLE_API_KEY_PROP, GOOGLE_CSE_KEY_PROP);
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
    @SuppressFBWarnings({ "URLCONNECTION_SSRF_FD", "REC_CATCH_EXCEPTION" })
    @SuppressWarnings(("PMD.AvoidInstantiatingObjectsInLoops"))
    static List<Message> searchGoogle(final String query, final String apiKey, final String cseKey)
            throws ModuleException {
        if (StringUtils.isBlank(apiKey) || StringUtils.isBlank(cseKey)) {
            throw new ModuleException(StringUtils.capitalize(GOOGLE_CMD) + " is disabled. The API keys are missing.");
        }

        if (StringUtils.isNotBlank(query)) {
            final ArrayList<Message> results = new ArrayList<>();
            try {
                final URL url =
                        new URL("https://www.googleapis.com/customsearch/v1?key="
                                + apiKey
                                + "&cx="
                                + cseKey
                                + "&q="
                                + Utils.encodeUrl(query)
                                + "&filter=1&num=5&alt=json");

                final JSONObject json = new JSONObject(Utils.urlReader(url));
                final JSONArray ja = json.getJSONArray("items");

                for (int i = 0; i < ja.length(); i++) {
                    final JSONObject j = ja.getJSONObject(i);
                    results.add(new NoticeMessage(Utils.unescapeXml(j.getString("title"))));
                    results.add(
                            new NoticeMessage(Utils.helpIndent(j.getString("link"), false), Colors.DARK_GREEN));
                }
            } catch (IOException e) {
                throw new ModuleException("searchGoogle(" + query + ')', "An error has occurred searching Google.", e);
            }

            return results;
        } else {
            throw new ModuleException("Invalid query. Please try again.");
        }
    }

    /**
     * Searches Google.
     */
    @Override
    void run(final String sender, final String cmd, final String query, final boolean isPrivate) {
        if (StringUtils.isNotBlank(query)) {
            try {
                final List<Message> results = searchGoogle(query, properties.get(GOOGLE_API_KEY_PROP),
                                                           properties.get(GOOGLE_CSE_KEY_PROP));
                for (final Message msg : results) {
                    bot.send(sender, msg);
                }
            } catch (ModuleException e) {
                bot.getLogger().warn(e.getDebugMessage(), e);
                bot.send(sender, e.getMessage(), isPrivate);
            }
        } else {
            helpResponse(sender, isPrivate);
        }
    }
}
