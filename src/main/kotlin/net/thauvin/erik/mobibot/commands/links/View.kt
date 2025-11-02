/*
 * View.kt
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpCmdSyntax
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.lastOrEmpty
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.links.LinksManager.Companion.entries
import net.thauvin.erik.mobibot.entries.EntriesUtils
import net.thauvin.erik.mobibot.entries.EntryLink
import org.pircbotx.hooks.events.PrivateMessageEvent
import org.pircbotx.hooks.types.GenericMessageEvent

/**
 * Displays a list of entries or an appropriate message if no entries exist.
 */
class View : AbstractCommand() {
    override val name = VIEW_CMD

    override val isOpOnly = false
    override val isPublic = true
    override val isVisible = true

    init {
        addHelp(
            "To list or search the current URL posts:",
            helpFormat("%c $name [<start>] [<query>]")
        )
    }

    companion object {
        const val MAX_ENTRIES = 6
        const val VIEW_CMD = "view"
    }

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        if (entries.links.isNotEmpty()) {
            val p = parseArgs(args)
            viewEntries(p.first, p.second, event)
        } else {
            event.sendMessage("There is currently nothing to view. Why don't you post something?")
        }
    }

    /**
     * Parses the view command input arguments and determines a starting index and query string.
     *
     *`view [<start>] [<query>]`
     */
    internal fun parseArgs(args: String): Pair<Int, String> {
        var query = args.lowercase().trim()
        var start = 0
        if (query.isEmpty() && entries.links.size > MAX_ENTRIES) {
            start = entries.links.size - MAX_ENTRIES
        }
        if (query.matches("^\\d+(| .*)".toRegex())) { // view [<start>] [<query>]
            val split = query.split(" ", limit = 2)
            try {
                start = split[0].toInt() - 1
                query = split.lastOrEmpty().trim()
                if (start > entries.links.size) {
                    start = 0
                }
            } catch (_: NumberFormatException) {
                // Do nothing
            }
        }
        return Pair(start, query)
    }

    private fun viewEntries(start: Int, query: String, event: GenericMessageEvent) {
        var index = start
        var entry: EntryLink
        var sent = 0
        while (index < entries.links.size && sent < MAX_ENTRIES) {
            entry = entries.links[index]
            if (query.isNotBlank()) {
                if (entry.matches(query)) {
                    event.sendMessage(EntriesUtils.printLink(index, entry, true))
                    sent++
                }
            } else {
                event.sendMessage(EntriesUtils.printLink(index, entry, true))
                sent++
            }
            index++
            if (sent == MAX_ENTRIES && index < entries.links.size) {
                event.sendMessage("To view more, try: ")
                event.sendMessage(
                    helpFormat(
                        helpCmdSyntax("%c $name ${index + 1} $query", event.bot().nick, event is PrivateMessageEvent)
                    )
                )
            }
        }
        if (sent == 0) {
            event.sendMessage("No matches. Please try again.")
        }
    }
}
