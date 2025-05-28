/*
 * CalcTests.kt
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

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import net.objecthunter.exp4j.tokenizer.UnknownFunctionOrVariableException
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.modules.Calc.Companion.calculate
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.Mockito
import org.pircbotx.hooks.types.GenericMessageEvent
import kotlin.test.Test

class CalcTests {
    @Nested
    @DisplayName("Calculate Tests")
    inner class CalculateTests {
        @Test
        fun `Calculate basic addition`() {
            assertThat(calculate("1 + 1"), "calculate(1+1)").isEqualTo("1+1 = ${2.bold()}")
        }

        @Test
        fun `Calculate basic subtraction`() {
            assertThat(calculate("1 -3"), "calculate(1-3)").isEqualTo("1-3 = ${(-2).bold()}")
        }

        @Test
        fun `Calculate mathematical constants`() {
            assertThat(calculate("pi+π+e+φ"), "calculate(pi+π+e+φ)").isEqualTo("pi+π+e+φ = ${"10.62".bold()}")
        }

        @Test
        fun `Calculate scientific notations`() {
            assertThat(calculate("3e2 - 3e1"), "calculate(3e2-3e1 )").isEqualTo("3e2-3e1 = ${"270".bold()}")
        }

        @Test
        fun `Calculate trigonometric functions`() {
            assertThat(calculate("3*sin(10)-cos(2)"), "calculate(3*sin(10)-cos(2)")
                .isEqualTo("3*sin(10)-cos(2) = ${"-1.22".bold()}")
        }

        @Test
        fun `Empty calculation should throw exception`() {
            assertFailure { calculate(" ") }.isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `Invalid calculation should throw exception`() {
            assertFailure { calculate("a + b = c") }.isInstanceOf(UnknownFunctionOrVariableException::class.java)
        }
    }

    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun `Basic calculation`() {
            val calc = Calc()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            calc.commandResponse("channel", "calc", "1 + 1 * 2", event)
            Mockito.verify(event, Mockito.times(1)).respond("1+1*2 = ${"3".bold()}")
        }

        @Test
        fun `Invalid calculation`() {
            val calc = Calc()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            calc.commandResponse("channel", "calc", "two + two", event)
            Mockito.verify(event, Mockito.times(1)).respond("No idea. This is the kind of math I don't get.")
        }
    }
}
