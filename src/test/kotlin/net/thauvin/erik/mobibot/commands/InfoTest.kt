/*
 * InfoTest.kt
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

package net.thauvin.erik.mobibot.commands

import assertk.assertThat
import assertk.assertions.isEqualTo
import net.thauvin.erik.mobibot.commands.Info.Companion.toUptime
import org.testng.annotations.Test

class InfoTest {
    @Test(groups = ["commands"])
    fun testToUptime() {
        assertThat(547800300076L.toUptime(), "full").isEqualTo("17 years 2 months 2 weeks 1 day 6 hours 45 minutes")
        assertThat(24300000L.toUptime(), "hours minutes").isEqualTo("6 hours 45 minutes")
        assertThat(110700000L.toUptime(), "days hours minutes").isEqualTo("1 day 6 hours 45 minutes")
        assertThat(1320300000L.toUptime(), "weeks days hours minutes").isEqualTo("2 weeks 1 day 6 hours 45 minutes")
        assertThat(2700000L.toUptime(), "45 minutes").isEqualTo("45 minutes")
        assertThat(60000L.toUptime(), "1 minute").isEqualTo("1 minute")
        assertThat(59000L.toUptime(), "59 seconds").isEqualTo("59 seconds")
        assertThat(0L.toUptime(), "0 second").isEqualTo("0 second")

    }
}
