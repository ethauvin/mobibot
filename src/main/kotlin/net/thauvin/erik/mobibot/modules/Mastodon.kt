/*
 * Mastodon.kt
 *
 * Copyright 2004-2023 Erik C. Thauvin (erik@thauvin.net)
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
import net.thauvin.erik.mobibot.Utils.prefixIfMissing
import net.thauvin.erik.mobibot.entries.EntryLink
import net.thauvin.erik.mobibot.social.SocialModule
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONWriter
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Mastodon : SocialModule() {
    override val name = "Mastodon"

    override val handle: String?
        get() = properties[HANDLE_PROP]

    override val isAutoPost: Boolean
        get() = isEnabled && properties[AUTO_POST_PROP].toBoolean()

    override val isValidProperties: Boolean
        get() = !(properties[INSTANCE_PROP].isNullOrBlank() || properties[ACCESS_TOKEN_PROP].isNullOrBlank())

    /**
     * Formats the entry for posting.
     */
    override fun formatEntry(entry: EntryLink): String {
        return "${entry.title} (via ${entry.nick} on ${entry.channel})${formatTags(entry)}\n\n${entry.link}"
    }

    private fun formatTags(entry: EntryLink): String {
        return entry.tags.filter { !it.name.equals(entry.channel.removePrefix("#"), true) }
            .joinToString(separator = " ", prefix = "\n\n") { "#${it.name}" }
    }

    /**
     * Posts on Mastodon.
     */
    @Throws(ModuleException::class)
    override fun post(message: String, isDm: Boolean): String {
        return toot(
            apiKey = properties[ACCESS_TOKEN_PROP],
            instance = properties[INSTANCE_PROP],
            handle = handle,
            message = message,
            isDm = isDm
        )
    }

    companion object {
        // Property keys
        const val ACCESS_TOKEN_PROP = "mastodon-access-token"
        const val AUTO_POST_PROP = "mastodon-auto-post"
        const val HANDLE_PROP = "mastodon-handle"
        const val INSTANCE_PROP = "mastodon-instance"

        private const val MASTODON_CMD = "mastodon"
        private const val TOOT_CMD = "toot"

        /**
         * Post on Mastodon.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun toot(apiKey: String?, instance: String?, handle: String?, message: String, isDm: Boolean): String {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://$instance/api/v1/statuses"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $apiKey")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        JSONWriter.valueToString(
                            if (isDm) {
                                mapOf("status" to "${handle?.prefixIfMissing('@')} $message", "visibility" to "direct")
                            } else {
                                mapOf("status" to message)
                            }
                        )
                    )
                )
                .build()
            try {
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() == 200) {
                    return try {
                        val jsonResponse = JSONObject(response.body())
                        if (isDm) {
                            jsonResponse.getString("content")
                        } else {
                            "Your message was posted to ${jsonResponse.getString("url")}"
                        }
                    } catch (e: JSONException) {
                        throw ModuleException("mastodonPost($message)", "A JSON error has occurred: ${e.message}", e)
                    }
                } else {
                    throw IOException("Status Code: " + response.statusCode())
                }
            } catch (e: IOException) {
                throw ModuleException("mastodonPost($message)", "An IO error has occurred: ${e.message}", e)
            } catch (e: InterruptedException) {
                throw ModuleException("mastodonPost($message)", "An error has occurred: ${e.message}", e)
            }
        }
    }

    init {
        commands.add(MASTODON_CMD)
        commands.add(TOOT_CMD)
        help.add("To toot on Mastodon:")
        help.add(Utils.helpFormat("%c $TOOT_CMD <message>"))
        properties[AUTO_POST_PROP] = "false"
        initProperties(ACCESS_TOKEN_PROP, HANDLE_PROP, INSTANCE_PROP)
    }
}
