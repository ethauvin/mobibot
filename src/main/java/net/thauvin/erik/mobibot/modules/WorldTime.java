/*
 * WorldTime.java
 *
 * Copyright (c) 2004-2019, Erik C. Thauvin (erik@thauvin.net)
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.Constants;
import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.msg.ErrorMessage;
import net.thauvin.erik.mobibot.msg.Message;
import net.thauvin.erik.mobibot.msg.PublicMessage;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * The WorldTime module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2014-04-27
 * @since 1.0
 */
public final class WorldTime extends AbstractModule {
    // The beats (Internet Time) keyword.
    private static final String BEATS_KEYWORD = ".beats";
    // The supported countries.
    private static final Map<String, String> COUNTRIES_MAP;

    /**
     * The time command.
     */
    private static final String TIME_CMD = "time";

    static {
        // Initialize the countries map
        final Map<String, String> countries = new TreeMap<>();
        countries.put("AE", "Asia/Dubai");
        countries.put("AF", "Asia/Kabul");
        countries.put("AQ", "Antarctica/South_Pole");
        countries.put("AT", "Europe/Vienna");
        countries.put("AU", "Australia/Sydney");
        countries.put("AKST", "America/Anchorage");
        countries.put("AKDT", "America/Anchorage");
        countries.put("BE", "Europe/Brussels");
        countries.put("BR", "America/Sao_Paulo");
        countries.put("CA", "America/Montreal");
        countries.put("CDT", "America/Chicago");
        countries.put("CET", "CET");
        countries.put("CH", "Europe/Zurich");
        countries.put("CN", "Asia/Shanghai");
        countries.put("CST", "America/Chicago");
        countries.put("CU", "Cuba");
        countries.put("DE", "Europe/Berlin");
        countries.put("DK", "Europe/Copenhagen");
        countries.put("EDT", "America/New_York");
        countries.put("EG", "Africa/Cairo");
        countries.put("ER", "Africa/Asmara");
        countries.put("ES", "Europe/Madrid");
        countries.put("EST", "America/New_York");
        countries.put("FI", "Europe/Helsinki");
        countries.put("FR", "Europe/Paris");
        countries.put("GB", "Europe/London");
        countries.put("GMT", "GMT");
        countries.put("GR", "Europe/Athens");
        countries.put("HK", "Asia/Hong_Kong");
        countries.put("HST", "Pacific/Honolulu");
        countries.put("IE", "Europe/Dublin");
        countries.put("IL", "Asia/Tel_Aviv");
        countries.put("IN", "Asia/Kolkata");
        countries.put("IQ", "Asia/Baghdad");
        countries.put("IR", "Asia/Tehran");
        countries.put("IS", "Atlantic/Reykjavik");
        countries.put("IT", "Europe/Rome");
        countries.put("JM", "Jamaica");
        countries.put("JP", "Asia/Tokyo");
        countries.put("LY", "Africa/Tripoli");
        countries.put("MA", "Africa/Casablanca");
        countries.put("MDT", "America/Denver");
        countries.put("MH", "Kwajalein");
        countries.put("MQ", "America/Martinique");
        countries.put("MST", "America/Denver");
        countries.put("MX", "America/Mexico_City");
        countries.put("NL", "Europe/Amsterdam");
        countries.put("NO", "Europe/Oslo");
        countries.put("NP", "Asia/Katmandu");
        countries.put("NZ", "Pacific/Auckland");
        countries.put("PDT", "America/Los_Angeles");
        countries.put("PH", "Asia/Manila");
        countries.put("PK", "Asia/Karachi");
        countries.put("PL", "Europe/Warsaw");
        countries.put("PST", "America/Los_Angeles");
        countries.put("PT", "Europe/Lisbon");
        countries.put("PR", "America/Puerto_Rico");
        countries.put("RU", "Europe/Moscow");
        countries.put("SE", "Europe/Stockholm");
        countries.put("SG", "Asia/Singapore");
        countries.put("TH", "Asia/Bangkok");
        countries.put("TM", "Asia/Ashgabat");
        countries.put("TN", "Africa/Tunis");
        countries.put("TR", "Europe/Istanbul");
        countries.put("TW", "Asia/Taipei");
        countries.put("UK", "Europe/London");
        countries.put("US", "America/New_York");
        countries.put("UTC", "UTC");
        countries.put("VA", "Europe/Vatican");
        countries.put("VE", "America/Caracas");
        countries.put("VN", "Asia/Ho_Chi_Minh");
        countries.put("ZA", "Africa/Johannesburg");
        countries.put("ZULU", "Zulu");
        countries.put("INTERNET", BEATS_KEYWORD);
        countries.put("BEATS", BEATS_KEYWORD);

        ZoneId.getAvailableZoneIds().stream().filter(tz ->
            !tz.contains("/") && tz.length() == 3 && !countries.containsKey(tz)).forEach(tz ->
            countries.put(tz, tz));

        COUNTRIES_MAP = Collections.unmodifiableMap(countries);
    }

    /**
     * Creates a new {@link WorldTime} instance.
     */
    public WorldTime() {
        commands.add(TIME_CMD);
    }

    /**
     * Returns the current Internet (beat) Time.
     *
     * @return The Internet Time string.
     */
    private static String internetTime() {
        final ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("UTC+01:00"));
        final int beats = (int) ((zdt.get(ChronoField.SECOND_OF_MINUTE) + (zdt.get(ChronoField.MINUTE_OF_HOUR) * 60)
            + (zdt.get(ChronoField.HOUR_OF_DAY) * 3600)) / 86.4);
        return String.format("%c%03d", '@', beats);
    }

    /**
     * Returns the world time.
     *
     * <ul>
     * <li>PST</li>
     * <li>BEATS</li>
     * </ul>
     *
     * @param query The query.
     * @return The {@link Message} containing the world time.
     */
    @SuppressFBWarnings(value = "STT_STRING_PARSING_A_FIELD")
    static Message worldTime(final String query) {
        final String tz = (COUNTRIES_MAP.get((query.substring(query.indexOf(' ') + 1).trim().toUpperCase())));
        final String response;

        if (tz != null) {
            if (BEATS_KEYWORD.equals(tz)) {
                response = ("The current Internet Time is: " + internetTime() + ' ' + BEATS_KEYWORD);
            } else {
                response = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(tz)).format(
                    DateTimeFormatter.ofPattern("'The time is 'HH:mm' on 'EEEE, d MMMM yyyy' in '"))
                    + tz.substring(tz.indexOf('/') + 1).replace('_', ' ');
            }
        } else {
            return new ErrorMessage("The supported countries/zones are: " + COUNTRIES_MAP.keySet());
        }

        return new PublicMessage(response);
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
        final Message msg = worldTime(args);

        if (isPrivate) {
            bot.send(sender, msg.getMessage(), true);
        } else {
            if (msg.isError()) {
                bot.send(sender, msg.getMessage());
            } else {
                bot.send(msg.getMessage());
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
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivateMsgEnabled() {
        return true;
    }
}
