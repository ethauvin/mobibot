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

import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.capitalize
import net.thauvin.erik.mobibot.Utils.colorize
import net.thauvin.erik.mobibot.Utils.cyan
import net.thauvin.erik.mobibot.Utils.getIntProperty
import net.thauvin.erik.mobibot.Utils.green
import net.thauvin.erik.mobibot.Utils.obfuscate
import net.thauvin.erik.mobibot.Utils.plural
import net.thauvin.erik.mobibot.Utils.reverseColor
import net.thauvin.erik.mobibot.Utils.toDir
import net.thauvin.erik.mobibot.Utils.toIsoLocalDate
import net.thauvin.erik.mobibot.Utils.toUtcDateTime
import net.thauvin.erik.mobibot.Utils.today
import net.thauvin.erik.mobibot.Utils.unescapeXml
import net.thauvin.erik.mobibot.Utils.uptime
import net.thauvin.erik.mobibot.Utils.urlReader
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.jibble.pircbot.Colors
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File
import java.io.IOException
import java.net.URL
import java.time.LocalDateTime
import java.util.Calendar

/**
 * The `Utils Test` class.
 */
class UtilsTest {
    private val ascii =
        " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
    private val cal = Calendar.getInstance()
    private val localDateTime = LocalDateTime.of(1952, 2, 17, 12, 30, 0)

    @BeforeClass
    fun setUp() {
        cal[1952, Calendar.FEBRUARY, 17, 12, 30] = 0
    }

    @Test
    fun testBold() {
        assertThat(bold(1)).describedAs("bold(1)").isEqualTo(Colors.BOLD + "1" + Colors.BOLD)
        assertThat(bold(ascii)).describedAs("bold(ascii)").isEqualTo(Colors.BOLD + ascii + Colors.BOLD)
    }

    @Test
    fun testCapitalize() {
        assertThat(capitalize("test")).describedAs("capitalize(test)").isEqualTo("Test")
        assertThat(capitalize("Test")).describedAs("capitalize(Test)").isEqualTo("Test")
        assertThat(capitalize(null)).describedAs("captitalize(null)").isNull()
        assertThat(capitalize("")).describedAs("capitalize()").isEqualTo("")
    }

    @Test
    fun testColorize() {
        assertThat(colorize(ascii, Colors.REVERSE)).describedAs("colorize(reverse)").isEqualTo(
            Colors.REVERSE + ascii + Colors.REVERSE
        )
        assertThat(colorize(ascii, Colors.RED)).describedAs("colorize(red)")
            .isEqualTo(Colors.RED + ascii + Colors.NORMAL)
        assertThat(colorize(null, Colors.RED)).describedAs("colorize(null)").isEqualTo(Colors.NORMAL)
    }

    @Test
    fun testCyan() {
        assertThat(cyan(ascii)).isEqualTo(Colors.CYAN + ascii + Colors.NORMAL)
    }

    @Test
    fun testGetIntProperty() {
        assertThat(getIntProperty("10", 1)).describedAs("getIntProperty(10, 1)").isEqualTo(10)
        assertThat(getIntProperty("a", 1)).describedAs("getIntProperty(a, 1)").isEqualTo(1)
    }

    @Test
    fun testGreen() {
        assertThat(green(ascii)).isEqualTo(Colors.DARK_GREEN + ascii + Colors.NORMAL)
    }

    @Test
    fun testIsoLocalDate() {
        assertThat(cal.time.toIsoLocalDate()).describedAs("isoLocalDate(date)").isEqualTo("1952-02-17")
        assertThat(localDateTime.toIsoLocalDate()).describedAs("isoLocalDate(localDate)").isEqualTo("1952-02-17")
    }

    @Test
    fun testObfuscate() {
        assertThat(ascii.obfuscate().length).describedAs("obfuscate is right length").isEqualTo(ascii.length)
        assertThat(ascii.obfuscate()).describedAs("obfuscate()").isEqualTo(StringUtils.repeat("x", ascii.length))
        assertThat(" ".obfuscate()).describedAs("obfuscate(blank)").isEqualTo(" ")
    }

    @Test
    fun testPlural() {
        val week = "week"
        val weeks = "weeks"
        assertThat(week.plural(-1, weeks)).describedAs("plural(-1)").isEqualTo(week)
        assertThat(week.plural(0, weeks)).describedAs("plural(0)").isEqualTo(week)
        assertThat(week.plural(1, weeks)).describedAs("plural(1)").isEqualTo(week)
        assertThat(week.plural(2, weeks)).describedAs("plural(2)").isEqualTo(weeks)
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
    fun testToDir() {
        assertThat("dir".toDir(false)).describedAs("toDir(dir, false)").isEqualTo("dir" + File.separatorChar)
        assertThat("https://erik.thauvin.net".toDir(true)).describedAs("toDir(erik.thauvin.net, true)")
            .isEqualTo("https://erik.thauvin.net/")
    }

    @Test
    fun testUnescapeXml() {
        assertThat(unescapeXml("&lt;a name=&quot;test &amp; &apos;&#39;&quot;&gt;")).isEqualTo(
            "<a name=\"test & ''\">"
        )
    }

    @Test
    fun testUptime() {
        assertThat("17 years 2 months 2 weeks 1 day 6 hours 45 minutes").isEqualTo(uptime(547800300076L))
    }

    @Test
    @Throws(IOException::class)
    fun testUrlReader() {
        assertThat(urlReader(URL("https://postman-echo.com/status/200"))).describedAs("urlReader()")
            .isEqualTo("{\"status\":200}"
        )
    }

    @Test
    fun testUtcDateTime() {
        assertThat(cal.time.toUtcDateTime()).describedAs("utcDateTime(date)").isEqualTo("1952-02-17 12:30")
        assertThat(localDateTime.toUtcDateTime()).describedAs("utcDateTime(localDate)").isEqualTo("1952-02-17 12:30")
    }
}
