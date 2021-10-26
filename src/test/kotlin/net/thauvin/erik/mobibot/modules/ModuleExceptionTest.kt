/*
 * ModuleExceptionTest.kt
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

import net.thauvin.erik.mobibot.Sanitize.sanitizeException
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.IOException
import java.lang.reflect.Method

/**
 * The `ModuleExceptionTest` class.
 */
class ModuleExceptionTest {
    companion object {
        const val debugMessage = "debugMessage"
        const val message = "message"
    }

    @DataProvider(name = "dp")
    fun createData(@Suppress("UNUSED_PARAMETER") m: Method?): Array<Array<Any>> {
        return arrayOf(
            arrayOf(ModuleException(debugMessage, message, IOException("URL http://foobar.com"))),
            arrayOf(ModuleException(debugMessage, message, IOException("URL http://foobar.com?"))),
            arrayOf(ModuleException(debugMessage, message))
        )
    }

    @Test(dataProvider = "dp")
    fun testGetDebugMessage(e: ModuleException) {
        assertThat(e.debugMessage).describedAs("get debug message").isEqualTo(debugMessage)
    }

    @Test(dataProvider = "dp")
    fun testGetMessage(e: ModuleException) {
        assertThat(e).describedAs("get message").hasMessage(message)
    }

    @Test
    fun testSanitizeMessage() {
        val apiKey = "1234567890"
        var e = ModuleException(debugMessage, message, IOException("URL http://foo.com?apiKey=$apiKey&userID=me"))
        assertThat(sanitizeException(e, apiKey, "", "me")).describedAs("sanitized url")
            .hasMessageContainingAll("xxxxxxxxxx", "userID=xx").hasMessageNotContainingAny(apiKey, "me")

        e = ModuleException(debugMessage, message, null)
        assertThat(sanitizeException(e, apiKey)).describedAs("no cause").hasMessage(message)

        e = ModuleException(debugMessage, message, IOException())
        assertThat(sanitizeException(e, apiKey)).describedAs("no cause message").hasMessage(message)

        e = ModuleException(apiKey)
        assertThat(sanitizeException(e, apiKey)).describedAs("api key in message").hasMessageNotContaining(apiKey)

        val msg: String? = null
        e = ModuleException(debugMessage, msg, IOException(msg))
        assertThat(sanitizeException(e, apiKey).message).describedAs("null message").isNull()

        e = ModuleException(msg, msg, IOException("foo is $apiKey"))
        assertThat(sanitizeException(e, "   ", apiKey, "foo").message).describedAs("key in cause")
            .doesNotContain(apiKey).endsWith("xxx is xxxxxxxxxx")
    }
}
