/*
 * CryptoPricesTests.kt
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

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import net.thauvin.erik.crypto.CryptoPrice
import net.thauvin.erik.mobibot.modules.CryptoPrices.Companion.currentPrice
import net.thauvin.erik.mobibot.modules.CryptoPrices.Companion.getCurrencyName
import net.thauvin.erik.mobibot.modules.CryptoPrices.Companion.loadCurrencies
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.pircbotx.hooks.types.GenericMessageEvent
import rife.bld.extension.testing.LoggingExtension
import kotlin.test.Test

@ExtendWith(LoggingExtension::class)
class CryptoPricesTests {
    companion object {
        @RegisterExtension
        @JvmField
        val loggingExtension = LoggingExtension(CryptoPrice.logger)
    }

    init {
        loadCurrencies()
    }

    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun `Current price for BTC`() {
            val cryptoPrices = CryptoPrices()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            cryptoPrices.commandResponse("channel", "crypto", "btc", event)

            Mockito.verify(event, Mockito.times(1)).respond(captor.capture())
            assertThat(captor.value).startsWith("BTC current price is $")
        }

        @Test
        fun `Current price for BTC in EUR`() {
            val cryptoPrices = CryptoPrices()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            cryptoPrices.commandResponse("channel", "crypto", "eth eur", event)

            Mockito.verify(event, Mockito.times(1)).respond(captor.capture())
            assertThat(captor.value).matches(Regex("ETH current price is €.* \\[Euro]"))
        }

        @Test
        fun `Invalid crypto symbol`() {
            val cryptoPrices = CryptoPrices()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            cryptoPrices.commandResponse("channel", "crypto", "foo", event)

            Mockito.verify(event, Mockito.times(1)).respond(captor.capture())
            assertThat(captor.value)
                .isEqualTo("${CryptoPrices.DEFAULT_ERROR_MESSAGE}: not found")
        }
    }

    @Nested
    @DisplayName("Currency Name Tests")
    inner class CurrencyNameTests {
        @Test
        fun `Currency name for USD`() {
            assertThat(getCurrencyName("USD"), "USD").isEqualTo("United States Dollar")
        }

        @Test
        fun `Currency name for EUR`() {
            assertThat(getCurrencyName("EUR"), "EUR").isEqualTo("Euro")
        }
    }

    @Nested
    @DisplayName("Current Price Tests")
    inner class CurrentPriceTests {
        @Test
        @Throws(ModuleException::class)
        fun `Current price for Bitcoin`() {
            val price = currentPrice(listOf("BTC"))
            assertThat(price, "currentPrice(BTC)").all {
                prop(CryptoPrice::base).isEqualTo("BTC")
                prop(CryptoPrice::currency).isEqualTo("USD")
                prop(CryptoPrice::amount).transform { it.signum() }.isGreaterThan(0)
            }
        }

        @Test
        @Throws(ModuleException::class)
        fun `Current price for Ethereum in Euro`() {
            val price = currentPrice(listOf("ETH", "EUR"))
            assertThat(price, "currentPrice(ETH, EUR)").all {
                prop(CryptoPrice::base).isEqualTo("ETH")
                prop(CryptoPrice::currency).isEqualTo("EUR")
                prop(CryptoPrice::amount).transform { it.signum() }.isGreaterThan(0)
            }
        }
    }
}
