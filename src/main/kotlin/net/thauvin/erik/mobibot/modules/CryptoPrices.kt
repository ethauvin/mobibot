/*
 * CryptoPrices.kt
 *
 * Copyright (c) 2004-2022, Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.crypto.CryptoException
import net.thauvin.erik.crypto.CryptoPrice
import net.thauvin.erik.crypto.CryptoPrice.Companion.spotPrice
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.Utils.today
import org.json.JSONObject
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * The Cryptocurrency Prices  module.
 */
class CryptoPrices : ThreadedModule() {
    private val logger: Logger = LoggerFactory.getLogger(CryptoPrices::class.java)

    override val name = "CryptoPrices"
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        synchronized(this) {
            if (pubDate != today()) {
                CODES.clear()
            }
        }
        super.commandResponse(channel, cmd, args, event)
    }

    /**
     * Returns the cryptocurrency market price from [Coinbase](https://developers.coinbase.com/api/v2#get-spot-price).
     */
    override fun run(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (CODES.isEmpty()) {
            try {
                loadCodes()
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
            }
        }

        val debugMessage = "crypto($cmd $args)"
        if (args == CURRENCY_CODES_KEYWORD) {
            event.sendMessage("The supported currency codes are:")
            event.sendList(ArrayList(CODES.keys), 10, isIndent = true)
        } else if (args.matches("\\w+( [a-zA-Z]{3}+)?".toRegex())) {
            try {
                val price = currentPrice(args.split(' '))
                val amount = try {
                    price.toCurrency()
                } catch (ignore: IllegalArgumentException) {
                    price.amount
                }
                event.respond("${price.base} current price is $amount [${CODES[price.currency]}]")
            } catch (e: CryptoException) {
                if (logger.isWarnEnabled) logger.warn("$debugMessage => ${e.statusCode}", e)
                e.message?.let {
                    event.sendMessage(it)
                }
            } catch (e: IOException) {
                if (logger.isErrorEnabled) logger.error(debugMessage, e)
                event.sendMessage("An IO error has occurred while retrieving the cryptocurrency market price.")
            }
        } else {
            helpResponse(event)
        }

    }

    companion object {
        // Crypto command
        private const val CRYPTO_CMD = "crypto"

        // Currency codes
        private val CODES: MutableMap<String, String> = mutableMapOf()

        // Currency codes keyword
        private const val CURRENCY_CODES_KEYWORD = "codes"

        // Last currency table retrieval date
        private var pubDate = ""

        /**
         * Get current market price.
         */
        @JvmStatic
        fun currentPrice(args: List<String>): CryptoPrice {
            return if (args.size == 2)
                spotPrice(args[0], args[1])
            else
                spotPrice(args[0])
        }

        /**
         * For testing purposes.
         */
        fun getCurrencyName(code: String): String? {
            return CODES[code]
        }

        /**
         * Loads the country ISO codes.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun loadCodes() {
            if (CODES.isEmpty()) {
                try {
                    val json = JSONObject(CryptoPrice.apiCall(listOf("currencies")))
                    val data = json.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        val d = data.getJSONObject(i)
                        CODES[d.getString("id")] = d.getString("name")
                    }
                    pubDate = today()
                } catch (e: CryptoException) {
                    throw ModuleException(
                        "loadCodes(): CE",
                        "An error has occurred while retrieving the currency table.",
                        e
                    )
                }
            }
        }
    }

    init {
        commands.add(CRYPTO_CMD)
        with(help) {
            add("To retrieve a cryptocurrency's market price:")
            add(helpFormat("%c $CRYPTO_CMD <symbol> [<currency>]"))
            add("For example:")
            add(helpFormat("%c $CRYPTO_CMD BTC"))
            add(helpFormat("%c $CRYPTO_CMD ETH EUR"))
            add(helpFormat("%c $CRYPTO_CMD ETH2 GPB"))
            add("To list the supported currency codes:")
            add(helpFormat("%c $CRYPTO_CMD $CURRENCY_CODES_KEYWORD"))
        }
    }
}
