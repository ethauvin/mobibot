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
	private final Mobibot _bot;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String _sender;

	/**
	 * The station ID.
	 */
	private final String _station;

	/**
	 * The private message flag.
	 */
	private final boolean _isPrivate;

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
		_bot = bot;
		_sender = sender;
		_station = station.toUpperCase();
		_isPrivate = isPrivate;
	}

	/**
	 * Main processing method.
	 */
	public final void run()
	{
		if (_station.length() == 4)
		{
			final Metar metar = net.sf.jweather.Weather.getMetar(_station);

			if (metar != null)
			{
				Float result;

				_bot.send(_sender, "Station ID: " + metar.getStationID(), _isPrivate);

				_bot.send(_sender,
				          "At: " + metar.getDateString() + " UTC ("
				          + (((new Date()).getTime() - metar.getDate().getTime()) / 1000L / 60L) + " minutes ago)",
				          _isPrivate);

				result = metar.getWindSpeedInMPH();

				if (result != null)
				{
					_bot.send(_sender,
					          "Wind Speed: " + result + " mph, " + metar.getWindSpeedInKnots() + " knots",
					          _isPrivate);
				}

				result = metar.getVisibility();

				if (result != null)
				{
					if (!metar.getVisibilityLessThan())
					{
						_bot.send(_sender, "Visibility: " + NUMBER_FORMAT.format(result) + " mile(s)", _isPrivate);
					}
					else
					{
						_bot.send(_sender, "Visibility: < " + NUMBER_FORMAT.format(result) + " mile(s)", _isPrivate);
					}
				}

				result = metar.getPressure();

				if (result != null)
				{
					_bot.send(_sender, "Pressure: " + result + " in Hg", _isPrivate);
				}

				result = metar.getTemperatureInCelsius();

				if (result != null)
				{
					_bot.send(_sender,
					          "Temperature: " + result + " C, " + metar.getTemperatureInFahrenheit() + " F",
					          _isPrivate);
				}

				if (metar.getWeatherConditions() != null)
				{
					final Iterator it = metar.getWeatherConditions().iterator();

					while (it.hasNext())
					{
						final WeatherCondition weatherCondition = (WeatherCondition) it.next();
						_bot.send(_sender, weatherCondition.getNaturalLanguageString(), _isPrivate);
					}
				}

				if (metar.getSkyConditions() != null)
				{
					final Iterator it = metar.getSkyConditions().iterator();

					while (it.hasNext())
					{
						final SkyCondition skyCondition = (SkyCondition) it.next();
						_bot.send(_sender, skyCondition.getNaturalLanguageString(), _isPrivate);
					}
				}

				return;
			}
			else
			{
				_bot.send(_sender, "Invalid Station ID. Please try again.", _isPrivate);

				return;
			}
		}

		_bot.helpResponse(_sender, Mobibot.WEATHER_CMD);
	}
}
