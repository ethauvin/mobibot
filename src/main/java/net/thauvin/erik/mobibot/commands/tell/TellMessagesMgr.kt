/*
 * TellMessagesMgr.kt
 *
 * Copyright (c) 2004-2020, Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot.commands.tell

import org.apache.logging.log4j.Logger
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Clock
import java.time.LocalDateTime

/**
 * The Tell Messages Manager.
 */
internal class TellMessagesMgr private constructor() {
    companion object {
        /**
         * Cleans the messages queue.
         */
        fun clean(tellMessages: MutableList<TellMessage>, tellMaxDays: Int): Boolean {
            val today = LocalDateTime.now(Clock.systemUTC())
            return tellMessages.removeIf { o: TellMessage -> o.queued.plusDays(tellMaxDays.toLong()).isBefore(today) }
        }

        /**
         * Loads the messages.
         */

        fun load(file: String, logger: Logger): List<TellMessage> {
            try {
                ObjectInputStream(
                    BufferedInputStream(Files.newInputStream(Paths.get(file)))
                ).use { input ->
                    if (logger.isDebugEnabled) logger.debug("Loading the messages.")
                    @Suppress("UNCHECKED_CAST")
                    return input.readObject() as List<TellMessage>
                }
            } catch (ignore: FileNotFoundException) {
                // Do nothing
            } catch (e: IOException) {
                logger.error("An IO error occurred loading the messages queue.", e)
            } catch (e: ClassNotFoundException) {
                logger.error("An error occurred loading the messages queue.", e)
            }
            return listOf()
        }

        /**
         * Saves the messages.
         */
        fun save(file: String, messages: List<TellMessage?>?, logger: Logger) {
            try {
                BufferedOutputStream(Files.newOutputStream(Paths.get(file))).use { bos ->
                    ObjectOutputStream(bos).use { output ->
                        if (logger.isDebugEnabled) logger.debug("Saving the messages.")
                        output.writeObject(messages)
                    }
                }
            } catch (e: IOException) {
                logger.error("Unable to save messages queue.", e)
            }
        }
    }
}
