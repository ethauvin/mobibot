/*
 * MessageTests.kt
 *
 * Copyright (c) 2004-2026 Erik C. Thauvin (erik@thauvin.net)
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

package net.thauvin.erik.mobibot.msg

import assertk.all
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class MessageTests {
    @Nested
    @DisplayName("Error Tests")
    inner class ErrorTests {
        @Test
        fun `Validate message with error`() {
            assertThat(Message("foo", isError = true)).all {
                prop(Message::isError).isTrue()
                prop(Message::isNotice).isFalse()
                prop(Message::isPrivate).isFalse()
            }
        }

        @Test
        fun `Validate message without error`() {
            assertThat(Message("foo", isError = false)).all {
                prop(Message::isError).isFalse()
                prop(Message::isNotice).isFalse()
                prop(Message::isPrivate).isFalse()
            }
        }
    }

    @Nested
    @DisplayName("Validate Tests")
    inner class ValidateTests {
        @Test
        fun `Validate error message`() {
            assertThat(ErrorMessage("foo")).all {
                prop(Message::isError).isTrue()
                prop(Message::isNotice).isTrue()
                prop(Message::isPrivate).isFalse()
            }
        }

        @Test
        fun `Validate notice message`() {
            assertThat(NoticeMessage("foo")).all {
                prop(Message::isError).isFalse()
                prop(Message::isNotice).isTrue()
                prop(Message::isPrivate).isFalse()
            }
        }

        @Test
        fun `Validate private message`() {
            assertThat(PrivateMessage("foo")).all {
                prop(Message::isPrivate).isTrue()
                prop(Message::isError).isFalse()
                prop(Message::isNotice).isFalse()
            }
        }

        @Test
        fun `Validate public message`() {
            assertThat(PublicMessage("foo")).all {
                prop(Message::isError).isFalse()
                prop(Message::isNotice).isFalse()
                prop(Message::isPrivate).isFalse()
            }
        }
    }
}
