/*
 * CryptoPricesTest.kt
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
import net.thauvin.erik.crypto.CryptoPrice.Companion.marketPrice
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.testng.annotations.Test

/**
 * The `CryptoPricesTest` class.
 */
class CryptoPricesTest {
    @Test
    @Throws(ModuleException::class)
    fun testMarketPrice() {
        var price = marketPrice("BTC", "USD")
        assertThat(price.base).describedAs("is BTC").isEqualTo("BTC")
        assertThat(price.currency).describedAs("is USD").isEqualTo("USD")
        assertThat(price.amount).describedAs("BTC > 0").isGreaterThan(0.00)

        price = marketPrice("ETH", "EUR")
        assertThat(price.base).describedAs("is ETH").isEqualTo("ETH")
        assertThat(price.currency).describedAs("is EUR").isEqualTo("EUR")
        assertThat(price.amount).describedAs("ETH > 0").isGreaterThan(0.00)

        price = marketPrice("ETH2", "GBP")
        assertThat(price.base).describedAs("is ETH2").isEqualTo("ETH2")
        assertThat(price.currency).describedAs("is GBP").isEqualTo("GBP")
        assertThat(price.amount).describedAs("ETH2 > 0").isGreaterThan(0.00)

        assertThatThrownBy { marketPrice("FOO", "USD") }
            .describedAs("FOO")
            .isInstanceOf(CryptoException::class.java)
            .hasMessageContaining("Invalid base currency")

        assertThatThrownBy { marketPrice("FOO", "BAR") }
            .describedAs("FOO-BAR")
            .isInstanceOf(CryptoException::class.java)
            .hasMessageContaining("Invalid currency (BAR)")
    }
}
