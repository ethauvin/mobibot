/*
 * BitcoinTest.kt
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
import net.thauvin.erik.mobibot.modules.Bitcoin.Companion.marketPrice
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

/**
 * The `BitcoinTest` class.
 */
class BitcoinTest : LocalProperties() {
    @Test
    @Throws(ModuleException::class)
    fun testMarketPrice() {
        var messages = marketPrice("USD")
        assertThat(messages).`as`("not empty").isNotEmpty
        assertThat(messages[0].msg).`as`("bitcoin &  $").startsWith("BITCOIN").contains("$")
        assertThat(messages[1].msg).`as`("15m").contains("15m")

        messages = marketPrice("GBP")
        assertThat(messages[0].msg).`as`("£").contains("£").contains("GBP")
        assertThat(messages[1].msg).`as`("GBP 15m").contains("15m")
    }
}
