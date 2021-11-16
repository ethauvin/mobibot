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

import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.pircbotx.hooks.types.GenericMessageEvent
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Collections

/**
 * The WorldTime module.
 */
class WorldTime : AbstractModule() {
    companion object {
        // Beats (Internet Time) keyword
        const val BEATS_KEYWORD = ".beats"

        // Supported countries
        var COUNTRIES_MAP: Map<String, String>

        // The Time command
        private const val TIME_CMD = "time"

        // The zones arguments
        private const val ZONES_ARGS = "zones"

        // The default zone
        private const val DEFAULT_ZONE = "PST"

        // Date/Time Format
        private var dtf =
            DateTimeFormatter.ofPattern("'The time is ${"'HH:mm'".bold()} on ${"'EEEE, d MMMM yyyy'".bold()} in '")

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
            val tz = COUNTRIES_MAP[(if (query.isNotBlank()) query.trim().uppercase() else DEFAULT_ZONE)]
            return if (tz != null) {
                if (BEATS_KEYWORD == tz) {
                    "The current Internet Time is ${internetTime().bold()} $BEATS_KEYWORD"
                } else {
                    (ZonedDateTime.now().withZoneSameInstant(ZoneId.of(tz)).format(dtf)
                            + tz.substring(tz.lastIndexOf('/') + 1).replace('_', ' ').bold())
                }
            } else {
                "Unsupported country/zone. Please try again."
            }
        }

