/*
 * Recap.kt
 *
 * Copyright (c) 2004-2022, Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.Utils.toUtcDateTime
import org.pircbotx.hooks.types.GenericMessageEvent
import java.time.Clock
import java.time.LocalDateTime

class Recap : AbstractCommand() {
    override val name = "recap"
    override val help = listOf(
        "To list the last 10 public channel messages:",
        helpFormat("%c $name")
    )
    override val isOpOnly = false
    override val isPublic = true
    override val isVisible = true

    companion object {
        const val MAX_RECAPS = 10

        @JvmField
        val recaps = mutableListOf<String>()

        /**
         * Stores the last 10 public messages and actions.
         */
        @JvmStatic
        fun storeRecap(sender: String, message: String, isAction: Boolean) {
            recaps.add(
                LocalDateTime.now(Clock.systemUTC()).toUtcDateTime()
                        + " - $sender" + (if (isAction) " " else ": ") + message
            )
            if (recaps.size > MAX_RECAPS) {
                recaps.removeFirst()
            }
        }
    }

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        if (recaps.isNotEmpty()) {
            for (r in recaps) {
                event.sendMessage(r)
            }
        } else {
            event.sendMessage("Sorry, nothing to recap.")
        }
    }
}
