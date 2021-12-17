/*
 * AddonsTest.kt
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

package net.thauvin.erik.mobibot

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import net.thauvin.erik.mobibot.commands.ChannelFeed
import net.thauvin.erik.mobibot.commands.Cycle
import net.thauvin.erik.mobibot.commands.Die
import net.thauvin.erik.mobibot.commands.Ignore
import net.thauvin.erik.mobibot.commands.links.Comment
import net.thauvin.erik.mobibot.commands.links.View
import net.thauvin.erik.mobibot.modules.Joke
import net.thauvin.erik.mobibot.modules.RockPaperScissors
import net.thauvin.erik.mobibot.modules.Twitter
import org.testng.annotations.Test
import java.util.Properties

class AddonsTest {
    private val addons = Addons()

    @Test
    fun addTest() {
        val p = Properties()

        // Modules
        addons.add(Joke(), p)
        addons.add(RockPaperScissors(), p)
        addons.add(Twitter(), p) // no properties, disabled.
        assertThat(addons.modules.size, "modules = 2").isEqualTo(2)

        assertThat(addons.modulesNames, "module names").containsExactly("Joke", "RockPaperScissors")

        // Commands
        addons.add(View(), p)
        addons.add(Comment(), p)
        addons.add(Cycle(), p)
        addons.add(Die(), p) // invisible
        addons.add(ChannelFeed("channel"), p) // no properties, disabled
        addons.add(Ignore(), p.apply { put(Ignore.IGNORE_PROP, "nick") })
        assertThat(addons.commands.size, "commands = 4").isEqualTo(5)

        assertThat(addons.ops, "ops").containsExactly("cycle")

        assertThat(addons.names, "names").containsExactly(
            "joke",
            "rock",
            "paper",
            "scissors",
            "view",
            "comment",
            "ignore"
        )
    }
}
