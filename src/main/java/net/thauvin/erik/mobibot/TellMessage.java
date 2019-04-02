/*
 * TellMessage.java
 *
 * Copyright (c) 2004-2018, Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The <code>TellMessage</code> class.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2014-04-24
 * @since 1.0
 */
public class TellMessage implements Serializable {
    private static final long serialVersionUID = 2L;
    private final String id;
    private final String message;
    private final LocalDateTime queued;
    private final String recipient;
    private final String sender;
    private boolean isNotified;
    private boolean isReceived;
    private LocalDateTime received;

    /**
     * Create a new message.
     *
     * @param sender    The sender's nick.
     * @param recipient The recipient's nick.
     * @param message   The message.
     */
    public TellMessage(final String sender, final String recipient, final String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;

        this.queued = LocalDateTime.now(Clock.systemUTC());
        this.id = this.queued.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * Returns the message id.
     *
     * @return The message id.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the message text.
     *
     * @return The text of the message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the state of the queue flag.
     *
     * @return <code>true</code> if the message is queued.
     */
    public LocalDateTime getQueued() {
        return queued;
    }

    /**
     * Returns the state of the received flag.
     *
     * @return <code>true</code> if the message has been received.
     */
    public LocalDateTime getReceived() {
        return received;
    }

    /**
     * Returns the message's recipient.
     *
     * @return The recipient of the message.
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Returns the message's sender.
     *
     * @return The sender of the message.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Matches the message sender or recipient.
     *
     * @param nick The nickname to match with.
     * @return <code>true</code> if the nickname matches.
     */
    public boolean isMatch(final String nick) {
        return (sender.equalsIgnoreCase(nick) || recipient.equalsIgnoreCase(nick));
    }

    /**
     * Match the message ID.
     *
     * @param id The ID to match with.
     * @return <code>true</code> if the id matches.
     */
    public boolean isMatchId(final String id) {
        return this.id.equals(id);
    }

    /**
     * Returns the notification flag state.
     *
     * @return <code>true</code> if the sender has been notified.
     */
    public boolean isNotified() {
        return isNotified;
    }

    /**
     * Returns the received flag state.
     *
     * @return <code>true</code> if the message was received.
     */
    public boolean isReceived() {
        return isReceived;
    }

    /**
     * Sets the notified flag.
     */
    public void setIsNotified() {
        isNotified = true;
    }

    /**
     * Sets the received flag.
     */
    public void setIsReceived() {
        received = LocalDateTime.now(Clock.systemUTC());
        isReceived = true;
    }
}
