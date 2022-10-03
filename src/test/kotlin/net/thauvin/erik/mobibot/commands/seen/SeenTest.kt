/*
 * SeenTest.kt
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

package net.thauvin.erik.mobibot.commands.seen

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.key
import assertk.assertions.size
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize

class SeenTest {
    private val tmpFile = kotlin.io.path.createTempFile(suffix = ".ser")
    private val seen = Seen(tmpFile.toAbsolutePath().toString())
    private val nick = "ErikT"

    @BeforeClass
    fun saveTest() {
        seen.add("ErikT")
        assertThat(tmpFile.fileSize(), "tmpFile.size").isGreaterThan(0)
    }

    @AfterClass(alwaysRun = true)
    fun afterClass() {
        tmpFile.deleteIfExists()
    }

    @Test(priority = 1, groups = ["commands"])
    fun loadTest() {
        seen.clear()
        assertThat(seen.seenNicks, "seenNicks").isEmpty()
        seen.load()
        assertThat(seen.seenNicks).key(nick).isNotNull()
    }

    @Test(groups = ["commands"])
    fun addTest() {
        val last = seen.seenNicks[nick]?.lastSeen
        seen.add(nick.lowercase())
        assertThat(seen.seenNicks, "seenNicks").size().isEqualTo(1)
        assertThat(seen.seenNicks[nick]?.lastSeen, "seenNicks[$nick].lastSeen").isNotEqualTo(last)
        assertThat(seen.seenNicks[nick]?.nick, "seenNicks[$nick].nick").isEqualTo(nick.lowercase())
    }

    @Test(priority = 10, groups = ["commands"])
    fun clearTest() {
        seen.clear()
        seen.save()
        seen.load()
        assertThat(seen.seenNicks, "seenNicks").size().isEqualTo(0)
    }
}
