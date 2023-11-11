/*
 * StockQuote.kt
 *
 * Copyright 2021-2023 Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.Utils.capitalise
import net.thauvin.erik.mobibot.Utils.encodeUrl
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.reader
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.Utils.unescapeXml
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.NoticeMessage
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.json.JSONException
import org.json.JSONObject
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL

/**
 * The StockQuote module.
 */
class StockQuote : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(StockQuote::class.java)

    override val name = "StockQuote"

    /**
     * Returns the specified stock quote from Alpha Vantage.
     */
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val messages = getQuote(args, properties[API_KEY_PROP])
                for (msg in messages) {
                    event.sendMessage(channel, msg)
                }
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                e.message?.let {
                    event.respond(it)
                }
            }
        } else {
            helpResponse(event)
        }
    }

    companion object {
        /**
         * The API property key.
         */
        const val API_KEY_PROP = "alphavantage-api-key"

        /**
         * The Invalid Symbol error string.
         */
        const val INVALID_SYMBOL = "Invalid symbol."

        // API URL
        private const val API_URL = "https://www.alphavantage.co/query?function="

        // Quote command
        private const val STOCK_CMD = "stock"

        @Throws(ModuleException::class)
        private fun getJsonResponse(response: String, debugMessage: String): JSONObject {
            return if (response.isNotBlank()) {
                val json = JSONObject(response)
                try {
                    val info = json.getString("Information")
                    if (info.isNotEmpty()) {
                        throw ModuleException(debugMessage, info.unescapeXml())
                    }
                } catch (ignore: JSONException) {
                    // Do nothing
                }
                try {
                    var error = json.getString("Note")
                    if (error.isNotEmpty()) {
                        throw ModuleException(debugMessage, error.unescapeXml())
                    }
                    error = json.getString("Error Message")
                    if (error.isNotEmpty()) {
                        throw ModuleException(debugMessage, error.unescapeXml())
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
                    "${StockQuote::class.java.name} is disabled.",
                    "${STOCK_CMD.capitalise()} is disabled. The API key is missing."
                )
            }
            val messages = mutableListOf<Message>()
            if (symbol.isNotBlank()) {
                val debugMessage = "getQuote($symbol)"
                var response: String
                try {
                    with(messages) {
                        // Search for symbol/keywords
                        response = URL(
                            "${API_URL}SYMBOL_SEARCH&keywords=" + symbol.encodeUrl() + "&apikey="
                                    + apiKey.encodeUrl()
                        ).reader().body
                        var json = getJsonResponse(response, debugMessage)
                        val symbols = json.getJSONArray("bestMatches")
                        if (symbols.isEmpty) {
                            messages.add(ErrorMessage(INVALID_SYMBOL))
                        } else {
                            val symbolInfo = symbols.getJSONObject(0)

                            // Get quote for symbol
                            response = URL(
                                "${API_URL}GLOBAL_QUOTE&symbol="
                                        + symbolInfo.getString("1. symbol").encodeUrl() + "&apikey="
                                        + apiKey.encodeUrl()
                            ).reader().body
                            json = getJsonResponse(response, debugMessage)
                            val quote = json.getJSONObject("Global Quote")
                            if (quote.isEmpty) {
                                add(ErrorMessage(INVALID_SYMBOL))
                            } else {

                                add(
                                    PublicMessage(
                                        "Symbol: " + quote.getString("01. symbol").unescapeXml()
                                                + " [" + symbolInfo.getString("2. name").unescapeXml() + ']'
                                    )
                                )

                                val pad = 10

                                add(
                                    PublicMessage(
                                        "Price:".padEnd(pad).prependIndent()
                                                + quote.getString("05. price").unescapeXml()
                                    )
                                )
                                add(
                                    PublicMessage(
                                        "Previous:".padEnd(pad).prependIndent()
                                                + quote.getString("08. previous close").unescapeXml()
                                    )
                                )

                                val data = arrayOf(
                                    "Open" to "02. open",
                                    "High" to "03. high",
                                    "Low" to "04. low",
                                    "Volume" to "06. volume",
                                    "Latest" to "07. latest trading day"
                                )

                                data.forEach {
                                    add(
                                        NoticeMessage(
                                            "${it.first}:".padEnd(pad).prependIndent()
                                                    + quote.getString(it.second).unescapeXml()
                                        )
                                    )
                                }

                                add(
                                    NoticeMessage(
                                        "Change:".padEnd(pad).prependIndent()
                                                + quote.getString("09. change").unescapeXml()
                                                + " [" + quote.getString("10. change percent").unescapeXml() + ']'
                                    )
                                )
                            }
                        }
                    }
                } catch (e: IOException) {
                    throw ModuleException("$debugMessage: IOE", "An IO error has occurred retrieving a stock quote.", e)
                } catch (e: NullPointerException) {
                    throw ModuleException("$debugMessage: NPE", "An error has occurred retrieving a stock quote.", e)
                }
            } else {
                messages.add(ErrorMessage(INVALID_SYMBOL))
            }
            return messages
        }
    }

    init {
        commands.add(STOCK_CMD)
        help.add("To retrieve a stock quote:")
        help.add(helpFormat("%c $STOCK_CMD <symbol|keywords>"))
        initProperties(API_KEY_PROP)
    }
}
