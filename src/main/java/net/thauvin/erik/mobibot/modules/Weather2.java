/*
 * Weather2.java
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

import net.aksingh.owmjapis.CurrentWeather;
import net.aksingh.owmjapis.OpenWeatherMap;
import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.Utils;

import java.io.IOException;

/**
 * The <code>Weather2</code> class.
 *
 * @author <a href="mailto:erik@thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2017-04-02
 * @since 1.0
 */
public class Weather2 extends AbstractModule {

    /**
     * The weather command.
     */
    public static final String WEATHER_CMD = "weather";

    // The OpenWeatherMap API Key property.
    private static final String OWM_API_KEY_PROP = "owm-api-key";

    /**
     * Creates a new {@link Weather} instance.
     */
    public Weather2() {
        commands.add(WEATHER_CMD);
        properties.put(OWM_API_KEY_PROP, "");
    }

    private String capitalize(final String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commandResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        new Thread(() -> run(bot, sender, args.toUpperCase(), isPrivate)).start();
    }

    private String fAndC(final Float f) {
        final Float c = (f - 32) * 5 / 9;
        return Math.round(f) + " \u00B0F, " + Math.round(c) + " \u00B0C";
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
            + ". Zip codes are supported in the US.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return isValidProperties();
    }

    /**
     * Fetches the weather data from a specific city.
     */
    private void run(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        final OpenWeatherMap owm = new OpenWeatherMap(properties.get(OWM_API_KEY_PROP));
        if (Utils.isValidString(args)) {
            final String[] argv = args.split(",");

            if (argv.length >= 1 && argv.length <= 2) {
                final String country;
                final String city = argv[0].trim();
                if (argv.length > 1 && Utils.isValidString(argv[1])) {
                    country = argv[1].trim();
                } else {
                    country = "US";
                }

                try {
                    final CurrentWeather cwd = owm.currentWeatherByCityName(city, country);
                    if (cwd.hasCityName()) {
                        bot.send(sender, "City: " + cwd.getCityName() + " [" + country + "]", isPrivate);

                        final CurrentWeather.Main main = cwd.getMainInstance();
                        if (main.hasTemperature()) {
                            bot.send(sender, "Temperature: " + fAndC(main.getTemperature()), isPrivate);
                        }

                        if (main.hasHumidity()) {
                            bot.send(sender, "Humidity: " + Math.round(main.getHumidity()) + "%", isPrivate);
                        }

                        if (cwd.hasWindInstance()) {
                            final CurrentWeather.Wind w = cwd.getWindInstance();
                            if (w.hasWindSpeed()) {
                                bot.send(sender, "Wind: " + wind(w.getWindSpeed()), isPrivate);
                            }
                        }

                        if (cwd.hasWeatherInstance()) {
                            CurrentWeather.Weather w;
                            final StringBuilder condition = new StringBuilder("Condition: ");
                            for (int i = 0; i < cwd.getWeatherCount(); i++) {
                                w = cwd.getWeatherInstance(i);
                                if (i != 0) {
                                    condition.append(", ").append(w.getWeatherDescription());
                                } else {
                                    condition.append(capitalize(w.getWeatherDescription()));
                                }
                            }
                            bot.send(sender, condition.toString(), isPrivate);
                        }

                        bot.send(sender, Utils.green("https://openweathermap.org/city/" + cwd.getCityCode()), isPrivate);

                        return;
                    }

                } catch (IOException e) {
                    if (bot.getLogger().isDebugEnabled()) {
                        bot.getLogger().debug("Unable to perform weather lookup: " + args, e);
                    }

                    bot.send(bot.getChannel(), "Unable to perform weather lookup: " + e.getMessage());
                }
            }
        }

        helpResponse(bot, sender, args, isPrivate);
    }

    private String wind(final Float w) {
        final double kmh = w * 1.60934;
        return Math.round(w) + " mph, " + Math.round(kmh) + " km/h";
    }
}
