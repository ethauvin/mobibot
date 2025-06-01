/*
 * CryptoPrices.kt
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

import net.thauvin.erik.crypto.CryptoException
import net.thauvin.erik.crypto.CryptoPrice
import net.thauvin.erik.crypto.CryptoPrice.Companion.spotPrice
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.modules.CurrencyConverter2.Companion.loadCurrencyCodes
import org.json.JSONObject
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.LocalDate

/**
 * Retrieves cryptocurrency market prices.
 */
class CryptoPrices : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(CryptoPrices::class.java)

    override val name = "CryptoPrices"

    companion object {
        // Crypto command
        private const val CRYPTO_CMD = "crypto"

        // Fiat Currencies
        private val CURRENCIES: MutableMap<String, String> = mutableMapOf()

        // Currency codes keyword
        private const val CODES_KEYWORD = "codes"

        // Default error message
        const val DEFAULT_ERROR_MESSAGE = "An error has occurred while retrieving the cryptocurrency market price"

        // Last checked date
        private var LAST_CHECKED = LocalDate.now()

        /**
         * Get the current market price.
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
            return CURRENCIES[code]
        }

        /**
         * Loads the Fiat currencies.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun loadCurrencies() {
            try {
                val json = JSONObject(CryptoPrice.apiCall(listOf("currencies")))
                val data = json.getJSONArray("data")
                for (i in 0 until data.length()) {
                    val d = data.getJSONObject(i)
                    CURRENCIES[d.getString("id")] = d.getString("name")
                }
                LAST_CHECKED = LocalDate.now()
            } catch (e: CryptoException) {
                throw ModuleException(
                    "loadCurrencies(): CE",
                    "An error has occurred while retrieving the currencies table.",
                    e
                )
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
            add("To list the supported currencies:")
            add(helpFormat("%c $CRYPTO_CMD $CODES_KEYWORD"))
        }
        loadCurrencies()
    }

    // Reload currencies
    private fun reload() {
        if (CURRENCIES.isEmpty() || LocalDate.now().isAfter(LAST_CHECKED.plusDays(1))) {
            try {
                loadCurrencyCodes()
                LAST_CHECKED = LocalDate.now()
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
            }
        }
    }

    /**
     * Returns the cryptocurrency market price from
     * [Coinbase](https://docs.cdp.coinbase.com/coinbase-app/docs/api-prices#get-spot-price).
     */
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        reload()

        if (CURRENCIES.isEmpty()) {
            event.respond("Sorry, but the currencies table is empty.")
            return
        }

        val debugMessage = "crypto($cmd $args)"
        if (args == CODES_KEYWORD) {
            event.sendMessage("The supported currencies are:")
            event.sendList(ArrayList(CURRENCIES.keys), 10, isIndent = true)
        } else if (args.matches("\\w+( [a-zA-Z]{3}+)?".toRegex())) {
            try {
                val price = currentPrice(args.split(' '))
                val amount = try {
                    price.toCurrency()
                } catch (_: IllegalArgumentException) {
                    price.amount
                }
                event.respond("${price.base} current price is $amount [${CURRENCIES[price.currency]}]")
            } catch (e: CryptoException) {
                if (logger.isWarnEnabled) logger.warn("$debugMessage => ${e.statusCode}", e)
                if (e.message != null) {
                    event.respond("$DEFAULT_ERROR_MESSAGE: ${e.message}")
                } else {
                    event.respond("$DEFAULT_ERROR_MESSAGE.")
                }
            } catch (e: IOException) {
                if (logger.isErrorEnabled) logger.error(debugMessage, e)
                event.respond("$DEFAULT_ERROR_MESSAGE: ${e.message}")
            }
        } else {
            helpResponse(event)
        }
    }
}
