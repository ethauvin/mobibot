/*
 * EntryComment.java
 *
 * Copyright (c) 2004-2016, Erik C. Thauvin (erik@thauvin.net)
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
import java.util.Calendar;
import java.util.Date;

/**
 * The class used to store comments associated to a specific entry.
 *
 * @author <a href="mailto:erik@thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Jan 31, 2004
 * @since 1.0
 */
public class EntryComment implements Serializable {
    // The serial version UID.
    static final long serialVersionUID = 6957415292233553224L;

    // The creation date.
    private final Date date = Calendar.getInstance().getTime();

    private String comment = "";
    private String nick = "";

    /**
     * Creates a new comment.
     *
     * @param comment The new comment.
     * @param nick    The nickname of the comment's author.
     */
    public EntryComment(final String comment, final String nick) {
        this.comment = comment;
        this.nick = nick;
    }

    /**
     * Creates a new comment.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected EntryComment() {
        ; // Required for serialization.
    }

    /**
     * Returns the comment.
     *
     * @return The comment.
     */
    public final String getComment() {
        return comment;
    }

    /**
     * Sets the comment.
     *
     * @param comment The actual comment.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final void setComment(final String comment) {
        this.comment = comment;
    }

    /**
     * Returns the comment's creation date.
     *
     * @return The date.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final Date getDate() {
        return date;
    }

    /**
     * Returns the nickname of the author of the comment.
     *
     * @return The nickname.
     */
    public final String getNick() {
        return nick;
    }

    /**
     * Sets the nickname of the author of the comment.
     *
     * @param nick The new nickname.
     */
    public final void setNick(final String nick) {
        this.nick = nick;
    }
}
