/*
 * EntriesUtilsTest.kt
 *
 * Copyright 2004-2023 Erik C. Thauvin (erik@thauvin.net)
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

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.entries.EntriesUtils.printComment
import net.thauvin.erik.mobibot.entries.EntriesUtils.printLink
import net.thauvin.erik.mobibot.entries.EntriesUtils.printTags
import net.thauvin.erik.mobibot.entries.EntriesUtils.toLinkLabel
import org.testng.annotations.Test

class EntriesUtilsTest {
    private val comment = EntryComment("comment", "nick")
    private val links = buildList {
        for (i in 0..5) {
            add(
                EntryLink(
                    "https://www.mobitopia.org/$i",
                    "Mobitopia$i",
                    "Skynx$i",
                    "JimH$i",
                    "#mobitopia$i",
                    listOf("tag1", "tag2", "tag3", "TAG4", "Tag5")
                )
            )
        }
    }

    @Test(groups = ["entries"])
    fun printCommentTest() {
        assertThat(printComment(0, 0, comment)).isEqualTo("${Constants.LINK_CMD}1.1: [nick] comment")
    }

    @Test(groups = ["entries"])
    fun printLinkTest() {
        for (i in links.indices) {
            assertThat(
                printLink(i - 1, links[i]), "link $i"
            ).isEqualTo("L$i: [Skynx$i] \u0002Mobitopia$i\u0002 ( \u000303https://www.mobitopia.org/$i\u000F )")
        }

        assertThat(links.first().addComment(comment), "addComment()").isEqualTo(0)
        assertThat(printLink(0, links.first(), isView = true), "printLink(isView=true)").contains("[+1]")
    }

    @Test(groups = ["entries"])
    fun printTagsTest() {
        for (i in links.indices) {
            assertThat(
                printTags(i - 1, links[i]), "tag $i"
            ).isEqualTo("L${i}T: tag1, tag2, tag3, tag4, tag5")
        }
    }

    @Test(groups = ["entries"])
    fun toLinkLabelTest() {
        assertThat(1.toLinkLabel()).isEqualTo("${Constants.LINK_CMD}2")
    }
}
