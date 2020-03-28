/*
 * TellMessageTest.java
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

package net.thauvin.erik.mobibot.commands.tell;

import org.testng.annotations.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The <code>TellMessageTest</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-07-29
 * @since 1.0
 */
public class TellMessageTest {
    private boolean isValidDate(final Temporal date) {
        return Duration.between(date, LocalDateTime.now()).toMinutes() < 1;
    }

    @Test
    void testTellMessage() {
        final String message = "Test message.";
        final String recipient = "recipient";
        final String sender = "sender";
        final TellMessage tellMessage = new TellMessage(sender, recipient, message);

        assertThat(tellMessage.getSender()).as(sender).isEqualTo(sender);
        assertThat(tellMessage.getRecipient()).as(recipient).isEqualTo(recipient);
        assertThat(tellMessage.getMessage()).as(message).isEqualTo(message);
        assertThat(isValidDate(tellMessage.getQueued())).as("queued is valid date/time").isTrue();

        assertThat(tellMessage.isMatch(sender)).as("match sender").isTrue();
        assertThat(tellMessage.isMatch(recipient)).as("match recipient").isTrue();
        assertThat(tellMessage.isMatch("foo")).as("foo is no match").isFalse();

        assertThat(tellMessage.isMatchId(tellMessage.getId())).as("is match ID").isTrue();

        tellMessage.setIsReceived();
        assertThat(tellMessage.isReceived()).as("is received").isTrue();
        assertThat(isValidDate(tellMessage.getReceived())).as("received is valid date/time").isTrue();

        tellMessage.setIsNotified();
        assertThat(tellMessage.isNotified()).as("is notified").isTrue();
    }
}

