/*
 * DiceTest.kt
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
import kotlin.test.Test

class DiceTest {
    @Test
    fun testRoll() {
        assertThat(Dice.roll(1, 1), "roll(1d1)").isEqualTo("\u00021\u0002")
        assertThat(Dice.roll(2, 1), "roll(2d1)")
            .isEqualTo("\u00021\u0002 + \u00021\u0002 = \u00022\u0002")
        assertThat(Dice.roll(5, 1), "roll(5d1)")
            .isEqualTo("\u00021\u0002 + \u00021\u0002 + \u00021\u0002 + \u00021\u0002 + \u00021\u0002 = \u00025\u0002")
        assertThat(Dice.roll(2, 6), "roll(2d6)")
            .matches("\u0002[1-6]\u0002 \\+ \u0002[1-6]\u0002 = \u0002[1-9][0-2]?\u0002".toRegex())
        assertThat(Dice.roll(3, 7), "roll(3d7)")
            .matches("\u0002[1-7]\u0002 \\+ \u0002[1-7]\u0002 \\+ \u0002[1-7]\u0002 = \u0002\\d{1,2}\u0002".toRegex())
    }
}
