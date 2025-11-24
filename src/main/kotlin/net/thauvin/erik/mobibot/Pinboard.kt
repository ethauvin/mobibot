/*
 * Pinboard.kt
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.thauvin.erik.mobibot.entries.EntryLink
import net.thauvin.erik.pinboard.PinboardPoster
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * Handles posts to `pinboard.in`.
 */
class Pinboard {
    private val poster = PinboardPoster()

    /**
     * Adds a pin.
     */
    fun addPin(ircServer: String, entry: EntryLink) {
        if (poster.apiToken.isNotBlank()) {
            with(entry) {
                poster.addPin(
                    link,
                    title,
                    postedBy(ircServer),
                    tagsToList(),
                    date.toZonedDateTime()
                )
            }
        }
    }

    /**
     * Deletes a pin.
     */
    fun deletePin(entry: EntryLink) {
        if (poster.apiToken.isNotBlank()) {
            poster.deletePin(entry.link)
        }

    }

    /**
     * Sets the pinboard API token.
     */
    fun setApiToken(apiToken: String) {
        poster.apiToken = apiToken
    }

    /**
     * Updates a pin.
     */
    fun updatePin(ircServer: String, oldUrl: String, entry: EntryLink) {
        if (poster.apiToken.isNotBlank()) {
            with(entry) {
                if (oldUrl != link) {
                    poster.deletePin(oldUrl)
                }
                poster.addPin(
                    link,
                    title,
                    postedBy(ircServer),
                    tagsToList(),
                    date.toZonedDateTime()
                )
            }
        }
    }

    /**
     * Formats the tags as a list, for pinboard.
     */
    @SuppressFBWarnings("BC_BAD_CAST_TO_ABSTRACT_COLLECTION")
    private fun EntryLink.tagsToList(): List<String> {
        return tags.map { it.name } + nick
    }

    /**
     * Returns the pinboard.in extended attribution line.
     */
    private fun EntryLink.postedBy(ircServer: String): String {
        return "Posted by $nick on $channel ( $ircServer )"
    }

    /**
     * Formats a date to a zoned date time.
     */
    private fun Date.toZonedDateTime(): ZonedDateTime {
        return ZonedDateTime.ofInstant(toInstant(), ZoneId.systemDefault())
    }
}
