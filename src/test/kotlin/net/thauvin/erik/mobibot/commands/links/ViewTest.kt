/*
 * ViewTest.kt
 *
 * Copyright 2021-2023 Erik C. Thauvin (erik@thauvin.net)
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

package net.thauvin.erik.mobibot.commands.links

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import net.thauvin.erik.mobibot.entries.EntryLink
import kotlin.test.Test

class ViewTest {
    @Test
    fun testParseArgs() {
        val view = View()

        for (i in 1..10) {
            LinksManager.entries.links.add(
                EntryLink(
                    "https://www.example.com/$i",
                    "Example $i",
                    "nick$i",
                    "login$i",
                    "#channel",
                    emptyList()
                )
            )
        }

        assertThat(view.parseArgs("1"), "parseArgs(1)").all {
            prop(Pair<Int, String>::first).isEqualTo(0)
            prop(Pair<Int, String>::second).isEqualTo("")
        }

        assertThat(view.parseArgs("2 foo"), "parseArgs(2, foo)").all {
            prop(Pair<Int, String>::first).isEqualTo(1)
            prop(Pair<Int, String>::second).isEqualTo("foo")
        }

        assertThat(view.parseArgs("3 FOO"), "parseArgs(3, FOO)").all {
            prop(Pair<Int, String>::first).isEqualTo(2)
            prop(Pair<Int, String>::second).isEqualTo("foo")
        }

        assertThat(view.parseArgs(" 4 foo bar "), "parseArgs( 4 foo bar )").all {
            prop(Pair<Int, String>::first).isEqualTo(3)
            prop(Pair<Int, String>::second).isEqualTo("foo bar")
        }

        assertThat(view.parseArgs("foo bar"), "parseArgs(foo bar)").all {
            prop(Pair<Int, String>::first).isEqualTo(0)
            prop(Pair<Int, String>::second).isEqualTo("foo bar")
        }

        assertThat(view.parseArgs("${Int.MAX_VALUE}1"), "parseArgs(overflow)").all {
            prop(Pair<Int, String>::first).isEqualTo(0)
            prop(Pair<Int, String>::second).isEqualTo("${Int.MAX_VALUE}1")
        }

        assertThat(view.parseArgs("1a"), "parseArgs(1a)").all {
            prop(Pair<Int, String>::first).isEqualTo(0)
            prop(Pair<Int, String>::second).isEqualTo("1a")
        }

        assertThat(view.parseArgs("20"), "parseArgs(20)").all {
            prop(Pair<Int, String>::first).isEqualTo(0)
            prop(Pair<Int, String>::second).isEqualTo("")
        }

        assertThat(view.parseArgs(""), "parseArgs()").all {
            prop(Pair<Int, String>::first).isEqualTo(LinksManager.entries.links.size - View.MAX_ENTRIES)
            prop(Pair<Int, String>::second).isEqualTo("")
        }

        LinksManager.entries.links.clear()

        assertThat(view.parseArgs("4"), "parseArgs(4)").all {
            prop(Pair<Int, String>::first).isEqualTo(0)
            prop(Pair<Int, String>::second).isEqualTo("")
        }
    }
}