        init {
            // Initialize the zones map
            val zones = mutableMapOf<String, String>()
            zones["AD"] = "Europe/Andorra"
            zones["AE"] = "Asia/Dubai"
            zones["AF"] = "Asia/Kabul"
            zones["AG"] = "America/Antigua"
            zones["AI"] = "America/Anguilla"
            zones["AKDT"] = "America/Anchorage"
            zones["AKST"] = "America/Anchorage"
            zones["AL"] = "Europe/Tirane"
            zones["AM"] = "Asia/Yerevan"
            zones["AO"] = "Africa/Luanda"
            zones["AQ"] = "Antarctica/South_Pole"
            zones["AR"] = "America/Argentina/Buenos_Aires"
            zones["AS"] = "Pacific/Pago_Pago"
            zones["AT"] = "Europe/Vienna"
            zones["AU"] = "Australia/Sydney"
            zones["AW"] = "America/Aruba"
            zones["AX"] = "Europe/Mariehamn"
            zones["AZ"] = "Asia/Baku"
            zones["BA"] = "Europe/Sarajevo"
            zones["BB"] = "America/Barbados"
            zones["BD"] = "Asia/Dhaka"
            zones["BE"] = "Europe/Brussels"
            zones["BEAT"] = BEATS_KEYWORD
            zones["BF"] = "Africa/Ouagadougou"
            zones["BG"] = "Europe/Sofia"
            zones["BH"] = "Asia/Bahrain"
            zones["BI"] = "Africa/Bujumbura"
            zones["BJ"] = "Africa/Porto-Novo"
            zones["BL"] = "America/St_Barthelemy"
            zones["BM"] = "Atlantic/Bermuda"
            zones["BMT"] = BEATS_KEYWORD
            zones["BN"] = "Asia/Brunei"
            zones["BO"] = "America/La_Paz"
            zones["BQ"] = "America/Kralendijk"
            zones["BR"] = "America/Sao_Paulo"
            zones["BS"] = "America/Nassau"
            zones["BT"] = "Asia/Thimphu"
            zones["BW"] = "Africa/Gaborone"
            zones["BY"] = "Europe/Minsk"
            zones["BZ"] = "America/Belize"
            zones["CA"] = "America/Montreal"
            zones["CC"] = "Indian/Cocos"
            zones["CD"] = "Africa/Kinshasa"
            zones["CDT"] = "America/Chicago"
            zones["CET"] = "CET"
            zones["CF"] = "Africa/Bangui"
            zones["CG"] = "Africa/Brazzaville"
            zones["CH"] = "Europe/Zurich"
            zones["CI"] = "Africa/Abidjan"
            zones["CK"] = "Pacific/Rarotonga"
            zones["CL"] = "America/Santiago"
            zones["CM"] = "Africa/Douala"
            zones["CN"] = "Asia/Shanghai"
            zones["CO"] = "America/Bogota"
            zones["CR"] = "America/Costa_Rica"
            zones["CST"] = "America/Chicago"
            zones["CU"] = "Cuba"
            zones["CV"] = "Atlantic/Cape_Verde"
            zones["CW"] = "America/Curacao"
            zones["CX"] = "Indian/Christmas"
            zones["CY"] = "Asia/Nicosia"
            zones["CZ"] = "Europe/Prague"
            zones["DE"] = "Europe/Berlin"
            zones["DJ"] = "Africa/Djibouti"
            zones["DK"] = "Europe/Copenhagen"
            zones["DM"] = "America/Dominica"
            zones["DO"] = "America/Santo_Domingo"
            zones["DZ"] = "Africa/Algiers"
            zones["EC"] = "Pacific/Galapagos"
            zones["EDT"] = "America/New_York"
            zones["EE"] = "Europe/Tallinn"
            zones["EG"] = "Africa/Cairo"
            zones["EH"] = "Africa/El_Aaiun"
            zones["ER"] = "Africa/Asmara"
            zones["ES"] = "Europe/Madrid"
            zones["EST"] = "America/New_York"
            zones["ET"] = "Africa/Addis_Ababa"
            zones["FI"] = "Europe/Helsinki"
            zones["FJ"] = "Pacific/Fiji"
            zones["FK"] = "Atlantic/Stanley"
            zones["FM"] = "Pacific/Yap"
            zones["FO"] = "Atlantic/Faroe"
            zones["FR"] = "Europe/Paris"
            zones["GA"] = "Africa/Libreville"
            zones["GB"] = "Europe/London"
            zones["GD"] = "America/Grenada"
            zones["GE"] = "Asia/Tbilisi"
            zones["GF"] = "America/Cayenne"
            zones["GG"] = "Europe/Guernsey"
            zones["GH"] = "Africa/Accra"
            zones["GI"] = "Europe/Gibraltar"
            zones["GL"] = "America/Thule"
            zones["GM"] = "Africa/Banjul"
            zones["GMT"] = "GMT"
            zones["GN"] = "Africa/Conakry"
            zones["GP"] = "America/Guadeloupe"
            zones["GQ"] = "Africa/Malabo"
            zones["GR"] = "Europe/Athens"
            zones["GS"] = "Atlantic/South_Georgia"
            zones["GT"] = "America/Guatemala"
            zones["GU"] = "Pacific/Guam"
            zones["GW"] = "Africa/Bissau"
            zones["GY"] = "America/Guyana"
            zones["HK"] = "Asia/Hong_Kong"
            zones["HN"] = "America/Tegucigalpa"
            zones["HR"] = "Europe/Zagreb"
            zones["HST"] = "Pacific/Honolulu"
            zones["HT"] = "America/Port-au-Prince"
            zones["HU"] = "Europe/Budapest"
            zones["ID"] = "Asia/Jakarta"
            zones["IE"] = "Europe/Dublin"
            zones["IL"] = "Asia/Tel_Aviv"
            zones["IM"] = "Europe/Isle_of_Man"
            zones["IN"] = "Asia/Kolkata"
            zones["IO"] = "Indian/Chagos"
            zones["IQ"] = "Asia/Baghdad"
            zones["IR"] = "Asia/Tehran"
            zones["IS"] = "Atlantic/Reykjavik"
            zones["IT"] = "Europe/Rome"
            zones["JE"] = "Europe/Jersey"
            zones["JM"] = "Jamaica"
            zones["JO"] = "Asia/Amman"
            zones["JP"] = "Asia/Tokyo"
            zones["KE"] = "Africa/Nairobi"
            zones["KG"] = "Asia/Bishkek"
            zones["KH"] = "Asia/Phnom_Penh"
            zones["KI"] = "Pacific/Tarawa"
            zones["KM"] = "Indian/Comoro"
            zones["KN"] = "America/St_Kitts"
            zones["KP"] = "Asia/Pyongyang"
            zones["KR"] = "Asia/Seoul"
            zones["KW"] = "Asia/Riyadh"
            zones["KY"] = "America/Cayman"
            zones["KZ"] = "Asia/Oral"
            zones["LA"] = "Asia/Vientiane"
            zones["LB"] = "Asia/Beirut"
            zones["LC"] = "America/St_Lucia"
            zones["LI"] = "Europe/Vaduz"
            zones["LK"] = "Asia/Colombo"
            zones["LR"] = "Africa/Monrovia"
            zones["LS"] = "Africa/Maseru"
            zones["LT"] = "Europe/Vilnius"
            zones["LU"] = "Europe/Luxembourg"
            zones["LV"] = "Europe/Riga"
            zones["LY"] = "Africa/Tripoli"
            zones["MA"] = "Africa/Casablanca"
            zones["MC"] = "Europe/Monaco"
            zones["MD"] = "Europe/Chisinau"
            zones["MDT"] = "America/Denver"
            zones["ME"] = "Europe/Podgorica"
            zones["MF"] = "America/Marigot"
            zones["MG"] = "Indian/Antananarivo"
            zones["MH"] = "Pacific/Majuro"
            zones["MK"] = "Europe/Skopje"
            zones["ML"] = "Africa/Timbuktu"
            zones["MM"] = "Asia/Yangon"
            zones["MN"] = "Asia/Ulaanbaatar"
            zones["MO"] = "Asia/Macau"
            zones["MP"] = "Pacific/Saipan"
            zones["MQ"] = "America/Martinique"
            zones["MR"] = "Africa/Nouakchott"
            zones["MS"] = "America/Montserrat"
            zones["MST"] = "America/Denver"
            zones["MT"] = "Europe/Malta"
            zones["MU"] = "Indian/Mauritius"
            zones["MV"] = "Indian/Maldives"
            zones["MW"] = "Africa/Blantyre"
            zones["MX"] = "America/Mexico_City"
            zones["MY"] = "Asia/Kuala_Lumpur"
            zones["MZ"] = "Africa/Maputo"
            zones["NA"] = "Africa/Windhoek"
            zones["NC"] = "Pacific/Noumea"
            zones["NE"] = "Africa/Niamey"
            zones["NF"] = "Pacific/Norfolk"
            zones["NG"] = "Africa/Lagos"
            zones["NI"] = "America/Managua"
            zones["NL"] = "Europe/Amsterdam"
            zones["NO"] = "Europe/Oslo"
            zones["NP"] = "Asia/Kathmandu"
            zones["NR"] = "Pacific/Nauru"
            zones["NU"] = "Pacific/Niue"
            zones["NZ"] = "Pacific/Auckland"
            zones["OM"] = "Asia/Muscat"
            zones["PA"] = "America/Panama"
            zones["PDT"] = "America/Los_Angeles"
            zones["PE"] = "America/Lima"
            zones["PF"] = "Pacific/Tahiti"
            zones["PG"] = "Pacific/Port_Moresby"
            zones["PH"] = "Asia/Manila"
            zones["PK"] = "Asia/Karachi"
            zones["PL"] = "Europe/Warsaw"
            zones["PM"] = "America/Miquelon"
            zones["PN"] = "Pacific/Pitcairn"
            zones["PR"] = "America/Puerto_Rico"
            zones["PS"] = "Asia/Gaza"
            zones["PST"] = "America/Los_Angeles"
            zones["PT"] = "Europe/Lisbon"
            zones["PW"] = "Pacific/Palau"
            zones["PY"] = "America/Asuncion"
            zones["QA"] = "Asia/Qatar"
            zones["RE"] = "Indian/Reunion"
            zones["RO"] = "Europe/Bucharest"
            zones["RS"] = "Europe/Belgrade"
            zones["RU"] = "Europe/Moscow"
            zones["RW"] = "Africa/Kigali"
            zones["SA"] = "Asia/Riyadh"
            zones["SB"] = "Pacific/Guadalcanal"
            zones["SC"] = "Indian/Mahe"
            zones["SD"] = "Africa/Khartoum"
            zones["SE"] = "Europe/Stockholm"
            zones["SG"] = "Asia/Singapore"
            zones["SH"] = "Atlantic/St_Helena"
            zones["SI"] = "Europe/Ljubljana"
            zones["SJ"] = "Atlantic/Jan_Mayen"
            zones["SK"] = "Europe/Bratislava"
            zones["SL"] = "Africa/Freetown"
            zones["SM"] = "Europe/San_Marino"
            zones["SN"] = "Africa/Dakar"
            zones["SO"] = "Africa/Mogadishu"
            zones["SR"] = "America/Paramaribo"
            zones["SS"] = "Africa/Juba"
            zones["ST"] = "Africa/Sao_Tome"
            zones["SV"] = "America/El_Salvador"
            zones["SX"] = "America/Lower_Princes"
            zones["SY"] = "Asia/Damascus"
            zones["SZ"] = "Africa/Mbabane"
            zones["TC"] = "America/Grand_Turk"
            zones["TD"] = "Africa/Ndjamena"
            zones["TF"] = "Indian/Kerguelen"
            zones["TG"] = "Africa/Lome"
            zones["TH"] = "Asia/Bangkok"
            zones["TJ"] = "Asia/Dushanbe"
            zones["TK"] = "Pacific/Fakaofo"
            zones["TL"] = "Asia/Dili"
            zones["TM"] = "Asia/Ashgabat"
            zones["TN"] = "Africa/Tunis"
            zones["TO"] = "Pacific/Tongatapu"
            zones["TR"] = "Europe/Istanbul"
            zones["TT"] = "America/Port_of_Spain"
            zones["TV"] = "Pacific/Funafuti"
            zones["TW"] = "Asia/Taipei"
            zones["TZ"] = "Africa/Dar_es_Salaam"
            zones["UA"] = "Europe/Kiev"
            zones["UG"] = "Africa/Kampala"
            zones["UK"] = "Europe/London"
            zones["UM"] = "Pacific/Wake"
            zones["US"] = "America/New_York"
            zones["UTC"] = "UTC"
            zones["UY"] = "America/Montevideo"
            zones["UZ"] = "Asia/Tashkent"
            zones["VA"] = "Europe/Vatican"
            zones["VC"] = "America/St_Vincent"
            zones["VE"] = "America/Caracas"
            zones["VG"] = "America/Tortola"
            zones["VI"] = "America/St_Thomas"
            zones["VN"] = "Asia/Ho_Chi_Minh"
            zones["VU"] = "Pacific/Efate"
            zones["WF"] = "Pacific/Wallis"
            zones["WS"] = "Pacific/Apia"
            zones["YE"] = "Asia/Aden"
            zones["YT"] = "Indian/Mayotte"
            zones["ZA"] = "Africa/Johannesburg"
            zones["ZM"] = "Africa/Lusaka"
            zones["ZULU"] = "Zulu"
            zones["ZW"] = "Africa/Harare"
            ZoneId.getAvailableZoneIds().filter { it.length <= 3 && !zones.containsKey(it) }
                .forEach { tz -> zones[tz] = tz }
            COUNTRIES_MAP = Collections.unmodifiableMap(zones)
        }
    }

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.equals(ZONES_ARGS, true)) {
            event.sendMessage("The supported countries/zones are: ")
            event.sendList(COUNTRIES_MAP.keys.sorted().map { it.padEnd(4) }, 14, isIndent = true)
        } else {
            event.respond(time(args))
        }
    }

    override val isPrivateMsgEnabled = true

    init {
        with(help) {
            add("To display a country's current date/time:")
            add(helpFormat("%c $TIME_CMD [<country code or zone>]"))
            add("For a listing of the supported countries/zones:")
            add(helpFormat("%c $TIME_CMD $ZONES_ARGS"))
        }
        commands.add(TIME_CMD)
    }
}
