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

import com.github.benmanes.caffeine.cache.Caffeine
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.thauvin.erik.crypto.CryptoException
import net.thauvin.erik.crypto.CryptoPrice
import net.thauvin.erik.crypto.CryptoPrice.Companion.spotPrice
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpCmdSyntax
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.json.JSONObject
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Duration
import java.util.*

/**
 * Retrieves cryptocurrency market prices from Coinbase.
 *
 * This module supports spot price lookups for cryptocurrencies in either the
 * default fiat currency or a user‑specified one. It also maintains a cached
 * table of supported fiat currencies, refreshed at most once per day.
 */
class CryptoPrices : AbstractModule() {
    override val name = "CryptoPrices"

    private val logger: Logger = LoggerFactory.getLogger(CryptoPrices::class.java)

    private val currenciesCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofDays(1))
        .build<String, Map<String, String>>()

    companion object {
        const val DEFAULT_ERROR_MESSAGE =
            "An error has occurred while retrieving the cryptocurrency market price"

        private const val CRYPTO_CMD = "crypto"
        private const val CODES_KEYWORD = "codes"
        private const val CURRENCIES_CACHE_KEY = "currencies"
        private const val LOAD_CURRENCIES_INTERNAL = ".loadCurrenciesInternal()"
    }

    /**
     * Returns the cached currency table, loading it if expired or missing.
     *
     * @return immutable map of currency code to name
     * @throws ModuleException if the currencies cannot be retrieved
     */
    @Throws(ModuleException::class)
    internal fun currencies(): Map<String, String> {
        return currenciesCache.get(CURRENCIES_CACHE_KEY) { loadCurrenciesInternal() }
    }

    /**
     * Returns the human‑readable name of a fiat currency code, or null if
     * the code is not present in the cached currency table.
     *
     * @param code the 3-letter ISO 4217 currency code
     * @return the currency name, or null if not found
     */
    fun getCurrencyName(code: String): String? = currencies()[code]

    /**
     * Loads the fiat currency table from the Coinbase API.
     *
     * @return immutable map of currency code to name
     * @throws ModuleException if an API failure occurs
     */
    @Throws(ModuleException::class)
    private fun loadCurrenciesInternal(): Map<String, String> {
        try {
            val json = JSONObject(CryptoPrice.apiCall(listOf("currencies")))
            val data = json.getJSONArray("data")
            val newCurrencies = HashMap<String, String>(data.length())
            for (i in 0 until data.length()) {
                val d = data.getJSONObject(i)
                newCurrencies[d.getString("id")] = d.getString("name")
            }
            return Collections.unmodifiableMap(newCurrencies)
        } catch (e: CryptoException) {
            throw ModuleException(
                CryptoPrice::class.java.name + LOAD_CURRENCIES_INTERNAL,
                "An error has occurred while retrieving the currencies table.",
                e
            )
        }
    }

    /**
     * Resolves the current spot price for a cryptocurrency.
     *
     * Accepts either a symbol alone (using the default fiat currency) or a
     * symbol paired with a specific fiat currency code. Any other argument
     * count results in an error.
     *
     * @param args list with 1 or 2 elements: symbol and optional currency
     * @return the spot price
     * @throws IllegalArgumentException if args size is not 1 or 2
     */
    fun currentPrice(args: List<String>): CryptoPrice {
        require(args.size in 1..2) { "Invalid number of arguments: $args" }
        return if (args.size == 2) spotPrice(args[0], args[1]) else spotPrice(args[0])
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
        try {
            currencies() // prime cache
        } catch (e: ModuleException) {
            logger.atWarn().log(e.debugMessage, e)
        }
    }

    /**
     * Handles the `crypto` command and returns either a spot price or a list of
     * supported fiat currencies.
     */
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        val currencyMap = try {
            currencies()
        } catch (e: ModuleException) {
            logger.atWarn().log(e.debugMessage, e)
            event.respond("Sorry, but the currencies table is empty.")
            return
        }

        if (currencyMap.isEmpty()) {
            event.respond("Sorry, but the currencies table is empty.")
            return
        }

        val debugMessage = "crypto($cmd $args)"

        when {
            args.equals(CODES_KEYWORD, ignoreCase = true) -> {
                event.sendMessage("The supported currencies are:")
                event.sendList(currencyMap.keys.sorted(), 10, isIndent = true)
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
                    val currencyName = currencyMap[price.currency] ?: price.currency
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

    /**
     * Displays help for the crypto command.
     */
    override fun helpResponse(event: GenericMessageEvent): Boolean {
        val nick = event.bot().nick
        event.sendMessage("To retrieve a cryptocurrency's market price:")
        event.sendMessage(
            helpFormat(
                helpCmdSyntax("%c $CRYPTO_CMD <symbol> [<currency>]", nick, isPrivateMsgEnabled)
            )
        )
        event.sendMessage("For example:")
        event.sendMessage(
            helpFormat(
                helpCmdSyntax("%c $CRYPTO_CMD BTC", nick, isPrivateMsgEnabled)
            )
        )
        event.sendMessage(
            helpFormat(
                helpCmdSyntax("%c $CRYPTO_CMD ETH EUR", nick, isPrivateMsgEnabled)
            )
        )
        event.sendMessage("To list the supported currencies:")
        event.sendMessage(
            helpFormat(
                helpCmdSyntax("%c $CRYPTO_CMD $CODES_KEYWORD", nick, isPrivateMsgEnabled)
            )
        )
        return true
    }
}
