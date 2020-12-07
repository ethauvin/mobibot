/*
 * EntriesMgr.kt
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
package net.thauvin.erik.mobibot.entries

import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndFeedImpl
import com.rometools.rome.io.FeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.SyndFeedOutput
import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils.isoLocalDate
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Manages the feed entries.
 */
object EntriesMgr {
    /**
     * The name of the file containing the current entries.
     */
    const val CURRENT_XML = "current.xml"

    /**
     * The name of the file containing the backlog entries.
     */
    const val NAV_XML = "nav.xml"

    /**
     * The .xml extension
     */
    const val XML_EXT = ".xml"

    // Maximum number of backlogs to keep
    private const val maxBacklogs = 10

    /**
     * Loads the backlogs.
     */
    @Throws(IOException::class, FeedException::class)
    fun loadBacklogs(file: String, history: MutableList<String>) {
        history.clear()
        val input = SyndFeedInput()
        InputStreamReader(Files.newInputStream(Paths.get(file)), StandardCharsets.UTF_8).use { reader ->
            val feed = input.build(reader)
            val items = feed.entries
            for (i in items.indices.reversed()) {
                history.add(items[i].title)
            }
        }
    }

    /**
     * Loads the current entries.
     */
    @Throws(IOException::class, FeedException::class)
    fun loadEntries(file: String, channel: String, entries: MutableList<EntryLink>): String {
        entries.clear()
        val input = SyndFeedInput()
        var today: String
        InputStreamReader(
            Files.newInputStream(Paths.get(file)), StandardCharsets.UTF_8
        ).use { reader ->
            val feed = input.build(reader)
            today = isoLocalDate(feed.publishedDate)
            val items = feed.entries
            var entry: EntryLink
            for (i in items.indices.reversed()) {
                with(items[i]) {
                    entry = EntryLink(
                        link,
                        title,
                        author.substring(author.lastIndexOf('(') + 1, author.length - 1),
                        channel,
                        publishedDate,
                        categories
                    )
                    var split: List<String>
                    for (comment in description.value.split("<br/>")) {
                        split = comment.split(": ".toRegex(), 2)
                        if (split.size == 2) {
                            entry.addComment(comment = split[1].trim(), nick = split[0].trim())
                        }
                    }
                }
                entries.add(entry)
            }
        }
        return today
    }

    /**
     * Saves the entries.
     */
    fun saveEntries(
        bot: Mobibot,
        entries: List<EntryLink>,
        history: MutableList<String>,
        isDayBackup: Boolean
    ) {
        if (bot.logger.isDebugEnabled) bot.logger.debug("Saving the feeds...")
        if (bot.logsDir.isNotBlank() && bot.weblogUrl.isNotBlank()) {
            try {
                val output = SyndFeedOutput()
                var rss: SyndFeed = SyndFeedImpl()
                val items: MutableList<SyndEntry> = mutableListOf()
                var item: SyndEntry
                OutputStreamWriter(
                    Files.newOutputStream(Paths.get(bot.logsDir + CURRENT_XML)), StandardCharsets.UTF_8
                ).use { fw ->
                    rss.apply {
                        feedType = "rss_2.0"
                        title = bot.channel + " IRC Links"
                        description = "Links from ${bot.ircServer} on ${bot.channel}"
                        link = bot.weblogUrl
                        publishedDate = Calendar.getInstance().time
                        language = "en"
                    }
                    var buff: StringBuilder
                    var comment: EntryComment
                    for (i in entries.size - 1 downTo 0) {
                        with(entries[i]) {
                            buff = StringBuilder()
                                .append("Posted by <b>")
                                .append(nick)
                                .append("</b> on <a href=\"irc://")
                                .append(bot.ircServer).append('/')
                                .append(channel)
                                .append("\"><b>")
                                .append(channel)
                                .append("</b></a>")
                            if (comments.size > 0) {
                                buff.append(" <br/><br/>")
                                val comments = comments
                                for (j in comments.indices) {
                                    comment = comments[j]
                                    if (j > 0) {
                                        buff.append(" <br/>")
                                    }
                                    buff.append(comment.nick).append(": ").append(comment.comment)
                                }
                            }
                            item = SyndEntryImpl()
                            item.link = link
                            item.description = SyndContentImpl().apply { value = buff.toString() }
                            item.title = title
                            item.publishedDate = date
                            item.author = "${bot.channel.substring(1)}@${bot.ircServer} ($nick)"
                            item.categories = tags
                            items.add(item)
                        }
                    }
                    rss.entries = items
                    if (bot.logger.isDebugEnabled) bot.logger.debug("Writing the entries feed.")
                    output.output(rss, fw)
                }
                OutputStreamWriter(
                    Files.newOutputStream(
                        Paths.get(
                            bot.logsDir + bot.today + XML_EXT
                        )
                    ), StandardCharsets.UTF_8
                ).use { fw -> output.output(rss, fw) }
                if (isDayBackup) {
                    if (bot.backlogsUrl.isNotBlank()) {
                        if (!history.contains(bot.today)) {
                            history.add(bot.today)
                            while (history.size > maxBacklogs) {
                                history.removeAt(0)
                            }
                        }
                        OutputStreamWriter(
                            Files.newOutputStream(Paths.get(bot.logsDir + NAV_XML)), StandardCharsets.UTF_8
                        ).use { fw ->
                            rss = SyndFeedImpl()
                            rss.apply {
                                feedType = "rss_2.0"
                                title = "${bot.channel} IRC Links Backlogs"
                                description = "Backlogs of Links from ${bot.ircServer} on ${bot.channel}"
                                link = bot.backlogsUrl
                                publishedDate = Calendar.getInstance().time
                            }
                            var date: String
                            items.clear()
                            for (i in history.size - 1 downTo 0) {
                                date = history[i]
                                item = SyndEntryImpl()
                                item.apply {
                                    link = bot.backlogsUrl + date + ".xml"
                                    title = date
                                    description = SyndContentImpl().apply { value = "Links for $date" }
                                }
                                items.add(item)
                            }
                            rss.entries = items
                            if (bot.logger.isDebugEnabled) bot.logger.debug("Writing the backlog feed.")
                            output.output(rss, fw)
                        }
                    } else {
                        bot.logger.warn("Unable to generate the backlogs feed. No property configured.")
                    }
                }
            } catch (e: FeedException) {
                if (bot.logger.isWarnEnabled) bot.logger.warn("Unable to generate the entries feed.", e)
            } catch (e: IOException) {
                if (bot.logger.isWarnEnabled) bot.logger.warn("Unable to generate the entries feed.", e)
            }
        } else {
            if (bot.logger.isWarnEnabled) {
                bot.logger.warn("Unable to generate the entries feed. A required property is missing.")
            }
        }
    }
}
