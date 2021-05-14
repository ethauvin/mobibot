/*
 * StockQuote.kt
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
package net.thauvin.erik.mobibot.modules

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.NoticeMessage
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL

/**
 * The StockQuote module.
 */
class StockQuote(bot: Mobibot) : ThreadedModule(bot) {
    /**
     * Returns the specified stock quote from Alpha Avantage.
     */
    override fun run(sender: String, cmd: String, args: String, isPrivate: Boolean) {
        with(bot) {
            if (args.isNotBlank()) {
                try {
                    val messages = getQuote(args, properties[ALPHAVANTAGE_API_KEY_PROP])
                    for (msg in messages) {
                        send(sender, msg)
                    }
                } catch (e: ModuleException) {
                    if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                    send(e.message)
                }
            } else {
                helpResponse(sender, isPrivate)
            }
        }
    }

    companion object {
        /**
         * The Alpha Advantage property key.
         */
        const val ALPHAVANTAGE_API_KEY_PROP = "alphavantage-api-key"

        /**
         * The Invalid Symbol error string.
         */
        const val INVALID_SYMBOL = "Invalid symbol."

        // Alpha Advantage URL
        private const val ALPHAVANTAGE_URL = "https://www.alphavantage.co/query?function="

        // Quote command
        private const val STOCK_CMD = "stock"

        @Throws(ModuleException::class)
        private fun getJsonResponse(response: String, debugMessage: String): JSONObject {
            return if (response.isNotBlank()) {
                val json = JSONObject(response)
                try {
                    val info = json.getString("Information")
                    if (info.isNotEmpty()) {
                        throw ModuleException(debugMessage, Utils.unescapeXml(info))
                    }
                } catch (ignore: JSONException) {
                    // Do nothing
                }
                try {
                    var error = json.getString("Note")
                    if (error.isNotEmpty()) {
                        throw ModuleException(debugMessage, Utils.unescapeXml(error))
                    }
                    error = json.getString("Error Message")
                    if (error.isNotEmpty()) {
                        throw ModuleException(debugMessage, Utils.unescapeXml(error))
                    }
                } catch (ignore: JSONException) {
                    // Do nothing
                }
                json
            } else {
                throw ModuleException(debugMessage, "Empty Response.")
            }
        }

        /**
         * Retrieves a stock quote.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun getQuote(symbol: String, apiKey: String?): List<Message> {
            if (apiKey.isNullOrBlank()) {
                throw ModuleException(
                    "${Utils.capitalize(STOCK_CMD)} is disabled. The API key is missing."
                )
            }
            return if (symbol.isNotBlank()) {
                val debugMessage = "getQuote($symbol)"
                val messages = mutableListOf<Message>()
                var response: String
                try {
                    with(messages) {
                        // Search for symbol/keywords
                        response = Utils.urlReader(
                            URL(
                                "${ALPHAVANTAGE_URL}SYMBOL_SEARCH&keywords=" + Utils.encodeUrl(symbol)
                                    + "&apikey=" + Utils.encodeUrl(apiKey)
                            )
                        )
                        var json = getJsonResponse(response, debugMessage)
                        val symbols = json.getJSONArray("bestMatches")
                        if (symbols.isEmpty) {
                            messages.add(ErrorMessage(INVALID_SYMBOL))
                        } else {
                            val symbolInfo = symbols.getJSONObject(0)

                            // Get quote for symbol
                            response = Utils.urlReader(
                                URL(
                                    "${ALPHAVANTAGE_URL}GLOBAL_QUOTE&symbol="
                                        + Utils.encodeUrl(symbolInfo.getString("1. symbol"))
                                        + "&apikey=" + Utils.encodeUrl(apiKey)
                                )
                            )
                            json = getJsonResponse(response, debugMessage)
                            val quote = json.getJSONObject("Global Quote")
                            if (quote.isEmpty) {
                                add(ErrorMessage(INVALID_SYMBOL))
                                return messages
                            }
                            add(
                                PublicMessage(
                                    "Symbol: " + Utils.unescapeXml(quote.getString("01. symbol"))
                                        + " [" + Utils.unescapeXml(symbolInfo.getString("2. name")) + ']'
                                )
                            )
                            add(PublicMessage("    Price:     " + Utils.unescapeXml(quote.getString("05. price"))))
                            add(
                                PublicMessage(
                                    "    Previous:  " + Utils.unescapeXml(quote.getString("08. previous close"))
                                )
                            )
                            add(NoticeMessage("    Open:      " + Utils.unescapeXml(quote.getString("02. open"))))
                            add(NoticeMessage("    High:      " + Utils.unescapeXml(quote.getString("03. high"))))
                            add(NoticeMessage("    Low:       " + Utils.unescapeXml(quote.getString("04. low"))))
                            add(
                                NoticeMessage(
                                    "    Volume:    " + Utils.unescapeXml(quote.getString("06. volume"))
                                )
                            )
                            add(
                                NoticeMessage(
                                    "    Latest:    "
                                        + Utils.unescapeXml(quote.getString("07. latest trading day"))
                                )
                            )
                            add(
                                NoticeMessage(
                                    "    Change:    " + Utils.unescapeXml(quote.getString("09. change")) + " ["
                                        + Utils.unescapeXml(quote.getString("10. change percent")) + ']'
                                )
                            )
                        }
                    }
                } catch (e: IOException) {
                    throw ModuleException(debugMessage, "An IO error has occurred retrieving a stock quote.", e)
                } catch (e: NullPointerException) {
                    throw ModuleException(debugMessage, "An error has occurred retrieving a stock quote.", e)
                }
                messages
            } else {
                throw ModuleException(INVALID_SYMBOL)
            }
        }
    }

    init {
        commands.add(STOCK_CMD)
        help.add("To retrieve a stock quote:")
        help.add(Utils.helpFormat("%c $STOCK_CMD <symbol|keywords>"))
        initProperties(ALPHAVANTAGE_API_KEY_PROP)
    }
}
