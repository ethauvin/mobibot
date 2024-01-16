/*
 * Info.kt
 *
 * Copyright 2004-2024 Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot.commands

import net.thauvin.erik.mobibot.ReleaseInfo
import net.thauvin.erik.mobibot.Utils.capitalise
import net.thauvin.erik.mobibot.Utils.green
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.isChannelOp
import net.thauvin.erik.mobibot.Utils.plural
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.commands.links.LinksManager
import net.thauvin.erik.mobibot.commands.seen.Seen
import net.thauvin.erik.mobibot.commands.tell.Tell
import org.pircbotx.hooks.types.GenericMessageEvent
import java.lang.management.ManagementFactory
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Info(private val tell: Tell, private val seen: Seen) : AbstractCommand() {
    private val allVersions = listOf(
        "${ReleaseInfo.PROJECT.capitalise()} ${ReleaseInfo.VERSION} (${ReleaseInfo.WEBSITE.green()})",
        "Written by ${ReleaseInfo.AUTHOR} (${ReleaseInfo.AUTHOR_URL.green()})"
    )
    override val name = "info"
    override val help = listOf("To view information about the bot:", helpFormat("%c $name"))
    override val isOpOnly = false
    override val isPublic = true
    override val isVisible = true

    companion object {
        /**
         * Converts milliseconds to year month week day hour and minutes.
         */
        @JvmStatic
        fun Long.toUptime(): String {
            this.toDuration(DurationUnit.MILLISECONDS).toComponents { wholeDays, hours, minutes, seconds, _ ->
                val years = wholeDays / 365
                var days = wholeDays % 365
                val months = days / 30
                days %= 30
                val weeks = days / 7
                days %= 7

                with(StringBuffer()) {
                    if (years > 0) {
                        append(years).append(" year".plural(years)).append(' ')
                    }
                    if (months > 0) {
                        append(months).append(" month".plural(months)).append(' ')
                    }
                    if (weeks > 0) {
                        append(weeks).append(" week".plural(weeks)).append(' ')
                    }
                    if (days > 0) {
                        append(days).append(" day".plural(days)).append(' ')
                    }
                    if (hours > 0) {
                        append(hours).append(" hour".plural(hours.toLong())).append(' ')
                    }

                    if (minutes > 0) {
                        append(minutes).append(" minute".plural(minutes.toLong()))
                    } else {
                        append(seconds).append(" second".plural(seconds.toLong()))
                    }

                    return toString()
                }
            }
        }
    }

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        event.sendList(allVersions, 1)
        val info = StringBuilder()
        info.append("Uptime: ")
            .append(ManagementFactory.getRuntimeMXBean().uptime.toUptime())
            .append(" [Entries: ")
            .append(LinksManager.entries.links.size)
        if (seen.isEnabled()) {
            info.append(", Seen: ").append(seen.count())
        }
        if (event.isChannelOp(channel)) {
            if (tell.isEnabled()) {
                info.append(", Messages: ").append(tell.size())
            }
            if (LinksManager.socialManager.entriesCount() > 0) {
                info.append(", Social: ").append(LinksManager.socialManager.entriesCount())
            }
        }
        info.append(", Recap: ").append(Recap.recaps.size).append(']')
        event.sendMessage(info.toString())
    }
}
