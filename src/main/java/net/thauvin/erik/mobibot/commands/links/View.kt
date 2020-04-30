/*
 * Links.kt
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

package net.thauvin.erik.mobibot.commands.links

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.links.LinksMgr.Companion.entriesCount
import net.thauvin.erik.mobibot.commands.links.LinksMgr.Companion.getEntry
import net.thauvin.erik.mobibot.entries.EntriesUtils
import net.thauvin.erik.mobibot.entries.EntryLink

class View(bot: Mobibot) : AbstractCommand(bot) {
    private val maxEntries = 8
    override val name = VIEW_CMD
    override val help = listOf(
        "To list or search the current URL posts:",
        Utils.helpIndent("%c $name [<start>] [<query>]")
    )
    override val isOp = false
    override val isPublic = true
    override val isVisible = true

    companion object {
        const val VIEW_CMD = "view"
    }

    override fun commandResponse(
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        if (entriesCount != 0) {
            showPosts(bot, args, sender)
        } else {
            bot.send(sender, "There is currently nothing to view. Why don't you post something?", isPrivate)
        }
    }

    private fun showPosts(bot: Mobibot, args: String, sender: String) {
        val max = entriesCount
        var lcArgs = args.toLowerCase()
        var i = 0
        if (lcArgs.isEmpty() && max > maxEntries) {
            i = max - maxEntries
        }
        if (lcArgs.matches("^\\d+(| .*)".toRegex())) {
            val split = lcArgs.split(" ", limit = 2)
            try {
                i = split[0].toInt() - 1
                lcArgs = if (split.size == 2) {
                    split[1].trim()
                } else {
                    ""
                }
                if (i > max) {
                    i = 0
                }
            } catch (ignore: NumberFormatException) {
                // Do nothing
            }
        }

        var entry: EntryLink
        var sent = 0
        while (i < max && sent < maxEntries) {
            entry = getEntry(i)
            if (lcArgs.isNotBlank()) {
                if (entry.matches(lcArgs)) {
                    bot.send(sender, EntriesUtils.buildLink(i, entry, true), false)
                    sent++
                }
            } else {
                bot.send(sender, EntriesUtils.buildLink(i, entry, true), false)
                sent++
            }
            i++
            if (sent == maxEntries && i < max) {
                bot.send(
                    sender, "To view more, try: " + Utils.bold("${bot.nick}: $name ${i + 1} $lcArgs"), false
                )
            }
        }
        if (sent == 0) {
            bot.send(sender, "No matches. Please try again.", false)
        }
    }
}
