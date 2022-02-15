/*
 * Utils.kt
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

import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.Message.Companion.DEFAULT_COLOR
import org.jsoup.Jsoup
import org.pircbotx.Colors
import org.pircbotx.PircBotX
import org.pircbotx.hooks.events.PrivateMessageEvent
import org.pircbotx.hooks.types.GenericMessageEvent
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
        return if (last() != suffix) {
            "$this${suffix}"
        } else {
            this
        }
    }

    /**
     * Makes the given int bold.
     */
    @JvmStatic
    fun Int.bold(): String = toString().bold()

    /**
     * Makes the given long bold.
     */
    @JvmStatic
    fun Long.bold(): String = toString().bold()

    /**
     * Makes the given string bold.
     */
    @JvmStatic
    fun String?.bold(): String = colorize(Colors.BOLD)

    /**
     * Returns the [PircBotX] instance.
     */
    fun GenericMessageEvent.bot(): PircBotX {
        return getBot() as PircBotX
    }

    /**
     * Capitalize a string.
     */
    @JvmStatic
    fun String.capitalise(): String = lowercase().replaceFirstChar { it.uppercase() }

    /**
     * Capitalize words
     */
    @JvmStatic
    fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalise() }

    /**
     * Colorize a string.
     */
    @JvmStatic
    fun String?.colorize(color: String): String {
        return if (isNullOrEmpty()) {
            ""
        } else if (color == DEFAULT_COLOR) {
            this
        } else if (Colors.BOLD == color || Colors.REVERSE == color) {
            color + this + color
        } else {
            color + this + Colors.NORMAL
        }
    }

    /**
     * Makes the given string cyan.
     */
    @JvmStatic
    fun String?.cyan(): String = colorize(Colors.CYAN)

    /**
     * URL encodes the given string.
     */
    @JvmStatic
    fun String.encodeUrl(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)

    /**
     * Returns a property as an int.
     */
    @JvmStatic
    fun Properties.getIntProperty(key: String, defaultValue: Int): Int {
        return getProperty(key)?.toIntOrDefault(defaultValue) ?: defaultValue
    }

    /**
     * Makes the given string green.
     */
    @JvmStatic
    fun String?.green(): String = colorize(Colors.DARK_GREEN)

    /**
     * Build a help command by replacing `%c` with the bot's pub/priv command, and `%n` with the bot's
     * nick.
     */
    @JvmStatic
    fun helpCmdSyntax(text: String, botNick: String, isPrivate: Boolean): String {
        val replace = arrayOf(if (isPrivate) "/msg $botNick" else "$botNick:", botNick)
        return text.replaceEach(searchFlags, replace)
    }

    /**
     * Returns a formatted help string.
     */
    @JvmStatic
    @JvmOverloads
    fun helpFormat(help: String, isBold: Boolean = true, isIndent: Boolean = true): String {
        val s = if (isBold) help.bold() else help
        return if (isIndent) s.prependIndent() else s
    }

    /**
     * Returns {@code true} if the specified user is an operator on the [channel].
     */
    @JvmStatic
    fun isChannelOp(channel: String, event: GenericMessageEvent): Boolean {
        return event.bot().userChannelDao.getChannel(channel).isOp(event.user)
    }

    /**
     * Returns the last item of a list of strings or empty if none.
     */
    @JvmStatic
    fun List<String>.lastOrEmpty(): String {
        return if (this.size >= 2) {
            this.last()
        } else
            ""
    }

    /**
     * Returns {@code true} if the list does not contain the given string.
     */
    @JvmStatic
    fun List<String>.notContains(text: String, ignoreCase: Boolean = false) = this.none { it.equals(text, ignoreCase) }

    /**
     * Obfuscates the given string.
     */
    @JvmStatic
    fun String.obfuscate(): String {
        return if (isNotBlank()) {
            "x".repeat(length)
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
    fun String?.red(): String = colorize(Colors.RED)

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
    fun String?.reverseColor(): String = colorize(Colors.REVERSE)

    /**
     * Send a formatted commands/modules, etc. list.
     */
    @JvmStatic
    @JvmOverloads
    fun GenericMessageEvent.sendList(
        list: List<String>,
        maxPerLine: Int,
        separator: String = " ",
        isBold: Boolean = false,
        isIndent: Boolean = false
    ) {
        var i = 0
        while (i < list.size) {
            sendMessage(
                helpFormat(
                    list.subList(i, list.size.coerceAtMost(i + maxPerLine)).joinToString(separator, truncated = ""),
                    isBold,
                    isIndent
                ),
            )
            i += maxPerLine
        }
    }

    /**
     * Sends a [message].
     */
    @JvmStatic
    fun GenericMessageEvent.sendMessage(channel: String, message: Message) {
        if (message.isNotice) {
            bot().sendIRC().notice(user.nick, message.msg.colorize(message.color))
        } else if (message.isPrivate || this is PrivateMessageEvent || channel.isBlank()) {
            respondPrivateMessage(message.msg.colorize(message.color))
        } else {
            bot().sendIRC().message(channel, message.msg.colorize(message.color))
        }
    }

    /**
     * Sends a response as a private message or notice.
     */
    @JvmStatic
    fun GenericMessageEvent.sendMessage(message: String) {
        if (this is PrivateMessageEvent) {
            respondPrivateMessage(message)
        } else {
            bot().sendIRC().notice(user.nick, message)
        }
    }

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
            toInt()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    /**
     * Returns the specified date as an ISO local date string.
     */
    @JvmStatic
    fun Date.toIsoLocalDate(): String {
        return LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault()).toIsoLocalDate()
    }

    /**
     * Returns the specified date as an ISO local date string.
     */
    @JvmStatic
    fun LocalDateTime.toIsoLocalDate(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

    /**
     * Returns the specified date formatted as `yyyy-MM-dd HH:mm`.
     */
    @JvmStatic
    fun Date.toUtcDateTime(): String {
        return LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault()).toUtcDateTime()
    }

    /**
     * Returns the specified date formatted as `yyyy-MM-dd HH:mm`.
     */
    @JvmStatic
    fun LocalDateTime.toUtcDateTime(): String = format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    /**
     * Converts XML/XHTML entities to plain text.
     */
    @JvmStatic
    fun String.unescapeXml(): String = Jsoup.parse(this).text()

    /**
     * Reads contents of a URL.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun URL.reader(): String {
        BufferedReader(InputStreamReader(this.openStream(), StandardCharsets.UTF_8))
            .use { reader -> return reader.lines().collect(Collectors.joining(System.lineSeparator())) }
    }
}
