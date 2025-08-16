/*
 * MastodonTests.kt
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
package net.thauvin.erik.mobibot.modules

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.rometools.rome.feed.synd.SyndCategory
import com.rometools.rome.feed.synd.SyndCategoryImpl
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.entries.EntryLink
import net.thauvin.erik.mobibot.modules.Mastodon.Companion.toot
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.pircbotx.hooks.types.GenericMessageEvent
import kotlin.test.Test

class MastodonTests : LocalProperties() {
    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun `API Key is not specified`() {
            val mastodon = Mastodon()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)
            val user = Mockito.mock(org.pircbotx.User::class.java)

            whenever(event.user).thenReturn(user)
            whenever(user.nick).thenReturn("mock")

            mastodon.commandResponse("channel", "toot", "This is a test.", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value).isEqualTo("The access token is missing.")
        }
    }

    @Nested
    @DisplayName("Toot Tests")
    inner class TootTests {
        @ParameterizedTest(name = "[{index}] ''{0}''")
        @NullAndEmptySource
        @Throws(ModuleException::class)
        fun `Empty Access Token should throw exception`(input: String?) {
            val msg = "Testing Mastodon API from ${getHostName()}"
            assertFailure {
                toot(
                    input,
                    getProperty(Mastodon.INSTANCE_PROP),
                    getProperty(Mastodon.HANDLE_PROP),
                    msg,
                    true
                )
            }.isInstanceOf(ModuleException::class.java).hasMessage("The access token is missing.")
        }

        @ParameterizedTest(name = "[{index}] ''{0}''")
        @NullAndEmptySource
        @Throws(ModuleException::class)
        fun `Empty Handle should throw exception`(input: String?) {
            val msg = "Testing Mastodon API from ${getHostName()}"
            assertFailure {
                toot(
                    getProperty(Mastodon.ACCESS_TOKEN_PROP),
                    getProperty(Mastodon.INSTANCE_PROP),
                    input,
                    msg,
                    true
                )
            }.isInstanceOf(ModuleException::class.java).hasMessage("The Mastodon handle is missing.")
        }

        @ParameterizedTest(name = "[{index}] ''{0}''")
        @NullAndEmptySource
        @Throws(ModuleException::class)
        fun `Empty Instance should throw exception`(input: String?) {
            val msg = "Testing Mastodon API from ${getHostName()}"
            assertFailure {
                toot(
                    getProperty(Mastodon.ACCESS_TOKEN_PROP),
                    input,
                    getProperty(Mastodon.HANDLE_PROP),
                    msg,
                    true
                )
            }.isInstanceOf(ModuleException::class.java).hasMessage("The Mastodon instance is missing.")
        }

        @Test
        @Throws(ModuleException::class)
        fun `Toot on Mastodon`() {
            val msg = "Testing Mastodon API from ${getHostName()}"
            assertThat(
                toot(
                    getProperty(Mastodon.ACCESS_TOKEN_PROP),
                    getProperty(Mastodon.INSTANCE_PROP),
                    getProperty(Mastodon.HANDLE_PROP),
                    msg,
                    true
                )
            ).contains(msg)
        }
    }

    @Nested
    @DisplayName("Format Entry Tests")
    inner class FormatEntryTests {
        @Test
        fun `formatEntry with tags should format correctly`() {
            val mastodon = Mastodon()
            val tags = mutableListOf<SyndCategory>(
                SyndCategoryImpl().apply { name = "testchannel" },
                SyndCategoryImpl().apply { name = "foo" },
                SyndCategoryImpl().apply { name = "bar" }
            )

            val entry = EntryLink(
                link = "https://example.com",
                title = "Test Title",
                nick = "testUser",
                channel = "#testChannel",
                tags = tags
            )
            val formattedEntry = mastodon.formatEntry(entry)
            assertThat(formattedEntry).isEqualTo(
                "Test Title (via testUser on #testChannel)\n\n#foo #bar\n\nhttps://example.com"
            )
        }

        @Test
        fun `formatEntry without tags should format correctly`() {
            val mastodon = Mastodon()
            val entry = EntryLink(
                link = "https://example.com",
                title = "Test Title",
                nick = "testUser",
                channel = "#testChannel",
                tags = mutableListOf()
            )
            val formattedEntry = mastodon.formatEntry(entry)
            assertThat(formattedEntry).isEqualTo(
                "Test Title (via testUser on #testChannel)\n\nhttps://example.com"
            )
        }
    }
}
