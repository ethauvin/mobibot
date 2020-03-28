/*
 * Links.kt
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

package net.thauvin.erik.mobibot.commands.links

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.TwitterTimer
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.Ignore
import net.thauvin.erik.mobibot.entries.EntriesMgr
import net.thauvin.erik.mobibot.entries.EntriesUtils
import net.thauvin.erik.mobibot.entries.EntryLink
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

class UrlMgr(defaultTags: String, keywords: String) : AbstractCommand() {
    private val tagsKeywords = ArrayList<String>()
    override val command = Constants.LINK_CMD
    override val help = emptyList<String>()
    override val isOp = false
    override val isPublic = false
    override val isVisible = false

    init {
        tagsKeywords.addAll(keywords.split(", +?| +"))
        Companion.defaultTags = defaultTags
    }

    companion object {
        const val LINK_MATCH = "^[hH][tT][tT][pP](|[sS])://.*"

        // Entries array
        private val entries = ArrayList<EntryLink>(0)

        // History/backlogs array
        private val history = ArrayList<String>(0)
        private lateinit var defaultTags: String

        @JvmStatic
        val entriesCount
            get() = entries.size

        @JvmStatic
        var today: String = Utils.today()
            private set

        @JvmStatic
        fun addHistory(index: Int, entry: String) {
            history.add(index, entry)
        }

        /**
         * Saves the entries.
         *
         * @param isDayBackup Set the `true` if the daily backup file should also be created.
         */
        @JvmStatic
        fun saveEntries(bot: Mobibot, isDayBackup: Boolean) {
            EntriesMgr.saveEntries(bot, entries, history, isDayBackup)
        }

        @JvmStatic
        fun removeEntry(index: Int) {
            entries.removeAt(index)
        }

        @JvmStatic
        fun getEntry(index: Int): EntryLink {
            return entries[index]
        }

        @JvmStatic
        fun getHistory(): List<String> {
            return history
        }

        @JvmStatic
        fun startup(current: String, backlogs: String, channel: String) {
            today = EntriesMgr.loadEntries(current, channel, entries)
            if (Utils.today() != today) {
                this.entries.clear()
                today = Utils.today()
            }
            EntriesMgr.loadBacklogs(backlogs, history)
        }
    }

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    override fun commandResponse(
        bot: Mobibot,
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        val cmds = args.split(" ".toRegex(), 2)

        if (Ignore.isNotIgnored(sender) && (cmds.size == 1
                || !cmds[1].contains(bot.nick)
                || !cmds[1].endsWith(" (${Ignore.IGNORE}"))) {
            val link = cmds[0].trim()
            var isBackup = false
            val dupIndex: Int = findDupEntry(link)
            if (dupIndex == -1) {
                if (Utils.today() != today) {
                    isBackup = true
                    saveEntries(bot, true)
                    entries.clear()
                    today = Utils.today()
                }
                val tags: StringBuilder = StringBuilder(defaultTags)
                var title = Constants.NO_TITLE
                if (cmds.size == 2) {
                    val data = cmds[1].trim().split("${Tags.COMMAND}:", limit = 2)
                    if (data.size == 1) {
                        title = data[0].trim()
                    } else {
                        if (data[0].isNotBlank()) {
                            title = data[0].trim()
                        }
                        tags.append(' ').append(data[1].trim())
                    }
                }
                if (Constants.NO_TITLE == title) {
                    try {
                        val html = Jsoup.connect(link).userAgent("Mozilla").get()
                        val htmlTitle = html.title()
                        if (htmlTitle.isNotBlank()) {
                            val split = htmlTitle.split("( \\| )".toRegex(), 2)
                            title = if (split.size == 2) {
                                split[0]
                            } else {
                                htmlTitle
                            }
                        }
                    } catch (ignore: IOException) {
                        // Do nothing
                    }
                }
                if (tagsKeywords.isNotEmpty()) {
                    for (match in tagsKeywords) {
                        val m = match.trim()
                        if (title.matches("(?i).*\\b$m\\b.*".toRegex())) {
                            tags.append(' ').append(m)
                        }
                    }
                }
                entries.add(EntryLink(link, title, sender, login, bot.channel, tags.toString()))
                val index: Int = entries.size - 1
                val entry: EntryLink = entries[index]
                bot.send(bot.channel, EntriesUtils.buildLink(index, entry))

                // Add Entry to pinboard.
                bot.addPin(entry)

                // Queue link for posting to twitter
                if (bot.isTwitterAutoPost) {
                    bot.twitterAddEntry(index)
                    Mobibot.timer.schedule(
                        TwitterTimer(bot, index), Constants.TIMER_DELAY * 60L * 1000L
                    )
                }
                saveEntries(bot, isBackup)
                if (Constants.NO_TITLE == entry.title) {
                    bot.send(sender, Utils.bold("Please specify a title, by typing:"), true)
                    bot.send(
                        sender,
                        Utils.helpIndent(Constants.LINK_CMD + (index + 1) + ":|This is the title"),
                        true
                    )
                }
            } else {
                val entry: EntryLink = entries[dupIndex]
                bot.send(
                    sender, Utils.bold("Duplicate") + " >> " + EntriesUtils.buildLink(dupIndex, entry)
                )
            }
        }
    }

    override fun helpResponse(
        bot: Mobibot,
        command: String,
        sender: String,
        isOp: Boolean,
        isPrivate: Boolean
    ): Boolean = false

    /**
     * Returns the index of the specified duplicate entry, if any.
     *
     * @param link The link.
     * @return The index or -1 if none.
     */
    private fun findDupEntry(link: String): Int {
        synchronized(entries) {
            for (i in entries.indices) {
                if (link == entries[i].link) {
                    return i
                }
            }
        }
        return -1
    }

    override fun matches(message: String): Boolean {
        return message.matches(LINK_MATCH.toRegex())
    }
}
