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

import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.entries.EntriesUtils
import net.thauvin.erik.mobibot.entries.EntryLink

class Comment : AbstractCommand() {
    override val command = COMMAND
    override val help = listOf(
        Utils.bold("To add a comment:"),
        Utils.helpIndent("${Constants.LINK_CMD}1:This is a comment"),
        "I will reply with a label, for example: ${Utils.bold(Constants.LINK_CMD)}1.1",
        Utils.bold("To edit a comment, use its label: "),
        Utils.helpIndent("${Constants.LINK_CMD}1.1:This is an edited comment"),
        Utils.bold("To delete a comment, use its label and a minus sign: "),
        Utils.helpIndent("${Constants.LINK_CMD}1.1:-")
    )
    override val isOp = false
    override val isPublic = true
    override val isVisible = true

    companion object {
        const val COMMAND = "comment"
    }

    override fun commandResponse(
        bot: Mobibot,
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        val cmds = args.substring(1).split("[.:]".toRegex(), 3)
        val index = cmds[0].toInt() - 1

        if (index < UrlMgr.entriesCount) {
            val entry: EntryLink = UrlMgr.getEntry(index)
            val commentIndex = cmds[1].toInt() - 1
            if (commentIndex < entry.commentsCount) {
                when (val cmd = cmds[2].trim()) {
                    "" -> showComment(bot, entry, index, commentIndex) // L1.1:
                    "-" -> deleteComment(bot, entry, index, commentIndex) // L11:-
                    "?" -> changeAuthor(bot, cmd, sender, isOp, entry, index, commentIndex) // L1.1:?<author>
                    else -> setComment(bot, cmd, sender, entry, index, commentIndex)
                }
            }
        }
    }

    override fun helpResponse(
        bot: Mobibot,
        command: String,
        sender: String,
        isOp: Boolean,
        isPrivate: Boolean
    ): Boolean {
        if (super.helpResponse(bot, command, sender, isOp, isPrivate)) {
            if (isOp) {
                bot.send(sender, Utils.bold("To change a comment's author:"))
                bot.send(sender, Utils.helpIndent("/msg ${bot.nick} ${Constants.LINK_CMD}1.1:?<nick>"))
            }
            return true
        }
        return false
    }

    override fun matches(message: String): Boolean {
        return message.matches("^${Constants.LINK_CMD}[0-9]+\\.[0-9]+:.*".toRegex())
    }

    private fun changeAuthor(
        bot: Mobibot,
        cmd: String,
        sender: String,
        isOp: Boolean,
        entry: EntryLink,
        index: Int,
        commentIndex: Int
    ) {
        if (isOp && cmd.length > 1) {
            val comment = entry.getComment(commentIndex)
            comment.nick = cmd.substring(1)
            bot.send(bot.channel, EntriesUtils.buildComment(index, commentIndex, comment))
            UrlMgr.saveEntries(bot, false)
        } else {
            bot.send(sender, "Please ask a channel op to change the author of this comment for you.")
        }
    }

    private fun deleteComment(bot: Mobibot, entry: EntryLink, index: Int, commentIndex: Int) {
        entry.deleteComment(commentIndex)
        bot.send(bot.channel, "Comment ${Constants.LINK_CMD}${index + 1}.${commentIndex + 1} removed.")
        UrlMgr.saveEntries(bot, false)
    }

    private fun setComment(bot: Mobibot, cmd: String, sender: String, entry: EntryLink, index: Int, commentIndex: Int) {
        entry.setComment(commentIndex, cmd, sender)
        val comment = entry.getComment(commentIndex)
        bot.send(sender, EntriesUtils.buildComment(index, commentIndex, comment))
        UrlMgr.saveEntries(bot, false)
    }

    private fun showComment(bot: Mobibot, entry: EntryLink, index: Int, commentIndex: Int) {
        bot.send(bot.channel, EntriesUtils.buildComment(index, commentIndex, entry.getComment(commentIndex)))
    }
}
