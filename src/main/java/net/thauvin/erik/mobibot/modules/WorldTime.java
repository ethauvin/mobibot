/*
 * WorldTime.java
 *
 * Copyright (c) 2004-2017, Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot.modules;

import net.thauvin.erik.mobibot.Mobibot;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Map;
import java.util.TreeMap;

/**
 * The WorldTime module.
 *
 * @author <a href="http://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2014-04-27
 * @since 1.0
 */
final public class WorldTime extends AbstractModule {
    /**
     * The time command.
     */
    public static final String TIME_CMD = "time";

    // The beats (Internet Time) keyword.
    private static final String BEATS_KEYWORD = ".beats";

    // The supported countries.
    private static final Map<String, String> COUNTRIES_MAP = new TreeMap<>();

    /**
     * Creates a new {@link WorldTime} instance.
     */
    public WorldTime() {
        commands.add(TIME_CMD);

        // Initialize the countries map
        COUNTRIES_MAP.put("AE", "Asia/Dubai");
        COUNTRIES_MAP.put("AF", "Asia/Kabul");
        COUNTRIES_MAP.put("AQ", "Antarctica/South_Pole");
        COUNTRIES_MAP.put("AT", "Europe/Vienna");
        COUNTRIES_MAP.put("AU", "Australia/Sydney");
        COUNTRIES_MAP.put("AKST", "America/Anchorage");
        COUNTRIES_MAP.put("AKDT", "America/Anchorage");
        COUNTRIES_MAP.put("BE", "Europe/Brussels");
        COUNTRIES_MAP.put("BR", "America/Sao_Paulo");
        COUNTRIES_MAP.put("CA", "America/Montreal");
        COUNTRIES_MAP.put("CDT", "America/Chicago");
        COUNTRIES_MAP.put("CET", "CET");
        COUNTRIES_MAP.put("CH", "Europe/Zurich");
        COUNTRIES_MAP.put("CN", "Asia/Shanghai");
        COUNTRIES_MAP.put("CST", "America/Chicago");
        COUNTRIES_MAP.put("CU", "Cuba");
        COUNTRIES_MAP.put("DE", "Europe/Berlin");
        COUNTRIES_MAP.put("DK", "Europe/Copenhagen");
        COUNTRIES_MAP.put("EDT", "America/New_York");
        COUNTRIES_MAP.put("EG", "Africa/Cairo");
        COUNTRIES_MAP.put("ER", "Africa/Asmara");
        COUNTRIES_MAP.put("ES", "Europe/Madrid");
        COUNTRIES_MAP.put("EST", "America/New_York");
        COUNTRIES_MAP.put("FI", "Europe/Helsinki");
        COUNTRIES_MAP.put("FR", "Europe/Paris");
        COUNTRIES_MAP.put("GB", "Europe/London");
        COUNTRIES_MAP.put("GMT", "GMT");
        COUNTRIES_MAP.put("GR", "Europe/Athens");
        COUNTRIES_MAP.put("HK", "Asia/Hong_Kong");
        COUNTRIES_MAP.put("HST", "Pacific/Honolulu");
        COUNTRIES_MAP.put("IE", "Europe/Dublin");
        COUNTRIES_MAP.put("IL", "Asia/Tel_Aviv");
        COUNTRIES_MAP.put("IN", "Asia/Kolkata");
        COUNTRIES_MAP.put("IQ", "Asia/Baghdad");
        COUNTRIES_MAP.put("IR", "Asia/Tehran");
        COUNTRIES_MAP.put("IS", "Atlantic/Reykjavik");
        COUNTRIES_MAP.put("IT", "Europe/Rome");
        COUNTRIES_MAP.put("JM", "Jamaica");
        COUNTRIES_MAP.put("JP", "Asia/Tokyo");
        COUNTRIES_MAP.put("LY", "Africa/Tripoli");
        COUNTRIES_MAP.put("MA", "Africa/Casablanca");
        COUNTRIES_MAP.put("MDT", "America/Denver");
        COUNTRIES_MAP.put("MH", "Kwajalein");
        COUNTRIES_MAP.put("MQ", "America/Martinique");
        COUNTRIES_MAP.put("MST", "America/Denver");
        COUNTRIES_MAP.put("MX", "America/Mexico_City");
        COUNTRIES_MAP.put("NL", "Europe/Amsterdam");
        COUNTRIES_MAP.put("NO", "Europe/Oslo");
        COUNTRIES_MAP.put("NP", "Asia/Katmandu");
        COUNTRIES_MAP.put("NZ", "Pacific/Auckland");
        COUNTRIES_MAP.put("PDT", "America/Los_Angeles");
        COUNTRIES_MAP.put("PH", "Asia/Manila");
        COUNTRIES_MAP.put("PK", "Asia/Karachi");
        COUNTRIES_MAP.put("PL", "Europe/Warsaw");
        COUNTRIES_MAP.put("PST", "America/Los_Angeles");
        COUNTRIES_MAP.put("PT", "Europe/Lisbon");
        COUNTRIES_MAP.put("PR", "America/Puerto_Rico");
        COUNTRIES_MAP.put("RU", "Europe/Moscow");
        COUNTRIES_MAP.put("SE", "Europe/Stockholm");
        COUNTRIES_MAP.put("SG", "Asia/Singapore");
        COUNTRIES_MAP.put("TH", "Asia/Bangkok");
        COUNTRIES_MAP.put("TM", "Asia/Ashgabat");
        COUNTRIES_MAP.put("TN", "Africa/Tunis");
        COUNTRIES_MAP.put("TR", "Europe/Istanbul");
        COUNTRIES_MAP.put("TW", "Asia/Taipei");
        COUNTRIES_MAP.put("UK", "Europe/London");
        COUNTRIES_MAP.put("US", "America/New_York");
        COUNTRIES_MAP.put("UTC", "UTC");
        COUNTRIES_MAP.put("VA", "Europe/Vatican");
        COUNTRIES_MAP.put("VE", "America/Caracas");
        COUNTRIES_MAP.put("VN", "Asia/Ho_Chi_Minh");
        COUNTRIES_MAP.put("ZA", "Africa/Johannesburg");
        COUNTRIES_MAP.put("ZULU", "Zulu");
        COUNTRIES_MAP.put("INTERNET", BEATS_KEYWORD);
        COUNTRIES_MAP.put("BEATS", BEATS_KEYWORD);

        ZoneId.getAvailableZoneIds().stream().filter(
                tz -> !tz.contains("/") && tz.length() == 3 && !COUNTRIES_MAP.containsKey(tz)).forEach(
                tz -> COUNTRIES_MAP.put(tz, tz));
    }

