/*
 * CurrencyConverterTest.kt
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

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isInstanceOf
import assertk.assertions.matches
import assertk.assertions.prop
import net.thauvin.erik.mobibot.modules.CurrencyConverter.Companion.convertCurrency
import net.thauvin.erik.mobibot.modules.CurrencyConverter.Companion.loadSymbols
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * The `CurrencyConvertTest` class.
 */
class CurrencyConverterTest {
    @BeforeClass
    @Throws(ModuleException::class)
    fun before() {
        loadSymbols()
    }

    @Test
    fun testConvertCurrency() {
        assertThat(
            convertCurrency("100 USD to EUR").msg,
            "100 USD to EUR"
        ).matches("100 United States Dollar = \\d{2,3}\\.\\d+ Euro".toRegex())
        assertThat(
            convertCurrency("100,000.00 GBP to BTC").msg,
            "100 USD to EUR"
        ).matches("100,000.00 British Pound Sterling = \\d{1,2}\\.\\d+ Bitcoin".toRegex())
        assertThat(convertCurrency("100 USD to USD"), "100 USD to USD").all {
            prop(Message::msg).contains("You're kidding, right?")
            isInstanceOf(PublicMessage::class.java)
        }
        assertThat(convertCurrency("100 USD"), "100 USD").all {
            prop(Message::msg).contains("Invalid query.")
            isInstanceOf(ErrorMessage::class.java)
        }
    }
}
