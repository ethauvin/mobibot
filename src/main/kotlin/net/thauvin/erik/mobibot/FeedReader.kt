/*
 * FeedReader.kt
 *
 * Copyright (c) 2004-2026 Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot

import com.rometools.rome.io.FeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.thauvin.erik.mobibot.Utils.green
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.entries.FeedsManager
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.NoticeMessage
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI

/**
 * Reads an RSS feed.
 */
@SuppressFBWarnings("LO_SUSPECT_LOG_CLASS")
class FeedReader(private val url: String, val event: GenericMessageEvent) : Runnable {
    private val logger: Logger = LoggerFactory.getLogger(FeedsManager::class.java)

    @SuppressFBWarnings("")
    companion object {
        @JvmStatic
        @Throws(FeedException::class, IOException::class)
        fun readFeed(url: String, maxItems: Int = 5): List<Message> {
            val messages = mutableListOf<Message>()
            val input = SyndFeedInput()
            XmlReader(
                URI(url).toURL().openStream()
            ).use { reader ->
                val feed = input.build(reader)
                val items = feed.entries
                if (items.isEmpty()) {
                    messages.add(NoticeMessage("There is currently nothing to view."))
                } else {
                    items.take(maxItems).forEach {
                        messages.add(NoticeMessage(it.title))
                        messages.add(NoticeMessage(helpFormat(it.link.green(), false)))
                    }
                }
            }
            return messages
        }
    }

    /**
     * Fetches the Feed's items.
     */
    override fun run() {
        try {
            readFeed(url).forEach {
                event.sendMessage("", it)
            }
        } catch (e: FeedException) {
            if (logger.isWarnEnabled) logger.warn("Unable to parse the feed at $url", e)
            event.sendMessage("An error has occurred while parsing the feed: ${e.message}")
        } catch (e: IOException) {
            if (logger.isWarnEnabled) logger.warn("Unable to fetch the feed at $url", e)
            event.sendMessage("An IO error has occurred while fetching the feed: ${e.message}")
        }
    }
}
