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

import net.thauvin.erik.crypto.CryptoException
import net.thauvin.erik.crypto.CryptoPrice
import net.thauvin.erik.crypto.CryptoPrice.Companion.spotPrice
import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.msg.PublicMessage

/**
 * The Cryptocurrency Prices  module.
 */
class CryptoPrices(bot: Mobibot) : ThreadedModule(bot) {
    /**
     * Returns the cryptocurrency market price from [Coinbase](https://developers.coinbase.com/api/v2#get-spot-price).
     */
    override fun run(sender: String, cmd: String, args: String, isPrivate: Boolean) {
        val debugMessage = "crypto($cmd $args)"
        with(bot) {
            if (args.matches("\\w+( [a-zA-Z]{3}+)?".toRegex())) {
                try {
                    val price = currentPrice(args.split(' '))
                    val amount = try {
                        price.toCurrency()
                    } catch (ignore: IllegalArgumentException) {
                        price.amount
                    }
                    send(sender, PublicMessage("${price.base}: $amount [${price.currency}]"))
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

        /**
         * Get current market price.
         */
        fun currentPrice(args: List<String>): CryptoPrice {
            return if (args.size == 2)
                spotPrice(args[0], args[1])
            else
                spotPrice(args[0])
        }
    }

    init {
        commands.add(CRYPTO_CMD)
        with(help) {
            add("To retrieve a cryptocurrency's market price:")
            add(helpFormat("%c $CRYPTO_CMD <symbol> [<currency>]"))
            add("For example:")
            add(helpFormat("%c $CRYPTO_CMD BTC"))
            add(helpFormat("%c $CRYPTO_CMD ETH EUR"))
            add(helpFormat("%c $CRYPTO_CMD ETH2 GPB"))
        }
    }
}
