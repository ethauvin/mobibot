/*
 * Twitter.java
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

package net.thauvin.erik.mobibot.modules;

import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.Utils;
import net.thauvin.erik.mobibot.msg.Message;
import net.thauvin.erik.mobibot.msg.NoticeMessage;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * The Twitter module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Sept 10, 2008
 * @since 1.0
 */
public final class Twitter extends ThreadedModule {
    // Property keys
    static final String CONSUMER_KEY_PROP = "twitter-consumerKey";
    static final String CONSUMER_SECRET_PROP = "twitter-consumerSecret";
    static final String TOKEN_PROP = "twitter-token";
    static final String TOKEN_SECRET_PROP = "twitter-tokenSecret";
    // Twitter command
    private static final String TWITTER_CMD = "twitter";

    /**
     * Creates a new {@link Twitter} instance.
     */
    public Twitter() {
        super();

        commands.add(TWITTER_CMD);

        help.add("To post to Twitter:");
        help.add(Utils.helpIndent("%c " + TWITTER_CMD + " <message>"));

        properties.put(CONSUMER_SECRET_PROP, "");
        properties.put(CONSUMER_KEY_PROP, "");
        properties.put(TOKEN_PROP, "");
        properties.put(TOKEN_SECRET_PROP, "");
    }

    /**
     * Posts on Twitter.
     *
     * @param consumerKey    The consumer key.
     * @param consumerSecret The consumer secret.
     * @param token          The token.
     * @param tokenSecret    The token secret.
     * @param handle         The Twitter handle (dm) or nickname.
     * @param message        The message to post.
     * @param isDm           The direct message flag.
     * @return The confirmation {@link Message}.
     * @throws ModuleException If an error occurs while posting.
     */
    static Message twitterPost(final String consumerKey,
                               final String consumerSecret,
                               final String token,
                               final String tokenSecret,
                               final String handle,
                               final String message,
                               final boolean isDm) throws ModuleException {
        try {
            final ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
              .setOAuthAccessToken(token).setOAuthAccessTokenSecret(tokenSecret);

            final TwitterFactory tf = new TwitterFactory(cb.build());
            final twitter4j.Twitter twitter = tf.getInstance();

            if (!isDm) {
                final Status status = twitter.updateStatus(message);
                return new NoticeMessage("You message was posted to https://twitter.com/" + twitter.getScreenName()
                                         + "/statuses/" + status.getId());
            } else {
                final DirectMessage dm = twitter.sendDirectMessage(handle, message);
                return new NoticeMessage(dm.getText());
            }
        } catch (Exception e) {
            throw new ModuleException("twitterPost(" + message + ")", "An error has occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Posts on Twitter.
     *
     * @param handle  The Twitter handle (dm) or nickname.
     * @param message The message to post.
     * @param isDm    The direct message flag.
     * @return The {@link Message} to send back.
     * @throws ModuleException If an error occurs while posting.
     */
    public Message post(final String handle, final String message, final boolean isDm)
            throws ModuleException {
        return twitterPost(properties.get(CONSUMER_KEY_PROP),
                           properties.get(CONSUMER_SECRET_PROP),
                           properties.get(TOKEN_PROP),
                           properties.get(TOKEN_SECRET_PROP),
                           handle,
                           message,
                           isDm);
    }

    /**
     * Posts to twitter.
     */
    @Override
    void run(final Mobibot bot, final String sender, final String cmd, final String message, final boolean isPrivate) {
        try {
            bot.send(sender,
                     post(sender, message + " (by " + sender + " on " + bot.getChannel() + ')', false).getMessage(),
                     isPrivate);
        } catch (ModuleException e) {
            bot.getLogger().warn(e.getDebugMessage(), e);
            bot.send(sender, e.getMessage(), isPrivate);
        }
    }
}
