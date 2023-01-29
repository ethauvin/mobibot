/*
 * FeedReaderTest.kt
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
package net.thauvin.erik.mobibot

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import assertk.assertions.size
import com.rometools.rome.io.FeedException
import net.thauvin.erik.mobibot.FeedReader.Companion.readFeed
import net.thauvin.erik.mobibot.msg.Message
import org.testng.annotations.Test
import java.io.IOException
import java.net.MalformedURLException
import java.net.UnknownHostException

/**
 * The `FeedReader Test` class.
 */
class FeedReaderTest {
    @Test
    fun readFeedTest() {
        var messages = readFeed("https://feeds.thauvin.net/ethauvin")
        assertThat(messages, "messages").all {
            size().isEqualTo(10)
            index(1).prop(Message::msg).contains("erik.thauvin.net")
        }

        messages = readFeed("https://www.mobitopia.org/mobibot/logs/2021-10-27.xml")
        assertThat(messages, "messages").index(0).prop(Message::msg).contains("nothing")

        messages = readFeed("https://www.mobitopia.org/mobibot/logs/2005-10-11.xml", 42)
        assertThat(messages, "messages").size().isEqualTo(84)
        assertThat(messages.last(), "messages.last").prop(Message::msg).contains("techdigest.tv")

        assertThat { readFeed("blah") }.isFailure().isInstanceOf(MalformedURLException::class.java)

        assertThat { readFeed("https://www.example.com") }.isFailure().isInstanceOf(FeedException::class.java)

        assertThat { readFeed("https://www.thauvin.net/foo") }.isFailure().isInstanceOf(IOException::class.java)

        assertThat { readFeed("https://www.examplesfoo.com/") }.isFailure()
            .isInstanceOf(UnknownHostException::class.java)
    }
}
