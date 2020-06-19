/*
 * PinboardTest.kt
 *
 * Copyright (c) 2004-2020, Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.entries.EntryLink
import net.thauvin.erik.pinboard.PinboardPoster
import org.testng.Assert
import org.testng.annotations.Test
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class PinboardUtilsTest : LocalProperties() {
    @Test
    @Throws(InterruptedException::class, IOException::class, URISyntaxException::class)
    fun pinboardTest() {
        val apiToken = getProperty("pinboard-api-token")
        val pinboard = PinboardPoster(apiToken)
        val url = "https://www.example.com/"
        val ircServer = "irc.test.com"
        val entry = EntryLink(url, "Test Example", "ErikT", "", "#mobitopia", listOf("test"))

        PinboardUtils.addPin(pinboard, ircServer, entry)
        Assert.assertTrue(validatePin(apiToken, ircServer, entry.link), "add")
        entry.link = "https://www.foo.com/"

        PinboardUtils.updatePin(pinboard, ircServer, url, entry)
        Assert.assertTrue(validatePin(apiToken, ircServer, entry.link), "update")

        PinboardUtils.deletePin(pinboard, entry)
        Assert.assertFalse(validatePin(apiToken, url = entry.link), "delete")
    }

    @Throws(IOException::class, URISyntaxException::class, InterruptedException::class)
    private fun validatePin(apiToken: String, ircServer: String = "", url: String): Boolean {
        val request = HttpRequest.newBuilder().uri(
            URI(
                "https://api.pinboard.in/v1/posts/get?auth_token=${apiToken}&tag=test&"
                    + URLEncoder.encode(url, StandardCharsets.UTF_8)
            )
        ).GET().build()

        val response = HttpClient.newBuilder()
            .build()
            .send(request, HttpResponse.BodyHandlers.ofString())

        return if (response.statusCode() == 200) {
            response.body().contains(url) && response.body().contains(ircServer)
        } else false
    }
}
