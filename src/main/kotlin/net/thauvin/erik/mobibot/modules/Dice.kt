/*
 * Dice.kt
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
package net.thauvin.erik.mobibot.modules

import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpFormat
import org.pircbotx.hooks.types.GenericMessageEvent

/**
 * The Dice module.
 */
class Dice : AbstractModule() {
    override val name = "Dice"
    private val sides = 6

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        val botRoll = roll()
        val roll = roll()
        val botTotal = botRoll.first + botRoll.second
        val total = roll.first + roll.second
        with(event.bot()) {
            event.respond(
                "you rolled ${roll.first.bold()} and ${roll.second.bold()} for a total of ${total.bold()}"
            )
            sendIRC().action(
                channel,
                "rolled ${botRoll.first.bold()} and ${botRoll.second.bold()} for a total of ${botTotal.bold()}"
            )
            when (winLoseOrTie(botTotal, total)) {
                Result.WIN -> sendIRC().action(channel, "wins.")
                Result.LOSE -> sendIRC().action(channel, "lost.")
                else -> sendIRC().action(channel, "tied.")
            }
        }
    }

    enum class Result {
        WIN, LOSE, TIE
    }

    private fun roll(): Pair<Int, Int> {
        return (1..6).random() to (1..6).random()
    }

    companion object {
        // Dice command
        private const val DICE_CMD = "dice"

        @JvmStatic
        fun winLoseOrTie(bot: Int, player: Int): Result {
            return when {
                bot > player -> {
                    Result.WIN
                }
                bot < player -> {
                    Result.LOSE
                }
                else -> {
                    Result.TIE
                }
            }
        }
    }

    init {
        commands.add(DICE_CMD)
        help.add("To roll the dice:")
        help.add(helpFormat("%c $DICE_CMD"))
    }
}
