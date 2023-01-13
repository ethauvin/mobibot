/*
 * Twitter.kt
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

import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.entries.EntryLink
import net.thauvin.erik.mobibot.social.SocialModule
import twitter4j.TwitterException

/**
 * The Twitter module.
 */
class Twitter : SocialModule() {
    override val name = "Twitter"

    override val handle: String?
        get() = properties[HANDLE_PROP]

    override val isAutoPost: Boolean
        get() = isEnabled && properties[AUTO_POST_PROP].toBoolean()

    override val isValidProperties: Boolean
        get() = !(properties[CONSUMER_KEY_PROP].isNullOrBlank() || properties[CONSUMER_SECRET_PROP].isNullOrBlank()
                || properties[TOKEN_PROP].isNullOrBlank() || properties[TOKEN_SECRET_PROP].isNullOrBlank())

    /**
     * Formats the entry for posting.
     */
    override fun formatEntry(entry: EntryLink): String {
        return "${entry.title} ${entry.link} via ${entry.nick} on ${entry.channel}"
    }

    /**
     * Posts on Twitter.
     */
    @Throws(ModuleException::class)
    override fun post(message: String, isDm: Boolean): String {
        return tweet(
            consumerKey = properties[CONSUMER_KEY_PROP],
            consumerSecret = properties[CONSUMER_SECRET_PROP],
            token = properties[TOKEN_PROP],
            tokenSecret = properties[TOKEN_SECRET_PROP],
            handle = handle,
            message = message,
            isDm = isDm
        )
    }

    companion object {
        // Property keys
        const val AUTO_POST_PROP = "twitter-auto-post"
        const val CONSUMER_KEY_PROP = "twitter-consumerKey"
        const val CONSUMER_SECRET_PROP = "twitter-consumerSecret"
        const val HANDLE_PROP = "twitter-handle"
        const val TOKEN_PROP = "twitter-token"
        const val TOKEN_SECRET_PROP = "twitter-tokenSecret"

        // Twitter commands
        private const val TWITTER_CMD = "twitter"
        private const val TWEET_CMD = "tweet"

        /**
         * Post on Twitter.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun tweet(
            consumerKey: String?,
            consumerSecret: String?,
            token: String?,
            tokenSecret: String?,
            handle: String?,
            message: String,
            isDm: Boolean
        ): String {
            return try {
                val twitter = twitter4j.Twitter.newBuilder()
                    .prettyDebugEnabled(true)
                    .oAuthConsumer(consumerKey, consumerSecret)
                    .oAuthAccessToken(token, tokenSecret)
                    .build()
                if (!isDm) {
                    val status = twitter.v1().tweets().updateStatus(message)
                    "Your message was posted to https://twitter.com/${
                        twitter.v1().users().accountSettings.screenName
                    }/statuses/${status.id}"
                } else {
                    val dm = twitter.v1().directMessages().sendDirectMessage(handle, message)
                    dm.text
                }
            } catch (e: TwitterException) {
                throw ModuleException("tweet($message)", "An error has occurred: ${e.message}", e)
            }
        }
    }

    init {
        commands.add(TWITTER_CMD)
        commands.add(TWEET_CMD)
        help.add("To $TWEET_CMD on $name:")
        help.add(helpFormat("%c $TWEET_CMD <message>"))
        properties[AUTO_POST_PROP] = "false"
        initProperties(CONSUMER_KEY_PROP, CONSUMER_SECRET_PROP, HANDLE_PROP, TOKEN_PROP, TOKEN_SECRET_PROP)
    }
}
