/*
 * Weather2.kt
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.aksingh.owmjapis.api.APIException
import net.aksingh.owmjapis.core.OWM
import net.aksingh.owmjapis.core.OWM.Country
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.capitalize
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

private const val INVALID_SYNTAX = "Invalid syntax."

/**
 * Retrieve weather information from OpenWeatherMap.
 */
@SuppressFBWarnings("BC_BAD_CAST_TO_ABSTRACT_COLLECTION")
class Weather2 : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(Weather2::class.java)

    override val name = WEATHER_NAME

    companion object {
        /**
         * The OpenWeatherMap API Key property.
         */
        const val API_KEY_PROP = "owm-api-key"

        /**
         * The service name.
         */
        const val WEATHER_NAME = "Weather"

        // Weather command
        private const val WEATHER_CMD = "weather"

        /**
         * Converts and rounds temperature from °F to °C.
         */
        fun ftoC(d: Double): Pair<Int, Int> {
            val c = (d - 32) * 5 / 9
            return d.roundToInt() to c.roundToInt()
        }

        /**
         * Returns a country based on its country code. Defaults to [Country.UNITED_STATES] if not found.
         */
        fun getCountry(countryCode: String): Country {
            for (c in Country.entries) {
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
        @SuppressFBWarnings("DCN_NULLPOINTER_EXCEPTION")
        fun getWeather(query: String, apiKey: String?): List<Message> {
            if (apiKey.isNullOrBlank()) {
                throw ModuleException(
                    "${Weather2::class.java.name} is disabled.",
                    "${WEATHER_CMD.capitalize()} is disabled. The API key is missing."
                )
            }

            val owm = OWM(apiKey)
            val messages = mutableListOf<Message>()
            owm.unit = OWM.Unit.IMPERIAL

            if (query.isBlank()) {
                messages.add(ErrorMessage(INVALID_SYNTAX))
                return messages
            }

            val argv = query.split(",")
            if (argv.size !in 1..2) {
                messages.add(ErrorMessage(INVALID_SYNTAX))
                return messages
            }

            val city = argv[0].trim()
            val code = if (argv.size > 1 && argv[1].isNotBlank()) argv[1].trim() else "US"

            try {
                val country = getCountry(code)
                val cwd = if (city.matches("\\d+".toRegex())) {
                    owm.currentWeatherByZipCode(city.toInt(), country)
                } else {
                    owm.currentWeatherByCityName(city, country)
                }

                if (!cwd.hasCityName()) {
                    messages.add(ErrorMessage(INVALID_SYNTAX))
                    return messages
                }

                // City info
                messages.add(
                    PublicMessage(
                        "City: ${cwd.cityName}, " +
                                "${country.name.replace('_', ' ').capitalizeWords()} " +
                                "[${country.value}]"
                    )
                )

                // Temperature
                cwd.mainData?.temp?.let { t ->
                    val (f, c) = ftoC(t)
                    messages.add(PublicMessage("Temperature: ${f}°F, ${c}°C"))
                }

                // Humidity
                cwd.mainData?.humidity?.let { h ->
                    messages.add(NoticeMessage("Humidity: ${h.roundToInt()}%"))
                }

                // Wind
                cwd.windData?.speed?.let { s ->
                    val (mph, kmh) = mphToKmh(s)
                    messages.add(NoticeMessage("Wind: $mph mph, $kmh km/h"))
                }

                // Weather condition
                cwd.weatherList?.let { weatherList ->
                    val condition = weatherList.mapNotNull { it?.getDescription()?.capitalize() }
                        .joinToString(". ", " ", ".")
                    if (condition.isNotBlank()) {
                        messages.add(NoticeMessage("Condition: $condition"))
                    }
                }

                // OpenWeatherMap link
                val cityId = cwd.cityId
                val url = if (cityId != null && cityId > 0) {
                    "https://openweathermap.org/city/$cityId"
                } else {
                    "https://openweathermap.org/find?q=${city},${code.uppercase()}".encodeUrl()
                }
                messages.add(NoticeMessage(url, Colors.GREEN))

            } catch (e: APIException) {
                val errorMsg = if (e.code == 404) {
                    "The requested city was not found."
                } else {
                    e.message ?: "API error occurred."
                }
                throw ModuleException("getWeather($query): API ${e.code}", errorMsg, e)
            } catch (e: NullPointerException) {
                throw ModuleException("getWeather($query): NPE", "Unable to perform weather lookup.", e)
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
        addCommand(WEATHER_CMD)
        addHelp("To display weather information:")
        addHelp(helpFormat("%c $WEATHER_CMD <city> [, <country code>]"))
        addHelp("For example:")
        addHelp(helpFormat("%c $WEATHER_CMD paris, fr"))
        addHelp("The default ISO 3166 country code is ${"US".bold()}. Zip codes supported in most countries.")
        initProperties(API_KEY_PROP)
    }

    /**
     * Fetches the weather data from a specific location.
     */
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val messages = getWeather(args, getProperty(API_KEY_PROP))
                if (messages[0].isError) {
                    helpResponse(event)
                } else {
                    for (msg in messages) {
                        event.sendMessage(channel, msg)
                    }
                }
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                e.message?.let {
                    event.respond(it)
                }
            }
        } else {
            helpResponse(event)
        }
    }
}
