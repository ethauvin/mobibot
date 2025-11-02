/*
 * ChatGpt2.kt
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

import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Allows user to interact with ChatGPT.
 */
class ChatGpt2 : AbstractModule() {
    val logger: Logger = LoggerFactory.getLogger(ChatGpt2::class.java)

    override val name = CHATGPT_NAME

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
         * The max-tokens property.
         */
        const val MAX_TOKENS_PROP = "chatgpt-max-tokens"

        // ChatGPT command
        private const val CHATGPT_CMD = "chatgpt"

        @JvmStatic
        @Throws(ModuleException::class)
        fun chat(query: String, apiKey: String?, maxTokens: Int): String {
            if (!apiKey.isNullOrEmpty()) {
                try {
                    val model = OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(OpenAiChatModelName.GPT_4)
                        .maxTokens(maxTokens)
                        .build()

                    return model.chat(query)
                } catch (e: Exception) {
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
        addCommand(CHATGPT_CMD)
        addHelp("To get answers from $name:")
        addHelp(Utils.helpFormat("%c $CHATGPT_CMD <query>"))
        addHelp("For example:")
        addHelp(Utils.helpFormat("%c $CHATGPT_CMD explain quantum computing in simple terms"))
        addHelp(Utils.helpFormat("%c $CHATGPT_CMD how do I make an HTTP request in Javascript?"))
        initProperties(API_KEY_PROP, MAX_TOKENS_PROP)
    }

    /**
     * Gets answers by chatting with ChatGPT.
     */
    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val answer = chat(
                    args.trim(), getProperty(API_KEY_PROP),
                    getPropertyOrDefault(MAX_TOKENS_PROP, "1024").toInt()
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
}
