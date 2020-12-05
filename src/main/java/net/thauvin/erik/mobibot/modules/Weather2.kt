/*
 * Weather2.kt
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
package net.thauvin.erik.mobibot.modules

import net.aksingh.owmjapis.api.APIException
import net.aksingh.owmjapis.core.OWM
import net.aksingh.owmjapis.core.OWM.Country
import net.aksingh.owmjapis.model.CurrentWeather
import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.NoticeMessage
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.jibble.pircbot.Colors
import java.util.*
import kotlin.math.roundToInt

/**
 * The `Weather2` module.
 */
class Weather2(bot: Mobibot) : ThreadedModule(bot) {
    /**
     * Fetches the weather data from a specific city.
     */
    override fun run(sender: String, cmd: String, args: String, isPrivate: Boolean) {
        if (args.isNotBlank()) {
            try {
                val messages = getWeather(args, properties[OWM_API_KEY_PROP])
                if (messages[0].isError) {
                    helpResponse(sender, isPrivate)
                } else {
                    for (msg in messages) {
                        bot.send(sender, msg)
                    }
                }
            } catch (e: ModuleException) {
                bot.logger.debug(e.debugMessage, e)
                bot.send(e.message)
            }
        } else {
            helpResponse(sender, isPrivate)
        }
    }

    companion object {
        /**
         * The OpenWeatherMap API Key property.
         */
        const val OWM_API_KEY_PROP = "owm-api-key"

        // Weather command
        private const val WEATHER_CMD = "weather"
        private fun getCountry(countryCode: String): Country {
            for (c in Country.values()) {
                if (c.name.equals(countryCode, ignoreCase = true)) {
                    return c
                }
            }
            return Country.UNITED_STATES
        }

        private fun getTemps(d: Double?): String {
            val c = (d!! - 32) * 5 / 9
            return "${d.roundToInt()} °F, ${c.roundToInt()} °C"
        }

        /**
         * Retrieves the weather data.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun getWeather(query: String, apiKey: String?): List<Message> {
            if (apiKey.isNullOrBlank()) {
                throw ModuleException("${WEATHER_CMD.capitalize()} is disabled. The API key is missing.")
            }
            val owm = OWM(apiKey)
            val messages = ArrayList<Message>()
            owm.unit = OWM.Unit.IMPERIAL
            if (query.isNotBlank()) {
                val argv = query.split(",")
                if (argv.size in 1..2) {
                    val city = argv[0].trim()
                    val country: String = if (argv.size > 1 && argv[1].isNotBlank()) {
                        argv[1].trim()
                    } else {
                        "US"
                    }
                    try {
                        val cwd: CurrentWeather = if (city.matches("\\d+".toRegex())) {
                            owm.currentWeatherByZipCode(city.toInt(), getCountry(country))
                        } else {
                            owm.currentWeatherByCityName(city, getCountry(country))
                        }
                        if (cwd.hasCityName()) {
                            messages.add(
                                PublicMessage("City: ${cwd.cityName} [${country.toUpperCase()}]")
                            )
                            with(cwd.mainData) {
                                if (this != null) {
                                    if (hasTemp()) {
                                        messages.add(PublicMessage("Temperature: ${getTemps(temp)}"))
                                    }
                                    if (hasHumidity() && humidity != null) {
                                        messages.add(NoticeMessage("Humidity: ${(humidity!!).roundToInt()}%"))
                                    }
                                }
                            }
                            if (cwd.hasWindData()) {
                                with(cwd.windData) {
                                    if (this != null && hasSpeed() && speed != null) {
                                        messages.add(NoticeMessage("Wind: ${wind(speed!!)}"))
                                    }
                                }
                            }
                            if (cwd.hasWeatherList()) {
                                val condition = StringBuilder("Condition:")
                                val list = cwd.weatherList
                                if (list != null) {
                                    for (w in list) {
                                        if (w != null) {
                                            condition.append(' ').append(w.getDescription().capitalize()).append('.')
                                        }
                                    }
                                    messages.add(NoticeMessage(condition.toString()))
                                }
                            }
                            if (cwd.hasCityId() && cwd.cityId != null) {
                                if (cwd.cityId!! > 0) {
                                    messages.add(
                                        NoticeMessage("https://openweathermap.org/city/${cwd.cityId}", Colors.GREEN)
                                    )
                                } else {
                                    messages.add(
                                        NoticeMessage(
                                            "https://openweathermap.org/find?q="
                                                + Utils.encodeUrl("$city,${country.toUpperCase()}"),
                                            Colors.GREEN
                                        )
                                    )
                                }
                            }
                        }
                    } catch (e: APIException) {
                        throw ModuleException("getWeather($query)", "Unable to perform weather lookup.", e)
                    } catch (e: NullPointerException) {
                        throw ModuleException("getWeather($query)", "Unable to perform weather lookup.", e)
                    }
                }
            }
            if (messages.isEmpty()) {
                messages.add(ErrorMessage("Invalid syntax."))
            }
            return messages
        }

        private fun wind(w: Double): String {
            val kmh = w * 1.60934
            return "${w.roundToInt()} mph, ${kmh.roundToInt()} km/h"
        }
    }

    init {
        commands.add(WEATHER_CMD)
        help.add("To display weather information:")
        help.add(Utils.helpIndent("%c $WEATHER_CMD <city> [, <country code>]"))
        help.add("For example:")
        help.add(Utils.helpIndent("%c $WEATHER_CMD paris, fr"))
        help.add("The default ISO 3166 country code is ${Utils.bold("US")}. Zip codes supported in most countries.")
        initProperties(OWM_API_KEY_PROP)
    }
}
