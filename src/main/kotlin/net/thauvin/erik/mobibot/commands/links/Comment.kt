/*
 * Comment.kt
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
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.isChannelOp
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.entries.EntriesUtils.buildComment
import net.thauvin.erik.mobibot.entries.EntriesUtils.toLinkLabel
import net.thauvin.erik.mobibot.entries.EntryLink
import org.pircbotx.hooks.types.GenericMessageEvent

class Comment : AbstractCommand() {
    override val name = COMMAND
    override val help = listOf(
        "To add a comment:",
        helpFormat("${Constants.LINK_CMD}1:This is a comment"),
        "I will reply with a label, for example: ${Constants.LINK_CMD.bold()}1.1",
        "To edit a comment, use its label: ",
        helpFormat("${Constants.LINK_CMD}1.1:This is an edited comment"),
        "To delete a comment, use its label and a minus sign: ",
        helpFormat("${Constants.LINK_CMD}1.1:-")
    )
    override val isOpOnly = false
    override val isPublic = true
    override val isVisible = true

    companion object {
        const val COMMAND = "comment"
    }

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        val cmds = args.substring(1).split("[.:]".toRegex(), 3)
        val entryIndex = cmds[0].toInt() - 1

        if (entryIndex < LinksMgr.entries.links.size && LinksMgr.isUpToDate(event)) {
            val entry: EntryLink = LinksMgr.entries.links[entryIndex]
            val commentIndex = cmds[1].toInt() - 1
            if (commentIndex < entry.comments.size) {
                when (val cmd = cmds[2].trim()) {
                    "" -> showComment(entry, entryIndex, commentIndex, event) // L1.1:
                    "-" -> deleteComment(channel, entry, entryIndex, commentIndex, event) // L1.1:-
                    else -> {
                        if (cmd.startsWith('?')) {  // L1.1:?<author>
                            changeAuthor(channel, cmd, entry, entryIndex, commentIndex, event)
                        } else { // L1.1:<comment>
                            setComment(cmd, entry, entryIndex, commentIndex, event)
                        }
                    }
                }
            }
        }
    }

    override fun helpResponse(channel: String, topic: String, event: GenericMessageEvent): Boolean {
        if (super.helpResponse(channel, topic, event)) {
            if (isChannelOp(channel, event)) {
                event.sendMessage("To change a comment's author:")
                event.sendMessage(helpFormat("${Constants.LINK_CMD}1.1:?<nick>"))
            }
            return true
        }
        return false
    }

    override fun matches(message: String): Boolean {
        return message.matches("^${Constants.LINK_CMD}\\d+\\.\\d+:.*".toRegex())
    }

    private fun changeAuthor(
        channel: String,
        cmd: String,
        entry: EntryLink,
        entryIndex: Int,
        commentIndex: Int,
        event: GenericMessageEvent
    ) {
        if (isChannelOp(channel, event) && cmd.length > 1) {
            val comment = entry.getComment(commentIndex)
            comment.nick = cmd.substring(1)
            event.sendMessage(buildComment(entryIndex, commentIndex, comment))
            LinksMgr.entries.save()
        } else {
            event.sendMessage("Please ask a channel op to change the author of this comment for you.")
        }
    }

    private fun deleteComment(
        channel: String,
        entry: EntryLink,
        entryIndex: Int,
        commentIndex: Int,
        event: GenericMessageEvent
    ) {
        if (isChannelOp(channel, event) || event.user.nick == entry.getComment(commentIndex).nick) {
            entry.deleteComment(commentIndex)
            event.sendMessage("Comment ${entryIndex.toLinkLabel()}.${commentIndex + 1} removed.")
            LinksMgr.entries.save()
        } else {
            event.sendMessage("Please ask a channel op to delete this comment for you.")
        }
    }

    private fun setComment(
        cmd: String,
        entry: EntryLink,
        entryIndex: Int,
        commentIndex: Int,
        event: GenericMessageEvent
    ) {
        entry.setComment(commentIndex, cmd, event.user.nick)
        event.sendMessage(buildComment(entryIndex, commentIndex, entry.getComment(commentIndex)))
        LinksMgr.entries.save()
    }

    private fun showComment(entry: EntryLink, entryIndex: Int, commentIndex: Int, event: GenericMessageEvent) {
        event.sendMessage(buildComment(entryIndex, commentIndex, entry.getComment(commentIndex)))
    }
}
