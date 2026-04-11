/*
 * CryptoPrices.kt
 *
 * Copyright (c) 2004-2026 Erik C. Thauvin (erik@thauvin.net)
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
 * Retrieves cryptocurrency market prices from Coinbase.
 *
 * This module supports spot price lookups for cryptocurrencies in either the
 * default fiat currency or a user‑specified one. It also maintains a cached
 * table of supported fiat currencies, refreshed at most once per day.
 *
 * The currency table is stored in an immutable map and published through a
 * volatile reference to ensure safe concurrent access.
 */
class CryptoPrices : AbstractModule() {
    override val name = "CryptoPrices"

    @SuppressFBWarnings(value = ["MS_EXPOSE_REP", "EI_EXPOSE_STATIC_REP2"])
    companion object {
        const val DEFAULT_ERROR_MESSAGE =
            "An error has occurred while retrieving the cryptocurrency market price"

        private const val CRYPTO_CMD = "crypto"
        private const val CODES_KEYWORD = "codes"

        @Volatile
        private var CURRENCIES: Map<String, String> = emptyMap()

        @Volatile
        private var LAST_CHECKED: LocalDate = LocalDate.now()

        private val logger: Logger = LoggerFactory.getLogger(CryptoPrices::class.java)

        /**
         * Resolves the current spot price for a cryptocurrency.
         *
         * Accepts either a symbol alone (using the default fiat currency) or a
         * symbol paired with a specific fiat currency code. Any other argument
         * count results in an error.
         */
        @JvmStatic
        fun currentPrice(args: List<String>): CryptoPrice {
            if (args.size !in 1..2) {
                throw IllegalArgumentException("Invalid number of arguments: $args")
            }
            return if (args.size == 2) spotPrice(args[0], args[1]) else spotPrice(args[0])
        }

        /**
         * Returns the human‑readable name of a fiat currency code, or null if
         * the code is not present in the cached currency table.
         */
        @JvmStatic
        fun getCurrencyName(code: String): String? = CURRENCIES[code]

        /**
         * Loads the fiat currency table from the Coinbase API.
         *
         * The resulting map is wrapped in an unmodifiable view to guarantee
         * immutability. The volatile reference ensures safe publication to
         * concurrent readers.
         *
         * Any API failure is wrapped in a [ModuleException].
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
                    CryptoPrice::class.java.name + ".loadCurrencies()",
                    "An error has occurred while retrieving the currencies table.",
                    e
                )
            }
        }

        /**
         * Reloads the fiat currency table if it is empty or older than one day.
         *
         * Uses a double‑checked lock to avoid redundant reloads under
         * concurrency while keeping the fast path lock‑free.
         */
        private fun reloadIfNeeded() {
            val today = LocalDate.now()
            if (CURRENCIES.isEmpty() || today.isAfter(LAST_CHECKED.plusDays(1))) {
                synchronized(CryptoPrices::class.java) {
                    if (CURRENCIES.isEmpty() || today.isAfter(LAST_CHECKED.plusDays(1))) {
                        try {
                            loadCurrencies()
                        } catch (e: ModuleException) {
                            if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                        }
                    }
                }
            }
        }
    }

    override fun initialize() {
        addCommand(CRYPTO_CMD)
        addHelp("To retrieve a cryptocurrency's market price:")
        addHelp(helpFormat("%c $CRYPTO_CMD <symbol> [<currency>]"))
        addHelp("For example:")
        addHelp(helpFormat("%c $CRYPTO_CMD BTC"))
        addHelp(helpFormat("%c $CRYPTO_CMD ETH EUR"))
        addHelp(helpFormat("%c $CRYPTO_CMD ETH2 GBP"))
        addHelp("To list the supported currencies:")
        addHelp(helpFormat("%c $CRYPTO_CMD $CODES_KEYWORD"))
        loadCurrencies()
    }

    /**
     * Handles the `crypto` command and returns either a spot price or a list of
     * supported fiat currencies.
     *
     * Input is validated to ensure it matches the expected pattern of a symbol
     * optionally followed by a three‑letter fiat currency code. Any malformed
     * input falls back to the module's help response.
     */
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        reloadIfNeeded()

        if (CURRENCIES.isEmpty()) {
            event.respond("Sorry, but the currencies table is empty.")
            return
        }

        val debugMessage = "crypto($cmd $args)"

        when {
            args.equals(CODES_KEYWORD, ignoreCase = true) -> {
                event.sendMessage("The supported currencies are:")
                event.sendList(CURRENCIES.keys.sorted(), 10, isIndent = true)
            }

            args.matches("""[A-Za-z0-9]+( [A-Za-z]{3})?""".toRegex()) -> {
                val parts = args.trim().split(Regex("\\s+"))
                try {
                    val price = currentPrice(parts)
                    val amount = try {
                        price.toCurrency()
                    } catch (_: IllegalArgumentException) {
                        price.amount
                    }
                    val currencyName = CURRENCIES[price.currency] ?: price.currency
                    event.respond("${price.base} current price is $amount [$currencyName]")
                } catch (e: CryptoException) {
                    logger.warn("$debugMessage => ${e.statusCode}: ${e.message}", e)
                    event.respond(DEFAULT_ERROR_MESSAGE)
                } catch (e: IOException) {
                    logger.error(debugMessage, e)
                    event.respond("$DEFAULT_ERROR_MESSAGE: ${e.message}")
                } catch (_: IllegalArgumentException) {
                    event.respond("Invalid arguments. Usage: $CRYPTO_CMD <symbol> [<currency>]")
                }
            }

            else -> helpResponse(event)
        }
    }
}
