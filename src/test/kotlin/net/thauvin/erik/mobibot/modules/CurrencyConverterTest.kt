/*
 * CurrencyConverterTest.kt
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
import assertk.assertions.contains
import assertk.assertions.isInstanceOf
import assertk.assertions.matches
import assertk.assertions.prop
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.modules.CurrencyConverter.Companion.convertCurrency
import net.thauvin.erik.mobibot.modules.CurrencyConverter.Companion.loadSymbols
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import kotlin.test.Test

class CurrencyConverterTest : LocalProperties() {
    init {
        val apiKey = getProperty(CurrencyConverter.API_KEY_PROP)
        loadSymbols(apiKey)
    }

    @Test
    fun testConvertCurrency() {
        val apiKey = getProperty(CurrencyConverter.API_KEY_PROP)
        assertThat(
            convertCurrency(apiKey, "100 USD to EUR").msg,
            "convertCurrency(100 USD to EUR)"
        ).matches("100 United States Dollar = \\d{2,3}\\.\\d{2,3} Euro".toRegex())
        assertThat(
            convertCurrency(apiKey, "1 USD to GBP").msg,
            "convertCurrency(1 USD to BGP)"
        ).matches("1 United States Dollar = 0\\.\\d{2,3} Pound Sterling".toRegex())
        assertThat(
            convertCurrency(apiKey, "100,000.00 CAD to USD").msg,
            "convertCurrency(100,000.00 GBP to USD)"
        ).matches("100,000.00 Canadian Dollar = \\d+\\.\\d{2,3} United States Dollar".toRegex())
        assertThat(convertCurrency(apiKey, "100 USD to USD"), "convertCurrency(100 USD to USD)").all {
            prop(Message::msg).contains("You're kidding, right?")
            isInstanceOf(PublicMessage::class.java)
        }
        assertThat(convertCurrency(apiKey, "100 USD"), "convertCurrency(100 USD)").all {
            prop(Message::msg).contains("Invalid query.")
            isInstanceOf(ErrorMessage::class.java)
        }
    }
}
