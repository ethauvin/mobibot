/*
 * Ignore.kt
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

package net.thauvin.erik.mobibot.commands

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.commands.links.LinksMgr
import java.util.*

class Ignore(bot: Mobibot) : AbstractCommand(bot) {
    private val me = "me"

    init {
        initProperties(IGNORE_PROP)
    }

    override val name = IGNORE_CMD
    override val help = listOf(
        "To ignore a link posted to the channel:",
        Utils.helpFormat("https://www.foo.bar %n"),
        "To check your ignore status:",
        Utils.helpFormat("%c $name"),
        "To toggle your ignore status:",
        Utils.helpFormat("%c $name $me")
    )
    private val helpOp = listOf(
        "To ignore a link posted to the channel:",
        Utils.helpFormat("https://www.foo.bar " + Utils.bold("%n"), false),
        "To add/remove nicks from the ignored list:",
        Utils.helpFormat("%c $name <nick> [<nick> ...]")
    )

    override val isOp = false
    override val isPublic = true
    override val isVisible = true

    companion object {
        const val IGNORE_CMD = "ignore"
        const val IGNORE_PROP = IGNORE_CMD
        private val ignored = TreeSet<String>()

        @JvmStatic
        fun isNotIgnored(nick: String): Boolean {
            return !ignored.contains(nick.toLowerCase())
        }
    }

    override fun commandResponse(
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        if (!isOp) {
            val nick = sender.toLowerCase()
            val isMe = args.toLowerCase().startsWith(me)
            ignoreNick(bot, nick, isMe, isPrivate)
        } else {
            ignoreOp(bot, sender, args, isPrivate)
        }
    }

    override fun helpResponse(
        command: String,
        sender: String,
        isOp: Boolean,
        isPrivate: Boolean
    ): Boolean {
        return if (isOp) {
            for (h in helpOp) {
                bot.send(sender, Utils.buildCmdSyntax(h, bot.nick, isPrivate), isPrivate)
            }
            true
        } else {
            super.helpResponse(command, sender, isOp, isPrivate)
        }
    }

    private fun ignoreNick(bot: Mobibot, sender: String, isMe: Boolean, isPrivate: Boolean) {
        if (isMe) {
            if (ignored.remove(sender)) {
                bot.send(sender, "You are no longer ignored.", isPrivate)
            } else {
                ignored.add(sender)
                bot.send(sender, "You are now ignored.", isPrivate)
            }
        } else {
            if (ignored.contains(sender)) {
                bot.send(sender, "You are currently ignored.", isPrivate)
            } else {
                bot.send(sender, "You are not currently ignored.", isPrivate)
            }
        }
    }

    private fun ignoreOp(bot: Mobibot, sender: String, args: String, isPrivate: Boolean) {
        if (args.isNotEmpty()) {
            val nicks = args.toLowerCase().split(" ")
            for (nick in nicks) {
                val ignore = if (me == nick) {
                    nick.toLowerCase()
                } else {
                    nick
                }
                if (!ignored.remove(ignore)) {
                    ignored.add(ignore)
                }
            }
        }

        if (ignored.size > 0) {
            bot.send(sender, "The following nicks are ignored:", isPrivate)
            bot.sendList(sender, ignored.toList(), 8, isPrivate, true)
        } else {
            bot.send(sender, "No one is currently ${Utils.bold("ignored")}.", isPrivate)
        }
    }

    override fun setProperty(key: String, value: String) {
        super.setProperty(key, value)
        if (IGNORE_PROP == key) {
            ignored.addAll(value.split(LinksMgr.LINK_MATCH.toRegex()))
        }
    }

}
