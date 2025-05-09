/*
 * Gemini2Test.kt
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

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.*
import net.thauvin.erik.mobibot.DisableOnCi
import net.thauvin.erik.mobibot.LocalProperties
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.pircbotx.hooks.types.GenericMessageEvent
import kotlin.test.Test

class Gemini2Test : LocalProperties() {
    @Nested
    @DisplayName("Chat Tests")
    inner class ChatTests {
        private val apiKey = getProperty(Gemini2.GEMINI_API_KEY)

        @Test
        @DisableOnCi
        fun chatHttpRequestInJavascript() {
            val maxTokens = try {
                getProperty(Gemini2.MAX_TOKENS_PROP).toInt()
            } catch (_: NumberFormatException) {
                1024
            }

            assertThat(
                Gemini2.chat(
                    "javascript function to make a request with XMLHttpRequest, just code",
                    apiKey,
                    maxTokens
                )
            ).isNotNull().contains("```javascript")
        }

        @Test
        @DisableOnCi
        fun chatEncodeUrlInJava() {
            assertThat(
                Gemini2.chat("encode a url in java, one line, just code", apiKey, 60)
            ).isNotNull().contains("UrlEncoder", true)
        }
    }

    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun moduleMisconfigured() {
            val gemini2 = Gemini2()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            gemini2.commandResponse("channel", "gemini", "1 liter to gallon", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value).isEqualTo("The ${Gemini2.GEMINI_NAME} module is misconfigured.")
        }
    }

    @Nested
    @DisplayName("API Keys Test")
    inner class ApiKeysTest {
        @Test
        fun invalidApiKey() {
            assertFailure { Gemini2.chat("1 liter to gallon", "foo", 40) }
                .isInstanceOf(ModuleException::class.java)
                .hasMessage(Gemini2.IO_ERROR)
        }

        @Test
        fun emptyApiKey() {
            assertFailure { Gemini2.chat("1 liter to gallon", "", 40) }
                .isInstanceOf(ModuleException::class.java)
                .hasMessage(Gemini2.API_KEY_ERROR)
        }
    }
}
