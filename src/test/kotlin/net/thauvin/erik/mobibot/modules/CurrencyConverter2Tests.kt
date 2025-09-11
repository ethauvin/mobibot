/*
 * CurrencyConverter2Tests.kt
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
import assertk.assertions.contains
import assertk.assertions.isInstanceOf
import assertk.assertions.matches
import assertk.assertions.prop
import net.thauvin.erik.frankfurter.FrankfurterUtils
import net.thauvin.erik.mobibot.modules.CurrencyConverter2.Companion.convertCurrency
import net.thauvin.erik.mobibot.modules.CurrencyConverter2.Companion.loadCurrencyCodes
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
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
class CurrencyConverter2Tests {
    companion object {
        @RegisterExtension
        @JvmField
        @Suppress("unused")
        val loggingExtension = LoggingExtension(FrankfurterUtils.LOGGER)
    }

    init {
        loadCurrencyCodes()
    }

    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun `USD to CAD`() {
            val currencyConverter = CurrencyConverter2()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            currencyConverter.commandResponse("channel", "currency", "1 USD to CAD", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value)
                .matches("\\$1.00 \\(United States Dollar\\) = \\$\\d+\\.\\d+ \\(Canadian Dollar\\)".toRegex())
        }

        @Test
        fun `USD to GBP`() {
            val currencyConverter = CurrencyConverter2()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            currencyConverter.commandResponse("channel", "currency", "1 usd to gbp", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value)
                .matches("\\$1.00 \\(United States Dollar\\) = £0\\.\\d+ \\(British Pound\\)".toRegex())
        }
    }

    @Nested
    @DisplayName("Currency Converter Tests")
    inner class CurrencyConverterTests {
        @Test
        fun `Convert CAD to USD`() {
            assertThat(
                convertCurrency("123,456.7890 CAD to USD").msg,
                "convertCurrency(123,456.7890 CAD to USD)"
            ).matches("\\$123,456.789 \\(Canadian Dollar\\) = \\$\\d+,\\d+\\.\\d+ \\(United States Dollar\\)".toRegex())
        }

        @Test
        fun `Convert USD to EUR`() {
            assertThat(
                convertCurrency("100 USD to EUR").msg,
                "convertCurrency(100 USD to EUR)"
            ).matches("\\$100.00 \\(United States Dollar\\) = \\d+,\\d+ € \\(Euro\\)".toRegex())
        }

        @Test
        fun `Convert USD to CNY`() {
            assertThat(
                convertCurrency("1 USD to CNY").msg,
                "convertCurrency(1 USD to CNY)"
            ).matches("\\$1.00 \\(United States Dollar\\) = ¥\\d+\\.\\d+ \\(Chinese Renminbi Yuan\\)".toRegex())
        }

        @Test
        fun `Convert USD to Invalid Currency`() {
            assertThat(convertCurrency("100 USD to FOO"), "convertCurrency(100 USD to FOO)").all {
                prop(Message::msg)
                    .contains("Sounds like monopoly money to me! Try looking up the supported currency codes.")
                isInstanceOf(ErrorMessage::class.java)
            }
        }

        @Test
        fun `Convert USD to USD`() {
            assertThat(convertCurrency("100 USD to USD"), "convertCurrency(100 USD to USD)").all {
                prop(Message::msg).contains("You're kidding, right?")
                isInstanceOf(PublicMessage::class.java)
            }
        }

        @Test
        fun `Invalid Query should throw exception`() {
            assertThat(convertCurrency("100 USD"), "convertCurrency(100 USD)").all {
                prop(Message::msg).contains("Invalid query.")
                isInstanceOf(ErrorMessage::class.java)
            }
        }
    }
}
