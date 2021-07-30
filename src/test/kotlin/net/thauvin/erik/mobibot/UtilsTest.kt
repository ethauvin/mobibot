/*
 * UtilsTest.java
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

import net.thauvin.erik.mobibot.Utils.appendIfMissing
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.buildCmdSyntax
import net.thauvin.erik.mobibot.Utils.capitalise
import net.thauvin.erik.mobibot.Utils.colorize
import net.thauvin.erik.mobibot.Utils.cyan
import net.thauvin.erik.mobibot.Utils.encodeUrl
import net.thauvin.erik.mobibot.Utils.getIntProperty
import net.thauvin.erik.mobibot.Utils.green
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.obfuscate
import net.thauvin.erik.mobibot.Utils.plural
import net.thauvin.erik.mobibot.Utils.red
import net.thauvin.erik.mobibot.Utils.replaceEach
import net.thauvin.erik.mobibot.Utils.reverseColor
import net.thauvin.erik.mobibot.Utils.toIntOrDefault
import net.thauvin.erik.mobibot.Utils.toIsoLocalDate
import net.thauvin.erik.mobibot.Utils.toUtcDateTime
import net.thauvin.erik.mobibot.Utils.today
import net.thauvin.erik.mobibot.Utils.unescapeXml
import net.thauvin.erik.mobibot.Utils.uptime
import net.thauvin.erik.mobibot.Utils.urlReader
import org.assertj.core.api.Assertions.assertThat
import org.jibble.pircbot.Colors
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
        assertThat(dir.appendIfMissing(File.separatorChar)).describedAs("appendIfMissing(dir)")
            .isEqualTo(dir + File.separatorChar)
        assertThat(url.appendIfMissing(sep)).describedAs("appendIfMissing(url)").isEqualTo("$url$sep")
        assertThat("$url$sep".appendIfMissing(sep)).describedAs("appendIfMissing($url$sep)").isEqualTo("$url$sep")
    }

    @Test
    fun testBold() {
        assertThat(bold(1)).describedAs("bold(1)").isEqualTo(Colors.BOLD + "1" + Colors.BOLD)
        assertThat(bold(2L)).describedAs("bold(1)").isEqualTo(Colors.BOLD + "2" + Colors.BOLD)
        assertThat(bold(ascii)).describedAs("bold(ascii)").isEqualTo(Colors.BOLD + ascii + Colors.BOLD)
        assertThat(bold("test")).describedAs("bold(test)").isEqualTo(Colors.BOLD + "test" + Colors.BOLD)
    }

    @Test
    fun testBuildCmdSyntax() {
        val bot = "mobibot"
        assertThat(buildCmdSyntax("%c $test %n $test", bot, false)).describedAs("public")
            .isEqualTo("$bot: $test $bot $test")
        assertThat(buildCmdSyntax("%c %n $test %c $test %n", bot, true)).describedAs("public")
            .isEqualTo("/msg $bot $bot $test /msg $bot $test $bot")
    }


    @Test
    fun testCapitalise() {
        assertThat("test".capitalise()).describedAs("capitalize(test)").isEqualTo("Test")
        assertThat("Test".capitalise()).describedAs("capitalize(Test)").isEqualTo("Test")
        assertThat(test.capitalise()).describedAs("capitalize($test)").isEqualTo(test)
        assertThat("".capitalise()).describedAs("capitalize()").isEqualTo("")
    }

    @Test
    fun testColorize() {
        assertThat(colorize(ascii, Colors.REVERSE)).describedAs("colorize(reverse)").isEqualTo(
            Colors.REVERSE + ascii + Colors.REVERSE
        )
        assertThat(colorize(ascii, Colors.RED)).describedAs("colorize(red)")
            .isEqualTo(Colors.RED + ascii + Colors.NORMAL)
        assertThat(colorize(ascii, Colors.BOLD)).describedAs("colorized(bold)")
            .isEqualTo(Colors.BOLD + ascii + Colors.BOLD)
        assertThat(colorize(null, Colors.RED)).describedAs("colorize(null)").isEqualTo(Colors.NORMAL)
        assertThat(colorize("", Colors.RED)).describedAs("colorize()").isEqualTo(Colors.NORMAL)

    }

    @Test
    fun testCyan() {
        assertThat(cyan(ascii)).isEqualTo(Colors.CYAN + ascii + Colors.NORMAL)
    }

    @Test
    fun testEncodeUrl() {
        assertThat(encodeUrl("Hello Günter")).isEqualTo("Hello+G%C3%BCnter")
    }

    @Test
    fun testGetIntProperty() {
        val p = Properties()
        p["one"] = "1"
        p["two"] = "two"
        assertThat(p.getIntProperty("one", 9)).describedAs("getIntProperty(one)").isEqualTo(1)
        assertThat(p.getIntProperty("two", 2)).describedAs("getIntProperty(two)").isEqualTo(2)
        assertThat(p.getIntProperty("foo", 3)).describedAs("getIntProperty(foo)").isEqualTo(3)
    }

    @Test
    fun testGreen() {
        assertThat(green(ascii)).isEqualTo(Colors.DARK_GREEN + ascii + Colors.NORMAL)
    }

    @Test
    fun testHelpFormat() {
        assertThat(helpFormat(test, isBold = true, isIndent = false)).describedAs("bold")
            .isEqualTo("${Colors.BOLD}$test${Colors.BOLD}")
        assertThat(helpFormat(test, isBold = false, isIndent = true)).describedAs("indent")
            .isEqualTo(test.prependIndent())
        assertThat(helpFormat(test, isBold = true, isIndent = true)).describedAs("bold-indent")
            .isEqualTo(colorize(test, Colors.BOLD).prependIndent())

    }

    @Test
    fun testIsoLocalDate() {
        assertThat(cal.time.toIsoLocalDate()).describedAs("isoLocalDate(date)").isEqualTo("1952-02-17")
        assertThat(localDateTime.toIsoLocalDate()).describedAs("isoLocalDate(localDate)").isEqualTo("1952-02-17")
    }

    @Test
    fun testObfuscate() {
        assertThat(ascii.obfuscate().length).describedAs("obfuscate is right length").isEqualTo(ascii.length)
        assertThat(ascii.obfuscate()).describedAs("obfuscate()").isEqualTo("x".repeat(ascii.length))
        assertThat(" ".obfuscate()).describedAs("obfuscate(blank)").isEqualTo(" ")
    }

    @Test
    fun testPlural() {
        val week = "week"
        val weeks = "weeks"

        for (i in -1..3) {
            assertThat(week.plural(i.toLong())).describedAs("plural($i)").isEqualTo(if (i > 1) weeks else week)
        }
    }

    @Test
    fun testReplaceEach() {
        val search = arrayOf("one", "two", "three")
        val replace = arrayOf("1", "2", "3")
        assertThat(search.joinToString(",").replaceEach(search, replace)).describedAs("replaceEach(1,2,3")
            .isEqualTo(replace.joinToString(","))

        assertThat(test.replaceEach(search, replace)).describedAs("replaceEach(nothing)").isEqualTo(test)

        assertThat(test.replaceEach(arrayOf("t", "e"), arrayOf("", "E"))).describedAs("replaceEach($test)")
            .isEqualTo(test.replace("t", "").replace("e", "E"))

        assertThat(test.replaceEach(search, emptyArray())).describedAs("replaceEach(search, empty)")
            .isEqualTo(test)
    }

    @Test
    fun testRed() {
        assertThat(red(ascii)).isEqualTo(colorize(ascii, Colors.RED))
    }

    @Test
    fun testReverseColor() {
        assertThat(reverseColor(ascii)).isEqualTo(Colors.REVERSE + ascii + Colors.REVERSE)
    }

    @Test
    fun testToday() {
        assertThat(today()).isEqualTo(LocalDateTime.now().toIsoLocalDate())
    }

    @Test
    fun testToIntOrDefault() {
        assertThat("10".toIntOrDefault(1)).describedAs("toIntOrDefault(10, 1)").isEqualTo(10)
        assertThat("a".toIntOrDefault(2)).describedAs("toIntOrDefault(a, 2)").isEqualTo(2)
    }

    @Test
    fun testUnescapeXml() {
        assertThat(unescapeXml("&lt;a name=&quot;test &amp; &apos;&#39;&quot;&gt;")).isEqualTo(
            "<a name=\"test & ''\">"
        )
    }

    @Test
    fun testUptime() {
        assertThat(uptime(547800300076L)).describedAs("full")
            .isEqualTo("17 years 2 months 2 weeks 1 day 6 hours 45 minutes")
        assertThat(uptime(2700000L)).describedAs("minutes").isEqualTo("45 minutes")
        assertThat(uptime(24300000L)).describedAs("hours minutes").isEqualTo("6 hours 45 minutes")
        assertThat(uptime(110700000L)).describedAs("days hours minutes").isEqualTo("1 day 6 hours 45 minutes")
        assertThat(uptime(1320300000L)).describedAs("weeks days hours minutes")
            .isEqualTo("2 weeks 1 day 6 hours 45 minutes")
        assertThat(uptime(0L)).describedAs("0 minutes").isEqualTo("0 minute")
    }

    @Test
    @Throws(IOException::class)
    fun testUrlReader() {
        assertThat(urlReader(URL("https://postman-echo.com/status/200"))).describedAs("urlReader()")
            .isEqualTo("{\"status\":200}")
    }

    @Test
    fun testUtcDateTime() {
        assertThat(cal.time.toUtcDateTime()).describedAs("utcDateTime(date)").isEqualTo("1952-02-17 12:30")
        assertThat(localDateTime.toUtcDateTime()).describedAs("utcDateTime(localDate)").isEqualTo("1952-02-17 12:30")
    }
}
