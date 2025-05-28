/*
 * ModuleExceptionTests.kt
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
import net.thauvin.erik.mobibot.ExceptionSanitizer.sanitize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.IOException
import kotlin.test.Test

class ModuleExceptionTests {
    companion object {
        const val DEBUG_MESSAGE = "debugMessage"
        const val MESSAGE = "message"

        @JvmStatic
        fun moduleExceptions(): List<ModuleException> {
            return listOf(
                ModuleException(DEBUG_MESSAGE, MESSAGE, IOException("foo")),
                ModuleException(DEBUG_MESSAGE, MESSAGE,
                    IllegalArgumentException("bar")),
                ModuleException(DEBUG_MESSAGE, MESSAGE)
            )
        }
    }

    @Nested
    @DisplayName("Message Tests")
    inner class MessageTests {
        @ParameterizedTest
        @MethodSource("net.thauvin.erik.mobibot.modules.ModuleExceptionTests#moduleExceptions")
        fun getDebugMessage(e: ModuleException) {
            assertThat(e::debugMessage).isEqualTo(DEBUG_MESSAGE)
        }

        @ParameterizedTest
        @MethodSource("net.thauvin.erik.mobibot.modules.ModuleExceptionTests#moduleExceptions")
        fun getMessage(e: ModuleException) {
            assertThat(e).hasMessage(MESSAGE)
        }

        @Test
        fun sanitizeMessage() {
            val apiKey = "1234567890"
            var e = ModuleException(DEBUG_MESSAGE, MESSAGE,
                IOException("URL https://foo.com?apiKey=$apiKey&userID=me"))
            assertThat(
                e.sanitize(apiKey, "", "me").message,
                "ModuleException(debugMessage, message, IOException(url))"
            ).isNotNull().all {
                contains("xxxxxxxxxx", "userID=xx", "java.io.IOException")
                doesNotContain(apiKey, "me")
            }

            e = ModuleException(DEBUG_MESSAGE, MESSAGE, null)
            assertThat(e.sanitize(apiKey),
                "ModuleException(debugMessage, message, null)").hasMessage(MESSAGE)

            e = ModuleException(DEBUG_MESSAGE, MESSAGE, IOException())
            assertThat(e.sanitize(apiKey),
                "ModuleException(debugMessage, message, IOException())").hasMessage(MESSAGE)

            e = ModuleException(DEBUG_MESSAGE, apiKey)
            assertThat(e.sanitize(apiKey).message,
                "ModuleException(debugMessage, apiKey)")
                .isNotNull()
                .doesNotContain(apiKey)

            val msg: String? = null
            e = ModuleException(DEBUG_MESSAGE, msg, IOException(msg))
            assertThat(e.sanitize(apiKey).message,
                "ModuleException(debugMessage, msg, IOException(msg))").isNull()

            e = ModuleException(DEBUG_MESSAGE, msg,
                IOException("foo is $apiKey"))
            assertThat(
                e.sanitize("   ", apiKey, "foo").message,
                "ModuleException(debugMessage, msg, IOException(foo is $apiKey))"
            ).isNotNull().all {
                doesNotContain(apiKey)
                endsWith("xxx is xxxxxxxxxx")
            }
            assertThat(e.sanitize(), "exception should be unchanged").isEqualTo(e)
        }
    }
}
