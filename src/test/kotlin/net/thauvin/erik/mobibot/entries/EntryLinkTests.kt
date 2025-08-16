/*
 * EntryLinkTests.kt
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
import com.rometools.rome.feed.synd.SyndCategory
import com.rometools.rome.feed.synd.SyndCategoryImpl
import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.commands.links.Tags
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.security.SecureRandom
import java.util.*
import kotlin.test.Test

class EntryLinkTests {
    private val entryLink = EntryLink(
        "https://www.mobitopia.org/", "Mobitopia", "Skynx", "JimH", "#mobitopia",
        listOf("tag1", "tag2", "tag3", "TAG4", "Tag5")
    )

    @Test
    fun `Add then delete comment`() {
        var i = 0
        while (i < 5) {
            entryLink.addComment("c$i", "u$i")
            i++
        }
        assertThat(entryLink.comments, "comments").size().isEqualTo(i)
        i = 0
        for (comment in entryLink.comments) {
            assertThat(comment).all {
                prop(EntryComment::comment).isEqualTo("c$i")
                prop(EntryComment::nick).isEqualTo("u$i")
            }
            i++
        }

        val r = SecureRandom()
        while (entryLink.comments.isNotEmpty()) {
            entryLink.deleteComment(r.nextInt(entryLink.comments.size))
        }
        assertThat(entryLink.comments, "hasComments()").isEmpty()
        entryLink.addComment("nothing", "nobody")
        entryLink.setComment(0, "something", "somebody")
        val comment = entryLink.getComment(0)
        assertThat(comment, "comment[first]").all {
            prop(EntryComment::nick).isEqualTo("somebody")
            prop(EntryComment::comment).isEqualTo("something")
        }
        assertThat(entryLink.deleteComment(comment), "deleteComment").isTrue()
        assertThat(entryLink.deleteComment(comment), "comment is already deleted").isFalse()
    }

    @Test
    fun `Validate EntryLink constructor`() {
        val tags = listOf(SyndCategoryImpl().apply { name = "tag1" }, SyndCategoryImpl().apply { name = "tag2" })
        val link = EntryLink("link", "title", "nick", "channel", Date(), tags)
        assertThat(link, "link").all {
            prop(EntryLink::tags).size().isEqualTo(tags.size)
            prop(EntryLink::tags).index(0).prop(SyndCategory::getName).isEqualTo("tag1")
        }
    }

    @Test
    fun `Validate EntryLink matches`() {
        assertThat(entryLink.matches("mobitopia"), "matches(mobitopia)").isTrue()
        assertThat(entryLink.matches("skynx"), "match(nick)").isTrue()
        assertThat(entryLink.matches("www.mobitopia.org"), "matches(url)").isTrue()
        assertThat(entryLink.matches("foo"), "matches(foo)").isFalse()
        assertThat(entryLink.matches("<empty>"), "matches(empty)").isFalse()
        assertThat(entryLink.matches(null), "matches(null)").isFalse()
    }

    @Nested
    @DisplayName("Validate Tags Test")
    inner class ValidateTagsTest {
        @Test
        fun `Validate tags parsing in constructor`() {
            val tags: List<SyndCategory> = entryLink.tags
            for ((i, tag) in tags.withIndex()) {
                assertThat(tag.name, "tag.name($i)").isEqualTo("tag${i + 1}")
            }
            assertThat(entryLink::tags).size().isEqualTo(5)
        }

        @Test
        fun `Validate attempting to remove channel tag`() {
            val link = entryLink
            link.setTags("+mobitopia")
            link.setTags("-mobitopia") // can't remove the channel tag
            assertThat(
                link.formatTags(",")
            ).isEqualTo("tag1,tag2,tag3,tag4,tag5,mobitopia")
        }

        @Test
        fun `Validate formatting tags with spaces`() {
            val link = entryLink
            link.setTags("-tag4")
            assertThat(
                link.formatTags(" ", ",")
            ).isEqualTo(",tag1 tag2 tag3 tag5")
        }

        @Test
        fun `Validate setting blank tags`() {
            val link = entryLink
            val size = link.tags.size
            link.setTags("    ")
            assertThat(link.tags, "setTags('    ')").size().isEqualTo(size)
        }

        @Test
        fun `Validate setting empty tags`() {
            val link = entryLink
            val size = link.tags.size
            link.setTags("")
            assertThat(link.tags, "setTags(\"\")").size().isEqualTo(size)

        }
    }

    @Nested
    @DisplayName("Tags Matches Tests")
    inner class TagsMatchesTests {
        @ParameterizedTest
        @ValueSource(
            strings = [
                Constants.LINK_CMD + "123" + Constants.TAG_CMD + ":validtag",
                Constants.LINK_CMD + "567" + Constants.TAG_CMD + ":AnotherTag",
                Constants.LINK_CMD + "42" + Constants.TAG_CMD + ":sometag",
                Constants.LINK_CMD + "123" + Constants.TAG_CMD + ":"
            ]
        )
        fun `Matches valid link command patterns`(input: String) {
            val tags = Tags()
            assertThat(tags.matches(input)).isTrue()

        }

        @ParameterizedTest(name = "[{index}] ''{0}''")
        @ValueSource(
            strings = [
                Constants.LINK_CMD + "",
                Constants.LINK_CMD + "123",
                Constants.LINK_CMD + "123" + Constants.TAG_CMD + ""
            ]
        )
        fun `Does not match invalid link command patterns`(input: String) {
            val tags = Tags()
            assertThat(tags.matches(input)).isFalse()
        }

        @ParameterizedTest(name = "[{index}] ''{0}''")
        @ValueSource(strings = ["", " "])
        fun `Handles empty or blank input`(input: String) {
            val tags = Tags()
            assertThat(tags.matches(input)).isFalse()
        }
    }
}
