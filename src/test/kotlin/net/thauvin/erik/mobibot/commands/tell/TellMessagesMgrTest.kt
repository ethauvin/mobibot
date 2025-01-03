/*
 * TellMessagesMgrTest.kt
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

package net.thauvin.erik.mobibot.commands.tell

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import org.junit.AfterClass
import java.time.LocalDateTime
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize
import kotlin.test.Test

class TellMessagesMgrTest {
    private val maxDays = 10L
    private val testMessages = mutableListOf<TellMessage>().apply {
        for (i in 0..5) {
            this.add(i, TellMessage("sender$i", "recipient$i", "message $i"))
        }
    }

    init {
        TellManager.save(testFile.toAbsolutePath().toString(), testMessages)
        assertThat(testFile.fileSize()).isGreaterThan(0)
    }

    @Test
    fun cleanTest() {
        testMessages.add(TellMessage("sender", "recipient", "message").apply {
            queued = LocalDateTime.now().minusDays(maxDays)
        })
        val size = testMessages.size
        assertThat(TellManager.clean(testMessages, maxDays + 2), "clean(maxDays=${maxDays + 2})").isFalse()
        assertThat(TellManager.clean(testMessages, maxDays), "clean(maxDays=$maxDays)").isTrue()
        assertThat(testMessages, "testMessages").size().isEqualTo(size - 1)
    }

    @Test
    fun loadTest() {
        val messages = TellManager.load(testFile.toAbsolutePath().toString())
        for (i in messages.indices) {
            assertThat(messages).index(i).all {
                prop(TellMessage::sender).isEqualTo(testMessages[i].sender)
                prop(TellMessage::recipient).isEqualTo(testMessages[i].recipient)
                prop(TellMessage::message).isEqualTo(testMessages[i].message)
            }
        }
    }

    companion object {
        private val testFile = createTempFile(suffix = ".ser")

        @JvmStatic
        @AfterClass
        fun afterClass() {
            testFile.deleteIfExists()
        }
    }
}
