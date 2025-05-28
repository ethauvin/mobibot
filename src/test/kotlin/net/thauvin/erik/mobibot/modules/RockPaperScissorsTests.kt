/*
 * RockPaperScissorsTests.kt
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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.matches
import net.thauvin.erik.mobibot.modules.RockPaperScissors.Companion.winLoseOrDraw
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.pircbotx.hooks.types.GenericMessageEvent
import kotlin.test.Test

class RockPaperScissorsTests {
    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @RepeatedTest(3)
        fun `Play Rock Paper Scissors`() {
            val rockPaperScissors = RockPaperScissors()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            rockPaperScissors.commandResponse("channel", "rock", "", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value)
                .matches(
                    ".* (vs\\.|crushes|covers|cuts) (ROCK|PAPER|SCISSORS) » You \u0002(tie|win|lose)\u0002.".toRegex()
                )
        }
    }

    @Nested
    @DisplayName("Win, Lose or Draw Tests")
    inner class WinLoseOrDrawTests {
        @Test
        fun `Paper versus Paper draws`() {
            assertThat(winLoseOrDraw("paper", "paper")).isEqualTo("draw")
        }

        @Test
        fun `Paper versus Rock wins`() {
            assertThat(winLoseOrDraw("paper", "rock")).isEqualTo("win")
        }

        @Test
        fun `Paper versus Scissors loses`() {
            assertThat(winLoseOrDraw("paper", "scissors")).isEqualTo("lose")
        }

        @Test
        fun `Rock versus Paper loses`() {
            assertThat(winLoseOrDraw("rock", "paper")).isEqualTo("lose")
        }

        @Test
        fun `Rock versus Rock draws`() {
            assertThat(winLoseOrDraw("rock", "rock")).isEqualTo("draw")
        }

        @Test
        fun `Rock versus Scissors wins`() {
            assertThat(winLoseOrDraw("rock", "scissors")).isEqualTo("win")
        }

        @Test
        fun `Scissors versus Paper wins`() {
            assertThat(winLoseOrDraw("scissors", "paper")).isEqualTo("win")
        }

        @Test
        fun `Scissors versus Rock loses`() {
            assertThat(winLoseOrDraw("scissors", "rock")).isEqualTo("lose")
        }

        @Test
        fun `Scissors versus Scissors draws`() {
            assertThat(winLoseOrDraw("scissors", "scissors")).isEqualTo("draw")
        }
    }
}
