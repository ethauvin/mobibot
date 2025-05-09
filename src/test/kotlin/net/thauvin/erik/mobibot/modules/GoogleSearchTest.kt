/*
 * GoogleSearchTest.kt
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
import net.thauvin.erik.mobibot.DisableOnCi
import net.thauvin.erik.mobibot.ExceptionSanitizer.sanitize
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.modules.GoogleSearch.Companion.searchGoogle
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.pircbotx.hooks.types.GenericMessageEvent
import kotlin.test.Test

class GoogleSearchTest : LocalProperties() {
    private val apiKey = getProperty(GoogleSearch.API_KEY_PROP)
    private val cseKey = getProperty(GoogleSearch.CSE_KEY_PROP)

    @Throws(ModuleException::class)
    fun sanitizedSearch(query: String, apiKey: String, cseKey: String): List<Message> {
        try {
            return searchGoogle(query, apiKey, cseKey)
        } catch (e: ModuleException) {
            // Avoid displaying api keys in CI logs
            if ("true" == System.getenv("CI")) {
                throw e.sanitize(apiKey, cseKey)
            } else {
                throw e
            }
        }
    }

    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun `API Keys are missing`() {
            val googleSearch = GoogleSearch()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)
            val user = Mockito.mock(org.pircbotx.User::class.java)

            whenever(event.user).thenReturn(user)
            whenever(user.nick).thenReturn("mock")

            googleSearch.commandResponse("channel", "google", "seattle seahawks", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value)
                .isEqualTo("${GoogleSearch.SERVICE_NAME} is disabled. The API keys are missing.")
        }

        @Test
        fun `No results found`() {
            val googleSearch = GoogleSearch()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)
            val user = Mockito.mock(org.pircbotx.User::class.java)

            whenever(event.user).thenReturn(user)
            whenever(user.nick).thenReturn("mock")

            googleSearch.properties.put(GoogleSearch.API_KEY_PROP, apiKey)
            googleSearch.properties.put(GoogleSearch.CSE_KEY_PROP, cseKey)

            googleSearch.commandResponse("channel", "google", "\"foobarbarfoofoobarblahblah\"", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value)
                .isEqualTo("\u000304No results found.\u000F")
        }
    }

    @Nested
    @DisplayName("API Keys Test")
    inner class ApiKeysTest {
        @Test
        fun `API key should not be empty`() {
            assertFailure { sanitizedSearch("test", "", "apiKey") }
                .isInstanceOf(ModuleException::class.java)
                .hasMessage("${GoogleSearch.SERVICE_NAME} is disabled. The API keys are missing.")
        }

        @Test
        fun `CSE key should not be empty`() {
            assertFailure { sanitizedSearch("test", "apiKey", "") }
                .isInstanceOf(ModuleException::class.java)
                .hasMessage("${GoogleSearch.SERVICE_NAME} is disabled. The API keys are missing.")
        }

        @Test
        fun `Invalid API key should throw exception`() {
            assertFailure { sanitizedSearch("test", "apiKey", "cseKey") }
                .isInstanceOf(ModuleException::class.java)
                .hasMessage("API key not valid. Please pass a valid API key.")
        }

        @Test
        fun `Invalid CSE key should throw exception`() {
            assertFailure { sanitizedSearch("test", apiKey, "cseKey") }
                .isInstanceOf(ModuleException::class.java)
                .hasMessage("Request contains an invalid argument.")
        }
    }

    @Nested
    @DisplayName("Search Tests")
    inner class SearchTests {
        @Test
        fun `Query should not be empty`() {
            assertThat(sanitizedSearch("", apiKey, cseKey).first()).isInstanceOf(ErrorMessage::class.java)
        }

        @Test
        @DisableOnCi
        @Throws(ModuleException::class)
        fun `No results found`() {
            val query = "adadflkjl"
            val messages = sanitizedSearch(query, apiKey, cseKey)
            assertThat(messages, "searchGoogle($query)").index(0).all {
                isInstanceOf(ErrorMessage::class.java)
                prop(Message::msg).isEqualTo("No results found.")
            }
        }

        @Test
        @DisableOnCi
        @Throws(ModuleException::class)
        fun `Search Google`() {
            val query = "mobibot"
            val messages = sanitizedSearch(query, apiKey, cseKey)
            assertThat(messages, "searchGoogle($query)").all {
                isNotEmpty()
                index(0).prop(Message::msg).contains(query, true)
            }
        }
    }
}
