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
    static final String ASCII =
        " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

    final Calendar cal = Calendar.getInstance();
    final LocalDateTime localDateTime =
        LocalDateTime.of(1952, 2, 17, 12, 30, 0);

    @BeforeClass
    public void setUp() {
        cal.set(1952, Calendar.FEBRUARY, 17, 12, 30, 0);
    }

    @Test
    public void testBold() throws Exception {
        assertThat(Utils.bold(1)).as("bold(1)").isEqualTo(Colors.BOLD + "1" + Colors.BOLD);
        assertThat(Utils.bold(ASCII)).as("bold(ascii").isEqualTo(Colors.BOLD + ASCII + Colors.BOLD);
    }

    @Test
    public void testCapitalize() throws Exception {
        assertThat(Utils.capitalize("this is a test.")).isEqualTo("This is a test.");
    }

    @Test
    public void testEnsureDir() throws Exception {
        assertThat(Utils.ensureDir("dir", false)).as("ensureDir(dir, false)")
            .isEqualTo("dir" + File.separatorChar);
        assertThat(Utils.ensureDir("https://erik.thauvin.net", true))
            .as("ensureDir(erik.thauvin.net, true)").isEqualTo("https://erik.thauvin.net/");
    }

    @Test
    public void testGetIntProperty() throws Exception {
        assertThat(Utils.getIntProperty("10", 1)).as("getIntProperty(10, 1)").isEqualTo(10);
        assertThat(Utils.getIntProperty("a", 1)).as("getIntProperty(a, 1)").isEqualTo(1);
    }

    @Test
    public void testGreen() throws Exception {
        assertThat(Utils.green(ASCII)).isEqualTo(Colors.DARK_GREEN + ASCII + Colors.NORMAL);
    }

    @Test
    public void testIsValidString() throws Exception {
        assertThat(Utils.isValidString(ASCII)).as("isValidString(ascii)").isTrue();
        assertThat(Utils.isValidString("")).as("isValidString(empty)").isFalse();
        assertThat(Utils.isValidString("    ")).as("isValidString(   )").isFalse();
        assertThat(Utils.isValidString("  \t ")).as("isValidString(tab)").isFalse();
        assertThat(Utils.isValidString(null)).as("isValidString(null)").isFalse();
    }

    @Test
    public void testIsoLocalDate() throws Exception {
        assertThat(Utils.isoLocalDate(cal.getTime())).as("isoLocalDate(date)").isEqualTo("1952-02-17");
        assertThat(Utils.isoLocalDate(localDateTime)).as("isoLocalDate(localDate)").isEqualTo("1952-02-17");
    }

    @Test
    public void testPlural() throws Exception {
        final String week = "week";
        final String weeks = "weeks";

        assertThat(Utils.plural(-1, week, weeks)).as("plural(-1)").isEqualTo(week);
        assertThat(Utils.plural(0, week, weeks)).as("plural(0)").isEqualTo(week);
        assertThat(Utils.plural(1, week, weeks)).as("plural(1)").isEqualTo(week);
        assertThat(Utils.plural(2, week, weeks)).as("plural(2)").isEqualTo(weeks);
    }

    @Test
    public void testReverseColor() throws Exception {
        assertThat(Utils.reverseColor(ASCII)).isEqualTo(Colors.REVERSE + ASCII + Colors.REVERSE);
    }

    @Test
    public void testToday() throws Exception {
        assertThat(Utils.today()).isEqualTo(Utils.isoLocalDate(LocalDateTime.now()));
    }

    @Test
    public void testUnescapeXml() throws Exception {
        assertThat(Utils.unescapeXml("&lt;a name=&quot;test &amp; &apos;&#39;&quot;&gt;"))
            .isEqualTo("<a name=\"test & ''\">");
    }

    @Test
    public void testUtcDateTime() throws Exception {
        assertThat(Utils.utcDateTime(cal.getTime())).as("utcDateTime(date)").isEqualTo("1952-02-17 12:30");
        assertThat(Utils.utcDateTime(localDateTime)).as("utcDateTime(localDate)")
            .isEqualTo("1952-02-17 12:30");
    }
}
