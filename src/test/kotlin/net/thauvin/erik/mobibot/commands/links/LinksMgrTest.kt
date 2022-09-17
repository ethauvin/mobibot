/*
 * LinksMgrTest.kt
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

package net.thauvin.erik.mobibot.commands.links

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import assertk.assertions.size
import net.thauvin.erik.mobibot.Constants
import org.testng.annotations.Test

class LinksMgrTest {
    private val linksMgr = LinksMgr()

    @Test(groups = ["commands", "links"])
    fun fetchTitle() {
        assertThat(linksMgr.fetchTitle("https://erik.thauvin.net/"), "Erik").contains("Erik's Weblog")
        assertThat(linksMgr.fetchTitle("https://www.google.com/foo"), "Foo").isEqualTo(Constants.NO_TITLE)
    }

    @Test(groups = ["commands", "links"])
    fun testMatches() {
        assertThat(linksMgr.matches("https://www.example.com/"), "https").isTrue()
        assertThat(linksMgr.matches("HTTP://erik.thauvin.net/blog/ Erik's Weblog"), "HTTP").isTrue()
    }

    @Test(groups = ["commands", "links"])
    fun matchTagKeywordsTest() {
        linksMgr.setProperty(LinksMgr.KEYWORDS_PROP, "key1 key2,key3")
        val tags = mutableListOf<String>()

        linksMgr.matchTagKeywords("Test title with key2", tags)
        assertThat(tags, "key2").contains("key2")
        tags.clear()

        linksMgr.matchTagKeywords("Test key3 title with key1", tags)
        assertThat(tags, "key1 & key 3").all {
            contains("key1")
            contains("key3")
            size().isEqualTo(2)
        }
    }
}
