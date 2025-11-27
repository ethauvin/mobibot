/*
 * CurrencyConverter2.kt
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

import net.thauvin.erik.frankfurter.CurrencyFormatter
import net.thauvin.erik.frankfurter.CurrencyRegistry
import net.thauvin.erik.frankfurter.LatestRates
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

/**
 * Converts between currencies.
 */
class CurrencyConverter2 : AbstractModule() {
    override val name = "CurrencyConverter"

    companion object {
        // Currency command
        private const val CURRENCY_CMD = "currency"

        // Currency codes keyword
        private const val CODES_KEYWORD = "codes"

        // Logger
        private val LOGGER: Logger = LoggerFactory.getLogger(CurrencyConverter2::class.java)

        // Regex pattern to match command arguments
        private val MATCH_CMD_ARGS = "\\d+([,\\d]+)?(\\.\\d+)? [a-zA-Z]{3}+ (to|in) [a-zA-Z]{3}+".toRegex()

        @JvmStatic
        fun convertCurrency(query: String): Message {
            val queryParts = query.trim().split("\\s+".toRegex())

            // Validate input format
            if (queryParts.size != 4) {
                return ErrorMessage("Invalid query. Let's try again.")
            }

            val (amountString, fromCurrencyCode, _, toCurrencyCode) = queryParts
            val fromCurrencySymbol = fromCurrencyCode.uppercase()
            val toCurrencySymbol = toCurrencyCode.uppercase()

            // Try to parse the amount early
            val amount = try {
                amountString.replace(",", "").toDouble()
            } catch (_: NumberFormatException) {
                return ErrorMessage("Invalid amount. Please enter a valid number.")
            }

            // Early return for same currencies or zero amounts
            if (fromCurrencySymbol == toCurrencySymbol || amount == 0.0) {
                return PublicMessage("You're kidding, right?")
            }

            // Validate currencies
            val fromCurrency = CurrencyRegistry.getInstance().findBySymbol(fromCurrencySymbol)
            val toCurrency = CurrencyRegistry.getInstance().findBySymbol(toCurrencySymbol)

            if (fromCurrency.isEmpty || toCurrency.isEmpty) {
                return ErrorMessage(
                    "Sounds like monopoly money to me! Try looking up the supported currency codes."
                )
            }

            return try {
                val exchangeRates = LatestRates.Builder()
                    .amount(amount)
                    .base(fromCurrencySymbol)
                    .symbols(toCurrencySymbol)
                    .build()
                    .exchangeRates()

                val toExchangeRate = exchangeRates.rateFor(toCurrencySymbol)
                val fromAmountFormatted = CurrencyFormatter.format(fromCurrencySymbol, amount)
                val toAmountFormatted = CurrencyFormatter.format(toCurrencySymbol, toExchangeRate)

                PublicMessage(
                    "$fromAmountFormatted (${fromCurrency.get().name}) = $toAmountFormatted (${toCurrency.get().name})"
                )
            } catch (e: IOException) {
                if (LOGGER.isWarnEnabled) {
                    LOGGER.warn("IO error while converting currencies: ${e.message}", e)
                }
                ErrorMessage("Sorry, an IO error occurred while converting the currencies.")
            }
        }
    }

    init {
        addCommand(CURRENCY_CMD)
    }

    /**
     * Converts the specified currencies.
     */
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        when {
            args.matches(MATCH_CMD_ARGS) -> {
                try {
                    val msg = convertCurrency(args)
                    if (msg.isError) {
                        helpResponse(event)
                    } else {
                        event.respond(msg.msg)
                    }
                } catch (e: ModuleException) {
                    if (LOGGER.isWarnEnabled) LOGGER.warn(e.debugMessage, e)
                    event.respond(e.message)
                }
            }

            args.contains(CODES_KEYWORD) -> {
                event.sendMessage("The supported currency codes are:")
                event.sendList(CurrencyRegistry.getInstance().allSymbols, 11, isIndent = true)
            }

            else -> {
                helpResponse(event)
            }
        }
    }

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
