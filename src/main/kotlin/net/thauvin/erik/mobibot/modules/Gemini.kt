/*
 * Gemini.kt
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

import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.api.GenerationConfig
import com.google.cloud.vertexai.api.HarmCategory
import com.google.cloud.vertexai.api.SafetySetting
import com.google.cloud.vertexai.generativeai.GenerativeModel
import com.google.cloud.vertexai.generativeai.ResponseHandler
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


class Gemini : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(Gemini::class.java)

    override val name = GEMINI_NAME

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val answer = chat(
                    args.trim(),
                    properties[PROJECT_ID_PROP],
                    properties[LOCATION_PROP],
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

    companion object {
        /**
         * The service name.
         */
        const val GEMINI_NAME = "Gemini"

        /**
         * The Google cloud project ID property.
         */
        const val PROJECT_ID_PROP = "gemini-project-id"

        /**
         * The Vertex AI location property.
         */
        const val LOCATION_PROP = "gemini-location"

        /**
         * The max number of tokens property.
         */
        const val MAX_TOKENS_PROP = "gemini-max-tokens"

        // Gemini command
        private const val GEMINI_CMD = "gemini"

        @JvmStatic
        @Throws(ModuleException::class)
        fun chat(
            query: String,
            projectId: String?,
            location: String?,
            maxToken: Int
        ): String? {
            if (!projectId.isNullOrEmpty() && !location.isNullOrEmpty()) {
                try {
                    VertexAI(projectId, location).use { vertexAI ->
                        val generationConfig = GenerationConfig.newBuilder().setMaxOutputTokens(maxToken).build()
                        val safetySettings = listOf(
                            SafetySetting.newBuilder()
                                .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                                .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                                .build(),
                            SafetySetting.newBuilder()
                                .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                                .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                                .build(),
                            SafetySetting.newBuilder()
                                .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                                .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                                .build(),
                            SafetySetting.newBuilder()
                                .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
                                .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                                .build()
                        )
                        val model = GenerativeModel.Builder().setModelName("gemini-1.5-flash-001")
                            .setGenerationConfig(generationConfig)
                            .setVertexAi(vertexAI).build()
                            .withSafetySettings(safetySettings)

                        val response = model.generateContent(query)
                        return ResponseHandler.getText(response)
                    }
                } catch (e: Exception) {
                    throw ModuleException(
                        "$GEMINI_CMD($query): IO",
                        "An IO error has occurred while conversing with ${GEMINI_NAME}.",
                        e
                    )
                }
            } else {
                throw ModuleException("${GEMINI_CMD}($query)", "No $GEMINI_NAME Project ID or Location specified.")
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
        initProperties(PROJECT_ID_PROP, LOCATION_PROP, MAX_TOKENS_PROP)
    }

}
