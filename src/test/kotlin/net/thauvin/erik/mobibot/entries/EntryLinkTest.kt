/*
 * EntryLinkTest.kt
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
package net.thauvin.erik.mobibot.entries

import com.rometools.rome.feed.synd.SyndCategory
import com.rometools.rome.feed.synd.SyndCategoryImpl
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test
import java.security.SecureRandom
import java.util.Date

/**
 * The `EntryUtilsTest` class.
 *
 * @author [Erik C. Thauvin](https://erik.thauvin.net/)
 * @created 2019-04-19
 * @since 1.0
 */
class EntryLinkTest {
    private val entryLink = EntryLink(
        "https://www.mobitopia.org/", "Mobitopia", "Skynx", "JimH", "#mobitopia",
        listOf("tag1", "tag2", "tag3", "TAG4", "Tag5")
    )

    @Test
    fun testAddDeleteComment() {
        var i = 0
        while (i < 5) {
            entryLink.addComment("c$i", "u$i")
            i++
        }
        assertThat(entryLink.comments.size).describedAs("getComments().size() == 5").isEqualTo(i)
        i = 0
        for (comment in entryLink.comments) {
            assertThat(comment.comment).describedAs("getComment($i)").isEqualTo("c$i")
            assertThat(comment.nick).describedAs("getNick($i)").isEqualTo("u$i")
            i++
        }
        val r = SecureRandom()
        while (entryLink.comments.size > 0) {
            entryLink.deleteComment(r.nextInt(entryLink.comments.size))
        }
        assertThat(entryLink.comments.isNotEmpty()).describedAs("hasComments()").isFalse
        entryLink.addComment("nothing", "nobody")
        entryLink.setComment(0, "something", "somebody")
        assertThat(entryLink.getComment(0).nick).describedAs("getNick(somebody)").isEqualTo("somebody")
        assertThat(entryLink.getComment(0).comment).describedAs("getComment(something)").isEqualTo("something")
    }

    @Test
    fun testConstructor() {
        val tag = "test"
        val tags = listOf(SyndCategoryImpl().apply { name = tag })
        val link = EntryLink("link", "title", "nick", "channel", Date(), tags)
        assertThat(link.tags.size).describedAs("check tag size").isEqualTo(tags.size)
        assertThat(link.tags[0].name).describedAs("check tag name").isEqualTo(tag)
        assertThat(link.pinboardTags).describedAs("check pinboard tags").isEqualTo("nick,$tag")
    }

    @Test
    fun testMatches() {
        assertThat(entryLink.matches("mobitopia")).describedAs("match mobitopia").isTrue
        assertThat(entryLink.matches("skynx")).describedAs("match nick").isTrue
        assertThat(entryLink.matches("www.mobitopia.org")).describedAs("match url").isTrue
        assertThat(entryLink.matches("foo")).describedAs("match foo").isFalse
        assertThat(entryLink.matches("")).describedAs("match empty").isFalse
        assertThat(entryLink.matches(null)).describedAs("match null").isFalse
    }


    @Test
    fun testTags() {
        val tags: List<SyndCategory> = entryLink.tags
        for ((i, tag) in tags.withIndex()) {
            assertThat(tag.name).describedAs("tag.getName($i)").isEqualTo("tag" + (i + 1))
        }
        assertThat(entryLink.tags.size).describedAs("getTags().size() is 5").isEqualTo(5)
        assertThat(entryLink.tags.isNotEmpty()).describedAs("hasTags() is true").isTrue
        entryLink.setTags("-tag5")
        entryLink.setTags("+mobitopia")
        entryLink.setTags("tag4")
        entryLink.setTags("-mobitopia")
        assertThat(entryLink.pinboardTags).describedAs("getPinboardTags()")
            .isEqualTo(entryLink.nick + ",tag1,tag2,tag3,tag4,mobitopia")
        val size = entryLink.tags.size
        entryLink.setTags("")
        assertThat(entryLink.tags.size).describedAs("empty tag").isEqualTo(size)
        entryLink.setTags("    ")
        assertThat(entryLink.tags.size).describedAs("blank tag").isEqualTo(size)
    }
}
