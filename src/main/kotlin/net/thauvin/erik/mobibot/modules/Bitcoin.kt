/*
 * Bitcoin.kt
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

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.NoticeMessage
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.text.DecimalFormat

/**
 * The Bitcoin module.
 */
class Bitcoin(bot: Mobibot) : ThreadedModule(bot) {
    // Currencies
    private val currencies = listOf(
            "USD", "AUD", "BRL", "CAD", "CHF", "CLP", "CNY", "DKK", "EUR", "GBP", "HKD", "INR", "ISK", "JPY", "KRW",
            "NZD", "PLN", "RUB", "SEK", "SGD", "THB", "TRY", "TWD");

    override fun helpResponse(sender: String, isPrivate: Boolean): Boolean {
        with(bot) {
            send(sender, "To retrieve the bitcoin market price:", isPrivate)
            send(
                    sender,
                    Utils.helpFormat(
                            Utils.buildCmdSyntax(
                                    "%c $BITCOIN_CMD <USD|GBP|EUR|...>",
                                    nick,
                                    isPrivateMsgEnabled)
                    ),
                    isPrivate
            )
            send(sender, "The supported currencies are: ", isPrivate)
            @Suppress("MagicNumber")
            sendList(sender, currencies, 12, isPrivate, isIndent = true)
        }
        return true
    }

    /**
     * Returns the bitcoin market price from [Blockchain.info](https://blockchain.info/ticker).
     */
    override fun run(sender: String, cmd: String, args: String, isPrivate: Boolean) {
        with(bot) {
            val arg = args.trim().uppercase()
            @Suppress("MagicNumber")
            if (!currencies.contains(arg)) {
                helpResponse(sender, isPrivate)
            } else {
                try {
                    val messages = marketPrice(arg)
                    for (msg in messages) {
                        send(sender, msg)
                    }
                } catch (e: ModuleException) {
                    if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                    send(e.message)
                }
            }
        }
    }

    companion object {
        // Blockchain Ticker URL
        private const val TICKER_URL = "https://blockchain.info/ticker"

        // Bitcoin command
        private const val BITCOIN_CMD = "bitcoin"

        // BTC command
        private const val BTC_CMD = "btc"

        private fun JSONObject.getDecimal(key: String): String {
            return DecimalFormat("0.00").format(this.getBigDecimal(key))
        }

        /**
         * Retrieves the bitcoin market price.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun marketPrice(currency: String): List<Message> {
            val debugMessage = "marketPrice($currency)"
            val messages = mutableListOf<Message>()
            try {
                val response = Utils.urlReader(URL("$TICKER_URL"))
                val json = JSONObject(response)
                val bpi = json.getJSONObject(currency.trim().uppercase())
                val symbol = bpi.getString("symbol");
                with(messages) {
                    add(PublicMessage("BITCOIN: $symbol" + bpi.getDecimal("last") + " [$currency]"))
                    add(NoticeMessage("    15m:     $symbol" + bpi.getDecimal("15m")))
                    add(NoticeMessage("    Buy:     $symbol" + bpi.getDecimal("buy")))
                    add(NoticeMessage("    Sell:    $symbol" + bpi.getDecimal("sell")))
                }
                return messages
            } catch (e: IOException) {
                throw ModuleException(debugMessage, "An IO error has occurred retrieving the bitcoin market price.", e)
            } catch (e: NullPointerException) {
                throw ModuleException(debugMessage, "An error has occurred retrieving the bitcoin market price.", e)
            } catch (e: org.json.JSONException) {
                throw ModuleException(
                        debugMessage, "A parsing error has occurred retriving the bitcoin market price.", e)
            }
        }
    }

    init {
        commands.add(BITCOIN_CMD)
        commands.add(BTC_CMD)
    }
}
