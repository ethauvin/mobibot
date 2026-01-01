/*
 * LookupTests.kt
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
import assertk.assertions.any
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import net.thauvin.erik.mobibot.modules.Lookup.Companion.nslookup
import net.thauvin.erik.mobibot.modules.Lookup.Companion.whois
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.pircbotx.hooks.types.GenericMessageEvent
import kotlin.test.Test

class LookupTests {
    @Nested
    @DisplayName("Command Response Tests")
    inner class CommandResponseTests {
        @Test
        fun lookupByHostname() {
            val lookup = Lookup()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            lookup.commandResponse("channel", "lookup", "ec2-54-234-237-183.compute-1.amazonaws.com", event)

            Mockito.verify(event, Mockito.times(1)).respondWith(captor.capture())
            assertThat(captor.value).contains("54.234.237.183")
        }

        @Test
        fun lookupByAddress() {
            val lookup = Lookup()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            lookup.commandResponse("channel", "lookup", "54.234.237.183", event)

            Mockito.verify(event, Mockito.times(1)).respondWith(captor.capture())
            assertThat(captor.value).contains("ec2-54-234-237-183.compute-1.amazonaws.com")
        }

        @Test
        fun lookupUnknownHostname() {
            val lookup = Lookup()
            val event = Mockito.mock(GenericMessageEvent::class.java)
            val captor = ArgumentCaptor.forClass(String::class.java)

            lookup.commandResponse("channel", "lookup", "foobar", event)

            Mockito.verify(event, Mockito.times(1)).respond(captor.capture())
            assertThat(captor.value).isEqualTo("Unknown host.")
        }
    }

    @Nested
    @DisplayName("Lookup Tests")
    inner class LookupTests {
        @Test
        @Throws(Exception::class)
        fun lookupByHostname() {
            val result = nslookup("apple.com")
            assertThat(result, "lookup(apple.com)").contains("17.253.144.10")
        }

        @Test
        @Throws(Exception::class)
        fun lookupByIpAddress() {
            val result = nslookup("37.27.52.13")
            assertThat(result, "lookup(37.27.52.13)").contains("nix4.thauvin.us")
        }
    }

    @Test
    @Throws(Exception::class)
    fun whois() {
        val result = whois("17.178.96.59", Lookup.WHOIS_HOST)
        assertThat(result, "whois(17.178.96.59").any { it.contains("Apple Inc.") }
    }
}
