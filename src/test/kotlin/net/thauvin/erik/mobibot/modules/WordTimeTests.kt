/*
 * WordTimeTests.kt
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
package net.thauvin.erik.mobibot.modules

import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.matches
import assertk.assertions.startsWith
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.modules.WorldTime.Companion.BEATS_KEYWORD
import net.thauvin.erik.mobibot.modules.WorldTime.Companion.COUNTRIES_MAP
import net.thauvin.erik.mobibot.modules.WorldTime.Companion.time
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.pircbotx.Colors
import org.pircbotx.hooks.types.GenericMessageEvent
import java.time.ZoneId
import kotlin.test.Test

class WordTimeTests {
    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun `Time in Tokyo`() {
            val worldTime = WorldTime()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            worldTime.commandResponse("channel", "time", "jp", event)

            Mockito.verify(event, Mockito.atLeastOnce()).respond(captor.capture())
            assertThat(captor.value)
                .matches("The time is \u0002([01]\\d|2[0-3]):([0-5]\\d)\u0002 on .+ in \u0002Tokyo\u0002".toRegex())
        }
    }

    @Nested
    @DisplayName("Time Tests")
    inner class TimeTests {
        @Test
        fun `Check default time formatting`() {
            assertThat(time(), "time()").matches(
                ("The time is ${Colors.BOLD}\\d{1,2}:\\d{2}${Colors.BOLD} " +
                        "on ${Colors.BOLD}\\w+, \\d{1,2} \\w+ \\d{4}${Colors.BOLD} " +
                        "in ${Colors.BOLD}Los Angeles${Colors.BOLD}").toRegex()
            )
        }

        @Test
        fun `Time in Los Angeles`() {
            assertThat(time(""), "time()").endsWith("Los Angeles".bold())
        }

        @Test
        fun `Pacific Standard Time`() {
            assertThat(time("PST"), "time(PST)").endsWith("Los Angeles".bold())
        }

        @Test
        fun `Time in Great Britain`() {
            assertThat(time("GB"), "time(GB)").endsWith("London".bold())
        }

        @Test
        fun `Time in France`() {
            assertThat(time("FR"), "time(FR)").endsWith("Paris".bold())
        }

        @Test
        fun `Time in Unknown Country`() {
            assertThat(time("BLAH"), "time(BLAH)").startsWith("Unsupported")
        }

        @Test
        fun `Swatch Internet Time`() {
            assertThat(time("BEAT"), "time($BEATS_KEYWORD)").matches("[\\w ]+ .?@\\d{3}+.? .beats".toRegex())
        }
    }

    @Test
    fun `Check that all countries have a valid ZoneId`() {
        COUNTRIES_MAP.filter { it.value != BEATS_KEYWORD }.forEach {
            assertThat(ZoneId.of(it.value), "ZoneId(${it.value})")
        }
    }
}
