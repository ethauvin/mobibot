/*
 * Utils.java
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

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Miscellaneous utilities class.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2014-04-26
 * @since 1.0
 */
public final class Utils {
    /**
     * Disables the default constructor.
     *
     * @throws UnsupportedOperationException If the constructor is called.
     */
    private Utils() {
        throw new UnsupportedOperationException("Illegal constructor call.");
    }

    /**
     * Makes the given int bold.
     *
     * @param i The int.
     * @return The bold string.
     */
    public static String bold(final int i) {
        return bold(Integer.toString(i));
    }

    /**
     * Makes the given string bold.
     *
     * @param s The string.
     * @return The bold string.
     */
    public static String bold(final String s) {
        return colorize(s, Colors.BOLD);
    }

    /**
     * Capitalize a string.
     *
     * @param s The string.
     * @return The capitalized string.
     */
    public static String capitalize(final String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Colorize a string.
     *
     * @param s     The string.
     * @param color The color.
     * @return The colorized string.
     */
    static String colorize(final String s, final String color) {
        if (!Utils.isValidString(color) || Colors.NORMAL.equals(color)) {
            return s;
        } else if (Colors.BOLD.equals(color) || Colors.REVERSE.equals(color)) {
            return color + s + color;
        }

        return color + s + Colors.NORMAL;
    }

    /**
     * Meks the given string cyan.
     *
     * @param s The string.
     * @return The cyan string.
     */
    public static String cyan(final String s) {
        return colorize(s, Colors.CYAN);
    }

    /**
     * Ensures that the given location (File/URL) has a trailing slash (<code>/</code>) to indicate a directory.
     *
     * @param location The File or URL location.
     * @param isUrl    Set to true if the location is a URL
     * @return The location ending with a slash.
     */
    static String ensureDir(final String location, final boolean isUrl) {
        if (isUrl) {
            if (location.charAt(location.length() - 1) == '/') {
                return location;
            } else {
                return location + '/';
            }
        } else {
            if (location.charAt(location.length() - 1) == File.separatorChar) {
                return location;
            } else {
                return location + File.separatorChar;
            }
        }
    }

    /**
     * Returns a property as an int.
     *
     * @param property The property value.
     * @param def      The default property value.
     * @return The port or default value if invalid.
     */
    public static int getIntProperty(final String property, final int def) {
        int prop;

        try {
            prop = Integer.parseInt(property);
        } catch (NumberFormatException ignore) {
            prop = def;
        }

        return prop;
    }

    /**
     * Makes the given string green.
     *
     * @param s The string.
     * @return The green string.
     */
    public static String green(final String s) {
        return colorize(s, Colors.DARK_GREEN);
    }

    /**
     * Returns <code>true</code> if the given string is <em>not</em> blank or null.
     *
     * @param s The string to check.
     * @return <code>true</code> if the string is valid, <code>false</code> otherwise.
     */
    public static boolean isValidString(final CharSequence s) {
        final int len;
        if (s == null || (len = s.length()) == 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the specified date as an ISO local date string.
     *
     * @param date The date.
     * @return The date in {@link DateTimeFormatter#ISO_LOCAL_DATE} format.
     */
    public static String isoLocalDate(final Date date) {
        return isoLocalDate(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * Returns the specified date as an ISO local date string.
     *
     * @param date The date.
     * @return The date in {@link DateTimeFormatter#ISO_LOCAL_DATE} format.
     */
    public static String isoLocalDate(final LocalDateTime date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Returns the plural form of a word, if count &gt; 1.
     *
     * @param count  The count.
     * @param word   The word.
     * @param plural The plural word.
     * @return The plural string.
     */
    public static String plural(final long count, final String word, final String plural) {
        if (count > 1) {
            return plural;
        } else {
            return word;
        }
    }

    /**
     * Makes the given string reverse color.
     *
     * @param s The string.
     * @return The reverse color string.
     */
    public static String reverseColor(final String s) {
        return colorize(s, Colors.REVERSE);
    }

    /**
     * Returns today's date.
     *
     * @return Today's date in {@link DateTimeFormatter#ISO_LOCAL_DATE} format.
     */
    public static String today() {
        return isoLocalDate(LocalDateTime.now());
    }

    /**
     * Converts XML/XHTML entities to plain text.
     *
     * @param str The string to unescape.
     * @return The unescaped string.
     */
    public static String unescapeXml(final String str) {
        return str.replaceAll("&amp;", "&")
            .replaceAll("&lt;", "<")
            .replaceAll("&gt;", ">")
            .replaceAll("&quot;", "\"")
            .replaceAll("&apos;", "'")
            .replaceAll("&#39;", "'");
    }

    /**
     * Returns the specified date formatted as <code>yyyy-MM-dd HH:mm</code>.
     *
     * @param date The date.
     * @return The fromatted date.
     */
    public static String utcDateTime(final Date date) {
        return utcDateTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * Returns the specified date formatted as <code>yyyy-MM-dd HH:mm</code>.
     *
     * @param date The date.
     * @return The formatted date.
     */
    public static String utcDateTime(final LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

}
