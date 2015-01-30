/*
 * Twitter.java
 *
 * Copyright (c) 2004-2015, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.thauvin.erik.mobibot;

import twitter4j.Status;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Processes the {@link Commands#TWITTER_CMD} command.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created Sept 10, 2008
 * @since 1.0
 */
public class Twitter implements Runnable
{
	/**
	 * The Twitter access token.
	 */
	private final String accessToken;

	/**
	 * The Twitter access token secret.
	 */
	private final String accessTokenSecret;

	/**
	 * The bot.
	 */
	private final Mobibot bot;

	/**
	 * The Twitter consumer key.
	 */
	private final String consumerKey;

	/**
	 * The Twitter consumer secret.
	 */
	private final String consumerSecret;

	/**
	 * The Twitter message.
	 */
	private final String message;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String sender;

	/**
	 * Creates a new {@link Twitter} instance.
	 *
	 * @param bot The bot's instance.
	 * @param sender The nick of the person who sent the message.
	 * @param consumerKey The Twitter consumer key.
	 * @param consumerSecret The Twitter consumer secret.
	 * @param accessToken The Twitter access token.
	 * @param accessTokenSecret The Twitter access token secret.
	 * @param message The Twitter message.
	 */
	public Twitter(Mobibot bot, String sender, String consumerKey, String consumerSecret, String accessToken,
	               String accessTokenSecret, String message)
	{
		this.bot = bot;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
		this.message = message;
		this.sender = sender;
	}

	/**
	 * Posts to twitter.
	 */
	public final void run()
	{
		try
		{
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
					.setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret);
			final TwitterFactory tf = new TwitterFactory(cb.build());
			final twitter4j.Twitter twitter = tf.getInstance();

			final Status status = twitter.updateStatus(message + " (" + sender + ')');

			bot.send(sender,
			         "You message was posted to http://twitter.com/" + twitter.getScreenName() + "/statuses/" + status
					         .getId());
		}
		catch (Exception e)
		{
			bot.getLogger().warn("Unable to post to Twitter: " + message, e);
			bot.send(sender, "An error has occurred: " + e.getMessage());
		}
	}
}