/*
 * StockQuote.java
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
import net.thauvin.erik.mobibot.msg.ErrorMessage;
import net.thauvin.erik.mobibot.msg.Message;
import net.thauvin.erik.mobibot.msg.NoticeMessage;
import net.thauvin.erik.mobibot.msg.PublicMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The StockQuote module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Feb 7, 2004
 * @since 1.0
 */
public final class StockQuote extends ThreadedModule {
    /**
     * The Alpha Advantage property key.
     */
    static final String ALPHAVANTAGE_API_KEY_PROP = "alphavantage-api-key";
    /**
     * The Invalid Symbol error string.
     */
    static final String INVALID_SYMBOL = "Invalid symbol.";
    // Alpha Advantage URL
    private static final String ALAPHAVANTAGE_URL = "https://www.alphavantage.co/query?function=";
    // Quote command
    private static final String STOCK_CMD = "stock";

    /**
     * Creates a new {@link StockQuote} instance.
     */
    public StockQuote() {
        super();
        commands.add(STOCK_CMD);

        help.add("To retrieve a stock quote:");
        help.add(Utils.helpIndent("%c " + STOCK_CMD + " <symbol|keywords>"));

        properties.put(ALPHAVANTAGE_API_KEY_PROP, "");
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
                        justification = "false positive?")
    private static JSONObject getJsonResponse(final Response response, final String debugMessage)
            throws IOException, ModuleException {
        if (response.isSuccessful()) {
            if (response.body() != null) {
                final JSONObject json = new JSONObject(Objects.requireNonNull(response.body()).string());

                try {
                    final String info = json.getString("Information");
                    if (!info.isEmpty()) {
                        throw new ModuleException(debugMessage, Utils.unescapeXml(info));
                    }
                } catch (JSONException ignore) {
                    // Do nothing
                }

                try {
                    final String error = json.getString("Note");
                    if (!error.isEmpty()) {
                        throw new ModuleException(debugMessage, Utils.unescapeXml(error));
                    }
                } catch (JSONException ignore) {
                    // Do nothing
                }

                try {
                    final String error = json.getString("Error Message");
                    if (!error.isEmpty()) {
                        throw new ModuleException(debugMessage, Utils.unescapeXml(error));
                    }
                } catch (JSONException ignore) {
                    // Do nothing
                }

                return json;
            } else {
                throw new ModuleException(debugMessage, "Invalid Response (" + response.code() + ')');
            }
        } else {
            throw new ModuleException(debugMessage, "Empty Response.");
        }
    }

    /**
     * Retrieves a stock quote.
     *
     * @param symbol The stock symbol.
     * @return The {@link Message} array containing the stock quote.
     * @throws ModuleException If an errors occurs.
     */
    @SuppressWarnings({ "PMD.CloseResource" })
    static List<Message> getQuote(final String symbol, final String apiKey) throws ModuleException {
        if (StringUtils.isBlank(apiKey)) {
            throw new ModuleException(StringUtils.capitalize(STOCK_CMD) + " is disabled. The API key is missing.");
        }

        if (StringUtils.isNotBlank(symbol)) {
            final String debugMessage = "getQuote(" + symbol + ')';
            final ArrayList<Message> messages = new ArrayList<>();
            final OkHttpClient client = new OkHttpClient();

            try {
                // Search for symbol/keywords
                Request request = new Request.Builder().url(
                        ALAPHAVANTAGE_URL + "SYMBOL_SEARCH&keywords=" + symbol + "&apikey=" + apiKey).build();
                Response response = client.newCall(request).execute();

                JSONObject json = getJsonResponse(response, debugMessage);

                final JSONArray symbols = json.getJSONArray("bestMatches");
                if (symbols.isEmpty()) {
                    messages.add(new ErrorMessage(INVALID_SYMBOL));
                    return messages;
                }

                final JSONObject symbolInfo = symbols.getJSONObject(0);

                // Get quote for symbol
                request = new Request.Builder().url(
                        ALAPHAVANTAGE_URL + "GLOBAL_QUOTE&symbol=" + symbolInfo.getString("1. symbol") + "&apikey="
                        + apiKey).build();
                response = client.newCall(request).execute();

                json = getJsonResponse(response, debugMessage);

                final JSONObject quote = json.getJSONObject("Global Quote");

                if (quote.isEmpty()) {
                    messages.add(new ErrorMessage(INVALID_SYMBOL));
                    return messages;
                }

                messages.add(new PublicMessage(
                        "Symbol: " + Utils.unescapeXml(quote.getString("01. symbol")) + " [" + Utils.unescapeXml(
                                symbolInfo.getString("2. name") + ']')));
                messages.add(new PublicMessage("    Price:     " + Utils.unescapeXml(quote.getString("05. price"))));
                messages.add(new PublicMessage(
                        "    Previous:  "
                        + Utils.unescapeXml(quote.getString("08. previous close"))));
                messages.add(new NoticeMessage("    Open:      " + Utils.unescapeXml(quote.getString("02. open"))));
                messages.add(new NoticeMessage("    High:      " + Utils.unescapeXml(quote.getString("03. high"))));
                messages.add(new NoticeMessage("    Low:       " + Utils.unescapeXml(quote.getString("04. low"))));
                messages.add(new NoticeMessage("    Volume:    " + Utils.unescapeXml(quote.getString("06. volume"))));
                messages.add(new NoticeMessage(
                        "    Latest:    " + Utils.unescapeXml(quote.getString("07. latest trading day"))));
                messages.add(new NoticeMessage(
                        "    Change:    " + Utils.unescapeXml(quote.getString("09. change")) + " [" + Utils.unescapeXml(
                                quote.getString("10. change percent")) + ']'));
            } catch (IOException | NullPointerException e) {
                throw new ModuleException(debugMessage, "An error has occurred retrieving a stock quote.", e);
            }
            return messages;
        } else {
            throw new ModuleException(INVALID_SYMBOL);
        }
    }

    /**
     * Returns the specified stock quote from Alpha Avantage.
     */
    @Override
    void run(final Mobibot bot, final String sender, final String cmd, final String symbol, final boolean isPrivate) {
        if (StringUtils.isNotBlank(symbol)) {
            try {
                final List<Message> messages = getQuote(symbol, properties.get(ALPHAVANTAGE_API_KEY_PROP));
                for (final Message msg : messages) {
                    bot.send(sender, msg);
                }
            } catch (ModuleException e) {
                bot.getLogger().warn(e.getDebugMessage(), e);
                bot.send(e.getMessage());
            }
        } else {
            helpResponse(bot, sender, isPrivate);
        }
    }
}
