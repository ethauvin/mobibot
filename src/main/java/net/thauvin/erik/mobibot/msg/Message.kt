/*
 * Message.java
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
package net.thauvin.erik.mobibot.msg

import org.jibble.pircbot.Colors

/**
 * The `Message` class.
 *
 * @author [Erik C. Thauvin](https://erik.thauvin.net/)
 * @created 2019-04-07
 * @since 1.0
 */
open class Message {
    /** Color */
    var color = Colors.NORMAL

    /** Error */
    var isError = false

    /** Notice */
    var isNotice = false

    /** Private */
    var isPrivate = false

    /** Message text*/
    var text = ""

    /** Creates a new message. */
    constructor() {
        // This constructor is intentionally empty
    }

    /**
     * Creates a new message.
     *
     * @param text The message.
     * @param isNotice The notice flag.
     * @param isError The error flag.
     * @param isPrivate The Private message
     */
    constructor(text: String, isNotice: Boolean, isError: Boolean, isPrivate: Boolean) {
        this.text = text
        this.isNotice = isNotice
        this.isError = isError
        this.isPrivate = isPrivate
    }

    /**
     * Creates a new message.
     *
     * @param text The message.
     * @param isNotice The notice flag.
     * @param isError The error flag.
     * @param isPrivate The Private message
     * @param color The color.
     */
    constructor(
        text: String,
        isNotice: Boolean,
        isError: Boolean,
        isPrivate: Boolean,
        color: String
    ) {
        this.text = text
        this.isNotice = isNotice
        this.isError = isError
        this.isPrivate = isPrivate
        this.color = color
    }

    override fun toString(): String {
        return "Message(color='$color', isError=$isError, isNotice=$isNotice, isPrivate=$isPrivate, message='$text')"
    }
}
