/*
 * Weather.java
 *
 * Copyright (c) 2004-2016, Erik C. Thauvin (erik@thauvin.net)
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

import net.sf.jweather.metar.Metar;
import net.sf.jweather.metar.SkyCondition;
import net.sf.jweather.metar.WeatherCondition;
import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.Utils;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * The Weather module
 *
 * @author Erik C. Thauvin
 * @created Feb 7, 2004
 * @since 1.0
 */
final public class Weather extends AbstractModule {
    /**
     * The decimal number format.
     */
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.##");

    /**
     * The URL where the stations are listed.
     */
    private static final String STATIONS_URL = "http://www.rap.ucar.edu/weather/surface/stations.txt";

    /**
     * THe weather command.
     */
    private static final String WEATHER_CMD = "weather";

    /**
     * Creates a new {@link Weather} instance.
     */
    public Weather() {
        commands.add(WEATHER_CMD);
    }

    @Override
    public void commandResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        new Thread(() -> run(bot, sender, args.toUpperCase(), isPrivate)).start();
    }

    @Override
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        bot.send(sender, "To display weather information:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + WEATHER_CMD + " <station id>"));
        bot.send(sender, "For a listing of the ICAO station IDs, please visit: " + STATIONS_URL);
    }

    @Override
    public boolean isPrivateMsgEnabled() {
        return true;
    }

    /**
     * Fetches the weather data from a specific station ID.
     */
    private void run(final Mobibot bot, final String sender, final String station, final boolean isPrivate) {
        if (station.length() == 4) {
            final Metar metar = net.sf.jweather.Weather.getMetar(station);

            if (metar != null) {
                Float result;

                bot.send(sender, "Station ID: " + metar.getStationID(), isPrivate);

                bot.send(sender,
                        "At: "
                                + Utils.UTC_SDF.format(metar.getDate())
                                + " UTC ("
                                + (((new Date()).getTime() - metar.getDate().getTime()) / 1000L / 60L)
                                + " minutes ago)",
                        isPrivate);

                result = metar.getWindSpeedInMPH();

                if (result != null) {
                    bot.send(sender,
                            "Wind Speed: "
                                    + result
                                    + " mph, "
                                    + metar.getWindSpeedInKnots()
                                    + " knots, "
                                    + metar.getWindSpeedInMPS()
                                    + " m/s",
                            isPrivate);
                }

                result = metar.getVisibility();

                if (result != null) {
                    bot.send(sender,
                            "Visibility: "
                                    + (metar.getVisibilityLessThan() ? "< " : "")
                                    + NUMBER_FORMAT.format(result)
                                    + " mi, " + metar.getVisibilityInKilometers() + " km",
                            isPrivate);
                }

                result = metar.getPressure();

                if (result != null) {
                    bot.send(sender,
                            "Pressure: " + result + " Hg, " + metar.getPressureInHectoPascals() + " hPa",
                            isPrivate);
                }

                result = metar.getTemperatureInCelsius();

                if (result != null) {
                    bot.send(sender,
                            "Temperature: " + result + " \u00B0C, " + metar.getTemperatureInFahrenheit() + " \u00B0F",
                            isPrivate);
                }

                if (metar.getWeatherConditions() != null) {
                    for (final Object weatherCondition : metar.getWeatherConditions()) {
                        bot.send(sender, ((WeatherCondition) weatherCondition).getNaturalLanguageString(), isPrivate);
                    }
                }

                if (metar.getSkyConditions() != null) {
                    for (final Object skyCondition : metar.getSkyConditions()) {
                        bot.send(sender, ((SkyCondition) skyCondition).getNaturalLanguageString(), isPrivate);
                    }
                }

                return;
            } else {
                bot.send(sender, "Invalid Station ID. Please try again.", isPrivate);

                return;
            }
        }

        helpResponse(bot, sender, station, isPrivate);
    }
}
