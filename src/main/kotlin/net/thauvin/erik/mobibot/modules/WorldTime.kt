/*
 * WorldTime.kt
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
package net.thauvin.erik.mobibot.modules

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.PublicMessage
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Collections
import java.util.Locale

/**
 * The WorldTime module.
 */
class WorldTime(bot: Mobibot) : AbstractModule(bot) {
    companion object {
        // Beats (Internet Time) keyword
        const val BEATS_KEYWORD = ".beats"

        // Supported countries
        var COUNTRIES_MAP: Map<String, String>

        // The Time command
        private const val TIME_CMD = "time"

        // Date/Time Format
        private var dtf =
            DateTimeFormatter.ofPattern("'The time is ${bold("'HH:mm'")} on ${bold("'EEEE, d MMMM yyyy'")} in '")

        /**
         * Returns the current Internet (beat) Time.
         */
        @Suppress("MagicNumber")
        private fun internetTime(): String {
            val zdt = ZonedDateTime.now(ZoneId.of("UTC+01:00"))
            val beats = ((zdt[ChronoField.SECOND_OF_MINUTE] + zdt[ChronoField.MINUTE_OF_HOUR] * 60
                    + zdt[ChronoField.HOUR_OF_DAY] * 3600) / 86.4).toInt()
            return String.format(Locale.getDefault(), "%c%03d", '@', beats)
        }

        /**
         * Returns the time for the given timezone/city.
         */
        @JvmStatic
        fun time(query: String): Message {
            val tz = COUNTRIES_MAP[(query.substring(query.indexOf(' ') + 1).trim()).uppercase()]
            val response: String = if (tz != null) {
                if (BEATS_KEYWORD == tz) {
                    "The current Internet Time is: ${bold(internetTime())} $BEATS_KEYWORD"
                } else {
                    (ZonedDateTime.now().withZoneSameInstant(ZoneId.of(tz)).format(dtf)
                            + bold(tz.substring(tz.indexOf('/') + 1).replace('_', ' ')))
                }
            } else {
                return ErrorMessage("Unsupported country/zone. Please try again.")
            }
            return PublicMessage(response)
        }

        init {
            // Initialize the countries map
            val countries = mutableMapOf<String, String>()
            countries["AD"] = "Europe/Andorra"
            countries["AE"] = "Asia/Dubai"
            countries["AF"] = "Asia/Kabul"
            countries["AKDT"] = "America/Anchorage"
            countries["AKST"] = "America/Anchorage"
            countries["AQ"] = "Antarctica/South_Pole"
            countries["AR"] = "America/Argentina/Buenos_Aires"
            countries["AS"] = "Pacific/Pago_Pago"
            countries["AT"] = "Europe/Vienna"
            countries["AU"] = "Australia/Sydney"
            countries["AX"] = "Europe/Mariehamn"
            countries["AZ"] = "Asia/Baku"
            countries["BA"] = "Europe/Sarajevo"
            countries["BB"] = "America/Barbados"
            countries["BD"] = "Asia/Dhaka"
            countries["BE"] = "Europe/Brussels"
            countries["BEAT"] = BEATS_KEYWORD
            countries["BF"] = "Africa/Ouagadougou"
            countries["BG"] = "Europe/Sofia"
            countries["BH"] = "Asia/Bahrain"
            countries["BI"] = "Africa/Bujumbura"
            countries["BJ"] = "Africa/Porto-Novo"
            countries["BL"] = "America/St_Barthelemy"
            countries["BM"] = "Atlantic/Bermuda"
            countries["BMT"] = BEATS_KEYWORD
            countries["BN"] = "Asia/Brunei"
            countries["BQ"] = "America/Kralendijk"
            countries["BR"] = "America/Sao_Paulo"
            countries["BS"] = "America/Toronto"
            countries["BT"] = "Asia/Thimphu"
            countries["BW"] = "Africa/Gaborone"
            countries["BY"] = "Europe/Minsk"
            countries["BZ"] = "America/Belize"
            countries["CA"] = "America/Montreal"
            countries["CC"] = "Indian/Cocos"
            countries["CD"] = "Africa/Kinshasa"
            countries["CDT"] = "America/Chicago"
            countries["CET"] = "CET"
            countries["CF"] = "Africa/Bangui"
            countries["CG"] = "Africa/Brazzaville"
            countries["CH"] = "Europe/Zurich"
            countries["CI"] = "Africa/Abidjan"
            countries["CK"] = "Pacific/Rarotonga"
            countries["CL"] = "America/Santiago"
            countries["CN"] = "Asia/Shanghai"
            countries["CO"] = "America/Bogota"
            countries["CR"] = "America/Costa_Rica"
            countries["CST"] = "America/Chicago"
            countries["CU"] = "Cuba"
            countries["CV"] = "Atlantic/Cape_Verde"
            countries["CW"] = "America/Curacao"
            countries["CX"] = "Indian/Christmas"
            countries["CY"] = "Asia/Nicosia"
            countries["CZ"] = "Europe/Prague"
            countries["DE"] = "Europe/Berlin"
            countries["DJ"] = "Africa/Djibouti"
            countries["DK"] = "Europe/Copenhagen"
            countries["DM"] = "America/Dominica"
            countries["DO"] = "America/Santo_Domingo"
            countries["DZ"] = "Africa/Algiers"
            countries["EC"] = "Pacific/Galapagos"
            countries["EDT"] = "America/New_York"
            countries["EE"] = "Europe/Tallinn"
            countries["EG"] = "Africa/Cairo"
            countries["EH"] = "Africa/El_Aaiun"
            countries["ER"] = "Africa/Asmara"
            countries["ES"] = "Europe/Madrid"
            countries["EST"] = "America/New_York"
            countries["ET"] = "Africa/Addis_Ababa"
            countries["FI"] = "Europe/Helsinki"
            countries["FJ"] = "Pacific/Fiji"
            countries["FK"] = "Atlantic/Stanley"
            countries["FM"] = "Pacific/Port_Moresby"
            countries["FO"] = "Atlantic/Faroe"
            countries["FR"] = "Europe/Paris"
            countries["GA"] = "Asia/Tbilisi"
            countries["GB"] = "Europe/London"
            countries["GD"] = "America/Grenada"
            countries["GE"] = "Asia/Tbilisi"
            countries["GF"] = "America/Cayenne"
            countries["GG"] = "Europe/London"
            countries["GH"] = "Africa/Accra"
            countries["GI"] = "Europe/Gibraltar"
            countries["GL"] = "America/Thule"
            countries["GM"] = "Africa/Banjul"
            countries["GMT"] = "GMT"
            countries["GN"] = "Africa/Conakry"
            countries["GP"] = "America/Guadeloupe"
            countries["GQ"] = "Africa/Malabo"
            countries["GR"] = "Europe/Athens"
            countries["GS"] = "Atlantic/South_Georgia"
            countries["GT"] = "America/Guatemala"
            countries["GU"] = "Pacific/Guam"
            countries["GW"] = "Africa/Bissau"
            countries["GY"] = "America/Guyana"
            countries["HK"] = "Asia/Hong_Kong"
            countries["HN"] = "America/Tegucigalpa"
            countries["HR"] = "Europe/Zagreb"
            countries["HST"] = "Pacific/Honolulu"
            countries["HT"] = "America/Port-au-Prince"
            countries["HU"] = "Europe/Budapest"
            countries["ID"] = "Asia/Jakarta"
            countries["IE"] = "Europe/Dublin"
            countries["IL"] = "Asia/Tel_Aviv"
            countries["IM"] = "Europe/London"
            countries["IN"] = "Asia/Kolkata"
            countries["IQ"] = "Asia/Baghdad"
            countries["IR"] = "Asia/Tehran"
            countries["IS"] = "Atlantic/Reykjavik"
            countries["IT"] = "Europe/Rome"
            countries["JE"] = "Europe/London"
            countries["JM"] = "Jamaica"
            countries["JO"] = "Asia/Amman"
            countries["JP"] = "Asia/Tokyo"
            countries["KE"] = "Africa/Nairobi"
            countries["KG"] = "Asia/Bishkek"
            countries["KH"] = "Asia/Phnom_Penh"
            countries["KI"] = "Pacific/Tarawa"
            countries["KM"] = "Indian/Comoro"
            countries["KN"] = "America/St_Kitts"
            countries["KP"] = "Asia/Pyongyang"
            countries["KR"] = "Asia/Seoul"
            countries["KW"] = "Asia/Riyadh"
            countries["KY"] = "America/Cayman"
            countries["KZ"] = "Asia/Oral"
            countries["LA"] = "Asia/Vientiane"
            countries["LB"] = "Asia/Beirut"
            countries["LC"] = "America/St_Lucia"
            countries["LI"] = "Europe/Vaduz"
            countries["LK"] = "Asia/Colombo"
            countries["LR"] = "Africa/Monrovia"
            countries["LS"] = "Africa/Maseru"
            countries["LT"] = "Europe/Vilnius"
            countries["LU"] = "Europe/Luxembourg"
            countries["LV"] = "Europe/Riga"
            countries["LY"] = "Africa/Tripoli"
            countries["MA"] = "Africa/Casablanca"
            countries["MC"] = "Europe/Monaco"
            countries["MD"] = "Europe/Chisinau"
            countries["MDT"] = "America/Denver"
            countries["ME"] = "Europe/Podgorica"
            countries["MF"] = "America/Marigot"
            countries["MG"] = "Indian/Antananarivo"
            countries["MH"] = "Kwajalein"
            countries["MK"] = "Europe/Skopje"
            countries["ML"] = "Africa/Bamako"
            countries["MM"] = "Asia/Yangon"
            countries["MN"] = "Asia/Ulaanbaatar"
            countries["MO"] = "Asia/Macau"
            countries["MP"] = "Pacific/Saipan"
            countries["MQ"] = "America/Martinique"
            countries["MR"] = "Africa/Nouakchott"
            countries["MS"] = "America/Montserrat"
            countries["MST"] = "America/Denver"
            countries["MT"] = "Europe/Malta"
            countries["MU"] = "Indian/Mauritius"
            countries["MV"] = "Indian/Maldives"
            countries["MW"] = "Africa/Blantyre"
            countries["MX"] = "America/Mexico_City"
            countries["MY"] = "Asia/Kuala_Lumpur"
            countries["MZ"] = "Africa/Maputo"
            countries["NA"] = "Africa/Windhoek"
            countries["NC"] = "Pacific/Noumea"
            countries["NE"] = "Africa/Niamey"
            countries["NF"] = "Pacific/Norfolk"
            countries["NG"] = "Africa/Lagos"
            countries["NI"] = "America/Managua"
            countries["NL"] = "Europe/Amsterdam"
            countries["NO"] = "Europe/Oslo"
            countries["NP"] = "Asia/Kathmandu"
            countries["NR"] = "Pacific/Nauru"
            countries["NU"] = "Pacific/Niue"
            countries["NZ"] = "Pacific/Auckland"
            countries["OM"] = "Asia/Muscat"
            countries["PA"] = "America/Panama"
            countries["PDT"] = "America/Los_Angeles"
            countries["PE"] = "America/Lima"
            countries["PF"] = "Pacific/Tahiti"
            countries["PG"] = "Pacific/Pohnpei"
            countries["PH"] = "Asia/Manila"
            countries["PK"] = "Asia/Karachi"
            countries["PL"] = "Europe/Warsaw"
            countries["PM"] = "America/Miquelon"
            countries["PN"] = "Pacific/Pitcairn"
            countries["PR"] = "America/Puerto_Rico"
            countries["PS"] = "Asia/Gaza"
            countries["PST"] = "America/Los_Angeles"
            countries["PT"] = "Europe/Lisbon"
            countries["PW"] = "Pacific/Palau"
            countries["PY"] = "America/Asuncion"
            countries["QA"] = "Asia/Qatar"
            countries["RE"] = "Indian/Reunion"
            countries["RO"] = "Europe/Bucharest"
            countries["RS"] = "Europe/Belgrade"
            countries["RU"] = "Europe/Moscow"
            countries["RW"] = "Africa/Kigali"
            countries["SA"] = "Africa/Johannesburg"
            countries["SB"] = "Pacific/Guadalcanal"
            countries["SC"] = "Indian/Mahe"
            countries["SD"] = "Africa/Khartoum"
            countries["SE"] = "Europe/Stockholm"
            countries["SG"] = "Asia/Singapore"
            countries["SH"] = "Atlantic/St_Helena"
            countries["SI"] = "Europe/Ljubljana"
            countries["SJ"] = "Atlantic/Jan_Mayen"
            countries["SK"] = "Europe/Bratislava"
            countries["SL"] = "Africa/Freetown"
            countries["SM"] = "Europe/San_Marino"
            countries["SN"] = "Africa/Dakar"
            countries["SO"] = "Africa/Mogadishu"
            countries["SR"] = "America/Paramaribo"
            countries["SS"] = "Africa/Juba"
            countries["ST"] = "Africa/Sao_Tome"
            countries["SV"] = "America/El_Salvador"
            countries["SX"] = "America/Marigot"
            countries["SY"] = "Asia/Damascus"
            countries["SZ"] = "Africa/Mbabane"
            countries["TC"] = "America/Grand_Turk"
            countries["TD"] = "Africa/Ndjamena"
            countries["TF"] = "Indian/Kerguelen"
            countries["TG"] = "Africa/Lome"
            countries["TH"] = "Asia/Bangkok"
            countries["TJ"] = "Asia/Dushanbe"
            countries["TK"] = "Pacific/Fakaofo"
            countries["TL"] = "Asia/Dili"
            countries["TM"] = "Asia/Ashgabat"
            countries["TN"] = "Africa/Tunis"
            countries["TO"] = "Pacific/Tongatapu"
            countries["TR"] = "Europe/Istanbul"
            countries["TT"] = "America/Port_of_Spain"
            countries["TV"] = "Pacific/Funafuti"
            countries["TW"] = "Asia/Taipei"
            countries["TZ"] = "Africa/Dar_es_Salaam"
            countries["UA"] = "Europe/Kiev"
            countries["UG"] = "Africa/Kampala"
            countries["UK"] = "Europe/London"
            countries["UM"] = "Pacific/Johnston"
            countries["US"] = "America/New_York"
            countries["UTC"] = "UTC"
            countries["UY"] = "America/Montevideo"
            countries["UZ"] = "Asia/Tashkent"
            countries["VA"] = "Europe/Vatican"
            countries["VC"] = "America/St_Vincent"
            countries["VE"] = "America/Caracas"
            countries["VG"] = "America/Tortola"
            countries["VI"] = "America/St_Johns"
            countries["VN"] = "Asia/Ho_Chi_Minh"
            countries["VU"] = "Pacific/Efate"
            countries["WF"] = "Pacific/Wallis"
            countries["WS"] = "Pacific/Apia"
            countries["YE"] = "Asia/Aden"
            countries["YT"] = "Indian/Mayotte"
            countries["ZA"] = "Africa/Johannesburg"
            countries["ZM"] = "Africa/Lusaka"
            countries["ZULU"] = "Zulu"
            @Suppress("MagicNumber")
            ZoneId.getAvailableZoneIds().stream()
                .filter { tz: String ->
                    tz.length <= 3 && !countries.containsKey(tz)
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
                @Suppress("MagicNumber")
                sendList(
                    sender,
                    COUNTRIES_MAP.keys.sorted().stream().map { it.padEnd(4) }.toList(),
                    14,
                    isPrivate = false,
                    isIndent = true
                )
            } else {
                val msg = time(args)
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
        with(help) {
            add("To display a country's current date/time:")
            add(helpFormat("%c $TIME_CMD <country code>"))
            add("For a listing of the supported countries:")
            add(helpFormat("%c $TIME_CMD"))
        }
        commands.add(TIME_CMD)
    }
}
