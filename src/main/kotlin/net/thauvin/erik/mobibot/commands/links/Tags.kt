/*
 * Tags.kt
 *
 * Copyright 2004-2023 Erik C. Thauvin (erik@thauvin.net)
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
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.isChannelOp
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.entries.EntriesUtils
import net.thauvin.erik.mobibot.entries.EntryLink
import org.pircbotx.hooks.types.GenericMessageEvent

class Tags : AbstractCommand() {
    override val name = COMMAND
    override val help = listOf(
            "To categorize or tag a URL, use its label and a ${Constants.TAG_CMD}:",
            helpFormat("${Constants.LINK_CMD}1${Constants.TAG_CMD}:<+tag|-tag> [...]")
    )
    override val isOpOnly = false
    override val isPublic = true
    override val isVisible = true

    companion object {
        const val COMMAND = "tags"
    }

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        val cmds = args.substring(1).split("${Constants.TAG_CMD}:", limit = 2)
        val index = cmds[0].toInt() - 1

        if (index < LinksManager.entries.links.size && LinksManager.isUpToDate(event)) {
            val cmd = cmds[1].trim()
            val entry: EntryLink = LinksManager.entries.links[index]
            if (cmd.isNotEmpty()) {
                if (entry.login == event.user.login || event.isChannelOp(channel)) {
                    entry.setTags(cmd)
                    LinksManager.pinboard.updatePin(event.bot().serverHostname, entry.link, entry)
                    event.sendMessage(EntriesUtils.printTags(index, entry))
                    LinksManager.entries.save()
                } else {
                    event.sendMessage("Please ask a channel op to change the tags for you.")
                }
            } else {
                if (entry.tags.isNotEmpty()) {
                    event.sendMessage(EntriesUtils.printTags(index, entry))
                } else {
                    event.sendMessage("The entry has no tags. Why don't add some?")
                }
            }
        }
    }

    override fun matches(message: String): Boolean {
        return message.matches("^${Constants.LINK_CMD}\\d+${Constants.TAG_CMD}:.*".toRegex())
    }
}
