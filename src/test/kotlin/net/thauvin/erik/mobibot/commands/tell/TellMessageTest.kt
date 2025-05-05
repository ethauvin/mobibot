/*
 * TellMessageTest.kt
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
package net.thauvin.erik.mobibot.commands.tell

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.Temporal
import kotlin.test.Test

class TellMessageTest {
    private fun isValidDate(date: Temporal): Boolean {
        return Duration.between(date, LocalDateTime.now()).toMinutes() < 1
    }

    @Test
    fun validateTellMessage() {
        val message = "Test message."
        val recipient = "recipient"
        val sender = "sender"
        val tellMessage = TellMessage(sender, recipient, message)
        assertThat(tellMessage).all {
            prop(TellMessage::sender).isEqualTo(sender)
            prop(TellMessage::recipient).isEqualTo(recipient)
            prop(TellMessage::message).isEqualTo(message)
        }
        assertThat(isValidDate(tellMessage.queued), "isValidDate()").isTrue()
        assertThat(tellMessage.isMatch(sender), "isMatch(sender)").isTrue()
        assertThat(tellMessage.isMatch(recipient), "isMatch(recipient)").isTrue()
        assertThat(tellMessage.isMatch("foo"), "isMatch(foo)").isFalse()
        tellMessage.isReceived = false
        assertThat(tellMessage.receptionDate, "receptionDate").isEqualTo(LocalDateTime.MIN)
        tellMessage.isReceived = true
        assertThat(isValidDate(tellMessage.receptionDate), "isValidDate(creationDate)").isTrue()
    }
}
