/*
 * EntryLink.kt
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
package net.thauvin.erik.mobibot.entries

import com.rometools.rome.feed.synd.SyndCategory
import com.rometools.rome.feed.synd.SyndCategoryImpl
import net.thauvin.erik.mobibot.commands.links.LinksMgr
import org.apache.commons.lang3.StringUtils
import java.io.Serializable
import java.util.Calendar
import java.util.Date

/**
 * The class used to store link entries.
 */
class EntryLink : Serializable {
    // Link's comments
    val comments: MutableList<EntryComment> = mutableListOf()

    // Tags/categories
    val tags: MutableList<SyndCategory> = mutableListOf()

    // Channel
    var channel: String

    // Creation date
    var date: Date = Calendar.getInstance().time

    // Link's URL
    var link: String

    // Author's login
    var login = ""

    // Author's nickname
    var nick: String

    // Link's title
    var title: String

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
    ) {
        this.link = link
        this.title = title
        this.nick = nick
        this.login = login
        this.channel = channel
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
    ) {
        this.link = link
        this.title = title
        this.nick = nick
        this.channel = channel
        this.date = Date(date.time)
        this.tags.addAll(tags)
    }

    /**
     * Adds a new comment.
     */
    fun addComment(comment: String, nick: String): Int {
        comments.add(EntryComment(comment, nick))
        return comments.size - 1
    }

    /**
     * Deletes a specific comment.
     */
    fun deleteComment(index: Int) {
        if (index < comments.size) {
            comments.removeAt(index)
        }
    }

    /**
     * Returns a comment.
     */
    fun getComment(index: Int): EntryComment = comments[index]

    /**
     * Returns the tags formatted for pinboard.in
     */
    val pinboardTags: String
        get() {
            val pinboardTags = StringBuilder(nick)
            for (tag in tags) {
                pinboardTags.append(',')
                pinboardTags.append(tag.name)
            }
            return pinboardTags.toString()
        }

    /**
     * Returns true if a string is contained in the link, title, or nick.
     */
    fun matches(match: String?): Boolean {
        return (StringUtils.containsIgnoreCase(link, match)
            || StringUtils.containsIgnoreCase(title, match)
            || StringUtils.containsIgnoreCase(nick, match))
    }

    /**
     * Sets a comment.
     */
    fun setComment(index: Int, comment: String?, nick: String?) {
        if (index < comments.size && (comment != null) && !nick.isNullOrBlank()) {
            comments[index] = EntryComment(comment, nick)
        }
    }

    /**
     * Sets the tags.
     */
    fun setTags(tags: String) {
        setTags(tags.split(LinksMgr.TAG_MATCH))
    }

    /**
     * Sets the tags.
     */
    private fun setTags(tags: List<String?>) {
        if (tags.isNotEmpty()) {
            var category: SyndCategoryImpl
            for (tag in tags) {
                if (!tag.isNullOrBlank()) {
                    val t = tag.lowercase()
                    val mod = t[0]
                    if (mod == '-') {
                        // Don't remove the channel tag
                        if (channel.substring(1) != t.substring(1)) {
                            category = SyndCategoryImpl()
                            category.name = t.substring(1)
                            this.tags.remove(category)
                        }
                    } else {
                        category = SyndCategoryImpl()
                        if (mod == '+') {
                            category.name = t.substring(1)
                        } else {
                            category.name = t
                        }
                        if (!this.tags.contains(category)) {
                            this.tags.add(category)
                        }
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

    companion object {
        // Serial version UID
        const val serialVersionUID = 1L
    }
}
