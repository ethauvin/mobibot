/*
 * PinboardTests.kt
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

package net.thauvin.erik.mobibot

import net.thauvin.erik.mobibot.Utils.encodeUrl
import net.thauvin.erik.mobibot.Utils.reader
import net.thauvin.erik.mobibot.entries.EntryLink
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PinboardTests : LocalProperties() {
    private val apiToken = getProperty("pinboard-api-token")

    private val ircServer = "irc.test.com"
    private val pinboard = Pinboard().apply { setApiToken(apiToken) }

    private fun newEntry(): EntryLink {
        return EntryLink(
            randomUrl(), "Test Example", "ErikT", "", "#mobitopia", listOf("test")
        )
    }

    private fun randomUrl(): String {
        return "https://www.example.com/${(5001..9999).random()}"
    }

    private fun validatePin(apiToken: String, url: String, vararg matches: String): Boolean {
        val response =
            URI(
                "https://api.pinboard.in/v1/posts/get?auth_token=${apiToken}&tag=test&" + url.encodeUrl()
            ).toURL().reader().body

        matches.forEach {
            if (!response.contains(it)) {
                return false
            }
        }

        return response.contains(url)
    }

    @Test
    fun addPin() {
        val entry = newEntry()
        pinboard.addPin(ircServer, entry)
        assertTrue(validatePin(apiToken, url = entry.link, entry.title, entry.nick, entry.channel), "addPin")

        pinboard.deletePin(entry)
        assertFalse(validatePin(apiToken, url = entry.link, entry.title, entry.nick, entry.channel), "deletePin")

    }

    @Test
    fun updatePin() {
        val entry = newEntry()
        pinboard.addPin(ircServer, entry)
        assertTrue(validatePin(apiToken, url = entry.link, entry.title, entry.nick, entry.channel), "addPin")

        val url = entry.link
        entry.link = randomUrl()
        pinboard.updatePin(ircServer, url, entry)
        assertTrue(validatePin(apiToken, url = entry.link, ircServer), "updatePin")

        pinboard.deletePin(entry)
        assertFalse(validatePin(apiToken, url = entry.link, entry.title, entry.nick, entry.channel), "deletePin")
    }

    @Test
    fun updatePinTitle() {
        val entry = newEntry()
        pinboard.addPin(ircServer, entry)
        assertTrue(validatePin(apiToken, url = entry.link, entry.title, entry.nick, entry.channel), "addPin")

        pinboard.updatePin(ircServer, entry.link, entry)
        assertTrue(validatePin(apiToken, url = entry.link, entry.title), "updatePin")

        pinboard.deletePin(entry)
        assertFalse(validatePin(apiToken, url = entry.link, entry.title, entry.nick, entry.channel), "deletePin")
    }
}
