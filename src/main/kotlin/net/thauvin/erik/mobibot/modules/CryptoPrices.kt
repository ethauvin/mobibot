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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.thauvin.erik.crypto.CryptoException
import net.thauvin.erik.crypto.CryptoPrice
import net.thauvin.erik.crypto.CryptoPrice.Companion.spotPrice
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.json.JSONObject
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.LocalDate
import java.util.*

/**
 * Retrieves cryptocurrency market prices.
 */
class CryptoPrices : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(CryptoPrices::class.java)

    override val name = "CryptoPrices"

    @SuppressFBWarnings(value = ["MS_EXPOSE_REP", "EI_EXPOSE_STATIC_REP2"])
    companion object {
        // Crypto command
        private const val CRYPTO_CMD = "crypto"

        // Fiat Currencies - Changed to immutable Map
        @Volatile
        private var CURRENCIES: Map<String, String> = emptyMap()

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
        @JvmStatic
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
                val newCurrencies = HashMap<String, String>(data.length())
                for (i in 0 until data.length()) {
                    val d = data.getJSONObject(i)
                    newCurrencies[d.getString("id")] = d.getString("name")
                }
                CURRENCIES = Collections.unmodifiableMap(newCurrencies)
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
        addCommand(CRYPTO_CMD)
        addHelp("To retrieve a cryptocurrency's market price:")
        addHelp(helpFormat("%c $CRYPTO_CMD <symbol> [<currency>]"))
        addHelp("For example:")
        addHelp(helpFormat("%c $CRYPTO_CMD BTC"))
        addHelp(helpFormat("%c $CRYPTO_CMD ETH EUR"))
        addHelp(helpFormat("%c $CRYPTO_CMD ETH2 GPB"))
        addHelp("To list the supported currencies:")
        addHelp(helpFormat("%c $CRYPTO_CMD $CODES_KEYWORD"))
        loadCurrencies()
    }

    // Reload currencies
    private fun reload() {
        if (CURRENCIES.isEmpty() || LocalDate.now().isAfter(LAST_CHECKED.plusDays(1))) {
            try {
                loadCurrencies()
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

        when {
            args == CODES_KEYWORD -> {
                event.sendMessage("The supported currencies are:")
                event.sendList(CURRENCIES.keys.toList(), 10, isIndent = true)
            }

            args.matches("\\w+( [a-zA-Z]{3}+)?".toRegex()) -> {
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
                    val errorMsg = e.message?.let { "$DEFAULT_ERROR_MESSAGE: $it" } ?: "$DEFAULT_ERROR_MESSAGE."
                    event.respond(errorMsg)
                } catch (e: IOException) {
                    if (logger.isErrorEnabled) logger.error(debugMessage, e)
                    event.respond("$DEFAULT_ERROR_MESSAGE: ${e.message}")
                }
            }

            else -> helpResponse(event)
        }
    }
}
