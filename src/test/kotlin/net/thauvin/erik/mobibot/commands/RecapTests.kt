/*
 * RecapTests.kt
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

package net.thauvin.erik.mobibot.commands

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.matches
import assertk.assertions.prop
import assertk.assertions.size
import kotlin.test.Test

class RecapTests {
    @Test
    fun storeRecap() {
        for (i in 1..20) {
            Recap.storeRecap("sender$i", "test $i", false)
        }
        assertThat(Recap.recaps, "Recap.recaps").all {
            size().isEqualTo(Recap.MAX_RECAPS)
            prop(MutableList<String>::first)
                .matches("[1-2]\\d{3}-[01]\\d-[0-3]\\d [0-2]\\d:[0-6]\\d - sender11: test 11".toRegex())
            prop(MutableList<String>::last)
                .matches("[1-2]\\d{3}-[01]\\d-[0-3]\\d [0-2]\\d:[0-6]\\d - sender20: test 20".toRegex())
        }

        Recap.storeRecap("sender", "test action", true)
        assertThat(Recap.recaps.last(), "Recap.recaps.last()")
            .matches("[1-2]\\d{3}-[01]\\d-[0-3]\\d [0-2]\\d:[0-6]\\d - sender test action".toRegex())
    }
}
