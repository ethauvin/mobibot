/*
 * @(#)Weather.java
 *
 * Copyright (c) 2004, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * $Id$
 *
 */
package net.thauvin.erik.mobibot;

import net.sf.jweather.metar.Metar;
import net.sf.jweather.metar.SkyCondition;
import net.sf.jweather.metar.WeatherCondition;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * Fetches the weather data from a specific station ID.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 * @created Feb 7, 2004
 * @since 1.0
 */
public class Weather implements Runnable
{
	/**
	 * The URL where the stations are listed.
	 */
	public static final String STATIONS_URL = "http://www.rap.ucar.edu/weather/surface/stations.txt";

	/**
	 * The decimal number format.
	 */
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.##");

	/**
	 * The bot.
	 */
	private final Mobibot bot;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String sender;

	/**
	 * The station ID.
	 */
	private final String station;

	/**
	 * The private message flag.
	 */
	private final boolean isPrivate;

	/**
	 * Creates a new Weather object.
	 *
	 * @param bot The bot.
	 * @param sender The nick of the person who sent the message.
	 * @param station The station ID.
	 * @param isPrivate Set to true is the response should be send as a private message.
	 */
	public Weather(Mobibot bot, String sender, String station, boolean isPrivate)
	{
		this.bot = bot;
		this.sender = sender;
		this.station = station.toUpperCase();
		this.isPrivate = isPrivate;
	}

	/**
	 * Main processing method.
	 */
	public final void run()
	{
		if (station.length() == 4)
		{
			final Metar metar = net.sf.jweather.Weather.getMetar(station);

			if (metar != null)
			{
				Float result;

				bot.send(sender, "Station ID: " + metar.getStationID(), isPrivate);

				bot.send(sender,
				          "At: " + metar.getDateString() + " UTC (" + (
						          ((new Date()).getTime() - metar.getDate().getTime()) / 1000L / 60L) + " minutes ago)",
				          isPrivate
				);

				result = metar.getWindSpeedInMPH();

				if (result != null)
				{
					bot.send(sender,
					          "Wind Speed: " + result + " mph, " + metar.getWindSpeedInKnots() + " knots", isPrivate);
				}

				result = metar.getVisibility();

				if (result != null)
				{
					if (!metar.getVisibilityLessThan())
					{
						bot.send(sender, "Visibility: " + NUMBER_FORMAT.format(result) + " mile(s)", isPrivate);
					}
					else
					{
						bot.send(sender, "Visibility: < " + NUMBER_FORMAT.format(result) + " mile(s)", isPrivate);
					}
				}

				result = metar.getPressure();

				if (result != null)
				{
					bot.send(sender, "Pressure: " + result + " in Hg", isPrivate);
				}

				result = metar.getTemperatureInCelsius();

				if (result != null)
				{
					bot.send(sender,
					          "Temperature: " + result + " C, " + metar.getTemperatureInFahrenheit() + " F", isPrivate);
				}

				if (metar.getWeatherConditions() != null)
				{
					final Iterator it = metar.getWeatherConditions().iterator();

					//noinspection WhileLoopReplaceableByForEach
					while (it.hasNext())
					{
						final WeatherCondition weatherCondition = (WeatherCondition) it.next();
						bot.send(sender, weatherCondition.getNaturalLanguageString(), isPrivate);
					}
				}

				if (metar.getSkyConditions() != null)
				{
					final Iterator it = metar.getSkyConditions().iterator();

					//noinspection WhileLoopReplaceableByForEach
					while (it.hasNext())
					{
						final SkyCondition skyCondition = (SkyCondition) it.next();
						bot.send(sender, skyCondition.getNaturalLanguageString(), isPrivate);
					}
				}

				return;
			}
			else
			{
				bot.send(sender, "Invalid Station ID. Please try again.", isPrivate);

				return;
			}
		}

		bot.helpResponse(sender, Mobibot.WEATHER_CMD);
	}
}
