/*
 * ChatGpt.kt
 *
 * Copyright 2004-2024 Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ChatGpt : AbstractModule() {
    val logger: Logger = LoggerFactory.getLogger(ChatGpt::class.java)

    override val name = CHATGPT_NAME

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val answer = chat(
                    args.trim(), properties[API_KEY_PROP],
                    properties.getOrDefault(MAX_TOKENS_PROP, "1024").toInt()
                )
                if (answer.isNotBlank()) {
                    event.sendMessage(answer)
                } else {
                    event.respond("$name is stumped.")
                }
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                e.message?.let {
                    event.respond(it)
                }
            } catch (e: NumberFormatException) {
                if (logger.isErrorEnabled) logger.error("Invalid $MAX_TOKENS_PROP property.", e)
                event.respond("The $name module is misconfigured.")
            }
        } else {
            helpResponse(event)
        }
    }

    companion object {
        /**
         * The service name.
         */
        const val CHATGPT_NAME = "ChatGPT"

        /**
         * The API Key property.
         */
        const val API_KEY_PROP = "chatgpt-api-key"

        /**
         * The max tokens property.
         */
        const val MAX_TOKENS_PROP = "chatgpt-max-tokens"

        // ChatGPT API URL
        private const val API_URL = "https://api.openai.com/v1/chat/completions"

        // ChatGPT command
        private const val CHATGPT_CMD = "chatgpt"

        @JvmStatic
        @Throws(ModuleException::class)
        fun chat(query: String, apiKey: String?, maxTokens: Int): String {
            if (!apiKey.isNullOrEmpty()) {
                val jsonObject = JSONObject()
                jsonObject.put("model", "gpt-3.5-turbo-1106")
                jsonObject.put("max_tokens", maxTokens)
                val message = JSONObject()
                message.put("role", "user")
                message.put("content", query)
                val messages = JSONArray()
                messages.put(message)
                jsonObject.put("messages", messages)

                val request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer $apiKey")
                    .header("User-Agent", Constants.USER_AGENT)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                    .build()
                try {
                    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
                    if (response.statusCode() == 200) {
                        try {
                            val jsonResponse = JSONObject(response.body())
                            val choices = jsonResponse.getJSONArray("choices")
                            return choices.getJSONObject(0).getJSONObject("message").getString("content").trim()
                        } catch (e: JSONException) {
                            throw ModuleException(
                                "$CHATGPT_CMD($query): JSON",
                                "A JSON error has occurred while conversing with $CHATGPT_NAME.",
                                e
                            )
                        }
                    } else {
                        if (response.statusCode() == 429) {
                            throw ModuleException(
                                "$CHATGPT_CMD($query): Rate limit reached",
                                "Rate limit reached. Please try again later."
                            )
                        } else {
                            throw IOException("HTTP Status Code: " + response.statusCode())
                        }
                    }
                } catch (e: IOException) {
                    throw ModuleException(
                        "$CHATGPT_CMD($query): IO",
                        "An IO error has occurred while conversing with $CHATGPT_NAME.",
                        e
                    )
                }
            } else {
                throw ModuleException("$CHATGPT_CMD($query)", "No $CHATGPT_NAME API key specified.")
            }
        }
    }

    init {
        commands.add(CHATGPT_CMD)
        with(help) {
            add("To get answers from $name:")
            add(Utils.helpFormat("%c $CHATGPT_CMD <query>"))
            add("For example:")
            add(Utils.helpFormat("%c $CHATGPT_CMD explain quantum computing in simple terms"))
            add(Utils.helpFormat("%c $CHATGPT_CMD how do I make an HTTP request in Javascript?"))
        }
        initProperties(API_KEY_PROP, MAX_TOKENS_PROP)
    }
}
