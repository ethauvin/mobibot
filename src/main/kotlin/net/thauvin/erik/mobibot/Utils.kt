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

import net.thauvin.erik.mobibot.msg.Message.Companion.DEFAULT_COLOR
import org.jibble.pircbot.Colors
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Properties
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * Miscellaneous utilities.
 */
@Suppress("TooManyFunctions")
object Utils {
    private val searchFlags = arrayOf("%c", "%n")

    /**
     * Appends a suffix to the end of the String if not present.
     */
    @JvmStatic
    fun String.appendIfMissing(suffix: Char): String {
        return if (this.last() != suffix) {
            "$this${suffix}"
        } else {
            this
        }
    }

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
        return text.replaceEach(searchFlags, replace)
    }

    /**
     * Capitalize a string.
     */
    @JvmStatic
    fun String.capitalise(): String = this.replaceFirstChar { it.uppercase() }

    /**
     * Capitalize words
     */
    fun String.capitalizeWords(): String = split(" ").map { it.lowercase().capitalise() }.joinToString(" ")


    /**
     * Colorize a string.
     */
    @JvmStatic
    fun colorize(s: String?, color: String): String {
        return if (s.isNullOrEmpty()) {
            ""
        } else if (color == DEFAULT_COLOR) {
            s
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
     * Returns a property as an int.
     */
    @JvmStatic
    fun Properties.getIntProperty(key: String, defaultValue: Int): Int {
        return this.getProperty(key)?.toIntOrDefault(defaultValue) ?: defaultValue
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
        val s = if (isBold) bold(help) else help
        return if (isIndent) s.prependIndent() else s
    }

    /**
     * Obfuscates the given string.
     */
    @JvmStatic
    fun String.obfuscate(): String {
        return if (this.isNotBlank()) {
            "x".repeat(this.length)
        } else this
    }

    /**
     * Returns the plural form of a word, if count &gt; 1.
     */
    @JvmStatic
    fun String.plural(count: Long): String {
        return if (count > 1) "${this}s" else this
    }

    /**
     * Makes the given string red.
     */
    @JvmStatic
    fun red(s: String?): String = colorize(s, Colors.RED)

    /**
     * Replaces all occurrences of Strings within another String.
     */
    @JvmStatic
    fun String.replaceEach(search: Array<out String>, replace: Array<out String>): String {
        var result = this
        if (search.size == replace.size) {
            search.forEachIndexed { i, s ->
                result = result.replace(s, replace[i])
            }
        }
        return result
    }

    /**
     * Makes the given string reverse color.
     */
    @JvmStatic
    fun reverseColor(s: String?): String = colorize(s, Colors.REVERSE)

    /**
     * Returns today's date.
     */
    @JvmStatic
    fun today(): String = LocalDateTime.now().toIsoLocalDate()

    /**
     * Converts a string to an int.
     */
    @JvmStatic
    fun String.toIntOrDefault(defaultValue: Int): Int {
        return try {
            this.toInt()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    /**
     * Returns the specified date as an ISO local date string.
     */
    @JvmStatic
    fun Date.toIsoLocalDate(): String {
        return LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault()).toIsoLocalDate()
    }

    /**
     * Returns the specified date as an ISO local date string.
     */
    @JvmStatic
    fun LocalDateTime.toIsoLocalDate(): String = this.format(DateTimeFormatter.ISO_LOCAL_DATE)

    /**
     * Returns the specified date formatted as `yyyy-MM-dd HH:mm`.
     */
    @JvmStatic
    fun Date.toUtcDateTime(): String {
        return LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault()).toUtcDateTime()
    }

    /**
     * Returns the specified date formatted as `yyyy-MM-dd HH:mm`.
     */
    @JvmStatic
    fun LocalDateTime.toUtcDateTime(): String = this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    /**
     * Converts XML/XHTML entities to plain text.
     */
    @JvmStatic
    fun unescapeXml(str: String): String = Jsoup.parse(str).text()

    /**
     * Converts milliseconds to year month week day hour and minutes.
     */
    @Suppress("MagicNumber")
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
        val hours = TimeUnit.MILLISECONDS.toHours(uptime) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(uptime))
        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(uptime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(uptime))

        with(info) {
            if (years > 0) {
                append(years).append(" year".plural(years)).append(' ')
            }
            if (months > 0) {
                append(weeks).append(" month".plural(months)).append(' ')
            }
            if (weeks > 0) {
                append(weeks).append(" week".plural(weeks)).append(' ')
            }
            if (days > 0) {
                append(days).append(" day".plural(days)).append(' ')
            }
            if (hours > 0) {
                append(hours).append(" hour".plural(hours)).append(' ')
            }

            append(minutes).append(" minute".plural(minutes))

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
}
