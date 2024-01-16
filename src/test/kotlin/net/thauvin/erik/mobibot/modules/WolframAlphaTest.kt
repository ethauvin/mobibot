/*
 * WolframAlphaTest.kt
 *
 * Copyright 2004-2024 Erik C. Thauvin (erik@thauvin.net)
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
import kotlin.test.Test

class WolframAlphaTest : LocalProperties() {
    @Test
    fun testAppId() {
        assertFailure { queryWolfram("1 gallon to liter", appId = "DEMO") }
            .isInstanceOf(ModuleException::class.java)
            .hasMessage("Error 1: Invalid appid")

        assertFailure { queryWolfram("1 gallon to liter", appId = "") }
            .isInstanceOf(ModuleException::class.java)
    }

    @Test
    @DisableOnCi
    @Throws(ModuleException::class)
    fun queryWolframTest() {
        val apiKey = getProperty(WolframAlpha.APPID_KEY_PROP)
        try {
            var query = "SFO to SEA"
            assertThat(queryWolfram(query, appId = apiKey), "queryWolfram($query)").contains("miles")

            query = "SFO to LAX"
            assertThat(
                queryWolfram(query, WolframAlpha.METRIC, apiKey),
                "queryWolfram($query)"
            ).contains("kilometers")
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
