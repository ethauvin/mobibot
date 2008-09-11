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
	 * The Twitter API password.
	 */
	private final String _pwd;

	/**
	 * The Twitter user.
	 */
	private final String _user;

	/**
	 * The Twitter message.
	 */
	private final String _message;

	/**
	 * The Twitter source.
	 */
	private final String _source;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String _sender;

	/**
	 * Creates a new Twitter object.
	 *
	 * @param bot The bot.
	 * @param sender The nick of the person who sent the message.
	 * @param user The Twitter user.
	 * @param password The Twitter password.
	 * @param source The Twitter client id/source.
	 * @param message The Twitter message.
	 */
	public Twitter(Mobibot bot, String sender, String user, String password, String source, String message)
	{
		_bot = bot;
		_user = user;
		_pwd = password;
		_source = source;
		_message = message;
		_sender = sender;
	}

	public final void run()
	{
		try
		{
			twitter4j.Twitter twitter = new twitter4j.Twitter(_user, _pwd);

			if (Mobibot.isValidString(_source))
			{
				twitter.setSource(_source);
			}

			twitter.update(_message + " (" + _sender + ')');

			_bot.send(_sender, "You message was posted to http://twitter.com/" + _user);
		}
		catch (Exception e)
		{
			_bot.getLogger().warn("Unable to post to Twitter: " + _message, e);
			_bot.send(_sender, "An error has occurred: " + e.getMessage());
		}
	}
}