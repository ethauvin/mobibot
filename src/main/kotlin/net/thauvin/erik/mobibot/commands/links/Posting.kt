/*
 * Posting.kt
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

package net.thauvin.erik.mobibot.commands.links

import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.isChannelOp
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.links.LinksMgr.Companion.entries
import net.thauvin.erik.mobibot.entries.EntriesUtils
import net.thauvin.erik.mobibot.entries.EntryLink
import org.pircbotx.hooks.types.GenericMessageEvent

class Posting : AbstractCommand() {
    override val name = "posting"
    override val help = listOf(
        "Post a URL, by saying it on a line on its own:",
        helpFormat("<url> [<title>] ${Tags.COMMAND}: <+tag> [...]]"),
        "I will reply with a label, for example: ${Constants.LINK_CMD.bold()}1",
        "To add a title, use its label and a pipe:",
        helpFormat("${Constants.LINK_CMD}1:|This is the title"),
        "To add a comment:",
        helpFormat("${Constants.LINK_CMD}1:This is a comment"),
        "I will reply with a label, for example: ${Constants.LINK_CMD.bold()}1.1",
        "To edit a comment, see: ",
        helpFormat("%c ${Constants.HELP_CMD} ${Comment.COMMAND}")
    )
    override val isOpOnly = false
    override val isPublic = true
    override val isVisible = true

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        val cmds = args.substring(1).split(":", limit = 2)
        val entryIndex = cmds[0].toInt() - 1

        if (entryIndex < entries.links.size) {
            val cmd = cmds[1].trim()
            if (cmd.isBlank()) {
                showEntry(entryIndex, event) // L1:
            } else if (LinksMgr.isUpToDate(event)) {
                if (cmd == "-") {
                    removeEntry(channel, entryIndex, event) // L1:-
                } else {
                    when (cmd[0]) {
                        '|' -> changeTitle(cmd, entryIndex, event) // L1:|<title>
                        '=' -> changeUrl(channel, cmd, entryIndex, event) // L1:=<url>
                        '?' -> changeAuthor(channel, cmd, entryIndex, event) // L1:?<author>
                        else -> addComment(cmd, entryIndex, event) // L1:<comment>
                    }
                }
            }
        }
    }

    override fun matches(message: String): Boolean {
        return message.matches("${Constants.LINK_CMD}[0-9]+:.*".toRegex())
    }

    private fun addComment(cmd: String, entryIndex: Int, event: GenericMessageEvent) {
        val entry: EntryLink = entries.links[entryIndex]
        val commentIndex = entry.addComment(cmd, event.user.nick)
        val comment = entry.getComment(commentIndex)
        event.sendMessage(EntriesUtils.buildComment(entryIndex, commentIndex, comment))
        entries.save()
    }

    private fun changeTitle(cmd: String, entryIndex: Int, event: GenericMessageEvent) {
        if (cmd.length > 1) {
            val entry: EntryLink = entries.links[entryIndex]
            entry.title = cmd.substring(1).trim()
            LinksMgr.pinboard.updatePin(event.bot().serverHostname, entry.link, entry)
            event.sendMessage(EntriesUtils.buildLink(entryIndex, entry))
            entries.save()
        }
    }

    private fun changeUrl(channel: String, cmd: String, entryIndex: Int, event: GenericMessageEvent) {
        val entry: EntryLink = entries.links[entryIndex]
        if (entry.login == event.user.login || isChannelOp(channel, event)) {
            val link = cmd.substring(1)
            if (link.matches(LinksMgr.LINK_MATCH.toRegex())) {
                val oldLink = entry.link
                entry.link = link
                LinksMgr.pinboard.updatePin(event.bot().serverHostname, oldLink, entry)
                event.sendMessage(EntriesUtils.buildLink(entryIndex, entry))
                entries.save()
            }
        }
    }

    private fun changeAuthor(channel: String, cmd: String, index: Int, event: GenericMessageEvent) {
        if (isChannelOp(channel, event)) {
            if (cmd.length > 1) {
                val entry: EntryLink = entries.links[index]
                entry.nick = cmd.substring(1)
                LinksMgr.pinboard.updatePin(event.bot().serverHostname, entry.link, entry)
                event.sendMessage(EntriesUtils.buildLink(index, entry))
                entries.save()
            }
        } else {
            event.sendMessage("Please ask a channel op to change the author of this link for you.")
        }
    }

    private fun removeEntry(channel: String, index: Int, event: GenericMessageEvent) {
        val entry: EntryLink = entries.links[index]
        if (entry.login == event.user.login || isChannelOp(channel, event)) {
            LinksMgr.pinboard.deletePin(entry)
            LinksMgr.twitter.removeEntry(index)
            entries.links.removeAt(index)
            event.sendMessage("Entry ${EntriesUtils.buildLinkLabel(index)} removed.")
            entries.save()
        } else {
            event.sendMessage("Please ask a channel op to remove this entry for you.")
        }
    }

    private fun showEntry(index: Int, event: GenericMessageEvent) {
        val entry: EntryLink = entries.links[index]
        event.sendMessage(EntriesUtils.buildLink(index, entry))
        if (entry.tags.isNotEmpty()) {
            event.sendMessage(EntriesUtils.buildTags(index, entry))
        }
        if (entry.comments.isNotEmpty()) {
            val comments = entry.comments
            for (i in comments.indices) {
                event.sendMessage(EntriesUtils.buildComment(index, i, comments[i]))
            }
        }
    }
}
