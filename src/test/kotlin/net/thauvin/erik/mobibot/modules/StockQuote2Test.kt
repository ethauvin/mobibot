/*
 * StockQuoteTest.kt
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
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.*
import net.thauvin.erik.mobibot.ExceptionSanitizer.sanitize
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.modules.StockQuote2.Companion.getQuote
import net.thauvin.erik.mobibot.modules.StockQuote2.Companion.lookup
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.pircbotx.hooks.types.GenericMessageEvent
import kotlin.test.Test

class StockQuote2Test : LocalProperties() {
    private val apiKey = getProperty(StockQuote2.API_KEY_PROP)

    private fun getSanitizedQuote(symbol: String, apiKey: String): List<Message> {
        try {
            return getQuote(symbol, apiKey)
        } catch (e: ModuleException) {
            // Avoid displaying api keys in CI logs
            if ("true" == System.getenv("CI")) {
                throw e.sanitize(apiKey)
            } else {
                throw e
            }
        }
    }

    private fun getSanitizedLookup(keywords: String, apiKey: String): List<Message> {
        try {
            return lookup(keywords, apiKey)
        } catch (e: ModuleException) {
            // Avoid displaying api keys in CI logs
            if ("true" == System.getenv("CI")) {
                throw e.sanitize(apiKey)
            } else {
                throw e
            }
        }
    }

    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun `API Key is missing`() {
            val stockQuote = StockQuote2()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            stockQuote.commandResponse("channel", "stock", "goog", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value)
                .isEqualTo("${StockQuote2.SERVICE_NAME} is disabled. The API key is missing.")
        }
    }

    @Nested
    @DisplayName("Get Quote Tests")
    inner class GetQuoteTests {
        @Test
        @Throws(ModuleException::class)
        fun `API key should not be empty`() {
            assertFailure { getSanitizedQuote("test", "") }.isInstanceOf(ModuleException::class.java)
                .messageContains("disabled")
        }

        @Test
        @Throws(ModuleException::class)
        fun `Symbol should not be empty`() {
            assertThat(getSanitizedQuote("", "apikey").first(), "getQuote(empty)").all {
                isInstanceOf(ErrorMessage::class.java)
                prop(Message::msg).isEqualTo(StockQuote2.INVALID_SYMBOL)
            }
        }

        @Test
        @Throws(ModuleException::class)
        fun `Get stock quote for Apple`() {
            val symbol = "aapl"
            val messages = getSanitizedQuote(symbol, apiKey)
            assertThat(messages, "response not empty").isNotEmpty()
            assertThat(messages, "getQuote($symbol)").index(0).prop(Message::msg)
                .isEqualTo("Symbol: \u0002AAPL\u0002")
            assertThat(messages, "getQuote($symbol)").index(1).prop(Message::msg)
                .matches("\\s+Price:\\s+\\d+\\.\\d+.*".toRegex())
            assertThat(messages, "getQuote($symbol)").index(8).prop(Message::msg)
                .matches("\\s+Latest:\\s+\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2} UTC".toRegex())
        }

        @Test
        @Throws(ModuleException::class)
        fun `Get stock quote for Google`() {
            val symbol = "goog"
            val messages = getSanitizedQuote(symbol, apiKey)
            assertThat(messages, "response not empty").isNotEmpty()
            assertThat(messages, "getQuote($symbol)").index(0).prop(Message::msg)
                .equals("Symbol: GOOG")
        }

        @Test
        @Throws(ModuleException::class)
        fun `Invalid symbol should throw exception`() {
            val symbol = "foobar"
            assertThat(getSanitizedQuote(symbol, apiKey).first(), "getQuote($symbol)").all {
                isInstanceOf(ErrorMessage::class.java)
                prop(Message::msg).isEqualTo(StockQuote2.INVALID_SYMBOL)
            }
        }
    }

    @Nested
    @DisplayName("Lookup Tests")
    inner class LookupTests {
        @Test
        @Throws(ModuleException::class)
        fun `Lookup alphabet`() {
            val keywords = "alphabet inc"
            val messages = getSanitizedLookup(keywords, apiKey)
            assertThat(messages, "messages should not be empty").isNotEmpty()
            assertThat(messages, "lookup($keywords)").index(0).prop(Message::msg)
                .isEqualTo("Search results for: \u0002alphabet inc\u0002")

            var hasGoog = false
            for (msg in messages) {
                if (msg.msg.matches("\\s+\u0002GOOG\u0002: .*".toRegex())) {
                    hasGoog = true
                    break
                }
            }
            assertThat(hasGoog, "GOOG not found").isTrue()
        }

        @Test
        @Throws(ModuleException::class)
        fun `Lookup empty keywords`() {
            val keywords = ""
            val messages = getSanitizedLookup(keywords, apiKey)
            assertThat(messages, "response not empty").isNotEmpty()
            assertThat(messages, "lookup($keywords)").index(0).prop(Message::msg)
                .isEqualTo("Please specify at least one search term.")
        }

        @Test
        @Throws(ModuleException::class)
        fun `Lookup not found`() {
            val keywords = "foo motors"
            val messages = getSanitizedLookup(keywords, apiKey)
            assertThat(messages, "response not empty").isNotEmpty()
            assertThat(messages, "lookup($keywords)").index(0).prop(Message::msg)
                .isEqualTo("Nothing found for: \u0002foo motors\u0002")
        }
    }
}
