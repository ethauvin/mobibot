/*
 * CurrencyConverter2.kt
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
import net.thauvin.erik.frankfurter.CurrencyFormatter
import net.thauvin.erik.frankfurter.Frankfurter
import net.thauvin.erik.frankfurter.FrankfurterException
import net.thauvin.erik.frankfurter.models.Currencies
import net.thauvin.erik.frankfurter.models.Currency
import net.thauvin.erik.frankfurter.models.ErrorResponse
import net.thauvin.erik.frankfurter.models.Rate
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpCmdSyntax
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Duration

/**
 * Converts between currencies using the Frankfurter API.
 */
class CurrencyConverter2 : AbstractModule() {
    override val name = "CurrencyConverter"

    private val logger: Logger = LoggerFactory.getLogger(CurrencyConverter2::class.java)

    private val currenciesCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofDays(1))
        .build<String, Currencies>()

    private val rateCache = Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(Duration.ofMinutes(15))
        .build<Pair<String, String>, Double>()

    companion object {
        private const val CURRENCY_CMD = "currency"
        private const val CODES_KEYWORD = "codes"
        private val MATCH_CMD_ARGS =
            """\s*\d{1,3}(?:,\d{3})*(?:\.\d+)?\s+[A-Za-z]{3}\s+(?:to|in)\s+[A-Za-z]{3}\s*"""
                .toRegex(RegexOption.IGNORE_CASE)
        private val FRANKFURTER = Frankfurter()
        private const val LOAD_CURRENCIES_INTERNAL = ".loadCurrenciesInternal()"
        private const val CURRENCIES_CACHE_KEY = "currencies"
    }

    /**
     * Returns the cached currencies, loading them if necessary.
     *
     * @return the loaded currencies
     * @throws ModuleException if the currencies cannot be retrieved
     */
    @Throws(ModuleException::class)
    internal fun currencies(): Currencies {
        return currenciesCache.get(CURRENCIES_CACHE_KEY) { loadCurrenciesInternal() }
    }

    /**
     * Returns the currency for the given ISO 4217 code, or throws if not present.
     *
     * This should only be called after [validateCurrencies] has confirmed the code exists.
     * Throws [IllegalStateException] if the currency was removed from [Currencies] between
     * validation and this call, which indicates a cache/API inconsistency.
     *
     * @param code the 3-letter ISO 4217 currency code, e.g. "USD"
     * @return the matching [Currency][net.thauvin.erik.frankfurter.models.Currency] instance
     * @throws IllegalStateException if the currency cannot be found
     */
    fun Currencies.require(code: String): Currency = find(code).orElseThrow {
        IllegalStateException("Currency $code disappeared after validation")
    }

    /**
     * Loads the currencies from the Frankfurter API.
     *
     * @return the loaded currencies
     * @throws ModuleException if an error occurs while retrieving currencies
     */
    @Throws(ModuleException::class)
    private fun loadCurrenciesInternal(): Currencies {
        return try {
            val result = FRANKFURTER.currencies
            if (result is ErrorResponse) {
                val msg = "Error retrieving currencies: ${result.message()} (${result.status()})"
                logger.atWarn().log(msg)
                throw ModuleException(
                    CurrencyConverter2::class.java.name + LOAD_CURRENCIES_INTERNAL,
                    msg
                )
            }
            result as Currencies
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw ModuleException(
                CurrencyConverter2::class.java.name + LOAD_CURRENCIES_INTERNAL,
                "Interrupted error while retrieving currencies.",
                e
            )
        } catch (e: IOException) {
            throw ModuleException(
                CurrencyConverter2::class.java.name + LOAD_CURRENCIES_INTERNAL,
                "IO error while retrieving currencies.",
                e
            )
        } catch (e: FrankfurterException) {
            throw ModuleException(
                CurrencyConverter2::class.java.name + LOAD_CURRENCIES_INTERNAL,
                "Frankfurter error while retrieving currencies.",
                e
            )
        }
    }

    /**
     * Represents a parsed currency conversion request.
     */
    private data class ConversionQuery(
        val amount: Double,
        val from: String,
        val to: String
    )

    /**
     * Parses a raw query string such as "100 USD to EUR".
     *
     * @param query the raw query
     * @return a parsed query or an error
     */
    private fun parseQuery(query: String): Result<ConversionQuery> {
        val parts = query.trim().split("\\s+".toRegex())
        if (parts.size != 4) return Result.failure(IllegalArgumentException("Invalid query format."))
        val (amountString, fromCode, _, toCode) = parts
        val amount = try {
            amountString.replace(",", "").toDouble()
        } catch (_: NumberFormatException) {
            return Result.failure(IllegalArgumentException("Invalid amount."))
        }
        return Result.success(
            ConversionQuery(
                amount = amount,
                from = fromCode.uppercase(),
                to = toCode.uppercase()
            )
        )
    }

    /**
     * Validates that both currencies exist.
     *
     * @param query the parsed query
     * @param currencies the available currencies
     * @return success or failure
     */
    private fun validateCurrencies(query: ConversionQuery, currencies: Currencies): Result<Unit> {
        return if (currencies.find(query.from).isEmpty || currencies.find(query.to).isEmpty)
            Result.failure(IllegalArgumentException("Unknown currency code."))
        else
            Result.success(Unit)
    }

    /**
     * Retrieves the exchange rate for the given query. Uses a 15-minute cache since
     * Frankfurter v2 aggregates multiple providers and rates can update intraday.
     *
     * @param query the parsed query
     * @return the exchange rate or an error
     */
    private fun fetchRate(query: ConversionQuery): Result<Double> {
        val key = query.from to query.to
        rateCache.getIfPresent(key)?.let { return Result.success(it) }

        return try {
            val rate = FRANKFURTER.getRate(query.from, query.to)
            if (rate is ErrorResponse) {
                Result.failure(IOException(rate.message()))
            } else {
                val exchangeRate = (rate as Rate).exchangeRate
                rateCache.put(key, exchangeRate)
                Result.success(exchangeRate)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: FrankfurterException) {
            Result.failure(e)
        }
    }

    /**
     * Formats the final conversion message.
     */
    private fun formatConversion(
        query: ConversionQuery,
        rate: Double,
        currencies: Currencies
    ): PublicMessage {
        val fromCurrency = currencies.require(query.from)
        val toCurrency = currencies.require(query.to)
        val fromAmount = CurrencyFormatter.format(query.amount, query.from)
        val toAmount = CurrencyFormatter.format(query.amount * rate, query.to)
        return PublicMessage(
            "$fromAmount (${fromCurrency.name}) = $toAmount (${toCurrency.name})"
        )
    }

    /**
     * Converts the specified currency query.
     *
     * @param query the query string (e.g., "100 USD to EUR")
     * @return a message containing the conversion result or an error
     */
    internal fun convertCurrency(query: String): Message {
        val currencies = try {
            currencies()
        } catch (e: ModuleException) {
            logger.atWarn().log(e.debugMessage, e)
            return ErrorMessage("Sorry, I couldn't load the list of currencies.")
        }

        val parsed = parseQuery(query).getOrElse {
            return ErrorMessage("Invalid query. Let's try again.")
        }

        if (parsed.from == parsed.to || parsed.amount == 0.0) {
            return PublicMessage("You're kidding, right?")
        }

        validateCurrencies(parsed, currencies).getOrElse {
            return ErrorMessage(
                "Sounds like monopoly money to me! Try looking up the supported currency codes."
            )
        }

        return fetchRate(parsed).fold(
            onSuccess = { rate -> formatConversion(parsed, rate, currencies) },
            onFailure = { e ->
                when (e) {
                    is InterruptedException -> {
                        Thread.currentThread().interrupt()
                        logger.atWarn().log("InterruptedException: ${e.message}", e)
                        ErrorMessage("Sorry, an error occurred while converting the currencies.")
                    }

                    is IOException -> {
                        logger.atWarn().log("IO error: ${e.message}", e)
                        ErrorMessage("Sorry, an IO error occurred while converting the currencies.")
                    }

                    else -> {
                        logger.atWarn().log("Frankfurter error: ${e.message}", e)
                        ErrorMessage("Sorry, an error occurred while converting the currencies.")
                    }
                }
            }
        )
    }

    override fun initialize() {
        addCommand(CURRENCY_CMD)
        try {
            currencies()
        } catch (e: ModuleException) {
            logger.atWarn().log(e.debugMessage, e)
        }
    }

    /**
     * Handles the currency command.
     */
    @SuppressFBWarnings("BC_BAD_CAST_TO_ABSTRACT_COLLECTION")
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        when {
            args.matches(MATCH_CMD_ARGS) -> {
                val msg = convertCurrency(args)
                if (msg.isError) helpResponse(event)
                else event.respond(msg.msg)
            }

            args.equals(CODES_KEYWORD, ignoreCase = true) -> {
                val list = try {
                    currencies().list().mapNotNull { it.isoCode }
                } catch (_: ModuleException) {
                    event.sendMessage("Could not load currency codes.")
                    return
                }
                event.sendMessage("The supported currency codes are:")
                event.sendList(list, 11, isIndent = true)
            }

            else -> helpResponse(event)
        }
    }

    /**
     * Displays help for the currency command.
     */
    override fun helpResponse(event: GenericMessageEvent): Boolean {
        val nick = event.bot().nick
        event.sendMessage("To convert from one currency to another:")
        event.sendMessage(
            helpFormat(
                helpCmdSyntax("%c $CURRENCY_CMD 100 USD to EUR", nick, isPrivateMsgEnabled)
            )
        )
        event.sendMessage(
            helpFormat(
                helpCmdSyntax("%c $CURRENCY_CMD 50,000 GBP to USD", nick, isPrivateMsgEnabled)
            )
        )
        event.sendMessage("To list the supported currency codes:")
        event.sendMessage(
            helpFormat(
                helpCmdSyntax("%c $CURRENCY_CMD $CODES_KEYWORD", nick, isPrivateMsgEnabled)
            )
        )
        return true
    }
}
