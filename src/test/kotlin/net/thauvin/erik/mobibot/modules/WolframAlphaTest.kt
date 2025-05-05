/*
 * WolframAlphaTest.kt
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
import assertk.assertions.contains
import assertk.assertions.hasMessage
import assertk.assertions.isInstanceOf
import net.thauvin.erik.mobibot.DisableOnCi
import net.thauvin.erik.mobibot.ExceptionSanitizer.sanitize
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.modules.WolframAlpha.Companion.queryWolfram
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test


class WolframAlphaTest : LocalProperties() {
    companion object {
        @JvmStatic
        fun queries(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("SFO to SEA", "", "miles"),
                Arguments.of("SFO to LAS", WolframAlpha.IMPERIAL, "miles"),
                Arguments.of("SFO to LAX", WolframAlpha.METRIC, "kilometers")
            )
        }
    }

    @Nested
    @DisplayName("App ID Tests")
    inner class AppIdTests {
        @Test
        fun emptyAppId() {
            assertFailure { queryWolfram("1 gallon to liter", appId = "") }
                .isInstanceOf(ModuleException::class.java)
        }

        @Test
        fun invalidAppId() {
            assertFailure { queryWolfram("1 gallon to liter", appId = "DEMO") }
                .isInstanceOf(ModuleException::class.java)
                .hasMessage("Error 1: Invalid appid")
        }
    }

    @ParameterizedTest
    @MethodSource("queries")
    @DisableOnCi
    @Throws(ModuleException::class)
    fun queryWolfram(query: String, units: String, expected: String) {
        val apiKey = getProperty(WolframAlpha.APPID_KEY_PROP)
        try {
            if (units.isBlank()) {
                assertThat(queryWolfram(query, appId = apiKey), "queryWolfram($query)").contains(expected)
            } else {
                assertThat(queryWolfram(query, units, appId = apiKey), "queryWolfram($query, $units)")
                    .contains(expected)
            }
        } catch (e: ModuleException) {
            // Avoid displaying api key in CI logs
            if ("true" == System.getenv("CI")) {
                throw e.sanitize(apiKey)
            } else {
                throw e
            }
        }
    }
}
