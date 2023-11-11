/*
 * CryptoPricesTest.kt
 *
 * Copyright 2004-2023 Erik C. Thauvin (erik@thauvin.net)
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
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.prop
import net.thauvin.erik.crypto.CryptoPrice
import net.thauvin.erik.mobibot.modules.CryptoPrices.Companion.currentPrice
import net.thauvin.erik.mobibot.modules.CryptoPrices.Companion.getCurrencyName
import net.thauvin.erik.mobibot.modules.CryptoPrices.Companion.loadCurrencies
import org.testng.annotations.BeforeClass
import kotlin.test.Test

/**
 * The `CryptoPricesTest` class.
 */
class CryptoPricesTest {
    @BeforeClass
    @Throws(ModuleException::class)
    fun before() {
        loadCurrencies()
    }

    @Test
    @Throws(ModuleException::class)
    fun testMarketPrice() {
        var price = currentPrice(listOf("BTC"))
        assertThat(price, "currentPrice(BTC)").all {
            prop(CryptoPrice::base).isEqualTo("BTC")
            prop(CryptoPrice::currency).isEqualTo("USD")
            prop(CryptoPrice::amount).transform { it.signum() }.isGreaterThan(0)
        }

        price = currentPrice(listOf("ETH", "EUR"))
        assertThat(price, "currentPrice(ETH, EUR)").all {
            prop(CryptoPrice::base).isEqualTo("ETH")
            prop(CryptoPrice::currency).isEqualTo("EUR")
            prop(CryptoPrice::amount).transform { it.signum() }.isGreaterThan(0)
        }
    }

    @Test
    fun testGetCurrencyName() {
        assertThat(getCurrencyName("USD"), "USD").isEqualTo("United States Dollar")
        assertThat(getCurrencyName("EUR"), "EUR").isEqualTo("Euro")
    }
}
