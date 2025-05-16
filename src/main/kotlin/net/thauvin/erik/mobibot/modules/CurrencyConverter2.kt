/*
 * CurrencyConverter.kt
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

import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpCmdSyntax
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.reader
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.json.JSONObject
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.text.DecimalFormat
import java.util.*

/**
 * Converts between currencies.
 */
class CurrencyConverter2 : AbstractModule() {
    override val name = "CurrencyConverter"

    companion object {
        // Currency command
        private const val CURRENCY_CMD = "currency"

        // Currency codes
        private val CURRENCY_CODES: TreeMap<String, String> = TreeMap()

        // Currency codes keyword
        private const val CODES_KEYWORD = "codes"

        // Decimal format
        private val DECIMAL_FORMAT = DecimalFormat("0.00#")

        // Empty codes table.
        private const val EMPTY_CODES_TABLE = "Sorry, but the currencies codes table is empty."

        // Logger
        private val LOGGER: Logger = LoggerFactory.getLogger(CurrencyConverter2::class.java)

        /**
         * Converts from a currency to another.
         */
        @JvmStatic
        fun convertCurrency(query: String): Message {
            val cmds = query.split(" ")
            return if (cmds.size == 4) {
                if (cmds[3] == cmds[1] || "0" == cmds[0]) {
                    PublicMessage("You're kidding, right?")
                } else {
                    val from = cmds[1].uppercase()
                    val to = cmds[3].uppercase()
                    if (CURRENCY_CODES.contains(to) && CURRENCY_CODES.contains(from)) {
                        try {
                            val amt = cmds[0].replace(",", "")
                            val url = URL("https://api.frankfurter.dev/v1/latest?base=$from&symbols=$to")
                            val body = url.reader().body
                            if (LOGGER.isTraceEnabled) {
                                LOGGER.trace(body)
                            }
                            val json = JSONObject(body)
                            val rates = json.getJSONObject("rates")
                            val rate = rates.getDouble(to)
                            val result = DECIMAL_FORMAT.format(amt.toDouble() * rate)


                            PublicMessage(
                                "${cmds[0]} ${CURRENCY_CODES[from]} = $result ${CURRENCY_CODES[to]}"
                            )
                        } catch (nfe: NumberFormatException) {
                            if (LOGGER.isWarnEnabled) {
                                LOGGER.warn("IO error while converting currencies: ${nfe.message}", nfe)
                            }
                            ErrorMessage("Sorry, an error occurred while converting the currencies.")
                        } catch (ioe: IOException) {
                            if (LOGGER.isWarnEnabled) {
                                LOGGER.warn("IO error while converting currencies: ${ioe.message}", ioe)
                            }
                            ErrorMessage("Sorry, an IO error occurred while converting the currencies.")
                        }
                    } else {
                        ErrorMessage(
                            "Sounds like monopoly money to me! Try looking up the supported currency codes."
                        )
                    }
                }
            } else {
                ErrorMessage("Invalid query. Let's try again.")
            }
        }

        /**
         * Loads the currency ISO codes.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun loadCurrencyCodes() {
            try {
                val url = URL("https://api.frankfurter.dev/v1/currencies")
                val body = url.reader().body
                val json = JSONObject(body)
                if (LOGGER.isTraceEnabled) {
                    LOGGER.trace(body)
                }
                json.keySet().forEach { key ->
                    CURRENCY_CODES[key] = json.getString(key)
                }
            } catch (e: IOException) {
                throw ModuleException(
                    "loadCurrencyCodes(): IOE",
                    "An IO error has occurred while retrieving the currency codes.",
                    e
                )
            }
        }
    }

    init {
        commands.add(CURRENCY_CMD)
    }

    // Reload currency codes
    private fun reload() {
        if (CURRENCY_CODES.isEmpty()) {
            try {
                loadCurrencyCodes()
            } catch (e: ModuleException) {
                if (LOGGER.isWarnEnabled) LOGGER.warn(e.debugMessage, e)
            }
        }
    }

    /**
     * Converts the specified currencies.
     */
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        reload()
        when {
            CURRENCY_CODES.isEmpty() -> {
                event.respond(EMPTY_CODES_TABLE)
            }

            args.matches("\\d+([,\\d]+)?(\\.\\d+)? [a-zA-Z]{3}+ (to|in) [a-zA-Z]{3}+".toRegex()) -> {
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
                event.sendList(CURRENCY_CODES.keys.toList(), 11, isIndent = true)
            }

            else -> {
                helpResponse(event)
            }
        }
    }

    override fun helpResponse(event: GenericMessageEvent): Boolean {
        reload()

        if (CURRENCY_CODES.isEmpty()) {
            event.sendMessage(EMPTY_CODES_TABLE)
        } else {
            val nick = event.bot().nick
            event.sendMessage("To convert from one currency to another:")
            event.sendMessage(helpFormat(helpCmdSyntax("%c $CURRENCY_CMD 100 USD to EUR", nick, isPrivateMsgEnabled)))
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
        }
        return true
    }
}
