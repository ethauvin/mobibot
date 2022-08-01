/*
 * Versions.kt
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

import net.thauvin.erik.mobibot.ReleaseInfo
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.isChannelOp
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.toIsoLocalDate
import org.pircbotx.hooks.types.GenericMessageEvent

class Versions : AbstractCommand() {
    private val allVersions = listOf(
        "Version  : ${ReleaseInfo.VERSION} (${ReleaseInfo.BUILDDATE.toIsoLocalDate()})",
        "Platform : ${System.getProperty("os.name")} ${System.getProperty("os.version")}" +
                " (${System.getProperty("os.arch")})",
        "Runtimes : Java ${System.getProperty("java.runtime.version")}, Kotlin " + KotlinVersion.CURRENT
    )
    override val name = "versions"
    override val help = listOf("To view the versions data (bot, platform, java, etc.):", helpFormat("%c $name"))
    override val isOpOnly = true
    override val isPublic = false
    override val isVisible = true

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        if (isChannelOp(channel, event)) {
            event.sendList(allVersions, 1)
        }
    }
}
