/*
 * FeedsManager.kt
 *
 * Copyright 2004-2024 Erik C. Thauvin (erik@thauvin.net)
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

import com.rometools.rome.feed.synd.*
import com.rometools.rome.io.FeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.SyndFeedOutput
import net.thauvin.erik.mobibot.Utils.toIsoLocalDate
import net.thauvin.erik.mobibot.Utils.today
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.exists

/**
 * Manages the RSS feeds.
 */
class FeedsManager private constructor() {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FeedsManager::class.java)

        // The file containing the current entries.
        private const val CURRENT_XML = "current.xml"

        // The .xml extension.
        private const val DOT_XML = ".xml"

        /**
         * Loads the current feed.
         */
        @JvmStatic
        @Throws(IOException::class, FeedException::class)
        fun loadFeed(entries: Entries, currentFile: String = CURRENT_XML): String {
            entries.links.clear()
            val xml = Paths.get("${entries.logsDir}${currentFile}")
            var pubDate = today()
            if (xml.exists()) {
                val input = SyndFeedInput()
                InputStreamReader(
                    Files.newInputStream(xml), StandardCharsets.UTF_8
                ).use { reader ->
                    val feed = input.build(reader)
                    pubDate = feed.publishedDate.toIsoLocalDate()
                    val items = feed.entries
                    var entry: EntryLink
                    for (i in items.indices.reversed()) {
                        with(items[i]) {
                            entry = EntryLink(
                                link,
                                title,
                                author.substring(author.lastIndexOf('(') + 1, author.length - 1),
                                entries.channel,
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
                        entries.links.add(entry)
                    }
                }
            } else {
                // Create an empty feed.
                saveFeed(entries)
            }
            return pubDate
        }

        /**
         * Saves the feeds.
         */
        @JvmStatic
        fun saveFeed(entries: Entries, currentFile: String = CURRENT_XML) {
            if (logger.isDebugEnabled) logger.debug("Saving the feeds...")
            if (entries.logsDir.isNotBlank()) {
                try {
                    val output = SyndFeedOutput()
                    val rss: SyndFeed = SyndFeedImpl()
                    val items: MutableList<SyndEntry> = mutableListOf()
                    var item: SyndEntry
                    OutputStreamWriter(
                        Files.newOutputStream(Paths.get("${entries.logsDir}${currentFile}")), StandardCharsets.UTF_8
                    ).use { fw ->
                        with(rss) {
                            feedType = "rss_2.0"
                            title = "${entries.channel} IRC Links"
                            description = "Links from ${entries.ircServer} on ${entries.channel}"
                            if (entries.backlogs.isNotBlank()) link = entries.backlogs
                            publishedDate = Calendar.getInstance().time
                            language = "en"
                        }
                        val buff: StringBuilder = StringBuilder()
                        for (i in entries.links.indices.reversed()) {
                            with(entries.links[i]) {
                                buff.setLength(0)
                                buff.append("Posted by <b>")
                                    .append(nick)
                                    .append("</b> on <a href=\"irc://")
                                    .append(entries.ircServer).append('/')
                                    .append(channel)
                                    .append("\"><b>")
                                    .append(channel)
                                    .append("</b></a>")
                                if (comments.isNotEmpty()) {
                                    buff.append(" <br/><br/>")
                                    for (j in comments.indices) {
                                        if (j > 0) {
                                            buff.append(" <br/>")
                                        }
                                        buff.append(comments[j].nick).append(": ").append(comments[j].comment)
                                    }
                                }
                                item = SyndEntryImpl()
                                item.link = link
                                item.description = SyndContentImpl().apply { value = buff.toString() }
                                item.title = title
                                item.publishedDate = date
                                item.author = "${channel.removePrefix("#")}@${entries.ircServer} ($nick)"
                                item.categories = tags
                                items.add(item)
                            }
                        }
                        rss.entries = items
                        if (logger.isDebugEnabled) logger.debug("Writing the entries feed.")
                        output.output(rss, fw)
                    }
                    OutputStreamWriter(
                        Files.newOutputStream(
                            Paths.get(
                                entries.logsDir + today() + DOT_XML
                            )
                        ), StandardCharsets.UTF_8
                    ).use { fw -> output.output(rss, fw) }
                } catch (e: FeedException) {
                    if (logger.isWarnEnabled) logger.warn("Unable to generate the entries feed.", e)
                } catch (e: IOException) {
                    if (logger.isWarnEnabled)
                        logger.warn("An IO error occurred while generating the entries feed.", e)
                }
            } else {
                if (logger.isWarnEnabled) {
                    logger.warn("Unable to generate the entries feed. A required property is missing.")
                }
            }
        }
    }
}
