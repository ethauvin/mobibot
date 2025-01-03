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
import kotlin.test.Test

class GoogleSearchTest : LocalProperties() {
    @Test
    fun testAPIKeys() {
        assertThat(
            searchGoogle("", "apikey", "cssKey").first(),
            "searchGoogle(empty)"
        ).isInstanceOf(ErrorMessage::class.java)

        assertFailure { searchGoogle("test", "", "apiKey") }
            .isInstanceOf(ModuleException::class.java).hasNoCause()

        assertFailure { searchGoogle("test", "apiKey", "") }
            .isInstanceOf(ModuleException::class.java).hasNoCause()

        assertFailure { searchGoogle("test", "apiKey", "cssKey") }
            .isInstanceOf(ModuleException::class.java)
            .hasMessage("API key not valid. Please pass a valid API key.")
    }

    @Test
    @DisableOnCi
    @Throws(ModuleException::class)
    fun testSearchGoogle() {
        val apiKey = getProperty(GoogleSearch.API_KEY_PROP)
        val cseKey = getProperty(GoogleSearch.CSE_KEY_PROP)

        try {
            var query = "mobibot"
            var messages = searchGoogle(query, apiKey, cseKey)
            assertThat(messages, "searchGoogle($query)").all {
                isNotEmpty()
                index(0).prop(Message::msg).contains(query, true)
            }

            query = "adadflkjl"
            messages = searchGoogle(query, apiKey, cseKey)
            assertThat(messages, "searchGoogle($query)").index(0).all {
                isInstanceOf(ErrorMessage::class.java)
                prop(Message::msg).isEqualTo("No results found.")
            }
        } catch (e: ModuleException) {
            // Avoid displaying api keys in CI logs
            if ("true" == System.getenv("CI")) {
                throw e.sanitize(apiKey, cseKey)
            } else {
                throw e
            }
        }
    }
}
