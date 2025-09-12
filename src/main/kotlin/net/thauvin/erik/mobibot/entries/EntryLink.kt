/*
 * EntryLink.kt
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
package net.thauvin.erik.mobibot.entries

import com.rometools.rome.feed.synd.SyndCategory
import com.rometools.rome.feed.synd.SyndCategoryImpl
import net.thauvin.erik.mobibot.commands.links.LinksManager
import java.io.Serializable
import java.util.*

/**
 * Holds [Entries] link.
 */
class EntryLink(
    // Link's comments
    val comments: MutableList<EntryComment> = mutableListOf(),

    // Tags/categories
    val tags: MutableList<SyndCategory> = mutableListOf(),

    // Channel
    var channel: String,

    // Creation date
    var date: Date = Calendar.getInstance().time,

    // Link's URL
    var link: String,

    // Author's login
    var login: String = "",

    // Author's nickname
    var nick: String,

    // Link's title
    var title: String
) : Serializable {
    /**
     * Creates a new entry.
     */
    constructor(
        link: String,
        title: String,
        nick: String,
        login: String,
        channel: String,
        tags: List<String?>
    ) : this(link = link, title = title, nick = nick, login = login, channel = channel) {
        setTags(tags)
    }

    /**
     * Creates a new entry.
     */
    constructor(
        link: String,
        title: String,
        nick: String,
        channel: String,
        date: Date,
        tags: List<SyndCategory>
    ) : this(link = link, title = title, nick = nick, channel = channel, date = Date(date.time)) {
        this.tags.addAll(tags)
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    /**
     * Adds a new comment
     */
    fun addComment(comment: EntryComment): Int {
        comments.add(comment)
        return comments.lastIndex
    }

    /**
     * Adds a new comment.
     */
    fun addComment(comment: String, nick: String): Int {
        return addComment(EntryComment(comment, nick))
    }

    /**
     * Deletes a specific comment.
     */
    fun deleteComment(index: Int): Boolean {
        if (index < comments.size) {
            comments.removeAt(index)
            return true
        }
        return false
    }

    /**
     * Deletes a comment.
     */
    fun deleteComment(entryComment: EntryComment): Boolean {
        return comments.remove(entryComment)
    }

    /**
     * Formats the tags.
     */
    fun formatTags(sep: String, prefix: String = ""): String {
        return tags.joinToString(separator = sep, prefix = prefix) { it.name }
    }

    /**
     * Returns a comment.
     */
    fun getComment(index: Int): EntryComment = comments[index]

    /**
     * Returns true if a string is contained in the link, title, or nick.
     */
    fun matches(match: String?): Boolean {
        return if (match.isNullOrEmpty()) {
            false
        } else {
            link.contains(match, true) || title.contains(match, true)
                    || nick.contains(match, true)
        }
    }

    /**
     * Sets a comment.
     */
    fun setComment(index: Int, comment: String?, nick: String?) {
        if (index < comments.size && !comment.isNullOrBlank() && !nick.isNullOrBlank()) {
            comments[index] = EntryComment(comment, nick)
        }
    }

    /**
     * Sets the tags.
     */
    fun setTags(tags: String) {
        setTags(tags.split(LinksManager.TAG_MATCH))
    }

    /**
     * Sets the tags.
     */
    private fun setTags(tags: List<String?>) {
        tags.forEach { tag ->
            if (tag.isNullOrBlank()) return@forEach

            val t = tag.lowercase()
            val tagName = when {
                t.startsWith('-') -> t.substring(1)
                t.startsWith('+') -> t.substring(1)
                else -> t
            }

            val category = SyndCategoryImpl().apply { name = tagName }

            when {
                t.startsWith('-') -> {
                    // Don't remove the channel tag
                    if (channel.substring(1) != tagName) {
                        this.tags.remove(category)
                    }
                }

                else -> {
                    if (!this.tags.contains(category)) {
                        this.tags.add(category)
                    }
                }
            }
        }
    }

    /**
     * Returns a string representation of the object.
     */
    override fun toString(): String {
        return ("EntryLink{channel='$channel', comments=$comments, date=$date, link='$link', login='$login'," +
                "nick='$nick', tags=$tags, title='$title'}")
    }
}
