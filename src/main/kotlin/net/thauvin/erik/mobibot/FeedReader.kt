/*
 * FeedReader.kt
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
package net.thauvin.erik.mobibot

import com.rometools.rome.io.FeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import net.thauvin.erik.mobibot.Utils.green
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import java.io.IOException
import java.net.URL

/**
 * Reads a RSS feed.
 */
class FeedReader(
    // Bot
    private val bot: Mobibot,
    // Nick of the person who sent the message
    private val sender: String,
    // URL to fetch
    private val url: String
) : Runnable {
    /**
     * Fetches the Feed's items.
     */
    override fun run() {
        with(bot) {
            try {
                readFeed(url).forEach {
                    send(sender, it)
                }
            } catch (e: FeedException) {
                if (logger.isDebugEnabled) logger.debug("Unabled to parse the feed at $url", e)
                send(sender, "An error has occured while parsing the feed: ${e.message}", false)
            } catch (e: IOException) {
                if (logger.isDebugEnabled) logger.debug("Unable to fetch the feed at $url", e)
                send(sender, "An error has occurred while fetching the feed: ${e.message}", false)
            }
        }
    }

    companion object {
        @JvmStatic
        @Throws(FeedException::class, IOException::class)
        fun readFeed(url: String, maxItems: Int = 5): List<Message> {
            val messages = mutableListOf<Message>()
            val input = SyndFeedInput()
            XmlReader(URL(url)).use { reader ->
                val feed = input.build(reader)
                val items = feed.entries
                if (items.isEmpty()) {
                    messages.add(PublicMessage("There is currently nothing to view."))
                } else {
                    items.take(maxItems).forEach {
                        messages.add(PublicMessage(it.title))
                        messages.add(PublicMessage(helpFormat(green(it.link), false)))
                    }
                }
            }
            return messages
        }
    }
}
