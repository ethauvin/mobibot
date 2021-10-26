/*
 * StockQuoteTest.kt
 *
 * Copyright (c) 2004-2021, Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.Sanitize.sanitizeException
import net.thauvin.erik.mobibot.modules.StockQuote.Companion.getQuote
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.testng.annotations.Test

/**
 * The `StockQuoteTest` class.
 */
class StockQuoteTest : LocalProperties() {
    private fun buildMatch(label: String): String {
        return "${label}:[ ]+[0-9.]+".prependIndent()
    }

    @Test
    @Throws(ModuleException::class)
    fun testGetQuote() {
        val apiKey = getProperty(StockQuote.ALPHAVANTAGE_API_KEY_PROP)
        try {
            val messages = getQuote("apple inc", apiKey)
            assertThat(messages).describedAs("response not empty").isNotEmpty
            assertThat(messages[0].msg).describedAs("same stock symbol").matches("Symbol: AAPL .*")
            assertThat(messages[1].msg).describedAs("price label").matches(buildMatch("Price"))
            assertThat(messages[2].msg).describedAs("previous label").matches(buildMatch("Previous"))
            assertThat(messages[3].msg).describedAs("open label").matches(buildMatch("Open"))
            try {
                getQuote("blahfoo", apiKey)
            } catch (e: ModuleException) {
                assertThat(e.message).describedAs("invalid symbol").containsIgnoringCase(StockQuote.INVALID_SYMBOL)
            }
            assertThatThrownBy { getQuote("test", "") }.describedAs("no API key")
                .isInstanceOf(ModuleException::class.java).hasNoCause()
            assertThatThrownBy { getQuote("", "apikey") }.describedAs("no symbol")
                .isInstanceOf(ModuleException::class.java).hasNoCause()
        } catch (e: ModuleException) {
            // Avoid displaying api keys in CI logs
            if ("true" == System.getenv("CI")) {
                throw sanitizeException(e, apiKey)
            } else {
                throw e
            }
        }
    }
}
