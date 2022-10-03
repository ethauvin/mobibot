/*
 * UtilsTest.kt
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
package net.thauvin.erik.mobibot

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.length
import net.thauvin.erik.mobibot.Utils.appendIfMissing
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.capitalise
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
import org.pircbotx.Colors
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File
import java.io.IOException
import java.net.URL
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Properties

/**
 * The `Utils Test` class.
 */
class UtilsTest {
    private val ascii =
        " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
    private val cal = Calendar.getInstance()
    private val localDateTime = LocalDateTime.of(1952, 2, 17, 12, 30, 0)
    private val test = "This is a test."

    @BeforeClass
    fun setUp() {
        cal[1952, Calendar.FEBRUARY, 17, 12, 30] = 0
    }

    @Test
    fun testAppendIfMissing() {
        val dir = "dir"
        val sep = '/'
        val url = "https://erik.thauvin.net"
        assertThat(dir.appendIfMissing(File.separatorChar), "appendIfMissing(dir)")
            .isEqualTo(dir + File.separatorChar)
        assertThat(url.appendIfMissing(sep), "appendIfMissing(url)").isEqualTo("$url$sep")
        assertThat("$url$sep".appendIfMissing(sep), "appendIfMissing($url$sep)").isEqualTo("$url$sep")
    }

    @Test
    fun testBold() {
        assertThat(1.bold(), "bold(1)").isEqualTo(Colors.BOLD + "1" + Colors.BOLD)
        assertThat(2L.bold(), "bold(2L)").isEqualTo(Colors.BOLD + "2" + Colors.BOLD)
        assertThat(ascii.bold(), "ascii.bold()").isEqualTo(Colors.BOLD + ascii + Colors.BOLD)
        assertThat("test".bold(), "test.bold()").isEqualTo(Colors.BOLD + "test" + Colors.BOLD)
    }


    @Test
    fun testCapitalise() {
        assertThat("test".capitalise(), "capitalize(test)").isEqualTo("Test")
        assertThat("Test".capitalise(), "capitalize(Test)").isEqualTo("Test")
        assertThat(test.capitalise(), "capitalize($test)").isEqualTo(test)
        assertThat("".capitalise(), "capitalize()").isEqualTo("")
    }

    @Test
    fun textCapitaliseWords() {
        assertThat(test.capitalizeWords(), "captiatlizeWords(test)").isEqualTo("This Is A Test.")
        assertThat("Already Capitalized".capitalizeWords(), "already capitalized")
            .isEqualTo("Already Capitalized")
        assertThat("    a  test  ".capitalizeWords(), "with spaces").isEqualTo("    A  Test  ")
    }

    @Test
    fun testColorize() {
        assertThat(ascii.colorize(Colors.REVERSE), "reverse.colorize()").isEqualTo(
            Colors.REVERSE + ascii + Colors.REVERSE
        )
        assertThat(ascii.colorize(Colors.RED), "red.colorize()")
            .isEqualTo(Colors.RED + ascii + Colors.NORMAL)
        assertThat(ascii.colorize(Colors.BOLD), "colorized(bold)")
            .isEqualTo(Colors.BOLD + ascii + Colors.BOLD)
        assertThat(null.colorize(Colors.RED), "null.colorize()").isEqualTo("")
        assertThat("".colorize(Colors.RED), "colorize()").isEqualTo("")
        assertThat(ascii.colorize(DEFAULT_COLOR), "ascii.colorize()").isEqualTo(ascii)
        assertThat("   ".colorize(Colors.NORMAL), "blank.colorize()")
            .isEqualTo(Colors.NORMAL + "   " + Colors.NORMAL)
    }

    @Test
    fun testCyan() {
        assertThat(ascii.cyan()).isEqualTo(Colors.CYAN + ascii + Colors.NORMAL)
    }

    @Test
    fun testEncodeUrl() {
        assertThat("Hello Günter".encodeUrl()).isEqualTo("Hello%20G%C3%BCnter")
    }

    @Test
    fun testGetIntProperty() {
        val p = Properties()
        p["one"] = "1"
        p["two"] = "two"
        assertThat(p.getIntProperty("one", 9), "getIntProperty(one)").isEqualTo(1)
        assertThat(p.getIntProperty("two", 2), "getIntProperty(two)").isEqualTo(2)
        assertThat(p.getIntProperty("foo", 3), "getIntProperty(foo)").isEqualTo(3)
    }

    @Test
    fun testGreen() {
        assertThat(ascii.green()).isEqualTo(Colors.DARK_GREEN + ascii + Colors.NORMAL)
    }

