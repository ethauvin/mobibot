/*
 * Weather2.kt
 *
 * Copyright (c) 2004-2021, Erik C. Thauvin (erik@thauvin.net)
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
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.capitalise
import net.thauvin.erik.mobibot.Utils.capitalizeWords
import net.thauvin.erik.mobibot.Utils.encodeUrl
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.msg.ErrorMessage
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.NoticeMessage
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.pircbotx.Colors
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

/**
 * The `Weather2` module.
 */
class Weather2 : ThreadedModule() {
    private val logger: Logger = LoggerFactory.getLogger(Weather2::class.java)

    /**
     * Fetches the weather data from a specific city.
     */
    override fun run(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val messages = getWeather(args, properties[OWM_API_KEY_PROP])
                if (messages[0].isError) {
                    helpResponse(event)
                } else {
                    for (msg in messages) {
                        event.sendMessage(channel, msg)
                    }
                }
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                event.respond(e.message)
            }
        } else {
            helpResponse(event)
        }
    }

    companion object {
        /**
         * The OpenWeatherMap API Key property.
         */
        const val OWM_API_KEY_PROP = "owm-api-key"

        // Weather command
        private const val WEATHER_CMD = "weather"

        /**
         * Converts and rounds temperature from °F to °C.
         */
        fun ftoC(d: Double?): Pair<Int, Int> {
            val c = (d!! - 32) * 5 / 9
            return d.roundToInt() to c.roundToInt()
        }

        /**
         * Returns a country based on its country code. Defaults to [Country.UNITED_STATES] if not found.
         */
        fun getCountry(countryCode: String): Country {
            for (c in Country.values()) {
                if (c.value.equals(countryCode, ignoreCase = true)) {
                    return c
                }
            }
            return Country.UNITED_STATES
        }

        /**
         * Retrieves the weather data.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun getWeather(query: String, apiKey: String?): List<Message> {
            if (apiKey.isNullOrBlank()) {
                throw ModuleException("${WEATHER_CMD.capitalise()} is disabled. The API key is missing.")
            }
            val owm = OWM(apiKey)
            val messages = mutableListOf<Message>()
            owm.unit = OWM.Unit.IMPERIAL
            if (query.isNotBlank()) {
                val argv = query.split(",")
                if (argv.size in 1..2) {
                    val city = argv[0].trim()
                    val code: String = if (argv.size > 1 && argv[1].isNotBlank()) {
                        argv[1].trim()
                    } else {
                        "US"
                    }
                    try {
                        val country = getCountry(code)
                        val cwd: CurrentWeather = if (city.matches("\\d+".toRegex())) {
                            owm.currentWeatherByZipCode(city.toInt(), country)
                        } else {
                            owm.currentWeatherByCityName(city, country)
                        }
                        if (cwd.hasCityName()) {
                            messages.add(
                                PublicMessage(
                                    "City: ${cwd.cityName}, " +
                                            country.name.replace('_', ' ').capitalizeWords() + " [${country.value}]"
                                )
                            )
                            with(cwd.mainData) {
                                if (this != null) {
                                    if (hasTemp()) {
                                        val t = ftoC(temp)
                                        messages.add(PublicMessage("Temperature: ${t.first}°F, ${t.second}°C"))
                                    }
                                    if (hasHumidity() && humidity != null) {
                                        messages.add(NoticeMessage("Humidity: ${(humidity!!).roundToInt()}%"))
                                    }
                                }
                            }
                            if (cwd.hasWindData()) {
                                with(cwd.windData) {
                                    if (this != null && hasSpeed() && speed != null) {
                                        val w = mphToKmh(speed!!)
                                        messages.add(NoticeMessage("Wind: ${w.first} mph, ${w.second} km/h"))
                                    }
                                }
                            }
                            if (cwd.hasWeatherList()) {
                                val condition = StringBuilder("Condition:")
                                val list = cwd.weatherList
                                if (list != null) {
                                    for (w in list) {
                                        if (w != null) {
                                            condition.append(' ')
                                                .append(w.getDescription().capitalise())
                                                .append('.')
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
                                                    + encodeUrl("$city,${code.uppercase()}"),
                                            Colors.GREEN
                                        )
                                    )
                                }
                            }
                        }
                    } catch (e: APIException) {
                        throw ModuleException("getWeather($query)", e.message, e)
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

        /**
         * Converts and rounds temperature from mph to km/h.
         */
        fun mphToKmh(w: Double): Pair<Int, Int> {
            val kmh = w * 1.60934
            return w.roundToInt() to kmh.roundToInt()
        }
    }

    init {
        commands.add(WEATHER_CMD)
        with(help) {
            add("To display weather information:")
            add(helpFormat("%c $WEATHER_CMD <city> [, <country code>]"))
            add("For example:")
            add(helpFormat("%c $WEATHER_CMD paris, fr"))
            add("The default ISO 3166 country code is ${bold("US")}. Zip codes supported in most countries.")
        }
        initProperties(OWM_API_KEY_PROP)
    }
}
