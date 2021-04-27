/*
 * AddLog.kt
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

import net.thauvin.erik.mobibot.Mobibot

import net.thauvin.erik.mobibot.commands.links.LinksMgr.Companion.history
import net.thauvin.erik.mobibot.entries.EntriesMgr
import java.io.File

class AddLog(bot: Mobibot) : AbstractCommand(bot) {
    override val name = "addlog"
    override val help = emptyList<String>()
    override val isOp = true
    override val isPublic = false
    override val isVisible = false

    override fun commandResponse(
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        if (isOp) {
            if (args.isNotBlank()) {
                // e.g: 2014-04-01
                val backlog = File("${bot.logsDir}$args${EntriesMgr.XML_EXT}")
                if (backlog.exists()) {
                    history.add(0, args)
                } else {
                    bot.send(sender, "The specified log could not be found.", isPrivate)
                    return
                }
            }
            bot.sendList(sender, history, 4, isPrivate, isIndent = true)
        }
    }
}
