/*
 * Links.kt
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

package net.thauvin.erik.mobibot.commands.links

import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.today
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.Ignore
import net.thauvin.erik.mobibot.entries.EntriesMgr
import net.thauvin.erik.mobibot.entries.EntriesUtils
import net.thauvin.erik.mobibot.entries.EntryLink
import org.jsoup.Jsoup
import java.io.IOException

class LinksMgr(bot: Mobibot) : AbstractCommand(bot) {
    private val keywords: MutableList<String> = mutableListOf()
    private val defaultTags: MutableList<String> = mutableListOf()

    override val name = Constants.LINK_CMD
    override val help = emptyList<String>()
    override val isOp = false
    override val isPublic = false
    override val isVisible = false

    init {
        initProperties(TAGS_PROP, KEYWORDS_PROP)
    }

    companion object {
        const val LINK_MATCH = "^[hH][tT][tT][pP](|[sS])://.*"
        const val KEYWORDS_PROP = "tags-keywords"
        const val TAGS_PROP = "tags"
        const val TAG_MATCH = ", *| +"

        // Entries array
        @JvmField
        val entries = mutableListOf<EntryLink>()

        // History/backlogs array
        @JvmField
        val history = mutableListOf<String>()

        @JvmStatic
        var startDate: String = today()
            private set

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
        fun startup(current: String, backlogs: String, channel: String) {
            startDate = EntriesMgr.loadEntries(current, channel, entries)
            if (today() != startDate) {
                entries.clear()
                startDate = today()
            }
            EntriesMgr.loadBacklogs(backlogs, history)
        }
    }

    override fun commandResponse(
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
                var title = ""
                val tags = ArrayList<String>(defaultTags)
                if (cmds.size == 2) {
                    val data = cmds[1].trim().split("${Tags.COMMAND}:", limit = 2)
                    title = data[0].trim()
                    if (data.size > 1) {
                        tags.addAll(data[1].split(TAG_MATCH.toRegex()))
                    }
                }

                if (title.isBlank()) {
                    title = fetchTitle(link)
                }

                if (title != Constants.NO_TITLE) {
                    matchTagKeywords(title, tags)
                }

                entries.add(EntryLink(link, title, sender, login, bot.channel, tags))
                val index: Int = entries.size - 1
                val entry: EntryLink = entries[index]
                bot.send(EntriesUtils.buildLink(index, entry))

                // Add Entry to pinboard.
                bot.addPin(entry)

                // Queue link for posting to Twitter.
                bot.twitter.queueEntry(index)

                saveEntries(bot, isBackup)

                if (Constants.NO_TITLE == entry.title) {
                    bot.send(sender, "Please specify a title, by typing:", isPrivate)
                    bot.send(
                        sender,
                        helpFormat("${EntriesUtils.buildLinkCmd(index)}:|This is the title"),
                        isPrivate
                    )
                }
            }
        }
    }

    override fun helpResponse(
        command: String,
        sender: String,
        isOp: Boolean,
        isPrivate: Boolean
    ): Boolean = false

    override fun matches(message: String): Boolean {
        return message.matches(LINK_MATCH.toRegex())
    }

    private fun fetchTitle(link: String): String {
        try {
            val html = Jsoup.connect(link)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/74.0")
                .get()
            val title = html.title()
            if (title.isNotBlank()) {
                return title
            }
        } catch (ignore: IOException) {
            // Do nothing
        }
        return Constants.NO_TITLE
    }

    private fun isDupEntry(bot: Mobibot, sender: String, link: String, isPrivate: Boolean): Boolean {
        synchronized(entries) {
            for (i in entries.indices) {
                if (link == entries[i].link) {
                    val entry: EntryLink = entries[i]
                    bot.send(sender, bold("Duplicate") + " >> " + EntriesUtils.buildLink(i, entry), isPrivate)
                    return true
                }
            }
        }
        return false
    }

    private fun matchTagKeywords(title: String, tags: MutableList<String>) {
        for (match in keywords) {
            val m = Regex.escape(match)
            if (title.matches("(?i).*\\b$m\\b.*".toRegex())) {
                tags.add(match)
            }
        }
    }

    private fun saveDayBackup(bot: Mobibot): Boolean {
        if (today() != startDate) {
            saveEntries(bot, true)
            entries.clear()
            startDate = today()
            return true
        }

        return false
    }

    override fun setProperty(key: String, value: String) {
        super.setProperty(key, value)
        if (KEYWORDS_PROP == key) {
            keywords.addAll(value.split(TAG_MATCH.toRegex()))
        } else if (TAGS_PROP == key) {
            defaultTags.addAll(value.split(TAG_MATCH.toRegex()))
        }
    }
}
