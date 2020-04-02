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

class UrlMgr(defaultTags: String, keywords: String) : AbstractCommand() {
    private val keywords: List<String>
    private val defaultTags: List<String>
    override val command = Constants.LINK_CMD
    override val help = emptyList<String>()
    override val isOp = false
    override val isPublic = false
    override val isVisible = false

    init {
        this.keywords = keywords.split(TAG_MATCH.toRegex())
        this.defaultTags = defaultTags.split(TAG_MATCH.toRegex())
    }

    companion object {
        const val LINK_MATCH = "^[hH][tT][tT][pP](|[sS])://.*"
        const val TAG_MATCH = ", *| +"

        // Entries array
        private val entries = ArrayList<EntryLink>(0)

        // History/backlogs array
        private val history = ArrayList<String>(0)

        @JvmStatic
        val entriesCount
            get() = entries.size

        @JvmStatic
        var startDate: String = Utils.today()
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
            startDate = EntriesMgr.loadEntries(current, channel, entries)
            if (Utils.today() != startDate) {
                this.entries.clear()
                startDate = Utils.today()
            }
            EntriesMgr.loadBacklogs(backlogs, history)
        }
    }

    override fun commandResponse(
        bot: Mobibot,
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        val cmds = args.split(" ".toRegex(), 2)

        if (Ignore.isNotIgnored(sender) && (cmds.size == 1 || !cmds[1].contains(bot.nick))) {
            val link = cmds[0].trim()
            if (!isDupEntry(bot, sender, link, isPrivate)) {
                val isBackup = saveDayBackup(bot)
                var title = Constants.NO_TITLE
                val tags = ArrayList<String>(defaultTags)
                if (cmds.size == 2) {
                    val data = cmds[1].trim().split("${Tags.COMMAND}:", limit = 2)
                    title = data[0].trim()
                    if (data.size > 1) {
                        tags.addAll(data[1].split(TAG_MATCH.toRegex()))
                    }
                }
                title = fetchTitle(link, title)
                matchTagKeywords(title, tags)

                entries.add(EntryLink(link, title, sender, login, bot.channel, tags))
                val index: Int = entries.size - 1
                val entry: EntryLink = entries[index]
                bot.send(EntriesUtils.buildLink(index, entry))

                // Add Entry to pinboard.
                bot.addPin(entry)

                // Queue link for posting to twitter
                twitterPost(bot, index)

                saveEntries(bot, isBackup)

                if (Constants.NO_TITLE == entry.title) {
                    bot.send(sender, "Please specify a title, by typing:", isPrivate)
                    bot.send(
                        sender,
                        Utils.helpIndent(Constants.LINK_CMD + (index + 1) + ":|This is the title"),
                        isPrivate
                    )
                }
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

    override fun matches(message: String): Boolean {
        return message.matches(LINK_MATCH.toRegex())
    }

    private fun fetchTitle(link: String, title: String): String {
        if (Constants.NO_TITLE == title) {
            try {
                val html = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/74.0")
                    .get()
                val htmlTitle = html.title()
                val split = htmlTitle.split("( \\| )".toRegex(), 2)
                return if (split.size == 2 && split[0].isNotBlank()) {
                    split[0]
                } else if (htmlTitle.isNotBlank()) {
                    htmlTitle
                } else {
                    title
                }
            } catch (ignore: IOException) {
                // Do nothing
            }
        }
        return title
    }

    private fun isDupEntry(bot: Mobibot, sender: String, link: String, isPrivate: Boolean): Boolean {
        synchronized(entries) {
            for (i in entries.indices) {
                if (link == entries[i].link) {
                    val entry: EntryLink = entries[i]
                    bot.send(sender, Utils.bold("Duplicate") + " >> " + EntriesUtils.buildLink(i, entry), isPrivate)
                    return true
                }
            }
        }
        return false
    }

    private fun matchTagKeywords(title: String, tags: ArrayList<String>) {
        for (match in keywords) {
            val m = Regex.escape(match.trim())
            if (title.matches("(?i).*\\b$m\\b.*".toRegex())) {
                tags.add(m)
            }
        }
    }

    private fun saveDayBackup(bot: Mobibot): Boolean {
        if (Utils.today() != startDate) {
            saveEntries(bot, true)
            entries.clear()
            startDate = Utils.today()
            return true
        }

        return false
    }

    private fun twitterPost(bot: Mobibot, index: Int) {
        if (bot.isTwitterAutoPost) {
            bot.twitterAddEntry(index)
            Mobibot.timer.schedule(TwitterTimer(bot, index), Constants.TIMER_DELAY * 60L * 1000L)
        }
    }
}
