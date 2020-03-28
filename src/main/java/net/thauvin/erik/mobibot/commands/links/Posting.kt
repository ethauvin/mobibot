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

class Posting : AbstractCommand() {
    override val command = "posting"
    override val help = listOf(
        Utils.bold("Post a URL, by saying it on a line on its own:"),
        Utils.helpIndent("<url> [<title>] ${Tags.COMMAND}}: <+tag> [...]]"),
        "I will reply with a label, for example:" + Utils.bold("${Constants.LINK_CMD}1"),
        Utils.bold("To add a title, use its label and a pipe:"),
        Utils.helpIndent("${Constants.LINK_CMD}1:|This is the title"),
        Utils.bold("To add a comment:"),
        Utils.helpIndent("${Constants.LINK_CMD}1:This is a comment"),
        "I will reply with a label, for example: ${Utils.bold(Constants.LINK_CMD)}1.1",
        Utils.bold("To edit a comment, see: "),
        Utils.helpIndent("/msg %s ${Constants.HELP_CMD} ${Comment.COMMAND}")
    )
    override val isOp = false
    override val isPublic = true
    override val isVisible = true

    override fun commandResponse(
        bot: Mobibot,
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        val cmds = args.substring(1).split(":", limit = 2)
        val index = cmds[0].toInt() - 1

        if (index < UrlMgr.entriesCount) {
            val cmd = cmds[1].trim()
            if (cmd.isEmpty()) {
                val entry: EntryLink = UrlMgr.getEntry(index)
                bot.send(bot.channel, EntriesUtils.buildLink(index, entry))
                if (entry.hasTags()) {
                    bot.send(bot.channel, EntriesUtils.buildTags(index, entry))
                }
                if (entry.hasComments()) {
                    val comments = entry.comments
                    for (i in comments.indices) {
                        bot.send(bot.channel, EntriesUtils.buildComment(index, i, comments[i]))
                    }
                }
            } else {
                // L1:-
                if ("-" == cmd) {
                    val entry: EntryLink = UrlMgr.getEntry(index)
                    if (entry.login == login || isOp) {
                        bot.deletePin(entry)
                        if (bot.isTwitterAutoPost) {
                            bot.twitterRemoveEntry(index)
                        }
                        UrlMgr.removeEntry(index)
                        bot.send(bot.channel, "Entry ${Constants.LINK_CMD}${index + 1} removed.")
                        UrlMgr.saveEntries(bot, false)
                    } else {
                        bot.send(sender, "Please ask a channel op to remove this entry for you.")
                    }
                } else if (cmd[0] == '|') { // L1:|<title>
                    if (cmd.length > 1) {
                        val entry: EntryLink = UrlMgr.getEntry(index)
                        entry.title = cmd.substring(1).trim()
                        bot.updatePin(entry.link, entry)
                        bot.send(bot.channel, EntriesUtils.buildLink(index, entry))
                        UrlMgr.saveEntries(bot, false)
                    }
                } else if (cmd[0] == '=') { // L1:=<url>
                    val entry: EntryLink = UrlMgr.getEntry(index)
                    if (entry.login == login || isOp) {
                        val link = cmd.substring(1)
                        if (link.matches(UrlMgr.LINK_MATCH.toRegex())) {
                            val oldLink = entry.link
                            entry.link = link
                            bot.updatePin(oldLink, entry)
                            bot.send(bot.channel, EntriesUtils.buildLink(index, entry))
                            UrlMgr.saveEntries(bot, false)
                        }
                    } else {
                        bot.send(sender, "Please ask channel op to change this link for you.")
                    }
                } else if (cmd[0] == '?') { // L1:?<author>
                    if (isOp) {
                        if (cmd.length > 1) {
                            val entry: EntryLink = UrlMgr.getEntry(index)
                            entry.nick = cmd.substring(1)
                            bot.send(bot.channel, EntriesUtils.buildLink(index, entry))
                            UrlMgr.saveEntries(bot, false)
                        }
                    } else {
                        bot.send(sender, "Please ask a channel op to change the author of this link for you.")
                    }
                } else {
                    val entry: EntryLink = UrlMgr.getEntry(index)
                    val cindex = entry.addComment(cmd, sender)
                    val comment = entry.getComment(cindex)
                    bot.send(sender, EntriesUtils.buildComment(index, cindex, comment))
                    UrlMgr.saveEntries(bot, false)
                }
            }
        }
    }

    override fun matches(message: String): Boolean {
        return message.matches("${Constants.LINK_CMD}[0-9]+:.*".toRegex())
    }
}
