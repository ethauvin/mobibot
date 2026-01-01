/*
 * RecapTests.kt
 *
 * Copyright (c) 2004-2026 Erik C. Thauvin (erik@thauvin.net)
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
import assertk.assertions.*
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.Test

class RecapTests {
    @Test
    fun storeRecap() {
        for (i in 1..20) {
            Recap.storeRecap("sender$i", "test $i", false)
        }
        assertThat(Recap.recaps, "Recap.recaps").all {
            hasSize(Recap.MAX_RECAPS)
            prop(MutableList<String>::first)
                .matches("[1-2]\\d{3}-[01]\\d-[0-3]\\d [0-2]\\d:[0-6]\\d - sender11: test 11".toRegex())
            prop(MutableList<String>::last)
                .matches("[1-2]\\d{3}-[01]\\d-[0-3]\\d [0-2]\\d:[0-6]\\d - sender20: test 20".toRegex())
        }

        Recap.storeRecap("sender", "test action", true)
        assertThat(Recap.recaps.last(), "Recap.recaps.last()")
            .matches("[1-2]\\d{3}-[01]\\d-[0-3]\\d [0-2]\\d:[0-6]\\d - sender test action".toRegex())
    }

    @Test
    fun storeRecapAddsToRecapsList() {
        Recap.recaps.clear()
        val sender = "User1"
        val message = "This is a test message"
        val isAction = false

        Recap.storeRecap(sender, message, isAction)

        assertThat(Recap.recaps).hasSize(1)
        assertThat(Recap.recaps[0]).contains("$sender: $message")
    }

    @Test
    fun storeRecapAddsTimestamp() {
        Recap.recaps.clear()
        val sender = "User1"
        val message = "Another test message"
        val isAction = false

        Recap.storeRecap(sender, message, isAction)

        val nowUtc = LocalDateTime.now(Clock.systemUTC())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        assertThat(Recap.recaps[0]).startsWith(nowUtc.take(10)) // Check date portion of timestamp
    }

    @Test
    fun storeRecapHandlesActions() {
        Recap.recaps.clear()
        val sender = "User2"
        val message = "performs an action"
        val isAction = true

        Recap.storeRecap(sender, message, isAction)

        assertThat(Recap.recaps[0]).contains("$sender $message")
    }

    @Test
    fun storeRecapMaintainsMaxSize() {
        Recap.recaps.clear()
        for (i in 1..Recap.MAX_RECAPS + 5) {
            Recap.storeRecap("User$i", "Message $i", false)
        }

        assertThat(Recap.recaps).hasSize(Recap.MAX_RECAPS)
        assertThat(Recap.recaps[0]).contains("User6: Message 6") // Oldest message that remains
    }

    @Test
    fun storeRecapRemovesOldest() {
        Recap.recaps.clear()

        for (i in 1..Recap.MAX_RECAPS) {
            Recap.storeRecap("User$i", "Message $i", false)
        }
        Recap.storeRecap("NewUser", "NewMessage", false)

        assertThat(Recap.recaps).all {
            hasSize(Recap.MAX_RECAPS)
            doesNotContain { "User1: Message 1" } // Oldest removed
        }
        assertThat(Recap.recaps.last()).contains("NewUser: NewMessage") // Newest added
    }
}
