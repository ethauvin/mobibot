/*
 * Ignore.kt
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

package net.thauvin.erik.mobibot.commands

import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.buildCmdSyntax
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.isChannelOp
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.commands.links.LinksMgr
import org.pircbotx.hooks.types.GenericMessageEvent

class Ignore : AbstractCommand() {
    private val me = "me"

    init {
        initProperties(IGNORE_PROP)
    }

    override val name = IGNORE_CMD
    override val help = listOf(
        "To ignore a link posted to the channel:",
        helpFormat("https://www.foo.bar %n"),
        "To check your ignore status:",
        helpFormat("%c $name"),
        "To toggle your ignore status:",
        helpFormat("%c $name $me")
    )
    private val helpOp = help.plus(
        arrayOf("To add/remove nicks from the ignored list:", helpFormat("%c $name <nick> [<nick> ...]"))
    )

    override val isOpOnly = false
    override val isPublic = true
    override val isVisible = true

    companion object {
        const val IGNORE_CMD = "ignore"
        const val IGNORE_PROP = IGNORE_CMD
        private val ignored = mutableSetOf<String>()

        @JvmStatic
        fun isNotIgnored(nick: String): Boolean {
            return !ignored.contains(nick.lowercase())
        }
    }

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        val isMe = args.trim().equals(me, true)
        if (isMe || !isChannelOp(channel, event)) {
            val nick = event.user.nick.lowercase()
            ignoreNick(nick, isMe, event)
        } else {
            ignoreOp(args, event)
        }
    }

    override fun helpResponse(channel: String, topic: String, event: GenericMessageEvent): Boolean {
        return if (isChannelOp(channel, event)) {
            for (h in helpOp) {
                event.sendMessage(buildCmdSyntax(h, event.bot().nick, true))
            }
            true
        } else {
            super.helpResponse(channel, topic, event)
        }
    }

    private fun ignoreNick(sender: String, isMe: Boolean, event: GenericMessageEvent) {
        if (isMe) {
            if (ignored.remove(sender)) {
                event.sendMessage("You are no longer ignored.")
            } else {
                ignored.add(sender)
                event.sendMessage("You are now ignored.")
            }
        } else {
            if (ignored.contains(sender)) {
                event.sendMessage("You are currently ignored.")
            } else {
                event.sendMessage("You are not currently ignored.")
            }
        }
    }

    private fun ignoreOp(args: String, event: GenericMessageEvent) {
        if (args.isNotEmpty()) {
            val nicks = args.lowercase().split(" ")
            for (nick in nicks) {
                val ignore = if (me == nick) {
                    nick.lowercase()
                } else {
                    nick
                }
                if (!ignored.remove(ignore)) {
                    ignored.add(ignore)
                }
            }
        }

        if (ignored.size > 0) {
            event.sendMessage("The following nicks are ignored:")
            event.sendList(ignored.sorted(), 8, isIndent = true)
        } else {
            event.sendMessage("No one is currently ${bold("ignored")}.")
        }
    }

    override fun setProperty(key: String, value: String) {
        super.setProperty(key, value)
        if (IGNORE_PROP == key) {
            ignored.addAll(value.split(LinksMgr.TAG_MATCH.toRegex()))
        }
    }

}
