/*
 * ChatGpt2Test.kt
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
import assertk.assertions.contains
import assertk.assertions.hasNoCause
import assertk.assertions.isInstanceOf
import net.thauvin.erik.mobibot.DisableOnCi
import net.thauvin.erik.mobibot.LocalProperties
import kotlin.test.Test

class ChatGpt2Test : LocalProperties() {
    @Test
    fun testApiKey() {
        assertFailure { ChatGpt2.chat("1 gallon to liter", "", 0) }
            .isInstanceOf(ModuleException::class.java)
            .hasNoCause()
    }

    @Test
    fun testChatOnCoverage() {
        if (System.getenv("CI") == null || System.getenv("COVERAGE_JDK") != null) {
            assertThat(
                ChatGpt2.chat("how do I encode a URL in java?", getProperty(ChatGpt2.API_KEY_PROP), 60)
            ).contains("URLEncoder")
        }
    }

    @Test
    @DisableOnCi
    fun testChat() {
        val apiKey = getProperty(ChatGpt2.API_KEY_PROP)
        assertThat(
            ChatGpt2.chat("how do I make an HTTP request in Javascript?", apiKey, 200)
        ).contains("XMLHttpRequest")

        assertFailure { ChatGpt2.chat("1 liter to gallon", apiKey, -1) }
            .isInstanceOf(ModuleException::class.java)
    }
}