    /**
     * Responds with the current time in the specified timezone/country.
     *
     * @param bot       The bot's instance.
     * @param sender    The sender.
     * @param args      The command arguments.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    @Override
    public void commandResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        boolean isInvalidTz = false;
        final String tz = (COUNTRIES_MAP.get((args.substring(args.indexOf(' ') + 1).trim().toUpperCase())));
        final String response;

        if (tz != null) {
            if (tz.equals(BEATS_KEYWORD)) {
                response = ("The current Internet Time is: " + internetTime() + ' ' + BEATS_KEYWORD);
            } else {
                response = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(tz)).format(
                        DateTimeFormatter.ofPattern("'The time is 'HH:mm' on 'EEEE, d MMMM yyyy' in '"))
                        + tz.substring(tz.indexOf('/') + 1).replace('_', ' ');
            }
        } else {
            isInvalidTz = true;
            response = "The supported countries/zones are: " + COUNTRIES_MAP.keySet().toString();
        }

        if (isPrivate) {
            bot.send(sender, response, true);
        } else {
            if (isInvalidTz) {
                bot.send(sender, response);
            } else {
                bot.send(bot.getChannel(), response);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        bot.send(sender, "To display a country's current date/time:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + TIME_CMD) + " [<country code>]");

        bot.send(sender, "For a listing of the supported countries:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + TIME_CMD));
    }

    /**
     * Returns the current Internet (beat) Time.
     *
     * @return The Internet Time string.
     */
    private String internetTime() {
        final ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("UTC+01:00"));
        final int beats = (int) ((zdt.get(ChronoField.SECOND_OF_MINUTE) + (zdt.get(ChronoField.MINUTE_OF_HOUR) * 60)
                + (zdt.get(ChronoField.HOUR_OF_DAY) * 3600)) / 86.4);
        return String.format("%c%03d", '@', beats);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivateMsgEnabled() {
        return true;
    }
}