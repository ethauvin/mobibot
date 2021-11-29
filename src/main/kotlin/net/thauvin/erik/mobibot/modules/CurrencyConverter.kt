/*
 * CurrencyConverter.kt
 *
 * Copyright (c) 2004-2021, Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.buildCmdSyntax
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.Utils.today
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.jdom2.JDOMException
import org.jdom2.input.SAXBuilder
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.xml.XMLConstants

/**
 * The CurrencyConverter module.
 */
class CurrencyConverter : ThreadedModule() {
    private val logger: Logger = LoggerFactory.getLogger(CurrencyConverter::class.java)

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        synchronized(this) {
            if (pubDate != today()) {
                EXCHANGE_RATES.clear()
            }
        }
        super.commandResponse(channel, cmd, args, event)
    }

    /**
     * Converts the specified currencies.
     */
    override fun run(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (EXCHANGE_RATES.isEmpty()) {
            try {
                loadRates()
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
            }
        }

        if (EXCHANGE_RATES.isEmpty()) {
            event.respond(EMPTY_RATE_TABLE)
        } else if (args.matches("\\d+([,\\d]+)?(\\.\\d+)? [a-zA-Z]{3}+ to [a-zA-Z]{3}+".toRegex())) {
            val msg = convertCurrency(args)
            event.respond(msg.msg)
            if (msg.isError) {
                helpResponse(event)
            }
        } else if (args.contains(CURRENCY_RATES_KEYWORD)) {
            event.sendMessage("The reference rates for ${pubDate.bold()} are:")
            event.sendList(currencyRates(), 3, "   ", isIndent = true)
        } else {
            helpResponse(event)
        }
    }

    override fun helpResponse(event: GenericMessageEvent): Boolean {
        if (EXCHANGE_RATES.isEmpty()) {
            try {
                loadRates()
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
            }
        }
        if (EXCHANGE_RATES.isEmpty()) {
            event.sendMessage(EMPTY_RATE_TABLE)
        } else {
            val nick = event.bot().nick
            event.sendMessage("To convert from one currency to another:")
            event.sendMessage(helpFormat(buildCmdSyntax("%c $CURRENCY_CMD 100 USD to EUR", nick, isPrivateMsgEnabled)))
            event.sendMessage("For a listing of current reference rates:")
            event.sendMessage(
                helpFormat(
                    buildCmdSyntax("%c $CURRENCY_CMD $CURRENCY_RATES_KEYWORD", nick, isPrivateMsgEnabled)
                )
            )
            event.sendMessage("The supported currencies are: ")
            event.sendList(ArrayList(EXCHANGE_RATES.keys), 11, isIndent = true)
        }
        return true
    }

    companion object {
        // Currency command
        private const val CURRENCY_CMD = "currency"

        // Rates keyword
        private const val CURRENCY_RATES_KEYWORD = "rates"

        // Empty rate table.
        private const val EMPTY_RATE_TABLE = "Sorry, but the exchange rate table is empty."

        // Exchange rates
        private val EXCHANGE_RATES: MutableMap<String, String> = mutableMapOf()

        // Exchange rates table URL
        private const val EXCHANGE_TABLE_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml"

        // Last exchange rates table publication date
        private var pubDate = ""

        private fun Double.formatCurrency(currency: String): String =
            NumberFormat.getCurrencyInstance(Locale.getDefault(Locale.Category.FORMAT)).let {
                it.currency = Currency.getInstance(currency)
                it.format(this)
            }

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
                    if (EXCHANGE_RATES.containsKey(to) && EXCHANGE_RATES.containsKey(from)) {
                        try {
                            val amt = cmds[0].replace(",", "").toDouble()
                            val doubleFrom = EXCHANGE_RATES[to]!!.toDouble()
                            val doubleTo = EXCHANGE_RATES[from]!!.toDouble()
                            PublicMessage(
                                amt.formatCurrency(to) + " = " + (amt * doubleTo / doubleFrom).formatCurrency(from)
                            )
                        } catch (e: NumberFormatException) {
                            ErrorMessage("Let's try with some real numbers next time, okay?")
                        }
                    } else {
                        ErrorMessage("Sounds like monopoly money to me!")
                    }
                }
            } else ErrorMessage("Invalid query. Let's try again.")
        }


        @JvmStatic
        fun currencyRates(): List<String> {
            val rates = buildList {
                for ((key, value) in EXCHANGE_RATES.toSortedMap()) {
                    add("$key: ${value.padStart(8)}")
                }
            }
            return rates
        }

        @JvmStatic
        @Throws(ModuleException::class)
        fun loadRates() {
            if (EXCHANGE_RATES.isEmpty()) {
                try {
                    val builder = SAXBuilder()
                    // See https://rules.sonarsourcecom/java/tag/owasp/RSPEC-2755
                    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "")
                    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
                    builder.ignoringElementContentWhitespace = true
                    val doc = builder.build(URL(EXCHANGE_TABLE_URL))
                    val root = doc.rootElement
                    val ns = root.getNamespace("")
                    val cubeRoot = root.getChild("Cube", ns)
                    val cubeTime = cubeRoot.getChild("Cube", ns)
                    pubDate = cubeTime.getAttribute("time").value
                    val cubes = cubeTime.children
                    for (cube in cubes) {
                        EXCHANGE_RATES[cube.getAttribute("currency").value] = cube.getAttribute("rate").value
                    }
                    EXCHANGE_RATES["EUR"] = "1"
                } catch (e: JDOMException) {
                    throw ModuleException(
                        "loadRates(): JDOM",
                        "An JDOM parsing error has occurred while parsing the exchange rates table.",
                        e
                    )
                } catch (e: IOException) {
                    throw ModuleException(
                        "loadRates(): IOE",
                        "An IO error has occurred while parsing the exchange rates table.",
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
