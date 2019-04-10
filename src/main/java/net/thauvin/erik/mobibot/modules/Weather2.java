/*
 * Weather2.java
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
import org.jibble.pircbot.Colors;

import java.util.ArrayList;
import java.util.List;

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
    public static final String OWM_API_KEY_PROP = "owm-api-key";

    // The weather command.
    private static final String WEATHER_CMD = "weather";

    /**
     * Creates a new {@link Weather2} instance.
     */
    public Weather2() {
        commands.add(WEATHER_CMD);
        properties.put(OWM_API_KEY_PROP, "");
    }

    private static String fAndC(final Double d) {
        final double c = (d - 32) * 5 / 9;
        return Math.round(d) + " \u00B0F, " + Math.round(c) + " \u00B0C";
    }

    private static OWM.Country getCountry(final String countryCode) {
        for (final OWM.Country c : OWM.Country.values()) {
            if (c.name().equalsIgnoreCase(countryCode)) {
                return c;
            }
        }

        return OWM.Country.UNITED_STATES;
    }

    static ArrayList<Message> getWeather(final String query, final String apiKey) throws ModuleException {
        if (!Utils.isValidString(apiKey)) {
            throw new ModuleException(Utils.capitalize(WEATHER_CMD) + " is disabled. The API key is missing.");
        }

        final OWM owm = new OWM(apiKey);
        final ArrayList<Message> messages = new ArrayList<>();

        owm.setUnit(OWM.Unit.IMPERIAL);

        if (Utils.isValidString(query)) {
            final String[] argv = query.split(",");

            if (argv.length >= 1 && argv.length <= 2) {
                final String country;
                final String city = argv[0].trim();
                if (argv.length > 1 && Utils.isValidString(argv[1])) {
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
                        messages.add(new NoticeMessage("City: " + cwd.getCityName() + " [" + country + "]"));

                        final Main main = cwd.getMainData();
                        if (main != null) {
                            if (main.hasTemp()) {
                                messages.add(new NoticeMessage("Temperature: " + fAndC(main.getTemp())));
                            }

                            if (main.hasHumidity() && (main.getHumidity() != null)) {
                                messages.add(new PublicMessage("Humidity: " + Math.round(main.getHumidity()) + "%"));
                            }
                        }

                        if (cwd.hasWindData()) {
                            final Wind w = cwd.getWindData();
                            if (w != null && w.hasSpeed()) {
                                messages.add(new PublicMessage("Wind: " + wind(w.getSpeed())));
                            }
                        }

                        if (cwd.hasWeatherList()) {
                            final StringBuilder condition = new StringBuilder("Condition: ");
                            final List<Weather> list = cwd.getWeatherList();
                            if (list != null) {
                                for (final Weather w : list) {
                                    if (condition.indexOf(",") == -1) {
                                        condition.append(Utils.capitalize(w.getDescription()));
                                    } else {
                                        condition.append(", ").append(w.getDescription());
                                    }
                                }
                                messages.add(new PublicMessage(condition.toString()));
                            }
                        }
                        messages.add(new NoticeMessage("https://openweathermap.org/city/"
                            + cwd.getCityId(), Colors.GREEN));
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
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        bot.send(sender, "To display weather information:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + WEATHER_CMD + " <city> [, <country code>]"));
        bot.send(sender, "For example:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + WEATHER_CMD + " paris, fr"));
        bot.send(sender, "The default ISO 3166 country code is " + Utils.bold("US")
            + ". Zip codes are supported in most countries.");
    }

    /**
     * Fetches the weather data from a specific city.
     */
    void run(final Mobibot bot, final String sender, final String args) {
        if (Utils.isValidString(args)) {
            try {
                final ArrayList<Message> messages = getWeather(args, properties.get(OWM_API_KEY_PROP));
                if (messages.get(0).isError()) {
                    helpResponse(bot, sender, args, true);
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
            helpResponse(bot, sender, args, true);
        }
    }
}
