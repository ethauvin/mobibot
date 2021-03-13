/*
 * WorldTime.kt
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
package net.thauvin.erik.mobibot.modules

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.*
import kotlin.collections.ArrayList

/**
 * The WorldTime module.
 */
class WorldTime(bot: Mobibot) : AbstractModule(bot) {
    companion object {
        // Beats (Internet Time) keyword
        private const val BEATS_KEYWORD = ".beats"

        // Supported countries
        private var COUNTRIES_MAP: Map<String, String>

        // The Time command
        private const val TIME_CMD = "time"

        /**
         * Returns the current Internet (beat) Time.
         */
        private fun internetTime(): String {
            val zdt = ZonedDateTime.now(ZoneId.of("UTC+01:00"))
            val beats = ((zdt[ChronoField.SECOND_OF_MINUTE] + zdt[ChronoField.MINUTE_OF_HOUR] * 60
                + zdt[ChronoField.HOUR_OF_DAY] * 3600) / 86.4).toInt()
            return String.format(Locale.getDefault(), "%c%03d", '@', beats)
        }

        /**
         * Returns the world time.
         */
        @JvmStatic
        fun worldTime(query: String): Message {
            val tz = COUNTRIES_MAP[(query.substring(query.indexOf(' ') + 1).trim()).toUpperCase()]
            val response: String = if (tz != null) {
                if (BEATS_KEYWORD == tz) {
                    "The current Internet Time is: " + Utils.bold(internetTime() + ' ' + BEATS_KEYWORD)
                } else {
                    (ZonedDateTime.now()
                        .withZoneSameInstant(ZoneId.of(tz))
                        .format(
                            DateTimeFormatter.ofPattern(
                                "'The time is ${Utils.bold("'HH:mm'")} on ${Utils.bold("'EEEE, d MMMM yyyy'")} in '"
                            )
                        )
                        + Utils.bold(tz.substring(tz.indexOf('/') + 1).replace('_', ' '))
                        )
                }
            } else {
                return ErrorMessage("Unsupported country/zone. Please try again.")
            }
            return PublicMessage(response)
        }

        init {
            // Initialize the countries map
            val countries = mutableMapOf<String, String>()
            countries["AE"] = "Asia/Dubai"
            countries["AF"] = "Asia/Kabul"
            countries["AQ"] = "Antarctica/South_Pole"
            countries["AT"] = "Europe/Vienna"
            countries["AU"] = "Australia/Sydney"
            countries["AKST"] = "America/Anchorage"
            countries["AKDT"] = "America/Anchorage"
            countries["BE"] = "Europe/Brussels"
            countries["BR"] = "America/Sao_Paulo"
            countries["CA"] = "America/Montreal"
            countries["CDT"] = "America/Chicago"
            countries["CET"] = "CET"
            countries["CH"] = "Europe/Zurich"
            countries["CN"] = "Asia/Shanghai"
            countries["CST"] = "America/Chicago"
            countries["CU"] = "Cuba"
            countries["DE"] = "Europe/Berlin"
            countries["DK"] = "Europe/Copenhagen"
            countries["EDT"] = "America/New_York"
            countries["EG"] = "Africa/Cairo"
            countries["ER"] = "Africa/Asmara"
            countries["ES"] = "Europe/Madrid"
            countries["EST"] = "America/New_York"
            countries["FI"] = "Europe/Helsinki"
            countries["FR"] = "Europe/Paris"
            countries["GB"] = "Europe/London"
            countries["GMT"] = "GMT"
            countries["GR"] = "Europe/Athens"
            countries["HK"] = "Asia/Hong_Kong"
            countries["HST"] = "Pacific/Honolulu"
            countries["IE"] = "Europe/Dublin"
            countries["IL"] = "Asia/Tel_Aviv"
            countries["IN"] = "Asia/Kolkata"
            countries["IQ"] = "Asia/Baghdad"
            countries["IR"] = "Asia/Tehran"
            countries["IS"] = "Atlantic/Reykjavik"
            countries["IT"] = "Europe/Rome"
            countries["JM"] = "Jamaica"
            countries["JP"] = "Asia/Tokyo"
            countries["LY"] = "Africa/Tripoli"
            countries["MA"] = "Africa/Casablanca"
            countries["MDT"] = "America/Denver"
            countries["MH"] = "Kwajalein"
            countries["MQ"] = "America/Martinique"
            countries["MST"] = "America/Denver"
            countries["MX"] = "America/Mexico_City"
            countries["NL"] = "Europe/Amsterdam"
            countries["NO"] = "Europe/Oslo"
            countries["NP"] = "Asia/Katmandu"
            countries["NZ"] = "Pacific/Auckland"
            countries["PDT"] = "America/Los_Angeles"
            countries["PH"] = "Asia/Manila"
            countries["PK"] = "Asia/Karachi"
            countries["PL"] = "Europe/Warsaw"
            countries["PST"] = "America/Los_Angeles"
            countries["PT"] = "Europe/Lisbon"
            countries["PR"] = "America/Puerto_Rico"
            countries["RU"] = "Europe/Moscow"
            countries["SE"] = "Europe/Stockholm"
            countries["SG"] = "Asia/Singapore"
            countries["TH"] = "Asia/Bangkok"
            countries["TM"] = "Asia/Ashgabat"
            countries["TN"] = "Africa/Tunis"
            countries["TR"] = "Europe/Istanbul"
            countries["TW"] = "Asia/Taipei"
            countries["UK"] = "Europe/London"
            countries["US"] = "America/New_York"
            countries["UTC"] = "UTC"
            countries["VA"] = "Europe/Vatican"
            countries["VE"] = "America/Caracas"
            countries["VN"] = "Asia/Ho_Chi_Minh"
            countries["ZA"] = "Africa/Johannesburg"
            countries["ZULU"] = "Zulu"
            countries["INTERNET"] = BEATS_KEYWORD
            countries["BEATS"] = BEATS_KEYWORD
            ZoneId.getAvailableZoneIds().stream()
                .filter { tz: String ->
                    !tz.contains("/") && tz.length == 3 && !countries.containsKey(tz)
                }
                .forEach { tz: String ->
                    countries[tz] = tz
                }
            COUNTRIES_MAP = Collections.unmodifiableMap(countries)
        }
    }

    override fun commandResponse(
        sender: String,
        cmd: String,
        args: String,
        isPrivate: Boolean
    ) {
        with(bot) {
            if (args.isEmpty()) {
                send(sender, "The supported countries/zones are: ", isPrivate)
                sendList(sender, ArrayList(COUNTRIES_MAP.keys), 17, isPrivate = false)
            } else {
                val msg = worldTime(args)
                if (isPrivate) {
                    send(sender, msg.msg, true)
                } else {
                    if (msg.isError) {
                        send(sender, msg.msg, false)
                    } else {
                        send(msg.msg)
                    }
                }
            }
        }
    }

    override val isPrivateMsgEnabled = true

    init {
        help.add("To display a country's current date/time:")
        help.add(Utils.helpFormat("%c $TIME_CMD") + " [<country code>]")
        help.add("For a listing of the supported countries:")
        help.add(Utils.helpFormat("%c $TIME_CMD"))
        commands.add(TIME_CMD)
    }
}
