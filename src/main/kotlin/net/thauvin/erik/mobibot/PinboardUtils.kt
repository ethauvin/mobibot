/*
 * PinboardUtils.kt
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

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.thauvin.erik.mobibot.entries.EntryLink
import net.thauvin.erik.pinboard.PinboardPoster
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Handles posts to pinboard.in.
 */
object PinboardUtils {
    /**
     * Adds a pin.
     */
    @JvmStatic
    fun addPin(poster: PinboardPoster, ircServer: String, entry: EntryLink) = runBlocking {
        async {
            poster.addPin(
                entry.link,
                entry.title,
                postedBy(entry, ircServer),
                entry.pinboardTags,
                entry.date.toTimestamp()
            )
        }.await()
    }

    /**
     * Deletes a pin.
     */
    @JvmStatic
    fun deletePin(poster: PinboardPoster, entry: EntryLink) = runBlocking {
        async {
            poster.deletePin(entry.link)
        }.await()
    }

    /**
     * Updates a pin.
     */
    @JvmStatic
    fun updatePin(poster: PinboardPoster, ircServer: String, oldUrl: String, entry: EntryLink) = runBlocking {
        async {
            with(entry) {
                if (oldUrl != link) {
                    poster.deletePin(oldUrl)
                    poster.addPin(
                        link,
                        title,
                        postedBy(entry, ircServer),
                        pinboardTags,
                        date.toTimestamp()
                    )
                } else {
                    poster.addPin(
                        link,
                        title,
                        postedBy(entry, ircServer),
                        pinboardTags,
                        date.toTimestamp(),
                        replace = true,
                        shared = true
                    )
                }
            }
        }.await()
    }

    /**
     * Format a date to a UTC timestamp.
     */
    @JvmStatic
    fun Date.toTimestamp(): String {
        return ZonedDateTime.ofInstant(
                this.toInstant().truncatedTo(ChronoUnit.SECONDS),
                ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT)
    }

    /**
     * Returns the pinboard.in extended attribution line.
     */
    private fun postedBy(entry: EntryLink, ircServer: String): String {
        return "Posted by ${entry.nick} on ${entry.channel} ( $ircServer )"
    }
}

