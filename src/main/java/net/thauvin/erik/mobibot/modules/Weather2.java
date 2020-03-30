/*
 * Weather2.java
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

package net.thauvin.erik.mobibot.modules;

import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.param.Main;
import net.aksingh.owmjapis.model.param.Weather;
import net.aksingh.owmjapis.model.param.Wind;
import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.Utils;
import net.thauvin.erik.mobibot.msg.ErrorMessage;
import net.thauvin.erik.mobibot.msg.Message;
import net.thauvin.erik.mobibot.msg.NoticeMessage;
import net.thauvin.erik.mobibot.msg.PublicMessage;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.jibble.pircbot.Colors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.thauvin.erik.mobibot.Utils.bold;

/**
 * The <code>Weather2</code> module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2017-04-02
 * @since 1.0
 */
public class Weather2 extends ThreadedModule {
    /**
     * The OpenWeatherMap API Key property.
     */
    static final String OWM_API_KEY_PROP = "owm-api-key";

    // Weather command
    private static final String WEATHER_CMD = "weather";

    /**
     * Creates a new {@link Weather2} instance.
     */
    public Weather2() {
        super();
        commands.add(WEATHER_CMD);
        properties.put(OWM_API_KEY_PROP, "");
    }

    private static OWM.Country getCountry(final String countryCode) {
        for (final OWM.Country c : OWM.Country.values()) {
            if (c.name().equalsIgnoreCase(countryCode)) {
                return c;
            }
        }

        return OWM.Country.UNITED_STATES;
    }

    @SuppressWarnings("AvoidEscapedUnicodeCharacters")
    private static String getTemps(final Double d) {
        final double c = (d - 32) * 5 / 9;
        return Math.round(d) + " °F, " + Math.round(c) + " °C";
    }

    /**
     * Retrieves the weather data.
     *
     * <ul>
     * <li>98204</li>
     * <li>London, UK</li>
     * </ul>
     *
     * @param query  The query.
     * @param apiKey The API key.
     * @return The {@link Message} array containing the weather data.
     * @throws ModuleException If an error occurs while retrieving the weather data.
     */
    static List<Message> getWeather(final String query, final String apiKey) throws ModuleException {
        if (StringUtils.isBlank(apiKey)) {
            throw new ModuleException(StringUtils.capitalize(WEATHER_CMD) + " is disabled. The API key is missing.");
        }

        final OWM owm = new OWM(apiKey);
        final ArrayList<Message> messages = new ArrayList<>();

        owm.setUnit(OWM.Unit.IMPERIAL);

        if (StringUtils.isNotBlank(query)) {
            final String[] argv = query.split(",");

            if (argv.length >= 1 && argv.length <= 2) {
                final String country;
                final String city = argv[0].trim();
                if (argv.length > 1 && StringUtils.isNotBlank(argv[1])) {
                    country = argv[1].trim();
                } else {
                    country = "US";
                }

                try {
                    final CurrentWeather cwd;
                    if (city.matches("\\d+")) {
                        cwd = owm.currentWeatherByZipCode(Integer.parseInt(city), getCountry(country));
                    } else {
                        cwd = owm.currentWeatherByCityName(city, getCountry(country));
                    }
                    if (cwd.hasCityName()) {
                        messages.add(new PublicMessage(
                                "City: " + cwd.getCityName() + " [" + StringUtils.upperCase(country) + "]"));

                        final Main main = cwd.getMainData();
                        if (main != null) {
                            if (main.hasTemp()) {
                                messages.add(new PublicMessage("Temperature: " + getTemps(main.getTemp())));
                            }

                            if (main.hasHumidity() && (main.getHumidity() != null)) {
                                messages.add(new NoticeMessage("Humidity: " + Math.round(main.getHumidity()) + "%"));
                            }
                        }

                        if (cwd.hasWindData()) {
                            final Wind w = cwd.getWindData();
                            if (w != null && w.hasSpeed()) {
                                messages.add(new NoticeMessage("Wind: " + wind(w.getSpeed())));
                            }
                        }

                        if (cwd.hasWeatherList()) {
                            final StringBuilder condition = new StringBuilder("Condition:");
                            final List<Weather> list = cwd.getWeatherList();
                            if (list != null) {
                                for (final Weather w : list) {
                                    condition.append(' ')
                                             .append(StringUtils.capitalize(w.getDescription()))
                                             .append('.');
                                }
                                messages.add(new NoticeMessage(condition.toString()));
                            }
                        }

                        if (cwd.hasCityId() && cwd.getCityId() != null) {
                            if (cwd.getCityId() > 0) {
                                messages.add(new NoticeMessage("https://openweathermap.org/city/" + cwd.getCityId(),
                                                               Colors.GREEN));
                            } else {
                                final HttpUrl url = Objects.requireNonNull(HttpUrl.parse(
                                        "https://openweathermap.org/find"))
                                                           .newBuilder()
                                                           .addQueryParameter("q",
                                                                              city + ','
                                                                              + StringUtils.upperCase(country))
                                                           .build();
                                messages.add(new NoticeMessage(url.toString(), Colors.GREEN));
                            }
                        }
                    }
                } catch (APIException | NullPointerException e) {
                    throw new ModuleException("getWeather(" + query + ')', "Unable to perform weather lookup.", e);
                }
            }
        }

        if (messages.isEmpty()) {
            messages.add(new ErrorMessage("Invalid syntax."));
        }

        return messages;
    }

    private static String wind(final Double w) {
        final double kmh = w * 1.60934;
        return Math.round(w) + " mph, " + Math.round(kmh) + " km/h";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final boolean isPrivate) {
        bot.send(sender, "To display weather information:", isPrivate);
        bot.send(sender,
                 Utils.helpIndent(bot.getNick() + ": " + WEATHER_CMD + " <city> [, <country code>]"),
                 isPrivate);
        bot.send(sender, "For example:", isPrivate);
        bot.send(sender, Utils.helpIndent(bot.getNick() + ": " + WEATHER_CMD + " paris, fr"), isPrivate);
        bot.send(sender,
                 "The default ISO 3166 country code is " + bold("US")
                 + ". Zip codes are supported in most countries.", isPrivate);
    }

    /**
     * Fetches the weather data from a specific city.
     */
    @Override
    void run(final Mobibot bot, final String sender, final String cmd, final String args, final boolean isPrivate) {
        if (StringUtils.isNotBlank(args)) {
            try {
                final List<Message> messages = getWeather(args, properties.get(OWM_API_KEY_PROP));
                if (messages.get(0).isError()) {
                    helpResponse(bot, sender, isPrivate);
                } else {
                    for (final Message msg : messages) {
                        bot.send(sender, msg);
                    }
                }
            } catch (ModuleException e) {
                bot.getLogger().debug(e.getDebugMessage(), e);
                bot.send(e.getMessage());
            }
        } else {
            helpResponse(bot, sender, isPrivate);
        }
    }
}
