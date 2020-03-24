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
import kotlin.random.Random


/**
 * Simple module example in Kotlin.
 */
class RockPaperScissors : AbstractModule() {
    init {
        with(commands) {
            add(Shapes.ROCK.value)
            add(Shapes.SCISSORS.value)
            add(Shapes.PAPER.value)
        }
    }

    enum class Shapes(val value: String) {
        ROCK("rock"), PAPER("paper"), SCISSORS("scissors")
    }

    enum class Results {
        WIN, LOSE, DRAW
    }


    companion object {
        /**
         * Returns the the randomly picked shape, result and action (cuts, crushes, covers, vs.)
         */
        fun winLoseOrDraw(hand: Shapes): Triple<Shapes, Results, String> {
            val botHand = Shapes.values()[Random.nextInt(0, Shapes.values().size)]
            val result: Results
            val action: String
            if (botHand == hand) {
                result = Results.DRAW
                action = "vs."
            } else {
                val shapes = arrayOf(hand, botHand)
                if (shapes.contains(Shapes.ROCK) && shapes.contains(Shapes.SCISSORS)) {
                    action = "crushes"
                    result = if (hand == Shapes.ROCK) {
                        Results.WIN
                    } else {
                        Results.LOSE
                    }
                } else if (shapes.contains(Shapes.PAPER) && shapes.contains(Shapes.ROCK)) {
                    action = "covers"
                    result = if (hand == Shapes.PAPER) {
                        Results.WIN
                    } else {
                        Results.LOSE
                    }
                } else { // SCISSORS vs. PAPER
                    action = "cuts"
                    result = if (hand == Shapes.SCISSORS) {
                        Results.WIN
                    } else {
                        Results.LOSE
                    }
                }
            }
            return Triple(botHand, result, action)
        }
    }

    override fun commandResponse(bot: Mobibot?, sender: String?, cmd: String?, args: String?, isPrivate: Boolean) {
        val result = winLoseOrDraw(Shapes.valueOf(cmd!!.toUpperCase()))
        when (result.second) {
            Results.WIN -> bot!!.action(
                "${Utils.green(cmd)} ${Utils.bold(result.third)} ${Utils.red(result.first.value)} ~ You win ~"
            )
            Results.LOSE -> bot!!.action(
                "${Utils.green(result.first.value)} ${Utils.bold(result.third)} ${Utils.red(cmd)} ~ You lose ~"
            )
            else -> bot!!.action(
                "${Utils.green(cmd)} ${Utils.bold(result.third)} ${Utils.green(result.first.value)}"
                    + " ~ The game is tied ~"
            )
        }
    }

    override fun helpResponse(bot: Mobibot?, sender: String?, args: String?, isPrivate: Boolean) {
        bot!!.send(sender, "To play Rock Paper Scissors:")
        bot.send(
            sender,
            bot.helpIndent("${bot.nick}: ${Shapes.ROCK.value} or ${Shapes.PAPER.value} or ${Shapes.SCISSORS.value}")
        )
    }
}
