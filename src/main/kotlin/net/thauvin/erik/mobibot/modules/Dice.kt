/*
 * Dice.kt
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

/**
 * Rolls dice.
 */
class Dice : AbstractModule() {
    override val name = "Dice"

    companion object {
        // Dice command
        private const val DICE_CMD = "dice"

        @JvmStatic
        fun roll(dice: Int, sides: Int): String {
            return buildString {
                var total = 0

                repeat(dice) {
                    val roll = (1..sides).random()
                    total += roll

                    if (isNotEmpty()) {
                        append(" + ")
                    }
                    append(roll.bold())
                }

                if (dice != 1) {
                    append(" = ${total.bold()}")
                }
            }
        }
    }

    init {
        addCommand(DICE_CMD)
        addHelp(
            "To roll 2 dice with 6 sides:",
            helpFormat("%c $DICE_CMD [2d6]")
        )
    }

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        val arg = if (args.isBlank()) "2d6" else args.trim()
        val match = Regex("^([1-9]|[12]\\d|3[0-2])[dD]([1-9]|[12]\\d|3[0-2])$").find(arg)
        if (match != null) {
            val (dice, sides) = match.destructured
            event.respond("you rolled " + roll(dice.toInt(), sides.toInt()))
        } else {
            helpResponse(event)
        }
    }
}
