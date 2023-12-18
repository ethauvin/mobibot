package net.thauvin.erik.mobibot.modules

import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.api.GenerationConfig
import com.google.cloud.vertexai.generativeai.preview.ChatSession
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class Gemini : AbstractModule() {
    private val logger: Logger = LoggerFactory.getLogger(Gemini::class.java)

    override val name = GEMINI_NAME

    override fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent) {
        if (args.isNotBlank()) {
            try {
                val answer = chat(
                    args.trim(),
                    properties[PROJECT_ID_PROP],
                    properties[LOCATION_PROPR],
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
         * The Google cloud project ID.
         */
        const val PROJECT_ID_PROP = "gemini-project-id"

        /**
         * The Vertex AI location.
         */
        const val LOCATION_PROPR = "gemini-location"

        /**
         * The max tokens property.
         */
        const val MAX_TOKENS_PROP = "gemini-max-tokens"

        // ChatGPT command
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
                        val model = GenerativeModel("gemini-pro-vision", generationConfig, vertexAI)
                        val session = ChatSession(model)
                        val response = session.sendMessage(query)

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
        initProperties(PROJECT_ID_PROP, LOCATION_PROPR, MAX_TOKENS_PROP)
    }

}
