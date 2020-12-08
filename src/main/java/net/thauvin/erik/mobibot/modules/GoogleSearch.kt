/*
 * GoogleSearch.kt
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

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.NoticeMessage
import org.apache.commons.lang3.StringUtils
import org.jibble.pircbot.Colors
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL

/**
 * The GoogleSearch module.
 */
class GoogleSearch(bot: Mobibot) : ThreadedModule(bot) {
    /**
     * Searches Google.
     */
    override fun run(sender: String, cmd: String, args: String, isPrivate: Boolean) {
        with(bot) {
            if (args.isNotBlank()) {
                try {
                    val results = searchGoogle(
                        args, properties[GOOGLE_API_KEY_PROP],
                        properties[GOOGLE_CSE_KEY_PROP]
                    )
                    for (msg in results) {
                        send(sender, msg)
                    }
                } catch (e: ModuleException) {
                    if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                    send(sender, e.message, isPrivate)
                }
            } else {
                helpResponse(sender, isPrivate)
            }
        }
    }

    companion object {
        // Google API Key property
        const val GOOGLE_API_KEY_PROP = "google-api-key"

        // Google Custom Search Engine ID property
        const val GOOGLE_CSE_KEY_PROP = "google-cse-cx"

        // Google command
        private const val GOOGLE_CMD = "google"

        /**
         * Performs a search on Google.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun searchGoogle(query: String, apiKey: String?, cseKey: String?): List<Message> {
            if (apiKey.isNullOrBlank() || cseKey.isNullOrBlank()) {
                throw ModuleException("${StringUtils.capitalize(GOOGLE_CMD)} is disabled. The API keys are missing.")
            }
            return if (query.isNotBlank()) {
                val results = mutableListOf<Message>()
                try {
                    val url = URL(
                        "https://www.googleapis.com/customsearch/v1?key=$apiKey&cx=$cseKey" +
                            "&q=${Utils.encodeUrl(query)}&filter=1&num=5&alt=json"
                    )
                    val json = JSONObject(Utils.urlReader(url))
                    val ja = json.getJSONArray("items")
                    for (i in 0 until ja.length()) {
                        val j = ja.getJSONObject(i)
                        results.add(NoticeMessage(Utils.unescapeXml(j.getString("title"))))
                        results.add(NoticeMessage(Utils.helpFormat(j.getString("link"), false), Colors.DARK_GREEN))
                    }
                } catch (e: IOException) {
                    throw ModuleException("searchGoogle($query)", "An IO error has occurred searching Google.", e)
                } catch (e: JSONException) {
                    throw ModuleException("searchGoogle($query)", "A JSON error has occurred searching Google.", e)
                }
                results
            } else {
                throw ModuleException("Invalid query. Please try again.")
            }
        }
    }

    init {
        commands.add(GOOGLE_CMD)
        help.add("To search Google:")
        help.add(Utils.helpFormat("%c $GOOGLE_CMD <query>"))
        initProperties(GOOGLE_API_KEY_PROP, GOOGLE_CSE_KEY_PROP)
    }
}
