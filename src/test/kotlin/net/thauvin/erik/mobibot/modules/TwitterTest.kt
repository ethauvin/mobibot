/*
 * TwitterTest.kt
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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.modules.Twitter.Companion.twitterPost
import org.testng.annotations.Test
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * The `TwitterTest` class.
 */
class TwitterTest : LocalProperties() {
    private val ci: String
        get() {
            val ciName = System.getenv("CI_NAME")
            return ciName ?: try {
                InetAddress.getLocalHost().hostName
            } catch (ignore: UnknownHostException) {
                "Unknown Host"
            }
        }

    @Test
    @Throws(ModuleException::class)
    fun testPostTwitter() {
        val msg = "Testing Twitter API from $ci"
        assertThat {
            twitterPost(
                getProperty(Twitter.CONSUMER_KEY_PROP),
                getProperty(Twitter.CONSUMER_SECRET_PROP),
                getProperty(Twitter.TOKEN_PROP),
                getProperty(Twitter.TOKEN_SECRET_PROP),
                getProperty(Twitter.HANDLE_PROP),
                msg,
                true
            )
        }.isSuccess().isEqualTo(msg)
    }
}
