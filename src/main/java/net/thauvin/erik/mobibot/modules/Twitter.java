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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.Constants;
import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.ReleaseInfo;
import net.thauvin.erik.mobibot.TwitterTimer;
import net.thauvin.erik.mobibot.Utils;
import net.thauvin.erik.mobibot.commands.links.UrlMgr;
import net.thauvin.erik.mobibot.entries.EntryLink;
import net.thauvin.erik.mobibot.msg.Message;
import net.thauvin.erik.mobibot.msg.NoticeMessage;
import org.apache.commons.lang3.StringUtils;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * The Twitter module.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Sept 10, 2008
 * @since 1.0
 */
public final class Twitter extends ThreadedModule {
    // Property keys
    static final String AUTOPOST_PROP = "twitter-auto-post";
    static final String CONSUMER_KEY_PROP = "twitter-consumerKey";
    static final String CONSUMER_SECRET_PROP = "twitter-consumerSecret";
    static final String HANDLE_PROP = "twitter-handle";
    static final String TOKEN_PROP = "twitter-token";
    static final String TOKEN_SECRET_PROP = "twitter-tokenSecret";
    // Twitter command
    private static final String TWITTER_CMD = "twitter";

    // Twitter auto-posts.
    private final Set<Integer> entries = new HashSet<>();

    /**
     * Creates a new {@link Twitter} instance.
     */
    public Twitter(final Mobibot bot) {
        super(bot);

        commands.add(TWITTER_CMD);

        help.add("To post to Twitter:");
        help.add(Utils.helpIndent("%c " + TWITTER_CMD + " <message>"));

        properties.put(AUTOPOST_PROP, "false");
        initProperties(CONSUMER_KEY_PROP,CONSUMER_SECRET_PROP,HANDLE_PROP,TOKEN_PROP,TOKEN_SECRET_PROP);
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
     * Add an entry to be posted on Twitter.
     *
     * @param index The entry index.
     */
    public final void addEntry(final int index) {
        entries.add(index);
    }

    public final int entriesCount() {
        return entries.size();
    }

    public String getHandle() {
        return properties.get(HANDLE_PROP);
    }

    public final boolean hasEntry(final int index) {
        return entries.contains(index);
    }

    public boolean isAutoPost() {
        return isEnabled() && Boolean.parseBoolean(properties.get(AUTOPOST_PROP));
    }

    @Override
    boolean isValidProperties() {
        for (final String s : getPropertyKeys()) {
            if (!AUTOPOST_PROP.equals(s) && !HANDLE_PROP.equals(s) && StringUtils.isBlank(properties.get(s))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Send a notification to the registered Twitter handle.
     *
     * @param msg The twitter message.
     */
    public final void notification(final String msg) {
        if (isEnabled() && isNotBlank(getHandle())) {
            new Thread(() -> {
                try {
                    post(String.format(msg, bot.getName(), ReleaseInfo.VERSION, bot.getChannel()), true);
                } catch (ModuleException e) {
                    bot.getLogger().warn("Failed to notify @{}: {}", getHandle(), msg, e);
                }
            }).start();
        }
    }

    /**
     * Posts on Twitter.
     *
     * @param message The message to post.
     * @param isDm    The direct message flag.
     * @throws ModuleException If an error occurs while posting.
     */
    public void post(final String message, final boolean isDm)
            throws ModuleException {
        post(properties.get(HANDLE_PROP), message, isDm);
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
     * Post an entry to twitter.
     *
     * @param index The post entry index.
     */
    @SuppressFBWarnings("SUI_CONTAINS_BEFORE_REMOVE")
    public final void postEntry(final int index) {
        if (isAutoPost() && hasEntry(index) && UrlMgr.getEntriesCount() >= index) {
            final EntryLink entry = UrlMgr.getEntry(index);
            final String msg =
                    entry.getTitle() + ' ' + entry.getLink() + " via " + entry.getNick() + " on " + bot.getChannel();
            new Thread(() -> {
                try {
                    if (bot.getLogger().isDebugEnabled()) {
                        bot.getLogger().debug("Posting {}{} to Twitter.", Constants.LINK_CMD, index + 1);
                    }
                    post(msg, false);
                } catch (ModuleException e) {
                    bot.getLogger().warn("Failed to post entry on Twitter.", e);
                }
            }).start();
            removeEntry(index);
        }
    }

    public void queueEntry(final int index) {
        if (isAutoPost()) {
            addEntry(index);
            bot.getLogger().debug("Scheduling ${Constants.LINK_CMD}${index + 1} for posting on Twitter.");
            bot.getTimer().schedule(new TwitterTimer(bot, index), Constants.TIMER_DELAY * 60L * 1000L);
        }
    }

    public final void removeEntry(final int index) {
        entries.remove(index);
    }

    /**
     * Posts to twitter.
     */
    @Override
    void run(final String sender, final String cmd, final String message, final boolean isPrivate) {
        try {
            bot.send(sender,
                     post(sender, message + " (by " + sender + " on " + bot.getChannel() + ')', false).getText(),
                     isPrivate);
        } catch (ModuleException e) {
            bot.getLogger().warn(e.getDebugMessage(), e);
            bot.send(sender, e.getMessage(), isPrivate);
        }
    }

    /**
     * Post all the entries to Twitter on shutdown.
     */
    public final void shutdown() {
        for (final int index : entries) {
            postEntry(index);
        }
    }
}
