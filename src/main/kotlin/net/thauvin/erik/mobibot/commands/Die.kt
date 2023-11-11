/*
 * Die.kt
 *
 * Copyright 2021-2023 Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.isChannelOp
import org.pircbotx.hooks.types.GenericMessageEvent

class Die : AbstractCommand() {
    override val name = "die"
    override val help = emptyList<String>()
    override val isOpOnly = true
    override val isPublic = false
    override val isVisible = false

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        with(event.bot()) {
            if (event.isChannelOp(channel) && (properties[DIE_PROP].isNullOrBlank() || args == properties[DIE_PROP])) {
                sendIRC().message(channel, "${event.user?.nick} has just signed my death sentence.")
                stopBotReconnect()
                sendIRC().quitServer("The Bot is Out There!")
            }
        }
    }

    companion object {
        const val DIE_PROP = "die"
    }

    init {
        initProperties(DIE_PROP)
    }
}
