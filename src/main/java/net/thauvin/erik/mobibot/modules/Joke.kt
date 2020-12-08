/*
 * Joke.kt
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
import net.thauvin.erik.mobibot.msg.PublicMessage
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL

/**
 * The Joke module.
 */
class Joke(bot: Mobibot) : ThreadedModule(bot) {
    override fun commandResponse(
        sender: String,
        cmd: String,
        args: String,
        isPrivate: Boolean
    ) {
        Thread { run(sender, cmd, args, isPrivate) }.start()
    }

    /**
     * Returns a random joke from [The Internet Chuck Norris Database](http://www.icndb.com/).
     */
    override fun run(sender: String, cmd: String, args: String, isPrivate: Boolean) {
        try {
            bot.send(Utils.cyan(randomJoke().msg))
        } catch (e: ModuleException) {
            if (bot.logger.isWarnEnabled) bot.logger.warn(e.debugMessage, e)
            bot.send(sender, e.message, isPrivate)
        }
    }

    companion object {
        // Joke command
        private const val JOKE_CMD = "joke"

        // ICNDB URL
        private const val JOKE_URL =
            "http://api.icndb.com/jokes/random?escape=javascript&exclude=[explicit]&limitTo=[nerdy]"

        /**
         * Retrieves a random joke.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun randomJoke(): Message {
            return try {
                val url = URL(JOKE_URL)
                val json = JSONObject(Utils.urlReader(url))
                PublicMessage(
                    json.getJSONObject("value")["joke"].toString().replace("\\'", "'")
                        .replace("\\\"", "\"")
                )
            } catch (e: IOException) {
                throw ModuleException("randomJoke()", "An IO error has occurred retrieving a random joke.", e)
            } catch (e: JSONException) {
                throw ModuleException("randomJoke()", "An JSON error has occurred retrieving a random joke.", e)
            }
        }
    }

    init {
        commands.add(JOKE_CMD)
        help.add("To retrieve a random joke:")
        help.add(Utils.helpFormat("%c $JOKE_CMD"))
    }
}
