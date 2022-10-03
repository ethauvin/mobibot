/*
 * EntriesUtilsTest.kt
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

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.entries.EntriesUtils.buildComment
import net.thauvin.erik.mobibot.entries.EntriesUtils.buildLink
import net.thauvin.erik.mobibot.entries.EntriesUtils.buildTags
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
    fun buildLinkLabelTest() {
        assertThat(1.toLinkLabel()).isEqualTo("${Constants.LINK_CMD}2")
    }

    @Test(groups = ["entries"])
    fun buildCommentTest() {
        assertThat(buildComment(0, 0, comment)).isEqualTo("${Constants.LINK_CMD}1.1: [nick] comment")
    }

    @Test(groups = ["entries"])
    fun buildLinkTest() {
        for (i in links.indices) {
            assertThat(
                buildLink(i - 1, links[i]), "link $i"
            ).isEqualTo("L$i: [Skynx$i] \u0002Mobitopia$i\u0002 ( \u000303https://www.mobitopia.org/$i\u000F )")
        }

        assertThat(links.first().addComment(comment), "addComment()").isEqualTo(0)
        assertThat(buildLink(0, links.first(), isView = true), "buildLink(isView=true)").contains("[+1]")
    }

    @Test(groups = ["entries"])
    fun buildTagsTest() {
        for (i in links.indices) {
            assertThat(
                buildTags(i - 1, links[i]), "tag $i"
            ).isEqualTo("L${i}T: Skynx$i, tag1, tag2, tag3, tag4, tag5")
        }
    }
}
