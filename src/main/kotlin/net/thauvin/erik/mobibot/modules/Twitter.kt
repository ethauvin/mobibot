/*
 * Twitter.kt
 *
 * Copyright (c) 2004-2021, Erik C. Thauvin (erik@thauvin.net)
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

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.TwitterTimer
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.commands.links.LinksMgr
import net.thauvin.erik.mobibot.entries.EntriesUtils
import net.thauvin.erik.mobibot.msg.Message
import net.thauvin.erik.mobibot.msg.NoticeMessage
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

/**
 * The Twitter module.
 */
class Twitter(bot: Mobibot) : ThreadedModule(bot) {
    // Twitter auto-posts.
    private val entries: MutableSet<Int> = HashSet()

    /**
     * Add an entry to be posted on Twitter.
     */
    private fun addEntry(index: Int) {
        entries.add(index)
    }

    fun entriesCount(): Int {
        return entries.size
    }

    private val handle: String?
        get() = properties[HANDLE_PROP]

    private fun hasEntry(index: Int): Boolean {
        return entries.contains(index)
    }

    val isAutoPost: Boolean
        get() = isEnabled && properties[AUTOPOST_PROP].toBoolean()

    override val isValidProperties: Boolean
        get() {
            for (s in propertyKeys) {
                if (AUTOPOST_PROP != s && HANDLE_PROP != s && properties[s].isNullOrBlank()) {
                    return false
                }
            }
            return true
        }

    /**
     * Send a notification to the registered Twitter handle.
     */
    fun notification(msg: String) {
        with(bot) {
            if (isEnabled && !handle.isNullOrBlank()) {
                runBlocking {
                    launch {
                        try {
                            post(message = msg, isDm = true)
                            if (logger.isDebugEnabled) logger.debug("Notified @$handle: $msg")
                        } catch (e: ModuleException) {
                            if (logger.isWarnEnabled) logger.warn("Failed to notify @$handle: $msg", e)
                        }
                    }
                }
            }
        }
    }

    /**
     * Posts on Twitter.
     */
    @Throws(ModuleException::class)
    fun post(handle: String = "${properties[HANDLE_PROP]}", message: String, isDm: Boolean): Message {
        return twitterPost(
            properties[CONSUMER_KEY_PROP],
            properties[CONSUMER_SECRET_PROP],
            properties[TOKEN_PROP],
            properties[TOKEN_SECRET_PROP],
            handle,
            message,
            isDm
        )
    }

    /**
     * Post an entry to twitter.
     */
    fun postEntry(index: Int) {
        with(bot) {
            if (isAutoPost && hasEntry(index) && LinksMgr.entries.size >= index) {
                val entry = LinksMgr.entries[index]
                val msg = "${entry.title} ${entry.link} via ${entry.nick} on $channel"
                runBlocking {
                    launch {
                        try {
                            if (logger.isDebugEnabled) {
                                logger.debug("Posting {} to Twitter.", EntriesUtils.buildLinkCmd(index))
                            }
                            post(message = msg, isDm = false)
                        } catch (e: ModuleException) {
                            if (logger.isWarnEnabled) logger.warn("Failed to post entry on Twitter.", e)
                        }
                    }
                }
                removeEntry(index)
            }
        }
    }

    fun queueEntry(index: Int) {
        if (isAutoPost) {
            addEntry(index)
            if (bot.logger.isDebugEnabled) {
                bot.logger.debug("Scheduling {} for posting on Twitter.", EntriesUtils.buildLinkCmd(index))
            }
            @Suppress("MagicNumber")
            bot.timer.schedule(TwitterTimer(bot, index), Constants.TIMER_DELAY * 60L * 1000L)
        }
    }

    fun removeEntry(index: Int) {
        entries.remove(index)
    }

    /**
     * Posts to twitter.
     */
    override fun run(sender: String, cmd: String, args: String, isPrivate: Boolean) {
        with(bot) {
            try {
                send(
                    sender,
                    post(sender, "$args (by $sender on $channel)", false).msg,
                    isPrivate
                )
            } catch (e: ModuleException) {
                if (logger.isWarnEnabled) logger.warn(e.debugMessage, e)
                send(sender, e.message, isPrivate)
            }
        }
    }

    /**
     * Post all the entries to Twitter on shutdown.
     */
    fun shutdown() {
        if (isAutoPost) {
            for (index in entries) {
                postEntry(index)
            }
        }
    }

    companion object {
        // Property keys
        const val AUTOPOST_PROP = "twitter-auto-post"
        const val CONSUMER_KEY_PROP = "twitter-consumerKey"
        const val CONSUMER_SECRET_PROP = "twitter-consumerSecret"
        const val HANDLE_PROP = "twitter-handle"
        const val TOKEN_PROP = "twitter-token"
        const val TOKEN_SECRET_PROP = "twitter-tokenSecret"

        // Twitter command
        private const val TWITTER_CMD = "twitter"

        /**
         * Posts on Twitter.
         */
        @JvmStatic
        @Throws(ModuleException::class)
        fun twitterPost(
            consumerKey: String?,
            consumerSecret: String?,
            token: String?,
            tokenSecret: String?,
            handle: String?,
            message: String,
            isDm: Boolean
        ): Message {
            return try {
                val cb = ConfigurationBuilder()
                cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(consumerKey)
                    .setOAuthConsumerSecret(consumerSecret)
                    .setOAuthAccessToken(token).setOAuthAccessTokenSecret(tokenSecret)
                val tf = TwitterFactory(cb.build())
                val twitter = tf.instance
                if (!isDm) {
                    val status = twitter.updateStatus(message)
                    NoticeMessage(
                        "You message was posted to https://twitter.com/${twitter.screenName}/statuses/${status.id}"
                    )
                } else {
                    val dm = twitter.sendDirectMessage(handle, message)
                    NoticeMessage(dm.text)
                }
            } catch (e: TwitterException) {
                throw ModuleException("twitterPost($message)", "An error has occurred: ${e.message}", e)
            }
        }
    }

    init {
        commands.add(TWITTER_CMD)
        help.add("To post to Twitter:")
        help.add(helpFormat("%c $TWITTER_CMD <message>"))
        properties[AUTOPOST_PROP] = "false"
        initProperties(CONSUMER_KEY_PROP, CONSUMER_SECRET_PROP, HANDLE_PROP, TOKEN_PROP, TOKEN_SECRET_PROP)
    }
}
