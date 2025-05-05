/*
 * WorldTime.kt
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

        /**
         * Supported countries
         */
        val COUNTRIES_MAP = buildMap<String, String> {
            put("AG", "America/Antigua")
            put("AI", "America/Anguilla")
            put("AE", "Asia/Dubai")
            put("AD", "Europe/Andorra")
            put("AKDT", "America/Anchorage")
            put("AF", "Asia/Kabul")
            put("AKST", "America/Anchorage")
            put("AL", "Europe/Tirane")
            put("AM", "Asia/Yerevan")
            put("AO", "Africa/Luanda")
            put("AQ", "Antarctica/South_Pole")
            put("AR", "America/Argentina/Buenos_Aires")
            put("AS", "Pacific/Pago_Pago")
            put("AT", "Europe/Vienna")
            put("AU", "Australia/Sydney")
            put("AW", "America/Aruba")
            put("AX", "Europe/Mariehamn")
            put("AZ", "Asia/Baku")
            put("BA", "Europe/Sarajevo")
            put("BB", "America/Barbados")
            put("BD", "Asia/Dhaka")
            put("BE", "Europe/Brussels")
            put("BEAT", BEATS_KEYWORD)
            put("BF", "Africa/Ouagadougou")
            put("BG", "Europe/Sofia")
            put("BH", "Asia/Bahrain")
            put("BI", "Africa/Bujumbura")
            put("BJ", "Africa/Porto-Novo")
            put("BL", "America/St_Barthelemy")
            put("BM", "Atlantic/Bermuda")
            put("BMT", BEATS_KEYWORD)
            put("BN", "Asia/Brunei")
            put("BO", "America/La_Paz")
            put("BQ", "America/Kralendijk")
            put("BR", "America/Sao_Paulo")
            put("BS", "America/Nassau")
            put("BT", "Asia/Thimphu")
            put("BW", "Africa/Gaborone")
            put("BY", "Europe/Minsk")
            put("BZ", "America/Belize")
            put("CA", "America/Montreal")
            put("CC", "Indian/Cocos")
            put("CD", "Africa/Kinshasa")
            put("CDT", "America/Chicago")
            put("CET", "CET")
            put("CF", "Africa/Bangui")
            put("CG", "Africa/Brazzaville")
            put("CH", "Europe/Zurich")
            put("CI", "Africa/Abidjan")
            put("CK", "Pacific/Rarotonga")
            put("CL", "America/Santiago")
            put("CM", "Africa/Douala")
            put("CN", "Asia/Shanghai")
            put("CO", "America/Bogota")
            put("CR", "America/Costa_Rica")
            put("CST", "America/Chicago")
            put("CU", "Cuba")
            put("CV", "Atlantic/Cape_Verde")
            put("CW", "America/Curacao")
            put("CX", "Indian/Christmas")
            put("CY", "Asia/Nicosia")
            put("CZ", "Europe/Prague")
            put("DE", "Europe/Berlin")
            put("DJ", "Africa/Djibouti")
            put("DK", "Europe/Copenhagen")
            put("DM", "America/Dominica")
            put("DO", "America/Santo_Domingo")
            put("DZ", "Africa/Algiers")
            put("EC", "Pacific/Galapagos")
            put("EDT", "America/New_York")
            put("EE", "Europe/Tallinn")
            put("EG", "Africa/Cairo")
            put("EH", "Africa/El_Aaiun")
            put("ER", "Africa/Asmara")
            put("ES", "Europe/Madrid")
            put("EST", "America/New_York")
            put("ET", "Africa/Addis_Ababa")
            put("FI", "Europe/Helsinki")
            put("FJ", "Pacific/Fiji")
            put("FK", "Atlantic/Stanley")
            put("FM", "Pacific/Yap")
            put("FO", "Atlantic/Faroe")
            put("FR", "Europe/Paris")
            put("GA", "Africa/Libreville")
            put("GB", "Europe/London")
            put("GD", "America/Grenada")
            put("GE", "Asia/Tbilisi")
            put("GF", "America/Cayenne")
            put("GG", "Europe/Guernsey")
            put("GH", "Africa/Accra")
            put("GI", "Europe/Gibraltar")
            put("GL", "America/Thule")
            put("GM", "Africa/Banjul")
            put("GMT", "GMT")
            put("GN", "Africa/Conakry")
            put("GP", "America/Guadeloupe")
            put("GQ", "Africa/Malabo")
            put("GR", "Europe/Athens")
            put("GS", "Atlantic/South_Georgia")
            put("GT", "America/Guatemala")
            put("GU", "Pacific/Guam")
            put("GW", "Africa/Bissau")
            put("GY", "America/Guyana")
            put("HK", "Asia/Hong_Kong")
            put("HN", "America/Tegucigalpa")
            put("HR", "Europe/Zagreb")
            put("HST", "Pacific/Honolulu")
            put("HT", "America/Port-au-Prince")
            put("HU", "Europe/Budapest")
            put("ID", "Asia/Jakarta")
            put("IE", "Europe/Dublin")
            put("IL", "Asia/Tel_Aviv")
            put("IM", "Europe/Isle_of_Man")
            put("IN", "Asia/Kolkata")
            put("IO", "Indian/Chagos")
            put("IQ", "Asia/Baghdad")
            put("IR", "Asia/Tehran")
            put("IS", "Atlantic/Reykjavik")
            put("IT", "Europe/Rome")
            put("JE", "Europe/Jersey")
            put("JM", "Jamaica")
            put("JO", "Asia/Amman")
            put("JP", "Asia/Tokyo")
            put("KE", "Africa/Nairobi")
            put("KG", "Asia/Bishkek")
            put("KH", "Asia/Phnom_Penh")
            put("KI", "Pacific/Tarawa")
            put("KM", "Indian/Comoro")
            put("KN", "America/St_Kitts")
            put("KP", "Asia/Pyongyang")
            put("KR", "Asia/Seoul")
            put("KW", "Asia/Riyadh")
            put("KY", "America/Cayman")
            put("KZ", "Asia/Oral")
            put("LA", "Asia/Vientiane")
            put("LB", "Asia/Beirut")
            put("LC", "America/St_Lucia")
            put("LI", "Europe/Vaduz")
            put("LK", "Asia/Colombo")
            put("LR", "Africa/Monrovia")
            put("LS", "Africa/Maseru")
            put("LT", "Europe/Vilnius")
            put("LU", "Europe/Luxembourg")
            put("LV", "Europe/Riga")
            put("LY", "Africa/Tripoli")
            put("MA", "Africa/Casablanca")
            put("MC", "Europe/Monaco")
            put("MD", "Europe/Chisinau")
            put("MDT", "America/Denver")
            put("ME", "Europe/Podgorica")
            put("MF", "America/Marigot")
            put("MG", "Indian/Antananarivo")
            put("MH", "Pacific/Majuro")
            put("MK", "Europe/Skopje")
            put("ML", "Africa/Timbuktu")
            put("MM", "Asia/Yangon")
            put("MN", "Asia/Ulaanbaatar")
            put("MO", "Asia/Macau")
            put("MP", "Pacific/Saipan")
            put("MQ", "America/Martinique")
            put("MR", "Africa/Nouakchott")
            put("MS", "America/Montserrat")
            put("MST", "America/Denver")
            put("MT", "Europe/Malta")
            put("MU", "Indian/Mauritius")
            put("MV", "Indian/Maldives")
            put("MW", "Africa/Blantyre")
            put("MX", "America/Mexico_City")
            put("MY", "Asia/Kuala_Lumpur")
            put("MZ", "Africa/Maputo")
            put("NA", "Africa/Windhoek")
            put("NC", "Pacific/Noumea")
            put("NE", "Africa/Niamey")
            put("NF", "Pacific/Norfolk")
            put("NG", "Africa/Lagos")
            put("NI", "America/Managua")
            put("NL", "Europe/Amsterdam")
            put("NO", "Europe/Oslo")
            put("NP", "Asia/Kathmandu")
            put("NR", "Pacific/Nauru")
            put("NU", "Pacific/Niue")
            put("NZ", "Pacific/Auckland")
            put("OM", "Asia/Muscat")
            put("PA", "America/Panama")
            put("PDT", "America/Los_Angeles")
            put("PE", "America/Lima")
            put("PF", "Pacific/Tahiti")
            put("PG", "Pacific/Port_Moresby")
            put("PH", "Asia/Manila")
            put("PK", "Asia/Karachi")
            put("PL", "Europe/Warsaw")
            put("PM", "America/Miquelon")
            put("PN", "Pacific/Pitcairn")
            put("PR", "America/Puerto_Rico")
            put("PS", "Asia/Gaza")
            put("PST", "America/Los_Angeles")
            put("PT", "Europe/Lisbon")
            put("PW", "Pacific/Palau")
            put("PY", "America/Asuncion")
            put("QA", "Asia/Qatar")
            put("RE", "Indian/Reunion")
            put("RO", "Europe/Bucharest")
            put("RS", "Europe/Belgrade")
            put("RU", "Europe/Moscow")
            put("RW", "Africa/Kigali")
            put("SA", "Asia/Riyadh")
            put("SB", "Pacific/Guadalcanal")
            put("SC", "Indian/Mahe")
            put("SD", "Africa/Khartoum")
            put("SE", "Europe/Stockholm")
            put("SG", "Asia/Singapore")
            put("SH", "Atlantic/St_Helena")
            put("SI", "Europe/Ljubljana")
            put("SJ", "Atlantic/Jan_Mayen")
            put("SK", "Europe/Bratislava")
            put("SL", "Africa/Freetown")
            put("SM", "Europe/San_Marino")
            put("SN", "Africa/Dakar")
            put("SO", "Africa/Mogadishu")
            put("SR", "America/Paramaribo")
            put("SS", "Africa/Juba")
            put("ST", "Africa/Sao_Tome")
            put("SV", "America/El_Salvador")
            put("SX", "America/Lower_Princes")
            put("SY", "Asia/Damascus")
            put("SZ", "Africa/Mbabane")
            put("TC", "America/Grand_Turk")
            put("TD", "Africa/Ndjamena")
            put("TF", "Indian/Kerguelen")
            put("TG", "Africa/Lome")
            put("TH", "Asia/Bangkok")
            put("TJ", "Asia/Dushanbe")
            put("TK", "Pacific/Fakaofo")
            put("TL", "Asia/Dili")
            put("TM", "Asia/Ashgabat")
            put("TN", "Africa/Tunis")
            put("TO", "Pacific/Tongatapu")
            put("TR", "Europe/Istanbul")
            put("TT", "America/Port_of_Spain")
            put("TV", "Pacific/Funafuti")
            put("TW", "Asia/Taipei")
            put("TZ", "Africa/Dar_es_Salaam")
            put("UA", "Europe/Kiev")
            put("UG", "Africa/Kampala")
            put("UK", "Europe/London")
            put("UM", "Pacific/Wake")
            put("US", "America/New_York")
            put("UTC", "UTC")
            put("UY", "America/Montevideo")
            put("UZ", "Asia/Tashkent")
            put("VA", "Europe/Vatican")
            put("VC", "America/St_Vincent")
            put("VE", "America/Caracas")
            put("VG", "America/Tortola")
            put("VI", "America/St_Thomas")
            put("VN", "Asia/Ho_Chi_Minh")
            put("VU", "Pacific/Efate")
            put("WF", "Pacific/Wallis")
            put("WS", "Pacific/Apia")
            put("YE", "Asia/Aden")
            put("YT", "Indian/Mayotte")
            put("ZA", "Africa/Johannesburg")
            put("ZM", "Africa/Lusaka")
            put("ZULU", "Zulu")
            put("ZW", "Africa/Harare")
            ZoneId.getAvailableZoneIds().filter { it.length <= 3 && !containsKey(it) }
                .forEach { tz -> put(tz, tz) }
        }

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
    }

    init {
        with(help) {
            add("To display a country's current date/time:")
            add(helpFormat("%c $TIME_CMD [<country code or zone>]"))
            add("For a listing of the supported countries/zones:")
            add(helpFormat("%c $TIME_CMD $ZONES_ARGS"))
        }
        commands.add(TIME_CMD)
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
}
