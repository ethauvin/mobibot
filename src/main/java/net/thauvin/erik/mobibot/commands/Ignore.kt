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
import java.util.*

class Ignore(defaultIgnore: String) : AbstractCommand() {
    private val me = "me"

    init {
        if (defaultIgnore.isNotBlank()) {
            ignored.addAll(defaultIgnore.split(", +?| +"))
        }
    }

    override val command = IGNORE_CMD
    override val help = listOf(
        Utils.bold("To ignore a link posted to the channel:"),
        Utils.helpIndent("https://www.foo.bar %s"),
        Utils.bold("To check your ignore status:"),
        Utils.helpIndent("%s: $command"),
        Utils.bold("To toggle your ignore status:"),
        Utils.helpIndent("%s: $command $me")
    )
    private val helpOp = listOf(
        Utils.bold("To ignore a link posted to the channel:"),
        Utils.helpIndent("https://www.foo.bar %s"),
        Utils.bold("To add/remove nicks from the ignored list:"),
        Utils.helpIndent("/msg %s $command <nick>|$me [<nick> ...]")
    )

    override val isOp = false
    override val isPublic = true
    override val isVisible = true

    companion object {
        const val IGNORE_CMD = "ignore"
        private val ignored = HashSet<String>()

        @JvmStatic
        fun isNotIgnored(nick: String): Boolean {
            return !ignored.contains(nick.toLowerCase())
        }
    }

    override fun commandResponse(
        bot: Mobibot,
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        if (!isOp) {
            val nick = sender.toLowerCase()
            val isMe = args.toLowerCase().startsWith(me)
            ignoreNick(bot, nick, isMe)
        } else {
            ignoreOp(bot, sender, args)
        }
    }

    override fun helpResponse(
        bot: Mobibot,
        command: String,
        sender: String,
        isOp: Boolean,
        isPrivate: Boolean
    ): Boolean {
        return if (isOp) {
            for (h in helpOp) {
                bot.send(sender, String.format(h, bot.nick))
            }
            true
        } else {
            super.helpResponse(bot, command, sender, isOp, isPrivate)
        }
    }

    private fun ignoreNick(bot: Mobibot, sender: String, isMe: Boolean) {
        if (isMe) {
            if (ignored.remove(sender)) {
                bot.send(sender, "You are no longer ignored.")
            } else {
                ignored.add(sender)
                bot.send(sender, "You are now ignored.")
            }
        } else {
            if (ignored.contains(sender)) {
                bot.send(sender, "You are currently ignored.")
            } else {
                bot.send(sender, "You are not currently ignored.")
            }
        }
    }

    private fun ignoreOp(bot: Mobibot, sender: String, args: String) {
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
            bot.send(sender, Utils.bold("The following nicks are ignored:"))
            bot.sendCommandsList(sender, ignored.toList(), false)
        } else {
            bot.send(sender, "No one is currently ${Utils.bold("ignored")}.")
        }
    }
}
