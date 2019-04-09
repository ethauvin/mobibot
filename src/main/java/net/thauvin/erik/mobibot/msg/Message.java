/*
 * Message.java
 *
 * Copyright (c) 2004-2019, Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot.msg;

/**
 * The <code>Message</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-04-07
 * @since 1.0
 */
public class Message {
    private boolean isError;
    private boolean isNotice;
    private String msg = "";

    /**
     * Creates a new message.
     */
    public Message() {

    }

    /**
     * Creates a new message.
     *
     * @param message  The message.
     * @param isNotice The notice flag.
     */
    public Message(String message, boolean isNotice, boolean isError) {
        msg = message;
        this.isNotice = isNotice;
        this.isError = isError;
    }

    /**
     * Returns the message.
     *
     * @return The message.
     */
    public String getMessage() {
        return msg;
    }

    /**
     * Sets the message.
     *
     * @param message The new message.
     */
    public void setMessage(String message) {
        msg = message;
    }

    /**
     * Returns the error flag.
     *
     * @return The error flag.
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Sets the error flag.
     *
     * @param error The error flag.
     */
    public void setError(boolean error) {
        isError = error;
    }

    /**
     * Returns the message notice flag.
     *
     * @return The notice flag.
     */
    public boolean isNotice() {
        return isNotice;
    }

    /**
     * Set the message notice flag.
     *
     * @param isNotice The notice flag.
     */
    public void setNotice(boolean isNotice) {
        this.isNotice = isNotice;
    }
}
