/*
 * Gemini2.kt
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

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Allows user to interact with Gemini.
 */
class Gemini2 : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(Gemini2::class.java)

    override val name = GEMINI_NAME

    companion object {
        /**
         * API Key error message
         */
        const val API_KEY_ERROR = "Please specify an API key."

        /**
         * The API key
         */
        const val GEMINI_API_KEY = "gemini-api-key"

        /**
         * The service name.
         */
        const val GEMINI_NAME = "Gemini"

        /**
         * IO error message
         */
        const val IO_ERROR = "An IO error has occurred while conversing with $GEMINI_NAME."

        /**
         * The max number of output tokens property.
         */
        const val MAX_TOKENS_PROP = "gemini-max-tokens"

        // Gemini command
        private const val GEMINI_CMD = "gemini"

        @JvmStatic
        @Throws(ModuleException::class)
        fun chat(
            query: String,
            apiKey: String?,
            maxTokens: Int
        ): String? {
            if (!apiKey.isNullOrEmpty()) {
                try {
                    val gemini = GoogleAiGeminiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName("gemini-2.0-flash")
                        .maxOutputTokens(maxTokens)
                        .build()

                    return gemini.generate(query)
                } catch (e: Exception) {
                    throw ModuleException("$GEMINI_CMD($query): IO", IO_ERROR, e)
                }
            } else {
                throw ModuleException("${GEMINI_CMD}($query)", API_KEY_ERROR)
            }
        }
    }

    init {
        commands.add(GEMINI_CMD)
        with(help) {
            add("To get answers from $name:")
            add(Utils.helpFormat("%c $GEMINI_CMD <query>"))
            add("For example:")
            add(Utils.helpFormat("%c $GEMINI_CMD explain quantum computing in simple terms"))
            add(Utils.helpFormat("%c $GEMINI_CMD how do I make an HTTP request in Javascript?"))
        }
        initProperties(GEMINI_API_KEY, MAX_TOKENS_PROP)
    }

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val answer = chat(
                    args.trim(),
                    properties[GEMINI_API_KEY],
                    properties.getOrDefault(MAX_TOKENS_PROP, "1024").toInt()
                )
                if (!answer.isNullOrEmpty()) {
                    event.sendMessage(answer)
                } else {
                    event.respond("$name is stumped.")
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
