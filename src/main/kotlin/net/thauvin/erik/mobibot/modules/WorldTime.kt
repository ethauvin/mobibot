/*
 * WorldTime.kt
 *
 * Copyright (c) 2004-2026 Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot.modules

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.pircbotx.hooks.types.GenericMessageEvent
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

/**
 * The WorldTime module.
 */
class WorldTime : AbstractModule() {
    override val name = "WorldTime"

    companion object {
        /**
         * Beats (Internet Time) keyword
         */
        const val BEATS_KEYWORD = ".beats"

        // The Time command
        private const val TIME_CMD = "time"

        // The `zones` arguments
        private const val ZONES_ARGS = "zones"

        // The default zone
        private const val DEFAULT_ZONE = "PST"

        // Date/Time Format
        private var dtf =
            DateTimeFormatter.ofPattern("'The time is ${"'HH:mm'".bold()} on ${"'EEEE, d MMMM yyyy'".bold()} in '")

        /**
         * Manual overrides for common country codes and abbreviations.
         * These take precedence over auto-generated entries.
         */
        private val MANUAL_OVERRIDES = mapOf(
            // Special keywords
            "BEAT" to BEATS_KEYWORD,
            "BMT" to BEATS_KEYWORD,

            // Common timezone abbreviations
            "PST" to "America/Los_Angeles",
            "PDT" to "America/Los_Angeles",
            "EST" to "America/New_York",
            "EDT" to "America/New_York",
            "CST" to "America/Chicago",
            "CDT" to "America/Chicago",
            "MST" to "America/Denver",
            "MDT" to "America/Denver",
            "HST" to "Pacific/Honolulu",
            "AKST" to "America/Anchorage",
            "AKDT" to "America/Anchorage",
            "GMT" to "GMT",
            "UTC" to "UTC",
            "ZULU" to "Zulu",
            "CET" to "CET",

            // Country codes with sensible defaults
            "US" to "America/New_York",
            "UK" to "Europe/London",
            "GB" to "Europe/London",
            "CA" to "America/Toronto",
            "AU" to "Australia/Sydney",
            "NZ" to "Pacific/Auckland",
            "JP" to "Asia/Tokyo",
            "CN" to "Asia/Shanghai",
            "IN" to "Asia/Kolkata",
            "DE" to "Europe/Berlin",
            "FR" to "Europe/Paris",
            "IT" to "Europe/Rome",
            "ES" to "Europe/Madrid",
            "RU" to "Europe/Moscow",
            "BR" to "America/Sao_Paulo",
            "MX" to "America/Mexico_City",
            "ZA" to "Africa/Johannesburg",
            "EG" to "Africa/Cairo"
        )

        /**
         * Supported countries/zones. Auto-populated from IANA + manual overrides.
         */
        val COUNTRIES_MAP: Map<String, String> by lazy {
            buildMap {
                // 1. Add manual overrides first so they take precedence
                putAll(MANUAL_OVERRIDES)

                // 2. Add all IANA timezone IDs
                ZoneId.getAvailableZoneIds().forEach { zone ->
                    // Full ID: AMERICA/NEW_YORK -> America/New_York
                    putIfAbsent(zone.uppercase(), zone)

                    // City name: TOKYO -> Asia/Tokyo
                    val city = zone.substringAfterLast('/')
                        .uppercase()
                        .replace("_", "")
                    putIfAbsent(city, zone)

                    // Short IDs like EST, HST already in ZoneId list
                }
            }
        }

        /**
         * Returns the current Internet (beat) Time.
         */
        private fun internetTime(): String {
            val zdt = ZonedDateTime.now(ZoneId.of("UTC+01:00"))
            val beats = ((zdt[ChronoField.SECOND_OF_MINUTE] + zdt[ChronoField.MINUTE_OF_HOUR] * 60
                    + zdt[ChronoField.HOUR_OF_DAY] * 3600) / 86.4).toInt()
            return "%c%03d".format('@', beats)
        }

        /**
         * Returns the time for the given timezone/city.
         */
        @JvmStatic
        fun time(query: String = DEFAULT_ZONE): String {
            val q = query.trim().uppercase().replace(" ", "_")

            // Try exact match first, then fuzzy match
            val tz = COUNTRIES_MAP[q] ?: COUNTRIES_MAP.entries.firstOrNull {
                it.key.startsWith(q) || it.value.uppercase().endsWith("/$q")
            }?.value

            return if (tz != null) {
                if (BEATS_KEYWORD == tz) {
                    "The current Internet Time is ${internetTime().bold()} $BEATS_KEYWORD"
                } else {
                    val zone = ZoneId.of(tz)
                    val displayName = tz.substringAfterLast('/')
                        .replace('_', ' ')
                    (ZonedDateTime.now().withZoneSameInstant(zone).format(dtf) + displayName.bold())
                }
            } else {
                "Unsupported country/zone. Try using ${TIME_CMD.bold()} ${ZONES_ARGS.bold()} for help."
            }
        }
    }

    override fun initialize() {
        addHelp("To display a country's current date/time:")
        addHelp(helpFormat("%c $TIME_CMD [<country code, city, or zone>]"))
        addHelp("Examples: $TIME_CMD US, $TIME_CMD Tokyo, $TIME_CMD Europe/Paris")
        addHelp("For a listing of common codes:")
        addHelp(helpFormat("%c $TIME_CMD $ZONES_ARGS"))
        addCommand(TIME_CMD)
    }

    @SuppressFBWarnings("BC_BAD_CAST_TO_ABSTRACT_COLLECTION")
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.equals(ZONES_ARGS, true)) {
            event.sendMessage("Common codes: ")
            event.sendList(MANUAL_OVERRIDES.keys.sorted().map { it.padEnd(6) }, 10, isIndent = true)
            event.sendMessage("Plus all IANA zones like ${"America/New_York".bold()}, ${"Europe/Paris".bold()}, ${"Asia/Tokyo".bold()}")
            event.sendMessage("Total supported: ${COUNTRIES_MAP.size} zones")
        } else {
            event.respond(time(args.ifBlank { DEFAULT_ZONE }))
        }
    }

    override val isPrivateMsgEnabled = true
}
