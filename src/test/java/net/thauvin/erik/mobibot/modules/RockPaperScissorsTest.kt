/*
 * RockPaperScissorsTest.kt
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

import net.thauvin.erik.mobibot.modules.RockPaperScissors.Results
import net.thauvin.erik.mobibot.modules.RockPaperScissors.Shapes
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

class RockPaperScissorsTest {
    @Test(invocationCount = 5)
    fun testWinLoseOrDraw() {
        var play = RockPaperScissors.winLoseOrDraw(Shapes.SCISSORS)
        // println("SCISSORS vs ${play.first}: ${playsecond}")
        when (play.first) {
            Shapes.SCISSORS -> assertThat(play.second).`as`("SCISSORS vs ${play.first}").isEqualTo(Results.DRAW)
            Shapes.ROCK -> assertThat(play.second).`as`("SCISSORS vs ${play.first}").isEqualTo(Results.LOSE)
            else -> assertThat(play.second).`as`("SCISSORS vs ${play.first}").isEqualTo(Results.WIN)
        }

        play = RockPaperScissors.winLoseOrDraw(Shapes.ROCK)
        // println("ROCK vs ${play.first}: ${playsecond}")
        when (play.first) {
            Shapes.SCISSORS -> assertThat(play.second).`as`("ROCK vs ${play.first}").isEqualTo(Results.WIN)
            Shapes.ROCK -> assertThat(play.second).`as`("ROCK vs ${play.first}").isEqualTo(Results.DRAW)
            else -> assertThat(play.second).`as`("ROCK vs ${play.first}").isEqualTo(Results.LOSE)
        }

        play = RockPaperScissors.winLoseOrDraw(Shapes.PAPER)
        // println("PAPER vs ${play.first}: ${playsecond}")
        when (play.first) {
            Shapes.SCISSORS -> assertThat(play.second).`as`("PAPER vs ${play.first}").isEqualTo(Results.LOSE)
            Shapes.ROCK -> assertThat(play.second).`as`("PAPER vs ${play.first}").isEqualTo(Results.WIN)
            else -> assertThat(play.second).`as`("PAPER vs ${play.first}").isEqualTo(Results.DRAW)
        }
    }
}
