/*
 * @(#)Jaiku.java
 *
 * Copyright (C) 2007 Erik C. Thauvin
 * All rights reserved.
 *
 * $Id$
 *
 */
package net.thauvin.erik.mobibot;

import twitter4j.Status;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;

/**
 * Inserts presence information into Twitter.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @version $Revision$, $Date$
 * @created Sept 10, 2008
 * @since 1.0
 */
public class Twitter implements Runnable
{
	/**
	 * The bot.
	 */
	private final Mobibot _bot;

	/**
	 * The Twitter consumer secret.
	 */
	private final String _consumerSecret;

	/**
	 * The Twitter consumer key.
	 */
	private final String _consumerKey;

	/**
	 * The Twitter message.
	 */
	private final String _message;

	/**
	 * The Twitter access token.
	 */
	private final String _accessToken;

	/**
	 * The Twitter access token secret.
	 */
	private final String _accessTokenSecret;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String _sender;

	/**
	 * Creates a new Twitter object.
	 *
	 * @param bot The bot.
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
		_bot = bot;
		_consumerKey = consumerKey;
		_consumerSecret = consumerSecret;
		_accessToken = accessToken;
		_accessTokenSecret = accessTokenSecret;
		_message = message;
		_sender = sender;
	}

	public final void run()
	{
		try
		{
			final twitter4j.Twitter twitter = new TwitterFactory().getOAuthAuthorizedInstance(_consumerKey,
			                                                                                  _consumerSecret,
			                                                                                  new AccessToken(
					                                                                                  _accessToken,
					                                                                                  _accessTokenSecret));

			final Status status = twitter.updateStatus(_message + " (" + _sender + ')');

			_bot.send(_sender,
			          "You message was posted to http://twitter.com/" + twitter.getScreenName() + "/statuses/" + status
					          .getId());
		}
		catch (Exception e)
		{
			_bot.getLogger().warn("Unable to post to Twitter: " + _message, e);
			_bot.send(_sender, "An error has occurred: " + e.getMessage());
		}
	}
}