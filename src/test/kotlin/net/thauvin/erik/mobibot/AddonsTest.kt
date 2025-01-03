/*
 * AddonsTest.kt
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

package net.thauvin.erik.mobibot

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.size
import net.thauvin.erik.mobibot.commands.ChannelFeed
import net.thauvin.erik.mobibot.commands.Cycle
import net.thauvin.erik.mobibot.commands.Die
import net.thauvin.erik.mobibot.commands.Ignore
import net.thauvin.erik.mobibot.commands.links.Comment
import net.thauvin.erik.mobibot.commands.links.View
import net.thauvin.erik.mobibot.modules.*
import java.util.*
import kotlin.test.Test

class AddonsTest {
    private val p = Properties().apply {
        put("disabled-modules", "war,dice Lookup")
        put("disabled-commands", "View | comment")
    }
    private val addons = Addons(p)

    @Test
    fun addTest() {
        // Modules
        addons.add(Joke())
        addons.add(RockPaperScissors())
        addons.add(War())
        addons.add(Dice())
        addons.add(Lookup())
        assertThat(addons::modules).size().isEqualTo(2)
        assertThat(addons.names.modules, "names.modules").containsExactly("Joke", "RockPaperScissors")

        // Commands
        addons.add(View())
        addons.add(Comment())
        addons.add(Cycle())
        addons.add(Die()) // invisible
        addons.add(ChannelFeed("channel")) // no properties, disabled
        p[Ignore.IGNORE_PROP] = "nick"
        addons.add(Ignore())
        assertThat(addons::commands).size().isEqualTo(3)

        assertThat(addons.names.ops, "names.ops").containsExactly("cycle")

        assertThat(addons.names.commands, "names.command").containsExactly(
            "joke",
            "rock",
            "paper",
            "scissors",
            "ignore"
        )
    }
}
