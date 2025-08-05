/*
 * InfoTests.kt
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

import assertk.assertThat
import assertk.assertions.isEqualTo
import net.thauvin.erik.mobibot.commands.Info.Companion.toUptime
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test

class InfoTests {
    @Nested
    @DisplayName("Uptime Tests")
    inner class UptimeTests {
        @ParameterizedTest(name = "{index}: {0}L = {1}")
        @CsvSource(
            value = [
                "1000,1 second",
                "60000,1 minute",
                "3600000,1 hour",
                "86400000,1 day"
            ]
        )
        fun `1 day, 1 hour, 1 minutes, 1 second`(ms: Long, expected: String) {
            assertThat(ms.toUptime()).isEqualTo(expected)
        }

        @ParameterizedTest(name = "{index}: {0}L = {1}")
        @CsvSource(
            value = [
                "31536000000,1 year",
                "2592000000,1 month",
                "604800000,1 week"
            ]
        )
        fun `1 year, 1 month, 1 week`(ms: Long, expecting: String) {
            assertThat(ms.toUptime()).isEqualTo(expecting)
        }

        @Test
        fun `Hours and Minutes`() {
            assertThat(24300000L.toUptime()).isEqualTo("6 hours 45 minutes")
        }

        @Test
        fun `Days, Hours and Minutes`() {
            assertThat(110700000L.toUptime(), "upTime(days hours minutes)")
                .isEqualTo("1 day 6 hours 45 minutes")
        }

        @Test
        fun `Weeks, Days, Hours and Minutes`() {
            assertThat(1320300000L.toUptime()).isEqualTo("2 weeks 1 day 6 hours 45 minutes")
        }


        @Test
        fun `3 Days`() {
            assertThat(259200000L.toUptime(), "upTime(3 days)").isEqualTo("3 days")
        }

        @Test
        fun `2 Hours`() {
            assertThat(7200000L.toUptime(), "upTime(2 hours)").isEqualTo("2 hours")
        }

        @Test
        fun `45 Minutes`() {
            assertThat(2700000L.toUptime(), "upTime(45 minutes)").isEqualTo("45 minutes")
        }


        @Test
        fun `59 Seconds`() {
            assertThat(59000L.toUptime(), "upTime(59 seconds)").isEqualTo("59 seconds")
        }

        @Test
        fun `0 Seconds`() {
            assertThat(0L.toUptime(), "upTime(0 second)").isEqualTo("0 seconds")
        }
    }
}
