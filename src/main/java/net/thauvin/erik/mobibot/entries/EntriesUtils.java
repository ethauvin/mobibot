/*
 * EntriesUtils.java
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

package net.thauvin.erik.mobibot.entries;

import net.thauvin.erik.mobibot.Commands;
import net.thauvin.erik.mobibot.Constants;

/**
 * The <code>Utils</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-04-19
 * @since 1.0
 */
public final class EntriesUtils {
    /**
     * Disables the default constructor.
     */
    private EntriesUtils() {
        throw new UnsupportedOperationException("Illegal constructor call.");
    }

    /**
     * Builds an entry's comment for display on the channel.
     *
     * @param entryIndex   The entry's index.
     * @param commentIndex The comment's index.
     * @param comment      The {@link EntryComment comment} object.
     * @return The entry's comment.
     */
    public static String buildComment(final int entryIndex, final int commentIndex, final EntryComment comment) {
        return (Commands.LINK_CMD + (entryIndex + 1) + '.' + (commentIndex + 1) + ": [" + comment.getNick() + "] "
            + comment.getComment());
    }

    /**
     * Builds an entry's link for display on the channel.
     *
     * @param index The entry's index.
     * @param entry The {@link EntryLink entry} object.
     * @return The entry's link.
     * @see #buildLink(int, EntryLink, boolean)
     */
    public static String buildLink(final int index, final EntryLink entry) {
        return buildLink(index, entry, false);
    }

    /**
     * Builds an entry's link for display on the channel.
     *
     * @param index  The entry's index.
     * @param entry  The {@link EntryLink entry} object.
     * @param isView Set to true to display the number of comments.
     * @return The entry's link.
     */
    public static String buildLink(final int index, final EntryLink entry, final boolean isView) {
        final StringBuilder buff = new StringBuilder(Commands.LINK_CMD + (index + 1) + ": ");

        buff.append('[').append(entry.getNick()).append(']');

        if (isView && entry.hasComments()) {
            buff.append("[+").append(entry.getCommentsCount()).append(']');
        }

        buff.append(' ');

        if (Constants.NO_TITLE.equals(entry.getTitle())) {
            buff.append(entry.getTitle());
        } else {
            buff.append(net.thauvin.erik.mobibot.Utils.bold(entry.getTitle()));
        }

        buff.append(" ( ").append(net.thauvin.erik.mobibot.Utils.green(entry.getLink())).append(" )");

        return buff.toString();
    }

    /**
     * Build an entry's tags/categories for display on the channel.
     *
     * @param entryIndex The entry's index.
     * @param entry      The {@link EntryLink entry} object.
     * @return The entry's tags.
     */
    public static String buildTags(final int entryIndex, final EntryLink entry) {
        return (Commands.LINK_CMD + (entryIndex + 1) + "T: " + entry.getPinboardTags().replaceAll(",", ", "));
    }
}
