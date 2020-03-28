/*
 * Utils.java
 *
 * Copyright (c) 2004-2020, Erik C. Thauvin (erik@thauvin.net)
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
import org.jsoup.Jsoup;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
     * Colorize a string.
     *
     * @param s     The string.
     * @param color The color.
     * @return The colorized string.
     */
    static String colorize(final String s, final String color) {
        if (s == null) {
            return Colors.NORMAL;
        } else if (Colors.BOLD.equals(color) || Colors.REVERSE.equals(color)) {
            return color + s + color;
        }

        return color + s + Colors.NORMAL;
    }

    /**
     * Makes the given string cyan.
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
     * Returns indented help string.
     *
     * @param help The help string.
     * @return The indented help string.
     */
    public static String helpIndent(final String help) {
        return "        " + help;
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
     * Obfuscates the given string.
     *
     * @param s The string.
     * @return The obfuscated string.
     */
    public static String obfuscate(final String s) {
        if (StringUtils.isNotBlank(s)) {
            return StringUtils.repeat('x', s.length());
        }
        return s;
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
     * Makes the given string red.
     *
     * @param s The string.
     * @return The red string.
     */
    public static String red(final String s) {
        return colorize(s, Colors.RED);
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
        return Jsoup.parse(str).text();
    }

    /**
     * Converts milliseconds to year month week day hour and minutes.
     *
     * @param uptime The uptime in milliseconds.
     * @return The uptime in year month week day hours and minutes.
     */
    public static String uptime(final long uptime) {
        final StringBuilder info = new StringBuilder();

        long days = TimeUnit.MILLISECONDS.toDays(uptime);
        final long years = days / 365;
        days %= 365;
        final long months = days / 30;
        days %= 30;
        final long weeks = days / 7;
        days %= 7;
        final long hours = TimeUnit.MILLISECONDS.toHours(uptime) - TimeUnit.DAYS.toHours(
                TimeUnit.MILLISECONDS.toDays(uptime));
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(uptime));

        if (years > 0) {
            info.append(years).append(plural(years, " year ", " years "));
        }

        if (months > 0) {
            info.append(weeks).append(plural(months, " month ", " months "));
        }

        if (weeks > 0) {
            info.append(weeks).append(plural(weeks, " week ", " weeks "));
        }


        if (days > 0) {
            info.append(days).append(plural(days, " day ", " days "));
        }

        if (hours > 0) {
            info.append(hours).append(plural(hours, " hour ", " hours "));
        }

        info.append(minutes).append(plural(minutes, " minute", " minutes"));

        return info.toString();
    }

    /**
     * Returns the specified date formatted as <code>yyyy-MM-dd HH:mm</code>.
     *
     * @param date The date.
     * @return The formatted date.
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
