/*
 * CalcTest.kt
 *
 * Copyright (c) 2004-2019, Erik C. Thauvin (erik@thauvin.net)
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

import net.objecthunter.exp4j.tokenizer.UnknownFunctionOrVariableException
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.modules.Calc.Companion.calculate
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.testng.annotations.Test

/**
 * The `CalcTest` class.
 */
class CalcTest {
    @Test
    fun testCalculate() {
        assertThat(calculate("1 + 1")).describedAs("calculate(1+1)").isEqualTo("1+1 = %s", Utils.bold(2))
        assertThat(calculate("1 -3")).describedAs("calculate(1 -3)").isEqualTo("1-3 = %s", Utils.bold(-2))
        assertThat(calculate("pi+π+e+φ")).describedAs("calculate(pi+π+e+φ)")
            .isEqualTo("pi+π+e+φ = %s", Utils.bold("10.62"))
        assertThatThrownBy { calculate("one + one") }.describedAs("calculate(one+one)")
            .isInstanceOf(UnknownFunctionOrVariableException::class.java)
    }
}
