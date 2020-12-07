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
import net.thauvin.erik.mobibot.Utils.colorize
import net.thauvin.erik.mobibot.Utils.cyan
import net.thauvin.erik.mobibot.Utils.ensureDir
import net.thauvin.erik.mobibot.Utils.getIntProperty
import net.thauvin.erik.mobibot.Utils.green
import net.thauvin.erik.mobibot.Utils.isoLocalDate
import net.thauvin.erik.mobibot.Utils.obfuscate
import net.thauvin.erik.mobibot.Utils.plural
import net.thauvin.erik.mobibot.Utils.reverseColor
import net.thauvin.erik.mobibot.Utils.today
import net.thauvin.erik.mobibot.Utils.unescapeXml
import net.thauvin.erik.mobibot.Utils.uptime
import net.thauvin.erik.mobibot.Utils.urlReader
import net.thauvin.erik.mobibot.Utils.utcDateTime
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions
import org.jibble.pircbot.Colors
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File
import java.io.IOException
import java.net.URL
import java.time.LocalDateTime
import java.util.*

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
        Assertions.assertThat(bold(1.toString())).`as`("bold(1)").isEqualTo(Colors.BOLD + "1" + Colors.BOLD)
        Assertions.assertThat(bold(ascii)).`as`("bold(ascii").isEqualTo(Colors.BOLD + ascii + Colors.BOLD)
    }

    @Test
    fun testColorize() {
        Assertions.assertThat(colorize(ascii, Colors.REVERSE)).`as`("colorize(reverse)").isEqualTo(
            Colors.REVERSE + ascii + Colors.REVERSE
        )
        Assertions.assertThat(colorize(ascii, Colors.RED)).`as`("colorize(red)")
            .isEqualTo(Colors.RED + ascii + Colors.NORMAL)
        Assertions.assertThat(colorize(null, Colors.RED)).`as`("colorize(null)").isEqualTo(Colors.NORMAL)
    }

    @Test
    fun testCyan() {
        Assertions.assertThat(cyan(ascii)).isEqualTo(Colors.CYAN + ascii + Colors.NORMAL)
    }

    @Test
    fun testEnsureDir() {
        Assertions.assertThat(ensureDir("dir", false)).`as`("ensureDir(dir, false)")
            .isEqualTo("dir" + File.separatorChar)
        Assertions.assertThat(ensureDir("https://erik.thauvin.net", true)).`as`("ensureDir(erik.thauvin.net, true)")
            .isEqualTo("https://erik.thauvin.net/")
    }

    @Test
    fun testGetIntProperty() {
        Assertions.assertThat(getIntProperty("10", 1)).`as`("getIntProperty(10, 1)").isEqualTo(10)
        Assertions.assertThat(getIntProperty("a", 1)).`as`("getIntProperty(a, 1)").isEqualTo(1)
    }

    @Test
    fun testGreen() {
        Assertions.assertThat(green(ascii)).isEqualTo(Colors.DARK_GREEN + ascii + Colors.NORMAL)
    }

    @Test
    fun testIsoLocalDate() {
        Assertions.assertThat(isoLocalDate(cal.time)).`as`("isoLocalDate(date)").isEqualTo("1952-02-17")
        Assertions.assertThat(isoLocalDate(localDateTime)).`as`("isoLocalDate(localDate)").isEqualTo("1952-02-17")
    }

    @Test
    fun testObfuscate() {
        Assertions.assertThat(obfuscate(ascii).length).`as`("obfuscate is right length").isEqualTo(ascii.length)
        Assertions.assertThat(obfuscate(ascii)).`as`("obfuscate()").isEqualTo(StringUtils.repeat("x", ascii.length))
        Assertions.assertThat(obfuscate(" ")).`as`("obfuscate(blank)").isEqualTo(" ")
    }

    @Test
    fun testPlural() {
        val week = "week"
        val weeks = "weeks"
        Assertions.assertThat(plural(-1, week, weeks)).`as`("plural(-1)").isEqualTo(week)
        Assertions.assertThat(plural(0, week, weeks)).`as`("plural(0)").isEqualTo(week)
        Assertions.assertThat(plural(1, week, weeks)).`as`("plural(1)").isEqualTo(week)
        Assertions.assertThat(plural(2, week, weeks)).`as`("plural(2)").isEqualTo(weeks)
    }

    @Test
    fun testReverseColor() {
        Assertions.assertThat(reverseColor(ascii)).isEqualTo(Colors.REVERSE + ascii + Colors.REVERSE)
    }

    @Test
    fun testToday() {
        Assertions.assertThat(today()).isEqualTo(isoLocalDate(LocalDateTime.now()))
    }

    @Test
    fun testUnescapeXml() {
        Assertions.assertThat(unescapeXml("&lt;a name=&quot;test &amp; &apos;&#39;&quot;&gt;")).isEqualTo(
            "<a name=\"test & ''\">"
        )
    }

    @Test
    fun testUptime() {
        Assertions.assertThat("17 years 2 months 2 weeks 1 day 6 hours 45 minutes").isEqualTo(uptime(547800300076L))
    }

    @Test
    @Throws(IOException::class)
    fun testUrlReader() {
        Assertions.assertThat(urlReader(URL("https://postman-echo.com/status/200"))).`as`("urlReader()").isEqualTo(
            "{\"status\":200}"
        )
    }

    @Test
    fun testUtcDateTime() {
        Assertions.assertThat(utcDateTime(cal.time)).`as`("utcDateTime(date)").isEqualTo("1952-02-17 12:30")
        Assertions.assertThat(utcDateTime(localDateTime)).`as`("utcDateTime(localDate)").isEqualTo("1952-02-17 12:30")
    }
}
