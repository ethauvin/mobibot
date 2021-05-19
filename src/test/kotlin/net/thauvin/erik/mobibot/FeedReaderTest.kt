/*
 * FeedReaderTest.java
 *
 * Copyright (c) 2004-2019, Erik C. Thauvin (erik@thauvin.net)
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

import com.rometools.rome.io.FeedException
import net.thauvin.erik.mobibot.FeedReader.Companion.readFeed
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.testng.annotations.Test

import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.net.UnknownHostException

/**
 * The `FeedReader Test` class.
 */
class FeedReaderTest {
    @Test
    fun readFeedTest() {
        var messages = readFeed("https://feeds.thauvin.net/ethauvin")
        assertThat(messages.size).describedAs("messages = 10").isEqualTo(10)
        assertThat(messages[1].msg).describedAs("feed entry url").contains("ethauvin")

        messages = readFeed("https://lorem-rss.herokuapp.com/feed?length=0")
        assertThat(messages[0].msg).describedAs("nothing to view").contains("nothing")

        messages = readFeed("https://lorem-rss.herokuapp.com/feed?length=42", 42)
        assertThat(messages.size).describedAs("messages = 84").isEqualTo(84)
        assertThat(messages.last().msg).describedAs("example entry url").contains("http://example.com/test/")

        assertThatThrownBy { readFeed("blah") }.describedAs("invalid URL")
            .isInstanceOf(MalformedURLException::class.java)

        assertThatThrownBy { readFeed("https://www.example.com") }.describedAs("not a feed")
            .isInstanceOf(FeedException::class.java)

        assertThatThrownBy { readFeed("https://www.examples.com/foo") }.describedAs("404 not found")
            .isInstanceOf(FileNotFoundException::class.java)

        assertThatThrownBy { readFeed("https://www.doesnotexists.com") }.describedAs("unknown host")
            .isInstanceOf(UnknownHostException::class.java)
    }
}
