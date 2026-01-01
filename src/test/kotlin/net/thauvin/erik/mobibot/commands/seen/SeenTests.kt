/*
 * SeenTests.kt
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

package net.thauvin.erik.mobibot.commands.seen

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import kotlin.io.path.createTempFile
import kotlin.test.Test


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SeenTests {
    companion object {
        private val tmpFile = createTempFile(SeenTests::class.java.simpleName, suffix = ".ser")
            .apply { toFile().deleteOnExit(); }
        private val seen = Seen(tmpFile.toAbsolutePath().toString())
        private const val NICK = "ErikT"
    }

    @Test
    @Order(1)
    fun add() {
        val last = seen.getSeenNick(NICK)?.lastSeen
        seen.add(NICK)
        assertThat(seen.getSeenNicks()).size().equals(1)
        assertThat(seen.getSeenNick(NICK)).all {
            isNotNull().prop(SeenNick::lastSeen).isNotEqualTo(last)
            isNotNull().prop(SeenNick::nick).isNotNull().isEqualTo(NICK)
        }
    }

    @Test
    @Order(2)
    fun load() {
        seen.clear()
        assertThat(seen.getSeenNicks()).isEmpty()
        seen.load()
        assertThat(seen.getSeenNick(NICK)).isNotNull()
    }

    @Test
    @Order(3)
    fun clear() {
        seen.clear()
        seen.save()
        seen.load()
        assertThat(seen.getSeenNicks()).isEmpty()
    }
}
