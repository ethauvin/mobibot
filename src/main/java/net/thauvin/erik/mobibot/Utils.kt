/*
 * Utils.kt
 *
 * Copyright (c) 2004-2021, Erik C. Thauvin (erik@thauvin.net)
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

import org.apache.commons.lang3.StringUtils
import org.jibble.pircbot.Colors
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * Miscellaneous utilities.
 */
object Utils {
    private val searchFlags = arrayOf("%c", "%n")

    /**
     * Makes the given int bold.
     */
    @JvmStatic
    fun bold(i: Int): String = bold(i.toString())

    /**
     * Makes the given long bold.
     */
    @JvmStatic
    fun bold(i: Long): String = bold(i.toString())

    /**
     * Makes the given string bold.
     */
    @JvmStatic
    fun bold(s: String?): String = colorize(s, Colors.BOLD)

    /**
     * Build a help command by replacing `%c` with the bot's pub/priv command, and `%n` with the bot's
     * nick.
     */
    @JvmStatic
    fun buildCmdSyntax(text: String, botNick: String, isPrivate: Boolean): String {
        val replace = arrayOf(if (isPrivate) "/msg $botNick" else "$botNick:", botNick)
        return StringUtils.replaceEach(text, searchFlags, replace)
    }
    
    /**
     * Capitalize a string.
     */
    @JvmStatic
    fun capitalize(s: String?): String {
        return s!!.replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Colorize a string.
     */
    @JvmStatic
    fun colorize(s: String?, color: String): String {
        return if (s.isNullOrBlank()) {
            Colors.NORMAL
        } else if (Colors.BOLD == color || Colors.REVERSE == color) {
            color + s + color
        } else {
            color + s + Colors.NORMAL
        }
    }

    /**
     * Makes the given string cyan.
     */
    @JvmStatic
    fun cyan(s: String?): String = colorize(s, Colors.CYAN)

    /**
     * URL encodes the given string.
     */
    @JvmStatic
    fun encodeUrl(s: String): String = URLEncoder.encode(s, StandardCharsets.UTF_8)

    /**
     * Ensures that the given location (File/URL) has a trailing slash (`/`) to indicate a directory.
     */
    @JvmStatic
    fun ensureDir(location: String, isUrl: Boolean): String {
        return if (location.isNotEmpty()) {
            if (isUrl) {
                if (location[location.length - 1] == '/') {
                    location
                } else {
                    "$location/"
                }
            } else {
                if (location[location.length - 1] == File.separatorChar) {
                    location
                } else {
                    location + File.separatorChar
                }
            }
        } else {
            location
        }
    }

    /**
     * Returns a property as an int.
     */
    @JvmStatic
    fun getIntProperty(property: String?, def: Int): Int {
        return try {
            property?.toInt() ?: def
        } catch (nfe: NumberFormatException) {
            def
        }
    }

    /**
     * Makes the given string green.
     */
    @JvmStatic
    fun green(s: String?): String = colorize(s, Colors.DARK_GREEN)

    /**
     * Returns a formatted help string.
     */
    @JvmStatic
    @JvmOverloads
    fun helpFormat(help: String, isBold: Boolean = true, isIndent: Boolean = true): String {
        return (if (isIndent) "    " else "").plus(if (isBold) bold(help) else help)
    }

    /**
     * Returns the specified date as an ISO local date string.
     */
    @JvmStatic
    fun isoLocalDate(date: Date): String {
        return isoLocalDate(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
    }

    /**
     * Returns the specified date as an ISO local date string.
     */
    @JvmStatic
    fun isoLocalDate(date: LocalDateTime): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    /**
     * Obfuscates the given string.
     */
    @JvmStatic
    fun obfuscate(s: String): String {
        return if (s.isNotBlank()) {
            StringUtils.repeat('x', s.length)
        } else s
    }

    /**
     * Returns the plural form of a word, if count &gt; 1.
     */
    @JvmStatic
    fun plural(count: Int, word: String, plural: String): String = plural(count.toLong(), word, plural)

    /**
     * Returns the plural form of a word, if count &gt; 1.
     */
    @JvmStatic
    fun plural(count: Long, word: String, plural: String): String {
        return if (count > 1) {
            plural
        } else {
            word
        }
    }

    /**
     * Makes the given string red.
     */
    @JvmStatic
    fun red(s: String?): String = colorize(s, Colors.RED)

    /**
     * Makes the given string reverse color.
     */
    @JvmStatic
    fun reverseColor(s: String?): String = colorize(s, Colors.REVERSE)

    /**
     * Returns today's date.
     */
    @JvmStatic
    fun today(): String = isoLocalDate(LocalDateTime.now())

    /**
     * Converts XML/XHTML entities to plain text.
     */
    @JvmStatic
    fun unescapeXml(str: String): String = Jsoup.parse(str).text()

    /**
     * Converts milliseconds to year month week day hour and minutes.
     */
    @JvmStatic
    fun uptime(uptime: Long): String {
        val info = StringBuilder()
        var days = TimeUnit.MILLISECONDS.toDays(uptime)
        val years = days / 365
        days %= 365
        val months = days / 30
        days %= 30
        val weeks = days / 7
        days %= 7
        val hours = TimeUnit.MILLISECONDS.toHours(uptime) - TimeUnit.DAYS.toHours(
            TimeUnit.MILLISECONDS.toDays(uptime)
        )
        val minutes = TimeUnit.MILLISECONDS.toMinutes(uptime) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(uptime)
        )
        with(info) {
            if (years > 0) {
                append(years).append(plural(years, " year ", " years "))
            }
            if (months > 0) {
                append(weeks).append(plural(months, " month ", " months "))
            }
            if (weeks > 0) {
                append(weeks).append(plural(weeks, " week ", " weeks "))
            }
            if (days > 0) {
                append(days).append(plural(days, " day ", " days "))
            }
            if (hours > 0) {
                append(hours).append(plural(hours, " hour ", " hours "))
            }
            append(minutes).append(plural(minutes, " minute", " minutes"))
            return toString()
        }
    }

    /**
     * Reads contents of a URL.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun urlReader(url: URL): String {
        BufferedReader(InputStreamReader(url.openStream(), StandardCharsets.UTF_8))
            .use { reader -> return reader.lines().collect(Collectors.joining(System.lineSeparator())) }
    }

    /**
     * Returns the specified date formatted as `yyyy-MM-dd HH:mm`.
     */
    @JvmStatic
    fun utcDateTime(date: Date): String {
        return utcDateTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
    }

    /**
     * Returns the specified date formatted as `yyyy-MM-dd HH:mm`.
     */
    @JvmStatic
    fun utcDateTime(date: LocalDateTime?): String {
        return date?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) ?: ""
    }
}
