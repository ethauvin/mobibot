/*
 * SocialModule.kt
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

package net.thauvin.erik.mobibot.social

import net.thauvin.erik.mobibot.commands.links.LinksManager
import net.thauvin.erik.mobibot.entries.EntriesUtils.toLinkLabel
import net.thauvin.erik.mobibot.entries.EntryLink
import net.thauvin.erik.mobibot.modules.AbstractModule
import net.thauvin.erik.mobibot.modules.ModuleException
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class SocialModule : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(SocialManager::class.java)

    abstract val handle: String?
    abstract val isAutoPost: Boolean

    abstract fun formatEntry(entry: EntryLink): String

    /**
     * Sends a DM.
     */
    fun notification(msg: String) {
        if (isEnabled && !handle.isNullOrBlank()) {
            try {
                post(message = msg, isDm = true)
                if (logger.isDebugEnabled) logger.debug("Notified $handle on $name: $msg")
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn("Failed to notify $handle on $name: $msg", e)
            }
        }
    }

    abstract fun post(message: String, isDm: Boolean): String

    /**
     * Post entry to social media.
     */
    fun postEntry(index: Int) {
        if (isAutoPost && LinksManager.entries.links.size >= index) {
            try {
                if (logger.isDebugEnabled) {
                    logger.debug("Posting {} to $name.", index.toLinkLabel())
                }
                post(message = formatEntry(LinksManager.entries.links[index]), isDm = false)
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(
                    "Failed to post entry ${index.toLinkLabel()} on $name.",
                    e
                )
            }
        }
    }

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        try {
            event.respond(post("$args (by ${event.user.nick} on $channel)", false))
        } catch (e: ModuleException) {
            if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
            e.message?.let {
                event.respond(it)
            }
        }
    }
}
