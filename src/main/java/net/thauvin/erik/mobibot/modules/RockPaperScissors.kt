/*
 * RockPaperScissors.kt
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

package net.thauvin.erik.mobibot.modules

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.green
import net.thauvin.erik.mobibot.Utils.red
import kotlin.random.Random


/**
 * Simple module example in Kotlin.
 */
class RockPaperScissors : AbstractModule() {
    init {
        with(commands) {
            add(Hands.ROCK.name.toLowerCase())
            add(Hands.PAPER.name.toLowerCase())
            add(Hands.SCISSORS.name.toLowerCase())
        }

        with(help) {
            add("To play Rock Paper Scissors:")
            add(
                Utils.helpIndent(
                    "%c ${Hands.ROCK.name.toLowerCase()} | ${Hands.PAPER.name.toLowerCase()}"
                        + " | ${Hands.SCISSORS.name.toLowerCase()}"
                )
            )
        }
    }

    enum class Hands(val action: String) {
        ROCK("crushes") {
            override fun beats(hand: Hands): Boolean {
                return hand == SCISSORS
            }
        },
        PAPER("covers") {
            override fun beats(hand: Hands): Boolean {
                return hand == ROCK
            }
        },
        SCISSORS("cuts") {
            override fun beats(hand: Hands): Boolean {
                return hand == PAPER
            }
        };

        abstract fun beats(hand: Hands): Boolean
    }

    companion object {
        // For testing.
        fun winLoseOrDraw(player: String, bot: String): String {
            val hand = Hands.valueOf(player.toUpperCase())
            val botHand = Hands.valueOf(bot.toUpperCase())

            return when {
                hand == botHand -> "draw"
                hand.beats(botHand) -> "win"
                else -> "lose"
            }
        }
    }

    override fun commandResponse(bot: Mobibot, sender: String, cmd: String, args: String?, isPrivate: Boolean) {
        val hand = Hands.valueOf(cmd.toUpperCase())
        val botHand = Hands.values()[Random.nextInt(0, Hands.values().size)]
        when {
            hand == botHand -> {
                bot.action("${green(hand.name)} vs. ${green(botHand.name)} ~ The game is tied ~")
            }
            hand.beats(botHand) -> {
                bot.action(
                    "${green(hand.name)} ${bold(hand.action)} ${red(botHand.name)} ~ You win ~"
                )
            }
            else -> {
                bot.action(
                    "${green(botHand.name)} ${bold(botHand.action)} ${red(hand.name)} ~ You lose ~"
                )
            }
        }
    }
}
