/*
 * Twitter.java
 *
 * Copyright (c) 2004-2019, Erik C. Thauvin (erik@thauvin.net)
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
    /**
     * The twitter command.
     */
    public static final String TWITTER_CMD = "twitter";

    // The property keys.
    private static final String CONSUMER_KEY_PROP = "twitter-consumerKey";
    private static final String CONSUMER_SECRET_PROP = "twitter-consumerSecret";
    private static final String TOKEN_PROP = "twitter-token";
    private static final String TOKEN_SECRET_PROP = "twitter-tokenSecret";

    /**
     * Creates a new {@link Twitter} instance.
     */
    public Twitter() {
        commands.add(TWITTER_CMD);
        properties.put(CONSUMER_SECRET_PROP, "");
        properties.put(CONSUMER_KEY_PROP, "");
        properties.put(TOKEN_PROP, "");
        properties.put(TOKEN_SECRET_PROP, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        if (isEnabled()) {
            bot.send(sender, "To post to Twitter:");
            bot.send(sender, bot.helpIndent(bot.getNick() + ": " + TWITTER_CMD + " <message>"));
        } else {
            bot.send(sender, "The Twitter posting facility is disabled.");
        }
    }

    /**
     * Posts to twitter.
     */
    @Override
    void run(final Mobibot bot, final String sender, final String message) {
        try {
            final ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                .setOAuthConsumerKey(properties.get(CONSUMER_KEY_PROP))
                .setOAuthConsumerSecret(properties.get(CONSUMER_SECRET_PROP))
                .setOAuthAccessToken(properties.get(TOKEN_PROP))
                .setOAuthAccessTokenSecret(properties.get(TOKEN_SECRET_PROP));
            final TwitterFactory tf = new TwitterFactory(cb.build());
            final twitter4j.Twitter twitter = tf.getInstance();

            final Status status = twitter.updateStatus(message + " (" + sender + ')');

            bot.send(sender,
                "You message was posted to http://twitter.com/" + twitter.getScreenName() + "/statuses/" + status
                    .getId());
        } catch (Exception e) {
            bot.getLogger().warn("Unable to post to Twitter: " + message, e);
            bot.send(sender, "An error has occurred: " + e.getMessage());
        }
    }
}
