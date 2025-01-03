/*
 * War.kt
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot.modules

import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.helpFormat
import org.pircbotx.hooks.types.GenericMessageEvent
import java.security.SecureRandom

/**
 * The War module.
 *
 * @author [Erik C. Thauvin](https://erik.thauvin.net/)
 * @since 1.0
 */
class War : AbstractModule() {
    override val name = "War"

    override fun commandResponse(
        channel: String, cmd: String, args: String,
        event: GenericMessageEvent
    ) {
        var i: Int
        var y: Int
        do {
            i = RANDOM.nextInt(HEARTS.size)
            y = RANDOM.nextInt(HEARTS.size)
            val result: String = if (i < y) {
                "win".bold()
            } else if (i > y) {
                "lose".bold()
            } else {
                "tie".bold() + ". This means " + "WAR".bold()
            }
            event.respond(
                DECK[RANDOM.nextInt(DECK.size)][i] + "  " + DECK[RANDOM.nextInt(DECK.size)][y] +
                        "  » You " + result + '!'
            )
        } while (i == y)
    }

    companion object {
        private val CLUBS = arrayOf("🃑", "🃞", "🃝", "🃛", "🃚", "🃙", "🃘", "🃗", "🃖", "🃕", "🃔", "🃓", "🃒")
        private val DIAMONDS = arrayOf("🃁", "🃎", "🃍", "🃋", "🃊", "🃉", "🃈", "🃇", "🃆", "🃅", "🃄", "🃃", "🃂")
        private val HEARTS = arrayOf("🂱", "🂾", "🂽", "🂻", "🂺", "🂹", "🂸", "🂷", "🂶", "🂵", "🂴", "🂳", "🂲")

        // Random
        private val RANDOM = SecureRandom()
        private val SPADES = arrayOf("🂡", "🂮", "🂭", "🂫", "🂪", "🂩", "🂨", "🂧", "🂦", "🂥", "🂤", "🂣", "🂢")
        private val DECK = arrayOf(HEARTS, SPADES, DIAMONDS, CLUBS)

        // War command
        private const val WAR_CMD = "war"
    }

    init {
        commands.add(WAR_CMD)
        help.add("To play war:")
        help.add(helpFormat("%c $WAR_CMD"))
    }
}
