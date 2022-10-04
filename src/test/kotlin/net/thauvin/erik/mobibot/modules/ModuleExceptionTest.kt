/*
 * ModuleExceptionTest.kt
 *
 * Copyright (c) 2004-2022, Erik C. Thauvin (erik@thauvin.net)
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

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.endsWith
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import net.thauvin.erik.mobibot.ExceptionSanitizer.sanitize
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
        assertThat(e::debugMessage).isEqualTo(debugMessage)
    }

    @Test(dataProvider = "dp")
    fun testGetMessage(e: ModuleException) {
        assertThat(e).hasMessage(message)
    }

    @Test(groups = ["modules"])
    fun testSanitizeMessage() {
        val apiKey = "1234567890"
        var e = ModuleException(debugMessage, message, IOException("URL http://foo.com?apiKey=$apiKey&userID=me"))
        assertThat(
            e.sanitize(apiKey, "", "me").message, "ModuleException(debugMessage, message, IOException(url))"
        ).isNotNull().all {
            contains("xxxxxxxxxx", "userID=xx", "java.io.IOException")
            doesNotContain(apiKey, "me")
        }

        e = ModuleException(debugMessage, message, null)
        assertThat(e.sanitize(apiKey), "ModuleException(debugMessage, message, null)").hasMessage(message)

        e = ModuleException(debugMessage, message, IOException())
        assertThat(e.sanitize(apiKey), "ModuleException(debugMessage, message, IOException())").hasMessage(message)

        e = ModuleException(debugMessage, apiKey)
        assertThat(e.sanitize(apiKey).message, "ModuleException(debugMessage, apiKey)").isNotNull()
            .doesNotContain(apiKey)

        val msg: String? = null
        e = ModuleException(debugMessage, msg, IOException(msg))
        assertThat(e.sanitize(apiKey).message, "ModuleException(debugMessage, msg, IOException(msg))").isNull()

        e = ModuleException(debugMessage, msg, IOException("foo is $apiKey"))
        assertThat(
            e.sanitize("   ", apiKey, "foo").message,
            "ModuleException(debugMessage, msg, IOException(foo is $apiKey))"
        ).isNotNull().all {
            doesNotContain(apiKey)
            endsWith("xxx is xxxxxxxxxx")
        }
        assertThat(e.sanitize(), "exception should be unchanged").isEqualTo(e)
    }
}
