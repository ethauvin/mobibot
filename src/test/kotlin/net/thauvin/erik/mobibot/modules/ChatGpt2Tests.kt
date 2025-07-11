/*
 * ChatGpt2Tests.kt
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
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import net.thauvin.erik.mobibot.DisableOnCi
import net.thauvin.erik.mobibot.LocalProperties
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.pircbotx.hooks.types.GenericMessageEvent
import kotlin.test.Test

class ChatGpt2Tests : LocalProperties() {
    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun moduleMisconfigured() {
            val chatGpt2 = ChatGpt2()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            chatGpt2.commandResponse("channel", "chatgpt", "1 liter to gallon", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value).isEqualTo("The ${ChatGpt2.CHATGPT_NAME} module is misconfigured.")
        }
    }

    @Nested
    @DisplayName("Chat Tests")
    inner class ChatTests {
        @Test
        fun apiKey() {
            assertFailure { ChatGpt2.chat("1 gallon to liter", "", 0) }
                .isInstanceOf(ModuleException::class.java)
                .hasNoCause()
        }

        private val apiKey = getProperty(ChatGpt2.API_KEY_PROP)

        @Test
        @DisableOnCi
        fun chat() {
            assertThat(
                ChatGpt2.chat(
                    "javascript function to make a request with XMLHttpRequest, just code",
                    apiKey,
                    50
                )
            ).contains("```javascript")
        }

        @Test
        @DisableOnCi
        fun chatFailure() {
            assertFailure { ChatGpt2.chat("1 liter to gallon", apiKey, -1) }
                .isInstanceOf(ModuleException::class.java)
        }
    }
}
