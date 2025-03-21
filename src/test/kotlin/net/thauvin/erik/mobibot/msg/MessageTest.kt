/*
 * MessageTest.kt
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

package net.thauvin.erik.mobibot.msg

import assertk.all
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test

class MessageTest {
    @Test
    fun testConstructor() {
        var msg = Message("foo")

        msg.isError = true
        assertThat(msg.isNotice, "message is notice").isTrue()

        msg = Message("foo", isError = true)
        assertThat(msg.isNotice, "message is notice too").isTrue()
    }

    @Test
    fun testErrorMessage() {
        val msg = ErrorMessage("foo")
        assertThat(msg).all {
            prop(Message::isError).isTrue()
            prop(Message::isNotice).isTrue()
            prop(Message::isPrivate).isFalse()
        }
    }

    @Test
    fun testIsError() {
        val msg = Message("foo")
        msg.isError = true
        assertThat(msg).all {
            prop(Message::isError).isTrue()
            prop(Message::isNotice).isTrue()
            prop(Message::isPrivate).isFalse()
        }
        msg.isError = false
        assertThat(msg).all {
            prop(Message::isError).isFalse()
            prop(Message::isNotice).isTrue()
            prop(Message::isPrivate).isFalse()
        }
    }

    @Test
    fun testNoticeMessage() {
        val msg = NoticeMessage("food")
        assertThat(msg).all {
            prop(Message::isError).isFalse()
            prop(Message::isNotice).isTrue()
            prop(Message::isPrivate).isFalse()
        }
    }

    @Test
    fun testPrivateMessage() {
        val msg = PrivateMessage("foo")
        assertThat(msg).all {
            prop(Message::isPrivate).isTrue()
            prop(Message::isError).isFalse()
            prop(Message::isNotice).isFalse()
        }
    }

    @Test
    fun testPublicMessage() {
        val msg = PublicMessage("foo")
        assertThat(msg).all {
            prop(Message::isError).isFalse()
            prop(Message::isNotice).isFalse()
            prop(Message::isPrivate).isFalse()
        }
    }
}
