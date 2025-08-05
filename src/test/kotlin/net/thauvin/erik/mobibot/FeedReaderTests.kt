/*
 * FeedReaderTests.kt
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

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.*
import com.rometools.rome.io.FeedException
import net.thauvin.erik.mobibot.FeedReader.Companion.readFeed
import net.thauvin.erik.mobibot.msg.Message
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.io.IOException
import java.net.UnknownHostException
import kotlin.test.Test

class FeedReaderTests {
    @Nested
    @DisplayName("Failure Tests")
    inner class FailureTests {
        @Test
        fun invalidFeed() {
            assertFailure { readFeed("https://www.example.com") }.isInstanceOf(FeedException::class.java)
                .messageContains("Invalid XML")
        }

        @Test
        fun invalidHost() {
            assertFailure { readFeed("https://www.examplesfoo.com/") }
                .isInstanceOf(UnknownHostException::class.java)
                .message().isEqualTo("www.examplesfoo.com")
        }

        @Test
        fun invalidLocation() {
            assertFailure { readFeed("https://www.google.com/404") }.isInstanceOf(IOException::class.java)
                .message().isEqualTo("https://www.google.com/404")
        }

        @Test
        fun invalidUrl() {
            assertFailure { readFeed("blah") }.isInstanceOf(IllegalArgumentException::class.java)
                .message().isEqualTo("URI is not absolute")
        }
    }

    @Nested
    @DisplayName("Read Feed Tests")
    inner class ReadFeedTests {
        @Test
        fun readEmptyFeed() {
            val messages = readFeed("https://lorem-rss.herokuapp.com/feed?length=0")
            assertThat(messages, "messages").index(0).prop(Message::msg).contains("nothing")
        }

        @Test
        fun readFeed() {
            val messages = readFeed("https://feeds.thauvin.net/ethauvin")
            assertThat(messages, "messages").all {
                size().isEqualTo(10)
                index(1).prop(Message::msg).contains("erik.thauvin.net")
            }
        }

        @Test
        fun readThenValidateFeedContent() {
            val messages = readFeed("https://lorem-rss.herokuapp.com/feed?length=84", 42)
            assertThat(messages, "messages").size().isEqualTo(84)
            messages.forEachIndexed { i, m ->
                if (i % 2 == 0) {
                    assertThat(m, "messages($i)").prop(Message::msg).startsWith("Lorem ipsum")
                } else {
                    @Suppress("HttpUrlsUsage")
                    assertThat(m, "messages($i)").prop(Message::msg).contains("http://example.com/test/")
                }
            }
        }
    }
}
