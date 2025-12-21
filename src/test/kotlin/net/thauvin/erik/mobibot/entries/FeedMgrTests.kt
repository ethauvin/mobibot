/*
 * FeedMgrTests.kt
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
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
import assertk.assertions.*
import net.thauvin.erik.mobibot.Utils.today
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize
import kotlin.io.path.name
import kotlin.test.Test

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FeedMgrTests {
    private val entries = Entries()
    private val channel = "mobibot"
    private var currentTestFile: Path
    private var backlogTestFile: Path

    init {
        entries.logsDir = "src/test/resources/"
        entries.ircServer = "irc.example.com"
        entries.channel = channel
        entries.backlogs = "https://www.mobitopia.org/mobibot/logs"

        currentTestFile = Paths.get("${entries.logsDir}test.xml")
        backlogTestFile = Paths.get("${entries.logsDir}${today()}.xml")
    }

    @Test
    @Order(1)
    fun loadFeed() {
        // Load the feed
        assertThat(FeedsManager.loadFeed(entries), "loadFeed()").isEqualTo("2021-10-31")
        assertThat(entries.links.count()).isEqualTo(2)

        entries.links.forEachIndexed { i, entryLink ->
            assertThat(entryLink, "entryLink[${i + 1}]").all {
                prop(EntryLink::title).isEqualTo("Example ${i + 1}")
                prop(EntryLink::link).isEqualTo("https://www.example.com/${i + 1}")
                prop(EntryLink::channel).isEqualTo(channel)
            }
            entryLink.tags.forEachIndexed { y, tag ->
                assertThat(tag.name, "tag${i + 1}-${y + 1}").isEqualTo("tag${i + 1}-${y + 1}")
            }
        }

        with(entries.links.first()) {
            assertThat(nick, "nick[first]").isEqualTo("ErikT")
            assertThat(date, "date[first]").isEqualTo(Date(1635638400000L))
            assertThat(comments.first(), "comments[first]").all {
                prop(EntryComment::comment).endsWith("comment 1.")
                prop(EntryComment::nick).isEqualTo("ErikT")
            }
            assertThat(comments.last(), "comments[last]").all {
                prop(EntryComment::comment).endsWith("comment 2.")
                prop(EntryComment::nick).isEqualTo("Skynx")
            }
        }

        assertThat(entries.links, "links").index(1).all {
            prop(EntryLink::nick).isEqualTo("Skynx")
            prop(EntryLink::date).isEqualTo(Date(1635638460000L))
        }
    }

    @Test
    @Order(2)
    fun saveFeed() {
        // Load the feed
        FeedsManager.loadFeed(entries)
        assertThat(entries.links.count()).isEqualTo(2)

        FeedsManager.saveFeed(entries, currentTestFile.name)

        assertThat(currentTestFile, "currentFile").exists()
        assertThat(backlogTestFile, "backlogFile").exists()

        assertThat(currentTestFile.fileSize(), "currentFile == backlogFile").isEqualTo(backlogTestFile.fileSize())
    }

    @Test
    @Order(3)
    fun loadTestFeed() {
        assertThat(entries.count()).isEqualTo(0)

        FeedsManager.loadFeed(entries, currentTestFile.name)

        assertThat(entries.count()).isEqualTo(2)
    }

    @Test
    @Order(4)
    fun deleteFeeds() {
        assertThat(currentTestFile.deleteIfExists(), "currentTestFile.deleteIfExists()").isTrue()
        assertThat(backlogTestFile.deleteIfExists(), "backlogTEstFile.deleteIfExists()").isTrue()
    }
}
