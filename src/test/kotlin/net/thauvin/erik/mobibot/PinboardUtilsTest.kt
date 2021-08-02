/*
 * PinboardUtilsTest.kt
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

import net.thauvin.erik.mobibot.PinboardUtils.toTimestamp
import net.thauvin.erik.mobibot.entries.EntryLink
import net.thauvin.erik.pinboard.PinboardPoster
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.net.URL
import java.util.Date

class PinboardUtilsTest : LocalProperties() {
    @Test
    fun pinboardTest() {
        val apiToken = getProperty("pinboard-api-token")
        val pinboard = PinboardPoster(apiToken)
        val url = "https://www.example.com/"
        val ircServer = "irc.test.com"
        val entry = EntryLink(url, "Test Example", "ErikT", "", "#mobitopia", listOf("test"))

        assertTrue(PinboardUtils.addPin(pinboard, ircServer, entry), "addPin")
        assertTrue(validatePin(apiToken, url = entry.link, entry.title, entry.nick, entry.channel), "validate add")

        entry.link = "https://www.foo.com/"
        assertTrue(PinboardUtils.updatePin(pinboard, ircServer, url, entry), "updatePin")
        assertTrue(validatePin(apiToken, url = entry.link, ircServer), "validate update")

        entry.title = "Foo Title"
        assertTrue(PinboardUtils.updatePin(pinboard, ircServer, entry.link, entry), "update title")
        assertTrue(validatePin(apiToken, url = entry.link, entry.title), "validate title")

        assertTrue(PinboardUtils.deletePin(pinboard, entry), "daletePin")
        assertFalse(validatePin(apiToken, url = entry.link), "validate delete")
    }

    @Test
    fun toTimestampTest() {
        val d = Date()
        assertTrue(d.toTimestamp().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z".toRegex()))
    }

    private fun validatePin(apiToken: String, url: String, vararg matches: String): Boolean {
        val response = Utils.urlReader(
            URL(
                "https://api.pinboard.in/v1/posts/get?auth_token=${apiToken}&tag=test&"
                        + Utils.encodeUrl(url)
            )
        )

        matches.forEach {
            if (!response.contains(it)) {
                return false
            }
        }

        return response.contains(url)
    }
}
