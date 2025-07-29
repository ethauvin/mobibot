/*
 * UtilsTests.kt
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
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.length
import net.thauvin.erik.mobibot.Utils.appendIfMissing
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.capitalize
import net.thauvin.erik.mobibot.Utils.capitalizeWords
import net.thauvin.erik.mobibot.Utils.colorize
import net.thauvin.erik.mobibot.Utils.cyan
import net.thauvin.erik.mobibot.Utils.encodeUrl
import net.thauvin.erik.mobibot.Utils.getIntProperty
import net.thauvin.erik.mobibot.Utils.green
import net.thauvin.erik.mobibot.Utils.helpCmdSyntax
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.lastOrEmpty
import net.thauvin.erik.mobibot.Utils.obfuscate
import net.thauvin.erik.mobibot.Utils.plural
import net.thauvin.erik.mobibot.Utils.reader
import net.thauvin.erik.mobibot.Utils.red
import net.thauvin.erik.mobibot.Utils.replaceEach
import net.thauvin.erik.mobibot.Utils.reverseColor
import net.thauvin.erik.mobibot.Utils.toIntOrDefault
import net.thauvin.erik.mobibot.Utils.toIsoLocalDate
import net.thauvin.erik.mobibot.Utils.toUtcDateTime
import net.thauvin.erik.mobibot.Utils.today
import net.thauvin.erik.mobibot.Utils.underline
import net.thauvin.erik.mobibot.Utils.unescapeXml
import net.thauvin.erik.mobibot.msg.Message.Companion.DEFAULT_COLOR
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.pircbotx.Colors
import java.io.File
import java.io.IOException
import java.net.URI
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test

class UtilsTests {
    private val ascii =
        " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
    private val p = Properties().apply {
        setProperty("one", "1")
        setProperty("two", "two")
    }
    private val test = "This is a test."

    @Nested
    @DisplayName("Date Tests")
    inner class DateTests {
        private val cal = Calendar.getInstance()
        private val localDateTime = LocalDateTime.of(1952, 2, 17, 12, 30, 0)

        @BeforeEach
        fun beforeEach() {
            cal[1952, Calendar.FEBRUARY, 17, 12, 30] = 0
        }

        @Test
        fun `Convert a Date to an ISO date`() {
            assertThat(cal.time.toIsoLocalDate(), "isoLocalDate(date)").isEqualTo("1952-02-17")
        }

        @Test
        fun `Convert a LocalDate to an ISO date`() {
            assertThat(localDateTime.toIsoLocalDate(), "isoLocalDate(localDate)").isEqualTo("1952-02-17")
        }

        @Test
        fun `Convert a Date to a UTC date-time`() {
            assertThat(cal.time.toUtcDateTime(), "utcDateTime(date)").isEqualTo("1952-02-17 12:30")
        }

        @Test
        fun `Convert a LocalDate to a UTC date-time`() {
            assertThat(localDateTime.toUtcDateTime(), "utcDateTime(localDate)").isEqualTo("1952-02-17 12:30")
        }

        @Test
        fun `Today should return the current date in ISO format`() {
            assertThat(today()).isEqualTo(LocalDateTime.now().toIsoLocalDate())
        }
    }

    @Nested
    @DisplayName("Help Tests")
    inner class HelpTests {
        private val bot = "mobibot"

        @Test
        fun `Construct help string for public message`() {
            assertThat(helpCmdSyntax("%c %n $test %c $test %n", bot, true), "helpCmdSyntax(public)")
                .isEqualTo("/msg $bot $bot $test /msg $bot $test $bot")
        }

        @Test
        fun `Construct help string for private message`() {
            assertThat(helpCmdSyntax("%c $test %n $test", bot, false), "helpCmdSyntax(private)")
                .isEqualTo("$bot: $test $bot $test")
        }

        @Test
        fun `Format help string with bold`() {
            assertThat(helpFormat(test, isBold = true, isIndent = false), "helpFormat(bold)")
                .isEqualTo("${Colors.BOLD}$test${Colors.BOLD}")
        }

        @Test
        fun `Format help string with indent`() {
            assertThat(helpFormat(test, isBold = false, isIndent = true), "helpFormat(indent)")
                .isEqualTo(test.prependIndent())
        }

        @Test
        fun `Format help string with bold and indent`() {
            assertThat(helpFormat(test, isBold = true, isIndent = true), "helpFormat(bold,indent)")
                .isEqualTo(test.colorize(Colors.BOLD).prependIndent())

        }
    }


    @Nested
    @DisplayName("Properties Tests")
    inner class PropertiesTests {
        @Test
        fun `Convert properties to int`() {
            assertThat(p.getIntProperty("one", 9), "getIntProperty(one)").isEqualTo(1)
            assertThat(p.getIntProperty("two", 2), "getIntProperty(two)").isEqualTo(2)
        }

        @Test
        fun `Convert property to int using default value`() {
            assertThat(p.getIntProperty("foo", 3), "getIntProperty(foo)").isEqualTo(3)
        }
    }


    @Nested
    @DisplayName("List Tests")
    inner class ListTests {
        @Test
        fun `Get last item of list`() {
            val two = listOf("1", "2")
            assertThat(two.lastOrEmpty(), "lastOrEmpty(1,2)").isEqualTo("2")
        }

        @Test
        fun `Return empty if list only has one item`() {
            val one = listOf("1")
            assertThat(one.lastOrEmpty(), "lastOrEmpty(1)").isEqualTo("")
        }
    }

    @Nested
    @DisplayName("String Manipulation Tests")
    inner class StringManipulationTests {
        private val dir = "dir"
        private val sep = '/'
        private val url = "https://erik.thauvin.net"

        @Nested
        @DisplayName("Appending Tests")
        inner class AppendingTests {
            @Test
            fun `Append separator char if missing`() {
                assertThat(dir.appendIfMissing(File.separatorChar), "appendIfMissing(dir)")
                    .isEqualTo(dir + File.separatorChar)
            }

            @Test
            fun `Append separator char if already present`() {
                assertThat(url.appendIfMissing(sep), "appendIfMissing(url)").isEqualTo("$url$sep")
            }

            @Test
            fun `Append separator char if not present`() {
                assertThat("$url$sep".appendIfMissing(sep), "appendIfMissing($url$sep)").isEqualTo("$url$sep")
            }
        }

        @Nested
        @DisplayName("Capitalization Tests")
        inner class CapitalizationTests {
            @Test
            fun `Capitalize string`() {
                assertThat("test".capitalize(), "capitalize(test)").isEqualTo("Test")
            }

            @Test
            fun `Capitalize string already capitalized`() {
                assertThat("Test".capitalize(), "capitalize(Test)").isEqualTo("Test")
            }

            @Test
            fun `Capitalize string with spaces`() {
                assertThat(test.capitalize(), "capitalize($test)").isEqualTo(test)
            }

            @Test
            fun `Capitalize empty string`() {
                assertThat("".capitalize(), "capitalize()").isEqualTo("")
            }

            @Test
            fun `Capitalize words`() {
                assertThat(test.capitalizeWords(), "capitalizeWords(test)").isEqualTo("This Is A Test.")
            }

            @Test
            fun `Capitalize words already capitalized`() {
                assertThat("Already Capitalized".capitalizeWords(), "already capitalized")
                    .isEqualTo("Already Capitalized")
            }

            @Test
            fun `Capitalize words with leading and ending spaces`() {
                assertThat("    a  test  ".capitalizeWords(), "with spaces").isEqualTo("    A  Test  ")
            }
        }

        @Nested
        @DisplayName("Conversion Tests")
        inner class ConversionTests {
            @Test
            fun `Convert string to int`() {
                assertThat("10".toIntOrDefault(1), "toIntOrDefault(10, 1)").isEqualTo(10)
            }

            @Test
            fun `Convert string to int using default value`() {
                assertThat("a".toIntOrDefault(2), "toIntOrDefault(a, 2)").isEqualTo(2)
            }
        }

        @Test
        fun `Encode URL`() {
            assertThat("Hello Günter".encodeUrl()).isEqualTo("Hello%20G%C3%BCnter")
        }

        @Nested
        @DisplayName("Obfuscation Tests")
        inner class ObfuscationTests {
            @Test
            fun `Obfuscate string`() {
                assertThat(ascii.obfuscate(), "obfuscate()").all {
                    length().isEqualTo(ascii.length)
                    isEqualTo(("x".repeat(ascii.length)))
                }
            }

            @Test
            fun `Obfuscate empty string`() {
                assertThat(" ".obfuscate(), "obfuscate(blank)").isEqualTo(" ")
            }
        }

        @Test
        fun `Pluralize string`() {
            val week = "week"
            val weeks = "weeks"

            for (i in -1..3) {
                assertThat(week.plural(i.toLong()), "plural($i)").isEqualTo(if (i > 1) weeks else week)
            }
        }

        @Nested
        @DisplayName("Replace Tests")
        inner class ReplaceTests {

            private val replace = arrayOf("1", "2", "3")

            private val search = arrayOf("one", "two", "three")

            @Test
            fun `Replace occurrences in string`() {
                assertThat(search.joinToString(",").replaceEach(search, replace), "replaceEach(1,2,3")
                    .isEqualTo(replace.joinToString(","))
            }

            @Test
            fun `Replace occurrences not found in string`() {
                assertThat(test.replaceEach(search, replace), "replaceEach(nothing)").isEqualTo(test)
            }

            @Test
            fun `Replace and remove occurrences in string`() {
                assertThat(test.replaceEach(arrayOf("t", "e"), arrayOf("", "E")), "replaceEach($test)")
                    .isEqualTo(test.replace("t", "").replace("e", "E"))
            }

            @Test
            fun `Replace empty occurrences in string`() {
                assertThat(test.replaceEach(search, emptyArray()), "replaceEach(search, empty)")
                    .isEqualTo(test)
            }
        }

        @Test
        fun `Unescape XML`() {
            assertThat("&lt;a name=&quot;test &amp; &apos;&#39;&quot;&gt;".unescapeXml()).isEqualTo(
                "<a name=\"test & ''\">"
            )
        }
    }

    @Nested
    @DisplayName("Text Styling Tests")
    inner class TextStylingTests {
        @Nested
        @DisplayName("Colorize Tests")
        inner class ColorizeTests {
            @Test
            fun `Colorize ASCII characters red`() {
                assertThat(ascii.colorize(Colors.RED), "red.colorize()")
                    .isEqualTo(Colors.RED + ascii + Colors.NORMAL)
            }

            @Test
            fun `Colorize blank string`() {
                assertThat("   ".colorize(Colors.NORMAL), "blank.colorize()")
                    .isEqualTo(Colors.NORMAL + "   " + Colors.NORMAL)
            }

            @Test
            fun `Colorize default color`() {
                assertThat(ascii.colorize(DEFAULT_COLOR), "ascii.colorize()").isEqualTo(ascii)
            }

            @Test
            fun `Colorize empty string`() {
                assertThat("".colorize(Colors.RED), "colorize()").isEqualTo("")
            }

            @Test
            fun `Colorize null`() {
                assertThat(null.colorize(Colors.RED), "null.colorize()").isEqualTo("")
            }
        }

        @Nested
        @DisplayName("Color Formatting Tests")
        inner class ColorFormattingTests {
            @Test
            fun `Make ASCII characters bold`() {
                assertThat(ascii.bold(), "ascii.bold()").isEqualTo(Colors.BOLD + ascii + Colors.BOLD)
            }

            @Test
            fun `Make int bold`() {
                assertThat(1.bold(), "bold(1)").isEqualTo(Colors.BOLD + "1" + Colors.BOLD)
            }

            @Test
            fun `Make long bold`() {
                assertThat(2L.bold(), "bold(2L)").isEqualTo(Colors.BOLD + "2" + Colors.BOLD)
            }

            @Test
            fun `Make string bold`() {
                assertThat("test".bold(), "test.bold()").isEqualTo(Colors.BOLD + "test" + Colors.BOLD)
            }

            @Test
            fun `Make text cyan`() {
                assertThat(ascii.cyan()).isEqualTo(Colors.CYAN + ascii + Colors.NORMAL)
            }

            @Test
            fun `Make text green`() {
                assertThat(ascii.green()).isEqualTo(Colors.DARK_GREEN + ascii + Colors.NORMAL)
            }

            @Test
            fun `Make text red`() {
                assertThat(ascii.red()).isEqualTo(ascii.colorize(Colors.RED))
            }
        }

        @Test
        fun `Reversed text`() {
            assertThat(ascii.reverseColor()).isEqualTo(Colors.REVERSE + ascii + Colors.REVERSE)
        }

        @Test
        fun `Underline text`() {
            assertThat(ascii.underline()).isEqualTo(ascii.colorize(Colors.UNDERLINE))
        }
    }

    @Nested
    @DisplayName("URI Reader Tests")
    inner class URIReaderTests {
        @Test
        @Throws(IOException::class)
        fun `URI reader`() {
            val reader = URI.create("https://postman-echo.com/status/200").reader()
            assertThat(reader.body).isEqualTo("{\n  \"status\": 200\n}")
            assertThat(reader.responseCode).isEqualTo(200)
        }

        @Test
        @Throws(IOException::class)
        fun `URI reader page not found`() {
            val reader = URI.create("https://www.google.com/404").reader()
            assertThat(reader.body.isEmpty()).isEqualTo(false)
            assertThat(reader.responseCode).isEqualTo(404)
        }

        @Test
        @Throws(IOException::class)
        fun `URI reader with empty body`() {
            val reader = URI.create("https://httpbin.org/status/200").reader()
            assertThat(reader.body.isEmpty()).isEqualTo(true)
            assertThat(reader.responseCode).isEqualTo(200)
        }
    }
}
