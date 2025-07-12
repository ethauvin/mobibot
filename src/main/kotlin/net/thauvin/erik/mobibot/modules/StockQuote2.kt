/*
 * StockQuote.kt
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.Utils.bold
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
import java.math.RoundingMode
import java.net.URI
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Retrieves stock quotes from Finnhub.
 */
class StockQuote2 : AbstractModule() {
    override val name = SERVICE_NAME

    companion object {
        /**
         * The API property key.
         */
        const val API_KEY_PROP = "finnhub-api-key"

        /**
         * The Invalid Symbol error string.
         */
        const val INVALID_SYMBOL = "Invalid symbol."

        /**
         * The service name.
         */
        const val SERVICE_NAME = "StockQuote"

        // API URL
        private const val API_URL = "https://finnhub.io/api/v1/"

        // Lookup keyword
        private const val LOOKUP_KEYWORD = "lookup"

        // Quote command
        private const val STOCK_CMD = "stock"

        // UTC date/time formatter
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")

        /**
         * The logger.
         */
        val logger: Logger = LoggerFactory.getLogger(StockQuote2::class.java)

        @Throws(ModuleException::class)
        private fun getJsonResponse(response: String, debugMessage: String): JSONObject {
            return if (response.isNotBlank()) {
                if (logger.isTraceEnabled) logger.trace(response)
                val json = JSONObject(response)
                try {
                    val error = json.getString("error")
                    if (error.isNotEmpty()) {
                        throw ModuleException(debugMessage, error.unescapeXml())
                    }
                } catch (_: JSONException) {
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
                    "$SERVICE_NAME is disabled.",
                    "$SERVICE_NAME is disabled. The API key is missing."
                )
            }
            val messages = mutableListOf<Message>()
            if (symbol.isNotBlank()) {
                val tickerSymbol = symbol.uppercase()
                val debugMessage = "getQuote($symbol)"
                val response: String
                try {
                    with(messages) {
                        // Get stock quote for symbol
                        response = URI(
                            "${API_URL}quote?symbol=" + tickerSymbol.encodeUrl() + "&token="
                                    + apiKey.encodeUrl()
                        ).toURL().reader().body
                        val json = getJsonResponse(response, debugMessage)
                        val c = json.getBigDecimal("c")
                        if (c == 0.toBigDecimal()) {
                            add(ErrorMessage(INVALID_SYMBOL))
                            return messages
                        }
                        val change = json.getBigDecimal("d")
                        val changePercent = json.getBigDecimal("dp")
                        val high = json.getBigDecimal("h")
                        val low = json.getBigDecimal("l")
                        val open = json.getBigDecimal("o")
                        val previous = json.getBigDecimal("pc")
                        val t = json.getInt("t")

                        val latest = formatter.format(
                            ZonedDateTime.ofInstant(
                                Instant.ofEpochSecond(t.toLong()),
                                ZoneId.of("UTC")
                            )
                        )

                        add(
                            PublicMessage(
                                "Symbol: ${tickerSymbol.bold()}"
                            )
                        )

                        val pad = 10

                        add(
                            PublicMessage(
                                "Price: ".padEnd(pad).prependIndent() + c
                            )
                        )
                        add(
                            PublicMessage(
                                "Previous: ".padEnd(pad).prependIndent() + previous
                            )
                        )
                        add(
                            NoticeMessage(
                                "Symbol: ${tickerSymbol.bold()}"
                            )
                        )
                        add(
                            NoticeMessage(
                                "Change: ".padEnd(pad).prependIndent() + change
                                        + " [${changePercent.setScale(2, RoundingMode.DOWN)}%]"
                            )
                        )
                        add(
                            NoticeMessage(
                                "High: ".padEnd(pad).prependIndent() + high
                            )
                        )
                        add(
                            NoticeMessage(
                                "Low: ".padEnd(pad).prependIndent() + low
                            )
                        )
                        add(
                            NoticeMessage(
                                "Open: ".padEnd(pad).prependIndent() + open
                            )
                        )
                        add(
                            NoticeMessage(
                                "Latest: ".padEnd(pad).prependIndent() + latest
                            )
                        )
                    }
                } catch (e: IOException) {
                    throw ModuleException(
                        "$debugMessage: IOE",
                        "An IO error has occurred retrieving a stock quote.", e
                    )
                } catch (e: NullPointerException) {
                    throw ModuleException(
                        "$debugMessage: NPE",
                        "An error has occurred retrieving a stock quote.", e
                    )
                }
            } else {
                messages.add(ErrorMessage(INVALID_SYMBOL))
            }
            return messages
        }

        /**
         * Retrieves a stock quote.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun lookup(keywords: String, apiKey: String?): List<Message> {
            if (apiKey.isNullOrBlank()) {
                throw ModuleException(
                    "$SERVICE_NAME is disabled.",
                    "$SERVICE_NAME is disabled. The API key is missing."
                )
            }
            val messages = mutableListOf<Message>()
            if (keywords.isNotBlank()) {
                val debugMessage = "getQuote($keywords)"
                val response: String
                try {
                    with(messages) {
                        // Search for symbol/keywords
                        response = URI(
                            "${API_URL}search?q=" + keywords.encodeUrl() + "&exchange=US&token="
                                    + apiKey.encodeUrl()
                        ).toURL().reader().body
                        val json = getJsonResponse(response, debugMessage)
                        val count = json.getInt("count")
                        if (count == 0) {
                            add(ErrorMessage("Nothing found for: ${keywords.bold()}"))
                            return messages
                        }

                        add(
                            NoticeMessage(
                                "Search results for: ${keywords.bold()}"
                            )
                        )
                        val results = json.getJSONArray("result")

                        for (i in 0 until count) {
                            val result = results.getJSONObject(i)
                            val symbol = result.getString("symbol")
                            val name = result.getString("description")

                            add(
                                NoticeMessage("${symbol.bold().padEnd(10)} $name".prependIndent())
                            )

                            if (i >= 4) {
                                break
                            }
                        }

                    }
                } catch (e: IOException) {
                    throw ModuleException(
                        "$debugMessage: IOE",
                        "An IO error has occurred looking up.", e
                    )
                } catch (e: NullPointerException) {
                    throw ModuleException(
                        "$debugMessage: NPE",
                        "An error has occurred looking up.", e
                    )
                }
            } else {
                messages.add(ErrorMessage("Please specify at least one search term."))
            }
            return messages
        }
    }

    init {
        commands.add(STOCK_CMD)
        help.add("To retrieve a stock quote:")
        help.add(helpFormat("%c $STOCK_CMD symbol"))
        help.add("To lookup a symbol:")
        help.add(helpFormat("%c $STOCK_CMD $LOOKUP_KEYWORD <keywords>"))
        initProperties(API_KEY_PROP)
    }

    /**
     * Returns the specified stock quote from Alpha Vantage.
     */
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val messages = if (args.startsWith(LOOKUP_KEYWORD)) {
                    lookup(
                        args.substring(LOOKUP_KEYWORD.length).trim(),
                        properties[API_KEY_PROP]
                    )
                } else {
                    getQuote(args, properties[API_KEY_PROP])
                }
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
}
