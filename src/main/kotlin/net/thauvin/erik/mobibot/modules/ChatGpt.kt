/*
 * ChatGpt.kt
 *
 * Copyright (c) 2004-2022, Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.apache.commons.text.WordUtils
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONWriter
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ChatGpt : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(ChatGpt::class.java)

    override val name = "ChatGPT"

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val answer = chat(args.trim(), properties[CHATGPT_API_KEY], properties[CHATGPT_MAX_TOKENS]!!.toInt())
                if (answer.isNotBlank()) {
                    event.sendMessage(WordUtils.wrap(answer, 400))
                } else {
                    event.respond("ChatGPT is stumped.")
                }
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                e.message?.let {
                    event.respond(it)
                }
            } catch (e: NumberFormatException) {
                if (logger.isErrorEnabled) logger.error("Invalid $CHATGPT_MAX_TOKENS property.", e)
                event.respond("The $name module is misconfigured.")
            }
        } else {
            helpResponse(event)
        }
    }

    companion object {
        /**
         * The ChatGPT API Key property.
         */
        const val CHATGPT_API_KEY = "chatgpt-api-key"

        /**
         * The ChatGPT max tokens property.
         */
        const val CHATGPT_MAX_TOKENS = "chatgpt-max-tokens"

        // ChatGPT command
        private const val CHATGPT_CMD = "chatgpt"

        // ChatGPT API URL
        private const val API_URL = "https://api.openai.com/v1/completions"

        @JvmStatic
        @Throws(ModuleException::class)
        fun chat(query: String, apiKey: String?, maxTokens: Int): String {
            if (!apiKey.isNullOrEmpty()) {
                val prompt = JSONWriter.valueToString("Q:$query\nA:")
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer $apiKey")
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            """{
                                "model": "text-davinci-003",
                                "prompt": $prompt,
                                "temperature": 0,
                                "max_tokens": $maxTokens,
                                "top_p": 1,
                                "frequency_penalty": 0,
                                "presence_penalty": 0
                            }""".trimIndent()
                        )
                    )
                    .build()
                try {
                    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
                    if (response.statusCode() == 200) {
                        try {
                            val jsonResponse = JSONObject(response.body())
                            val choices = jsonResponse.getJSONArray("choices")
                            return choices.getJSONObject(0).getString("text").trim()
                        } catch (e: JSONException) {
                            throw ModuleException(
                                "chatgpt($query): JSON",
                                "A JSON error has occurred while conversing with ChatGPT.",
                                e
                            )
                        }
                    } else {
                        throw IOException("Status Code: " + response.statusCode())
                    }
                } catch (e: IOException) {
                    throw ModuleException(
                        "chatgpt($query): IO",
                        "An IO error has occurred while conversing with ChatGPT.",
                        e
                    )
                }
            } else {
                throw ModuleException("chatgpt($query)", "No ChatGPT API key specified.")
            }
        }
    }

    init {
        commands.add(CHATGPT_CMD)
        with(help) {
            add("To get answers from ChatGPT:")
            add(Utils.helpFormat("%c $CHATGPT_CMD <query>"))
            add("For example:")
            add(Utils.helpFormat("%c $CHATGPT_CMD explain quantum computing in simple terms"))
            add(Utils.helpFormat("%c $CHATGPT_CMD how do I make an HTTP request in Javascript?"))
        }
        initProperties(CHATGPT_API_KEY, CHATGPT_MAX_TOKENS)
    }
}
