/*
 * WolframAlpha.kt
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

import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.Utils.encodeUrl
import net.thauvin.erik.mobibot.Utils.isHttpSuccess
import net.thauvin.erik.mobibot.Utils.reader
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL

class WolframAlpha : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(WolframAlpha::class.java)

    override val name = "WolframAlpha"

    companion object {
        /**
         * The Wolfram Alpha API Key property.
         */
        const val APPID_KEY_PROP = "wolfram-appid"

        /**
         * The Wolfram units properties
         */
        const val UNITS_PROP = "wolfram-units"

        const val METRIC = "metric"
        const val IMPERIAL = "imperial"

        // Wolfram command
        private const val WOLFRAM_CMD = "wolfram"

        // Wolfram Alpha API URL
        private const val API_URL = "http://api.wolframalpha.com/v1/spoken?appid="

        @JvmStatic
        @Throws(ModuleException::class)
        fun queryWolfram(query: String, units: String = IMPERIAL, appId: String?): String {
            if (!appId.isNullOrEmpty()) {
                try {
                    val urlReader = URL("${API_URL}${appId}&units=${units}&i=" + query.encodeUrl()).reader()
                    if (urlReader.responseCode.isHttpSuccess()) {
                        return urlReader.body
                    } else {
                        throw ModuleException(
                            "wolfram($query): ${urlReader.responseCode} : ${urlReader.body} ",
                            urlReader.body.ifEmpty {
                                "Looks like Wolfram Alpha isn't able to answer that. (${urlReader.responseCode})"
                            }
                        )
                    }
                } catch (ioe: IOException) {
                    throw ModuleException(
                        "wolfram($query): IOE", "An IO Error occurred while querying Wolfram Alpha.", ioe
                    )
                }
            } else {
                throw ModuleException("wolfram($query): No API Key", "No Wolfram Alpha API key specified.")
            }
        }
    }

    init {
        commands.add(WOLFRAM_CMD)
        with(help) {
            add("To get answers from Wolfram Alpha:")
            add(Utils.helpFormat("%c $WOLFRAM_CMD <query> [units=(${METRIC}|${IMPERIAL})]"))
            add("For example:")
            add(Utils.helpFormat("%c $WOLFRAM_CMD days until christmas"))
            add(Utils.helpFormat("%c $WOLFRAM_CMD distance earth moon units=metric"))
        }
        initProperties(APPID_KEY_PROP, UNITS_PROP)
    }

    private fun getUnits(unit: String?): String {
        return if (unit?.lowercase() == METRIC) {
            METRIC
        } else {
            IMPERIAL
        }
    }

    /**
     * Queries Wolfram Alpha.
     */
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val query = args.trim().split("units=", limit = 2, ignoreCase = true)
                event.sendMessage(
                    queryWolfram(
                        query[0].trim(),
                        units = if (query.size == 2) {
                            getUnits(query[1].trim())
                        } else {
                            getUnits(properties[UNITS_PROP])
                        },
                        appId = properties[APPID_KEY_PROP]
                    )
                )
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
