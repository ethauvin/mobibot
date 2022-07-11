/*
 * CurrencyConverter.kt
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

import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpCmdSyntax
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.reader
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.Utils.today
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.json.JSONObject
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL


/**
 * The CurrencyConverter module.
 */
class CurrencyConverter : ThreadedModule() {
    private val logger: Logger = LoggerFactory.getLogger(CurrencyConverter::class.java)

    override val name = "CurrencyConverter"

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        synchronized(this) {
            if (pubDate != today()) {
                CODES.clear()
            }
        }
        super.commandResponse(channel, cmd, args, event)
    }

    /**
     * Converts the specified currencies.
     */
    override fun run(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (CODES.isEmpty()) {
            try {
                loadCodes()
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
            }
        }

        if (CODES.isEmpty()) {
            event.respond(EMPTY_CODES_TABLE)
        } else if (args.matches("\\d+([,\\d]+)?(\\.\\d+)? [a-zA-Z]{3}+ to [a-zA-Z]{3}+".toRegex())) {
            val msg = convertCurrency(args)
            event.respond(msg.msg)
            if (msg.isError) {
                helpResponse(event)
            }
        } else if (args.contains(CURRENCY_CODES_KEYWORD)) {
            event.sendMessage("The supported currency codes are: ")
            event.sendList(ArrayList(CODES.keys.sorted()), 11, isIndent = true)
        } else {
            helpResponse(event)
        }
    }

    override fun helpResponse(event: GenericMessageEvent): Boolean {
        if (CODES.isEmpty()) {
            try {
                loadCodes()
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
            }
        }
        if (CODES.isEmpty()) {
            event.sendMessage(EMPTY_CODES_TABLE)
        } else {
            val nick = event.bot().nick
            event.sendMessage("To convert from one currency to another:")
            event.sendMessage(helpFormat(helpCmdSyntax("%c $CURRENCY_CMD 100 USD to EUR", nick, isPrivateMsgEnabled)))
            event.sendMessage(
                helpFormat(
                    helpCmdSyntax("%c $CURRENCY_CMD 50,000 GBP to BTC", nick, isPrivateMsgEnabled)
                )
            )
            event.sendMessage("To list the supported currency codes: ")
            event.sendMessage(
                helpFormat(
                    helpCmdSyntax("%c $CURRENCY_CMD $CURRENCY_CODES_KEYWORD", nick, isPrivateMsgEnabled)
                )
            )
        }
        return true
    }

    companion object {
        // Currency command
        private const val CURRENCY_CMD = "currency"

        // Currency codes keyword
        private const val CURRENCY_CODES_KEYWORD = "codes"

        // Empty code table.
        private const val EMPTY_CODES_TABLE = "Sorry, but the currency table is empty."

        // Currency codes
        private val CODES: MutableMap<String, String> = mutableMapOf()

        // Last exchange rates table publication date
        private var pubDate = ""

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
                    val to = cmds[1].uppercase()
                    val from = cmds[3].uppercase()
                    if (CODES.contains(to) && CODES.contains(from)) {
                        try {
                            val amt = cmds[0].replace(",", "")
                            val url = URL("https://api.exchangerate.host/convert?from=$to&to=$from&amount=$amt")
                            val json = JSONObject(url.reader())

                            if (json.getBoolean("success")) {
                                PublicMessage(
                                    "${cmds[0]} ${CODES[to]} = ${json.getDouble("result")} ${CODES[from]}"
                                )
                            } else {
                                ErrorMessage("Sorry, an error occurred while converting the currencies.")
                            }
                        } catch (ignore: IOException) {
                            ErrorMessage("Sorry, an IO error occurred while converting the currencies.")
                        }
                    } else {
                        ErrorMessage("Sounds like monopoly money to me!")
                    }
                }
            } else ErrorMessage("Invalid query. Let's try again.")
        }

        /**
         * Loads the country ISO codes.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun loadCodes() {
            if (CODES.isEmpty()) {
                try {
                    val url = URL("https://api.exchangerate.host/symbols")
                    val json = JSONObject(url.reader())
                    if (json.getBoolean("success")) {
                        val symbols = json.getJSONObject("symbols")
                        for (key in symbols.keys()) {
                            CODES[key] = symbols.getJSONObject(key).getString("description")
                        }
                    }
                } catch (e: IOException) {
                    throw ModuleException(
                        "loadCodes(): IOE",
                        "An IO error has occurred while retrieving the currency table.",
                        e
                    )
                }
            }
        }
    }

    init {
        commands.add(CURRENCY_CMD)
    }
}
