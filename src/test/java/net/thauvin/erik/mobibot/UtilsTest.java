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

package net.thauvin.erik.mobibot;

import org.apache.commons.lang3.StringUtils;
import org.jibble.pircbot.Colors;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The <code>Utils Test</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2017-05-30
 * @since 1.0
 */
public class UtilsTest {
    private static final String ASCII =
        " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

    private final Calendar cal = Calendar.getInstance();
    private final LocalDateTime localDateTime = LocalDateTime.of(1952, 2, 17, 12, 30, 0);

    @BeforeClass
    public void setUp() {
        cal.set(1952, Calendar.FEBRUARY, 17, 12, 30, 0);
    }

    @Test
    public void testBold() {
        assertThat(Utils.bold(1)).as("bold(1)").isEqualTo(Colors.BOLD + "1" + Colors.BOLD);
        assertThat(Utils.bold(ASCII)).as("bold(ascii").isEqualTo(Colors.BOLD + ASCII + Colors.BOLD);
    }

    @Test
    public void testColorize() {
        assertThat(Utils.colorize(ASCII, Colors.REVERSE)).as("colorize(reverse)").isEqualTo(
            Colors.REVERSE + ASCII + Colors.REVERSE);
        assertThat(Utils.colorize(ASCII, Colors.RED)).as("colorize(red)").isEqualTo(Colors.RED + ASCII + Colors.NORMAL);
        assertThat(Utils.colorize(null, Colors.RED)).as("colorize(null)").isEqualTo(Colors.NORMAL);
    }

    @Test
    public void testCyan() {
        assertThat(Utils.cyan(ASCII)).isEqualTo(Colors.CYAN + ASCII + Colors.NORMAL);
    }

    @Test
    public void testEnsureDir() {
        assertThat(Utils.ensureDir("dir", false)).as("ensureDir(dir, false)").isEqualTo("dir" + File.separatorChar);
        assertThat(Utils.ensureDir("https://erik.thauvin.net", true)).as("ensureDir(erik.thauvin.net, true)").isEqualTo(
            "https://erik.thauvin.net/");
    }

    @Test
    public void testGetIntProperty() {
        assertThat(Utils.getIntProperty("10", 1)).as("getIntProperty(10, 1)").isEqualTo(10);
        assertThat(Utils.getIntProperty("a", 1)).as("getIntProperty(a, 1)").isEqualTo(1);
    }

    @Test
    public void testGreen() {
        assertThat(Utils.green(ASCII)).isEqualTo(Colors.DARK_GREEN + ASCII + Colors.NORMAL);
    }

    @Test
    public void testIsoLocalDate() {
        assertThat(Utils.isoLocalDate(cal.getTime())).as("isoLocalDate(date)").isEqualTo("1952-02-17");
        assertThat(Utils.isoLocalDate(localDateTime)).as("isoLocalDate(localDate)").isEqualTo("1952-02-17");
    }

    @Test
    public void testObfuscate() {
        assertThat(Utils.obfuscate(ASCII).length()).as("obfuscate is right length").isEqualTo(ASCII.length());
        assertThat(Utils.obfuscate(ASCII)).as("obfuscate()").isEqualTo(StringUtils.repeat("x", ASCII.length()));
        assertThat(Utils.obfuscate(" ")).as("obfuscate(blank)").isEqualTo(" ");
    }

    @Test
    public void testPlural() {
        final String week = "week";
        final String weeks = "weeks";

        assertThat(Utils.plural(-1, week, weeks)).as("plural(-1)").isEqualTo(week);
        assertThat(Utils.plural(0, week, weeks)).as("plural(0)").isEqualTo(week);
        assertThat(Utils.plural(1, week, weeks)).as("plural(1)").isEqualTo(week);
        assertThat(Utils.plural(2, week, weeks)).as("plural(2)").isEqualTo(weeks);
    }

    @Test
    public void testReverseColor() {
        assertThat(Utils.reverseColor(ASCII)).isEqualTo(Colors.REVERSE + ASCII + Colors.REVERSE);
    }

    @Test
    public void testToday() {
        assertThat(Utils.today()).isEqualTo(Utils.isoLocalDate(LocalDateTime.now()));
    }

    @Test
    public void testUnescapeXml() {
        assertThat(Utils.unescapeXml("&lt;a name=&quot;test &amp; &apos;&#39;&quot;&gt;")).isEqualTo(
            "<a name=\"test & ''\">");
    }

    @Test
    public void testUptime() {
        assertThat("17 years 2 months 2 weeks 1 day 6 hours 45 minutes").isEqualTo(Utils.uptime(547800300076L));
    }

    @Test
    public void testUtcDateTime() {
        assertThat(Utils.utcDateTime(cal.getTime())).as("utcDateTime(date)").isEqualTo("1952-02-17 12:30");
        assertThat(Utils.utcDateTime(localDateTime)).as("utcDateTime(localDate)").isEqualTo("1952-02-17 12:30");
    }
}
