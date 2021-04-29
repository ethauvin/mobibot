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

import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.apache.commons.lang3.StringUtils
import org.jdom2.JDOMException
import org.jdom2.input.SAXBuilder
import java.io.IOException
import java.net.URL
import java.text.NumberFormat
import javax.xml.XMLConstants

/**
 * The CurrentConverter module.
 */
class CurrencyConverter(bot: Mobibot) : ThreadedModule(bot) {
    override fun commandResponse(
        sender: String,
        cmd: String,
        args: String,
        isPrivate: Boolean
    ) {
        synchronized(this) {
            if (pubDate != Utils.today()) {
                EXCHANGE_RATES.clear()
            }
        }
        super.commandResponse(sender, cmd, args, isPrivate)
    }

    /**
     * Converts the specified currencies.
     */
    override fun run(sender: String, cmd: String, args: String, isPrivate: Boolean) {
        bot.apply {
            if (EXCHANGE_RATES.isEmpty()) {
                try {
                    loadRates()
                } catch (e: ModuleException) {
                    if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                }
            }

            if (EXCHANGE_RATES.isEmpty()) {
                send(sender, EMPTY_RATE_TABLE, true)
            } else if (args.matches("\\d+([,\\d]+)?(\\.\\d+)? [a-zA-Z]{3}+ to [a-zA-Z]{3}+".toRegex())) {
                val msg = convertCurrency(args)
                send(sender, msg)
                if (msg.isError) {
                    helpResponse(sender, isPrivate)
                }
            } else if (args.contains(CURRENCY_RATES_KEYWORD)) {
                send(sender, "The currency rates for ${Utils.bold(pubDate)} are:", isPrivate)
                sendList(sender, currencyRates(), 3, isPrivate, isIndent = true)
            } else {
                helpResponse(sender, isPrivate)
            }
        }
    }

    override fun helpResponse(sender: String, isPrivate: Boolean): Boolean {
        with(bot) {
            if (EXCHANGE_RATES.isEmpty()) {
                try {
                    loadRates()
                } catch (e: ModuleException) {
                    if (logger.isDebugEnabled) logger.debug(e.debugMessage, e)
                }
            }
            if (EXCHANGE_RATES.isEmpty()) {
                send(sender, EMPTY_RATE_TABLE, isPrivate)
            } else {
                send(sender, "To convert from one currency to another:", isPrivate)
                send(
                    sender,
                    Utils.helpFormat(
                        Utils.buildCmdSyntax("%c $CURRENCY_CMD 100 USD to EUR", nick, isPrivateMsgEnabled)
                    ),
                    isPrivate
                )
                send(sender, "For a listing of current rates:", isPrivate)
                send(
                    sender,
                    Utils.helpFormat(
                        Utils.buildCmdSyntax("%c $CURRENCY_CMD $CURRENCY_RATES_KEYWORD", nick, isPrivateMsgEnabled)
                    ),
                    isPrivate
                )
                send(sender, "The supported currencies are: ", isPrivate)
                sendList(sender, ArrayList(EXCHANGE_RATES.keys), 11, isPrivate, isIndent = true)
            }
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
                                NumberFormat.getCurrencyInstance(Constants.LOCALE).format(amt).substring(1)
                                    + " ${cmds[1].uppercase()} = "
                                    + NumberFormat.getCurrencyInstance(Constants.LOCALE)
                                    .format(amt * doubleTo / doubleFrom).substring(1)
                                    + " ${cmds[3].uppercase()}"
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
            val rates = mutableListOf<String>()
            for ((key, value) in EXCHANGE_RATES) {
                rates.add("  $key: ${StringUtils.leftPad(value, 8)}")
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
                        e.message,
                        "An JDOM parsing error has occurred while parsing the exchange rates table.",
                        e
                    )
                } catch (e: IOException) {
                    throw ModuleException(
                        e.message,
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
