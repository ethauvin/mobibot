/*
 * StockQuoteTest.kt
 *
 * Copyright 2004-2023 Erik C. Thauvin (erik@thauvin.net)
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

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasNoCause
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.matches
import assertk.assertions.prop
import net.thauvin.erik.mobibot.ExceptionSanitizer.sanitize
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.modules.StockQuote.Companion.getQuote
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import org.testng.annotations.Test

/**
 * The `StockQuoteTest` class.
 */
class StockQuoteTest : LocalProperties() {
    private fun buildMatch(label: String): String {
        return "${label}:[ ]+[0-9.]+".prependIndent()
    }

    @Test(groups = ["modules"])
    @Throws(ModuleException::class)
    fun testGetQuote() {
        val apiKey = getProperty(StockQuote.API_KEY_PROP)
        try {
            var symbol = "apple inc"
            val messages = getQuote(symbol, apiKey)
            assertThat(messages, "response not empty").isNotEmpty()
            assertThat(messages, "getQuote($symbol)").index(0).prop(Message::msg).matches("Symbol: AAPL .*".toRegex())
            assertThat(messages, "getQuote($symbol)").index(1).prop(Message::msg).matches(buildMatch("Price").toRegex())
            assertThat(messages, "getQuote($symbol)").index(2).prop(Message::msg)
                .matches(buildMatch("Previous").toRegex())
            assertThat(messages, "getQuote($symbol)").index(3).prop(Message::msg).matches(buildMatch("Open").toRegex())

            symbol = "blahfoo"
            assertThat(getQuote(symbol, apiKey).first(), "getQuote($symbol)").all {
                isInstanceOf(ErrorMessage::class.java)
                prop(Message::msg).isEqualTo(StockQuote.INVALID_SYMBOL)
            }
            assertThat(getQuote("", "apikey").first(), "getQuote(empty)").all {
                isInstanceOf(ErrorMessage::class.java)
                prop(Message::msg).isEqualTo(StockQuote.INVALID_SYMBOL)
            }
            assertFailure { getQuote("test", "") }.isInstanceOf(ModuleException::class.java).hasNoCause()
        } catch (e: ModuleException) {
            // Avoid displaying api keys in CI logs
            if ("true" == System.getenv("CI")) {
                throw e.sanitize(apiKey)
            } else {
                throw e
            }
        }
    }
}