    @Test
    fun testHelpCmdSyntax() {
        val bot = "mobibot"
        assertThat(helpCmdSyntax("%c $test %n $test", bot, false), "helpCmdSyntax(private)")
            .isEqualTo("$bot: $test $bot $test")
        assertThat(helpCmdSyntax("%c %n $test %c $test %n", bot, true), "helpCmdSyntax(public)")
            .isEqualTo("/msg $bot $bot $test /msg $bot $test $bot")
    }

    @Test
    fun testHelpFormat() {
        assertThat(helpFormat(test, isBold = true, isIndent = false), "helpFormat(bold)")
            .isEqualTo("${Colors.BOLD}$test${Colors.BOLD}")
        assertThat(helpFormat(test, isBold = false, isIndent = true), "helpFormat(indent)")
            .isEqualTo(test.prependIndent())
        assertThat(helpFormat(test, isBold = true, isIndent = true), "helpFormat(bold,indent)")
            .isEqualTo(test.colorize(Colors.BOLD).prependIndent())

    }

    @Test
    fun testIsoLocalDate() {
        assertThat(cal.time.toIsoLocalDate(), "isoLocalDate(date)").isEqualTo("1952-02-17")
        assertThat(localDateTime.toIsoLocalDate(), "isoLocalDate(localDate)").isEqualTo("1952-02-17")
    }

    @Test
    fun testLastOrEmpty() {
        val two = listOf("1", "2")
        assertThat(two.lastOrEmpty(), "lastOrEmpty(1,2)").isEqualTo("2")
        val one = listOf("1")
        assertThat(one.lastOrEmpty(), "lastOrEmpty(1)").isEqualTo("")
    }

    @Test
    fun testObfuscate() {
        assertThat(ascii.obfuscate(), "obfuscate()").all {
            length().isEqualTo(ascii.length)
            isEqualTo(("x".repeat(ascii.length)))
        }
        assertThat(" ".obfuscate(), "obfuscate(blank)").isEqualTo(" ")
    }

    @Test
    fun testPlural() {
        val week = "week"
        val weeks = "weeks"

        for (i in -1..3) {
            assertThat(week.plural(i.toLong()), "plural($i)").isEqualTo(if (i > 1) weeks else week)
        }
    }

    @Test
    fun testReplaceEach() {
        val search = arrayOf("one", "two", "three")
        val replace = arrayOf("1", "2", "3")
        assertThat(search.joinToString(",").replaceEach(search, replace), "replaceEach(1,2,3")
            .isEqualTo(replace.joinToString(","))

        assertThat(test.replaceEach(search, replace), "replaceEach(nothing)").isEqualTo(test)

        assertThat(test.replaceEach(arrayOf("t", "e"), arrayOf("", "E")), "replaceEach($test)")
            .isEqualTo(test.replace("t", "").replace("e", "E"))

        assertThat(test.replaceEach(search, emptyArray()), "replaceEach(search, empty)")
            .isEqualTo(test)
    }

    @Test
    fun testRed() {
        assertThat(ascii.red()).isEqualTo(ascii.colorize(Colors.RED))
    }

    @Test
    fun testReverseColor() {
        assertThat(ascii.reverseColor()).isEqualTo(Colors.REVERSE + ascii + Colors.REVERSE)
    }

    @Test
    fun testToday() {
        assertThat(today()).isEqualTo(LocalDateTime.now().toIsoLocalDate())
    }

    @Test
    fun testToIntOrDefault() {
        assertThat("10".toIntOrDefault(1), "toIntOrDefault(10, 1)").isEqualTo(10)
        assertThat("a".toIntOrDefault(2), "toIntOrDefault(a, 2)").isEqualTo(2)
    }

    @Test
    fun testUnderline() {
        assertThat(ascii.underline()).isEqualTo(ascii.colorize(Colors.UNDERLINE))
    }

    @Test
    fun testUnescapeXml() {
        assertThat("&lt;a name=&quot;test &amp; &apos;&#39;&quot;&gt;".unescapeXml()).isEqualTo(
            "<a name=\"test & ''\">"
        )
    }

    @Test
    @Throws(IOException::class)
    fun testUrlReader() {
        assertThat(URL("https://postman-echo.com/status/200").reader().body, "urlReader()")
            .isEqualTo("{\"status\":200}")
    }

    @Test
    fun testUtcDateTime() {
        assertThat(cal.time.toUtcDateTime(), "utcDateTime(date)").isEqualTo("1952-02-17 12:30")
        assertThat(localDateTime.toUtcDateTime(), "utcDateTime(localDate)").isEqualTo("1952-02-17 12:30")
    }
}
