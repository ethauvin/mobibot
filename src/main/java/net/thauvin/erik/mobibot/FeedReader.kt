/*
 * FeedReader.kt
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
package net.thauvin.erik.mobibot

import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.net.MalformedURLException
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
    // Maximum number of feed items to display
    private val maxItems = 5

    /**
     * Fetches the Feed's items.
     */
    override fun run() {
        with(bot) {
            try {
                val input = SyndFeedInput()
                XmlReader(URL(url)).use { reader ->
                    val feed = input.build(reader)
                    val items = feed.entries
                    if (items.isEmpty()) {
                        send(sender, "There is currently nothing to view.", false)
                    } else {
                        var i = 0
                        while (i < items.size && i < maxItems) {
                            send(sender, items[i].title, false)
                            send(sender, Utils.helpFormat(Utils.green(items[i].link), false), false)
                            i++
                        }
                    }
                }
            } catch (e: MalformedURLException) {
                if (logger.isDebugEnabled) logger.debug("Invalid feed URL.", e)
                send(sender, "The feed URL is invalid.", false)
            } catch (e: Exception) {
                if (logger.isDebugEnabled) logger.debug("Unable to fetch the feed.", e)
                send(sender, "An error has occurred while fetching the feed: ${e.message}", false)
            }
        }
    }
}
