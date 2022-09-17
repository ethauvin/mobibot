/*
 * FeedMgrTest.kt
 *
 * Copyright (c) 2004-2022, Erik C. Thauvin (erik@thauvin.net)
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

import assertk.all
import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import assertk.assertions.prop
import net.thauvin.erik.mobibot.Utils.today
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Test
import java.nio.file.Paths
import java.util.Date
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.name

class FeedMgrTest {
    private val entries = Entries()
    private val channel = "mobibot"

    @BeforeSuite(alwaysRun = true)
    fun beforeSuite() {
        entries.logsDir = "src/test/resources/"
        entries.ircServer = "irc.example.com"
        entries.channel = channel
        entries.backlogs = "https://www.mobitopia.org/mobibot/logs"
    }

    @Test(groups = ["entries"])
    fun testFeedMgr() {
        // Load the feed
        assertThat(FeedsMgr.loadFeed(entries), "pubDate").isEqualTo("2021-10-31")

        assertThat(entries.links.size, "2 links").isEqualTo(2)
        entries.links.forEachIndexed { i, entryLink ->
            assertThat(entryLink, "Example $(i + 1)").all {
                prop(EntryLink::title).isEqualTo("Example ${i + 1}")
                prop(EntryLink::link).isEqualTo("https://www.example.com/${i + 1}")
                prop(EntryLink::channel).isEqualTo(channel)
            }
            entryLink.tags.forEachIndexed { y, tag ->
                assertThat(tag.name, "tag${i + 1}-${y + 1}").isEqualTo("tag${i + 1}-${y + 1}")
            }
        }

        with(entries.links.first()) {
            assertThat(nick, "first nick").isEqualTo("ErikT")
            assertThat(date, "first date").isEqualTo(Date(1635638400000L))
            assertThat(comments.first(), "first comment").all {
                prop(EntryComment::comment).endsWith("comment 1.")
                prop(EntryComment::nick).isEqualTo("ErikT")
            }
            assertThat(comments.last(), "last comment").all {
                prop(EntryComment::comment).endsWith("comment 2.")
                prop(EntryComment::nick).isEqualTo("Skynx")
            }
        }

        assertThat(entries.links[1], "second link").all {
            prop(EntryLink::nick).isEqualTo("Skynx")
            prop(EntryLink::date).isEqualTo(Date(1635638460000L))
        }

        val currentFile = Paths.get("${entries.logsDir}test.xml")
        val backlogFile = Paths.get("${entries.logsDir}${today()}.xml")

        // Save the feed
        FeedsMgr.saveFeed(entries, currentFile.name)

        assertThat(currentFile.exists(), "${currentFile.absolutePathString()} exists").isTrue()
        assertThat(backlogFile.exists(), "${backlogFile.absolutePathString()} exits").isTrue()

        assertThat(currentFile.fileSize(), "files are identical").isEqualTo(backlogFile.fileSize())

        // Load the test feed
        entries.links.clear()
        FeedsMgr.loadFeed(entries, currentFile.name)

        entries.links.forEachIndexed { i, entryLink ->
            assertThat(entryLink.title, "${currentFile.name} title ${i + 1}").isEqualTo("Example ${i + 1}")
        }

        assertThat(currentFile.deleteIfExists(), "delete ${currentFile.absolutePathString()}").isTrue()
        assertThat(backlogFile.deleteIfExists(), "delete ${backlogFile.absolutePathString()}").isTrue()
    }
}
