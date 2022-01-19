/*
 * EntriesUtils.kt
 *
 * Copyright (c) 2004-2022, Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot.entries

import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.green

/**
 * Entries utilities.
 */
object EntriesUtils {
    /**
     * Build link label based on its index. e.g: L1
     */
    @JvmStatic
    fun buildLinkLabel(index: Int): String = Constants.LINK_CMD + (index + 1)

    /**
     * Builds an entry's comment for display on the channel.
     */
    @JvmStatic
    fun buildComment(entryIndex: Int, commentIndex: Int, comment: EntryComment): String =
        ("${buildLinkLabel(entryIndex)}.${commentIndex + 1}: [${comment.nick}] ${comment.comment}")

    /**
     * Builds an entry's link for display on the channel.
     */
    @JvmStatic
    @JvmOverloads
    fun buildLink(entryIndex: Int, entry: EntryLink, isView: Boolean = false): String {
        val buff = StringBuilder().append(buildLinkLabel(entryIndex)).append(": ")
            .append('[').append(entry.nick).append(']')
        if (isView && entry.comments.isNotEmpty()) {
            buff.append("[+").append(entry.comments.size).append(']')
        }
        buff.append(' ')
        with(entry) {
            if (Constants.NO_TITLE == title) {
                buff.append(title)
            } else {
                buff.append(title.bold())
            }
            buff.append(" ( ").append(link.green()).append(" )")
        }
        return buff.toString()
    }

    /**
     * Build an entry's tags/categories for display on the channel.
     */
    @JvmStatic
    fun buildTags(entryIndex: Int, entry: EntryLink): String =
        buildLinkLabel(entryIndex) + "${Constants.TAG_CMD}: " + entry.pinboardTags.replace(",", ", ")
}
