/*
 * TellMessageTest.kt
 *
 * Copyright (c) 2004-2020, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
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

import java.time.temporal.Temporal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions
import org.testng.annotations.Test
import java.time.Duration

/**
 * The `TellMessageTest` class.
 */
class TellMessageTest {
    private fun isValidDate(date: Temporal): Boolean {
        return Duration.between(date, LocalDateTime.now()).toMinutes() < 1
    }

    @Test
    fun testTellMessage() {
        val message = "Test message."
        val recipient = "recipient"
        val sender = "sender"
        val tellMessage = TellMessage(sender, recipient, message)
        Assertions.assertThat(tellMessage.sender).`as`(sender).isEqualTo(sender)
        Assertions.assertThat(tellMessage.recipient).`as`(recipient).isEqualTo(recipient)
        Assertions.assertThat(tellMessage.message).`as`(message).isEqualTo(message)
        Assertions.assertThat(isValidDate(tellMessage.queued)).`as`("queued is valid date/time").isTrue
        Assertions.assertThat(tellMessage.isMatch(sender)).`as`("match sender").isTrue
        Assertions.assertThat(tellMessage.isMatch(recipient)).`as`("match recipient").isTrue
        Assertions.assertThat(tellMessage.isMatch("foo")).`as`("foo is no match").isFalse
        Assertions.assertThat(tellMessage.isMatchId(tellMessage.id)).`as`("is match ID").isTrue
        tellMessage.isReceived = true
        Assertions.assertThat(tellMessage.isReceived).`as`("is received").isTrue
        Assertions.assertThat(isValidDate(tellMessage.receptionDate)).`as`("received is valid date/time").isTrue
        tellMessage.isNotified = true
        Assertions.assertThat(tellMessage.isNotified).`as`("is notified").isTrue
    }
}
