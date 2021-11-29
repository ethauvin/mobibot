/*
 * TellMessage.kt
 *
 * Copyright (c) 2004-2021, Erik C. Thauvin (erik@thauvin.net)
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

import java.io.Serializable
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * The `TellMessage` class.
 */
class TellMessage(
    /**
     * Returns the message's sender.
     */
    val sender: String,

    /**
     * Returns the message's recipient.
     */
    val recipient: String,

    /**
     * Returns the message text.
     */
    val message: String
) : Serializable {
    /**
     * Returns the queued date/time.
     */
    var queued: LocalDateTime = LocalDateTime.now(Clock.systemUTC())

    /**
     * Returns the message id.
     */
    var id: String = queued.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

    /**
     * Returns {@code true} if a notification was sent.
     */
    var isNotified = false

    /**
     * Returns {@code true} if the message was received.
     */
    var isReceived = false
        set(value) {
            if (value) {
                receptionDate = LocalDateTime.now(Clock.systemUTC())
            }
            field = value
        }

    /**
     * Returns the message creating date.
     */
    var receptionDate: LocalDateTime = LocalDateTime.MIN

    /**
     * Matches the message sender or recipient.
     */
    fun isMatch(nick: String?): Boolean {
        return sender.equals(nick, ignoreCase = true) || recipient.equals(nick, ignoreCase = true)
    }

    override fun toString(): String {
        return ("TellMessage{id='$id', isNotified=$isNotified, isReceived=$isReceived, message='$message', " +
                "queued=$queued, received=$receptionDate, recipient='$recipient', sender='$sender'}")
    }

    companion object {
        private const val serialVersionUID = 2L
    }
}
