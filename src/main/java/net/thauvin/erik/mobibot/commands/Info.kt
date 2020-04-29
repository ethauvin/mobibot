/*
 * Info.kt
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
import net.thauvin.erik.mobibot.commands.links.UrlMgr
import java.lang.management.ManagementFactory

class Info(bot: Mobibot) : AbstractCommand(bot) {
    override val name = "info"
    override val help = listOf(
        "To view information about the bot:",
        Utils.helpIndent("%c $name")
    )
    override val isOp = false
    override val isPublic = true
    override val isVisible = true

    override fun commandResponse(
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        for (info in Mobibot.INFO) {
            bot.send(sender, info, isPrivate)
        }

        val info = StringBuilder("Uptime: ")

        with(info) {
            append(Utils.uptime(ManagementFactory.getRuntimeMXBean().uptime))
            append(" [Entries: ")
            append(UrlMgr.entriesCount)

            if (isOp) {
                if (bot.tell.isEnabled()) {
                    append(", Messages: ")
                    append(bot.tell.size())
                }
                if (bot.twitter.isAutoPost) {
                    append(", Twitter: ")
                    append(bot.twitter.entriesCount())
                }
            }

            append(", Recap: ")
            append(Recap.recapCount())
            append(']')
        }

        bot.send(sender, info.toString(), isPrivate)
    }
}
