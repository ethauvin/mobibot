/*
 * Links.kt
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

package net.thauvin.erik.mobibot.commands.links

import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.entries.EntriesUtils
import net.thauvin.erik.mobibot.entries.EntryLink

class Tags(bot: Mobibot) : AbstractCommand(bot) {
    override val name = COMMAND
    override val help = listOf(
        "To categorize or tag a URL, use its label and a T:",
        helpFormat("${Constants.LINK_CMD}1T:<+tag|-tag> [...]")
    )
    override val isOp = false
    override val isPublic = true
    override val isVisible = true

    companion object {
        const val COMMAND = "tags"
    }

    override fun commandResponse(
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        val cmds = args.substring(1).split("T:", limit = 2)
        val index = cmds[0].toInt() - 1

        if (index < LinksMgr.entries.size) {
            val cmd = cmds[1].trim()
            val entry: EntryLink = LinksMgr.entries[index]
            if (cmd.isNotEmpty()) {
                if (entry.login == login || isOp) {
                    entry.setTags(cmd)
                    bot.updatePin(entry.link, entry)
                    bot.send(EntriesUtils.buildTags(index, entry))
                    LinksMgr.saveEntries(bot, false)
                } else {
                    bot.send(sender, "Please ask a channel op to change the tags for you.", isPrivate)
                }
            } else {
                if (entry.tags.isNotEmpty()) {
                    bot.send(EntriesUtils.buildTags(index, entry))
                } else {
                    bot.send(sender, "The entry has no tags. Why don't add some?", isPrivate)
                }
            }
        }
    }

    override fun matches(message: String): Boolean {
        return message.matches("^${Constants.LINK_CMD}[0-9]+T:.*".toRegex())
    }
}
