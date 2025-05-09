/*
 * CurrencyConverterTest.kt
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
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
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.modules.CurrencyConverter.Companion.convertCurrency
import net.thauvin.erik.mobibot.modules.CurrencyConverter.Companion.loadSymbols
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.pircbotx.hooks.types.GenericMessageEvent
import kotlin.test.Test

class CurrencyConverterTest : LocalProperties() {
    init {
        val apiKey = getProperty(CurrencyConverter.API_KEY_PROP)
        loadSymbols(apiKey)
    }

    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun `USD to CAD`() {
            val currencyConverter = CurrencyConverter()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            currencyConverter.properties.put(
                CurrencyConverter.API_KEY_PROP, getProperty(CurrencyConverter.API_KEY_PROP)
            )
            currencyConverter.commandResponse("channel", "currency", "1 USD to CAD", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value).matches("1 United States Dollar = \\d+\\.\\d{2,3} Canadian Dollar".toRegex())
        }

        @Test
        fun `API Key is not specified`() {
            val currencyConverter = CurrencyConverter()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            currencyConverter.commandResponse("channel", "currency", "1 USD to CAD", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value).isEqualTo(CurrencyConverter.ERROR_MESSAGE_NO_API_KEY)
        }
    }

    @Nested
    @DisplayName("Currency Converter Tests")
    inner class CurrencyConverterTests {
        private val apiKey = getProperty(CurrencyConverter.API_KEY_PROP)

        @Test
        fun `Convert CAD to USD`() {
            assertThat(
                convertCurrency(apiKey, "100,000.00 CAD to USD").msg,
                "convertCurrency(100,000.00 GBP to USD)"
            ).matches("100,000.00 Canadian Dollar = \\d+\\.\\d{2,3} United States Dollar".toRegex())
        }

        @Test
        fun `Convert USD to EUR`() {
            assertThat(
                convertCurrency(apiKey, "100 USD to EUR").msg,
                "convertCurrency(100 USD to EUR)"
            ).matches("100 United States Dollar = \\d{2,3}\\.\\d{2,3} Euro".toRegex())
        }

        @Test
        fun `Convert USD to GBP`() {
            assertThat(
                convertCurrency(apiKey, "1 USD to GBP").msg,
                "convertCurrency(1 USD to BGP)"
            ).matches("1 United States Dollar = 0\\.\\d{2,3} Pound Sterling".toRegex())
        }

        @Test
        fun `Convert USD to USD`() {
            assertThat(convertCurrency(apiKey, "100 USD to USD"), "convertCurrency(100 USD to USD)").all {
                prop(Message::msg).contains("You're kidding, right?")
                isInstanceOf(PublicMessage::class.java)
            }
        }

        @Test
        fun `Invalid Query should throw exception`() {
            assertThat(convertCurrency(apiKey, "100 USD"), "convertCurrency(100 USD)").all {
                prop(Message::msg).contains("Invalid query.")
                isInstanceOf(ErrorMessage::class.java)
            }
        }
    }
}
