/*
 * CryptoPrices.kt
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

import okhttp3.OkHttpClient
import okhttp3.Request
import net.thauvin.erik.crypto.CryptoPrice.Companion.marketPrice
import net.thauvin.erik.crypto.CryptoException
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
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.ZoneId
import java.time.ZoneOffset

data class Price(val base: String, val currency: String, val amount: Double)

/**
 * The Cryptocurrency Prices  module.
 */
class CryptoPrices(bot: Mobibot) : ThreadedModule(bot) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)
    val decimalFormat = DecimalFormat("0.00")

    /**
     * Returns the cryptocurrency market price from [Coinbase](https://developers.coinbase.com/api/v2#get-spot-price).
     */
    override fun run(sender: String, cmd: String, args: String, isPrivate: Boolean) {
        val debugMessage = "crypto($cmd $args)"
        with(bot) {
            if (args.matches("\\w+( [a-zA-Z]{3}+)?".toRegex())) {
                val params = args.trim().split(" ");
                try {
                    val currency = if (params.size == 2) params[1] else "USD"
                    val price = marketPrice(params[0], currency)
                    send(sender, PublicMessage("${price.base}: ${price.amount} [${price.currency}]"))
                } catch (e: CryptoException) {
                    if (logger.isWarnEnabled) logger.warn("$debugMessage => ${e.statusCode}", e)
                    send(e.message)
                } catch (e: Exception) {
                    if (logger.isErrorEnabled) logger.error(debugMessage, e)
                    send("An error has occurred while retrieving the cryptocurrency market price.")
                }
            } else {
                helpResponse(sender, isPrivate)
            }
        }
    }

    companion object {
        // Crypto command
        private const val CRYPTO_CMD = "crypto"
    }

    init {
        commands.add(CRYPTO_CMD)
        help.add("To retrieve a cryptocurrency's market price:")
        help.add(Utils.helpFormat("%c $CRYPTO_CMD <symbol> [<currency>]"))
        help.add("For example:")
        help.add(Utils.helpFormat("%c $CRYPTO_CMD BTC"))
        help.add(Utils.helpFormat("%c $CRYPTO_CMD ETH EUR"))
        help.add(Utils.helpFormat("%c $CRYPTO_CMD ETH2 GPB"))
    }
}
