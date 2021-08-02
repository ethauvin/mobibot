/*
 * CurrencyConverterTest.kt
 *
 * Copyright (c) 2004-2019, Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.modules.CurrencyConverter.Companion.convertCurrency
import net.thauvin.erik.mobibot.modules.CurrencyConverter.Companion.currencyRates
import net.thauvin.erik.mobibot.modules.CurrencyConverter.Companion.loadRates
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * The `CurrencyConvertTest` class.
 */
class CurrencyConverterTest {
    @BeforeClass
    @Throws(ModuleException::class)
    fun before() {
        loadRates()
    }

    @Test
    fun testConvertCurrency() {
        assertThat(convertCurrency("100 USD to EUR").msg)
            .describedAs("100 USD to EUR").matches("\\$100\\.00 = €\\d{2,3}\\.\\d{2}")
        assertThat(convertCurrency("100 USD to USD").msg).describedAs("100 USD to USD")
            .contains("You're kidding, right?")
        assertThat(convertCurrency("100 USD").msg).describedAs("100 USD").contains("Invalid query.")
        val rates = currencyRates()
        assertThat(rates.size).describedAs("currencyRates.size == 33").isEqualTo(33)
        assertThat(rates).describedAs("currencyRates(EUR)").contains("EUR:        1")
        assertThat(rates.stream().anyMatch { it.matches("USD: .*".toRegex()) })
            .describedAs("currencyRates(USD)").isTrue
    }
}
