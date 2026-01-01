/*
 * LinksManagerTests.kt
 *
 * Copyright (c) 2004-2026 Erik C. Thauvin (erik@thauvin.net)
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
import assertk.assertions.*
import net.thauvin.erik.mobibot.Constants
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class LinksManagerTests {
    private val linksManager = LinksManager()

    @Nested
    @DisplayName("Fetch Page Title Tests")
    inner class FetchPageTitleTests {
        @Test
        fun fetchPageTitle() {
            assertThat(linksManager.fetchPageTitle("https://erik.thauvin.net/")).contains("Erik's Weblog")
        }

        @Test
        fun fetchPageNoTitle() {
            assertThat(linksManager.fetchPageTitle("https://www.google.com/foo")).isEqualTo(Constants.NO_TITLE)
        }
    }

    @Nested
    @DisplayName("Match Tests")
    inner class MatchTests {
        @Nested
        @DisplayName("Link Tests")
        inner class LinkTests {
            @Test
            fun matchBlankString() {
                assertThat(linksManager.matches("   ")).isFalse()
            }

            @Test
            fun matchEmptyString() {
                assertThat(linksManager.matches("")).isFalse()
            }

            @Test
            @Suppress("HttpUrlsUsage")
            fun matchInsecureLink() {
                assertThat(linksManager.matches("http://erik.thauvin.net/blog/ Erik's Weblog")).isTrue()
            }

            @Test
            fun matchInvalidProtocol() {
                assertThat(linksManager.matches("ftp://erik.thauvin.net/blog/")).isFalse()
            }

            @Test
            fun matchLink() {
                assertThat(linksManager.matches("https://erik.thauvin.net/blog/")).isTrue()
            }

            @Test
            fun matchLinkWithAnchor() {
                assertThat(linksManager.matches("https://erik.thauvin.net/blog/search?tag=java#foo")).isTrue()
            }

            @Test
            fun matchLinkWithParams() {
                assertThat(linksManager.matches("https://erik.thauvin.net/blog/search?tag=bld&cat=java")).isTrue()
            }

            @Test
            fun matchLinkWithSingleParam() {
                assertThat(linksManager.matches("https://erik.thauvin.net/blog/search?tag=java")).isTrue()
            }

            @Test
            fun matchLinkWithTitle() {
                assertThat(linksManager.matches("https://erik.thauvin.net/blog/ Erik's Weblog")).isTrue()
            }

            @Test
            fun matchLinkWithWhitespace() {
                assertThat(linksManager.matches("https://erik.thauvin.net/blog/   ")).isTrue()
            }

            @Test
            fun matchMalformedURL() {
                assertThat(linksManager.matches("https:/example.com")).isFalse()
            }

            @Test
            fun matchMixedCaseLink() {
                assertThat(linksManager.matches("https://Erik.Thauvin.Net/blog/")).isTrue()
            }

            @Test
            fun matchNonURLText() {
                assertThat(linksManager.matches("This is just a text string")).isFalse()
            }

            @Test
            fun matchNumericURL() {
                assertThat(linksManager.matches("https://123.456.789.0/")).isTrue()
            }

            @Test
            fun matchSpecialCharacterLink() {
                assertThat(linksManager.matches("https://erik.thauvin.net/blog/search?tag=java&name=%20foo")).isTrue()
            }

            @Test
            fun matchUnsupportedProtocol() {
                assertThat(linksManager.matches("mailto:example@example.com")).isFalse()
            }

            @Test
            fun matchUpperCaseLink() {
                assertThat(linksManager.matches("HTTPS://ERIK.THAUVIN.NET/BLOG/")).isTrue()
            }

            @Test
            fun matchURLWithLeadingSpaces() {
                assertThat(linksManager.matches("   https://example.com")).isFalse()
            }
        }

        @Nested
        @DisplayName("Tags Parsing Tests")
        inner class TagsParsingTests {
            @Test
            fun matchTagCaseInsensitive() {
                linksManager.setProperty(LinksManager.KEYWORDS_PROP, "Java kotlin")
                val tags = mutableListOf<String>()
                linksManager.matchTagKeywords("This is a JAVA and kotlin tutorial", tags)
                assertThat(tags, "tags").containsExactlyInAnyOrder("Java", "kotlin")
            }

            @Test
            fun matchTagKeywords() {
                val tags = mutableListOf("key1", "key3")
                linksManager.matchTagKeywords("Test key3 title with key1", tags)
                assertThat(tags, "tags(key1, key3)").all {
                    containsExactlyInAnyOrder("key1", "key3")
                    size().isEqualTo(2)
                }
            }

            @Test
            fun matchTagMultipleInTitle() {
                linksManager.setProperty(LinksManager.KEYWORDS_PROP, "java, spring, kotlin")
                val tags = mutableListOf<String>()
                linksManager.matchTagKeywords("Learning kotlin and spring for Java developers", tags)
                assertThat(tags, "tags").containsExactlyInAnyOrder("java", "spring", "kotlin")
            }

            @Test
            fun matchTagNoKeywords() {
                linksManager.setProperty(LinksManager.KEYWORDS_PROP, "python, ruby")
                val tags = mutableListOf<String>()
                linksManager.matchTagKeywords("This title has no matching keywords", tags)
                assertThat(tags, "tags").isEmpty()
            }

            @Test
            fun matchTagSingleKeyword() {
                linksManager.setProperty(LinksManager.KEYWORDS_PROP, "key1 key2,key3")
                val tags = mutableListOf<String>()
                linksManager.matchTagKeywords("Test title with key2", tags)
                assertThat(tags, "tags").containsExactly("key2")
            }
        }
    }
}
