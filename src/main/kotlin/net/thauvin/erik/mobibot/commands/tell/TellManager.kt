/*
 * TellManager.kt
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

import net.thauvin.erik.mobibot.Utils.loadSerialData
import net.thauvin.erik.mobibot.Utils.saveSerialData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.LocalDateTime

/**
 * Manages the [Tell] messages queue.
 */
object TellManager {
    private val logger: Logger = LoggerFactory.getLogger(TellManager::class.java)

    /**
     * Cleans the messages queue.
     */
    @JvmStatic
    fun clean(tellMessages: MutableList<TellMessage>, tellMaxDays: Long): Boolean {
        if (logger.isDebugEnabled) logger.debug("Cleaning the messages.")
        val today = LocalDateTime.now(Clock.systemUTC())
        return tellMessages.removeIf { o: TellMessage -> o.queued.plusDays(tellMaxDays).isBefore(today) }
    }

    /**
     * Loads the messages.
     */
    @JvmStatic
    fun load(file: String): List<TellMessage> {
        @Suppress("UNCHECKED_CAST")
        return loadSerialData(file, emptyList<TellMessage>(), logger, "message queue") as List<TellMessage>
    }

    /**
     * Saves the messages.
     */
    @JvmStatic
    fun save(file: String, messages: List<TellMessage?>?) {
        if (messages != null) {
            saveSerialData(file, messages, logger, "messages")
        }
    }
}
