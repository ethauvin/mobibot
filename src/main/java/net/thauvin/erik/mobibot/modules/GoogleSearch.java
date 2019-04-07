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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The GoogleSearch module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Feb 7, 2004
 * @since 1.0
 */
public final class GoogleSearch extends ThreadedModule {
    /**
     * The google command.
     */
    public static final String GOOGLE_CMD = "google";

    // The Google API Key property.
    private static final String GOOGLE_API_KEY_PROP = "google-api-key";
    // The Google Custom Search Engine ID property.
    private static final String GOOGLE_CSE_KEY_PROP = "google-cse-cx";

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
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        if (isEnabled()) {
            bot.send(sender, "To search Google:");
            bot.send(sender, bot.helpIndent(bot.getNick() + ": " + GOOGLE_CMD + " <query>"));
        } else {
            bot.send(sender, "The Google searching facility is disabled.");
        }
    }
    
    /**
     * Searches Google.
     */
    @SuppressFBWarnings(value = {"URLCONNECTION_SSRF_FD", "REC_CATCH_EXCEPTION"})
    void run(final Mobibot bot, final String sender, final String query) {
        try {
            final String q = URLEncoder.encode(query, "UTF-8");

            final URL url =
                new URL("https://www.googleapis.com/customsearch/v1?key="
                    + properties.get(GOOGLE_API_KEY_PROP)
                    + "&cx="
                    + properties.get(GOOGLE_CSE_KEY_PROP)
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
                    bot.send(sender, Utils.unescapeXml(j.getString("title")));
                    bot.send(sender, TAB_INDENT + Utils.green(j.getString("link")));
                }
            }
        } catch (Exception e) {
            bot.getLogger().warn("Unable to search in Google for: " + query, e);
            bot.send(sender, "An error has occurred searching in Google: " + e.getMessage());
        }
    }
}
