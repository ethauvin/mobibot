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

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inserts presence information into Jaiku.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @version $Revision$, $Date$
 * @created Oct 11, 2007
 * @since 1.0
 */
public class Jaiku implements Runnable
{
	/**
	 * The bot.
	 */
	private final Mobibot _bot;

	/**
	 * The Jaiku API key.
	 */
	private final String _key;

	/**
	 * The Jaiku user.
	 */
	private final String _user;

	/**
	 * The Jaiku message.
	 */
	private final String _message;

	/**
	 * The nick of the person who sent the message.
	 */
	private final String _sender;

	/**
	 * Creates a new Jaiku object.
	 *
	 * @param bot The bot.
	 * @param sender The nick of the person who sent the message.
	 * @param user The Jaiku user.
	 * @param key The Jaiku API key.
	 * @param message The Jaiku message.
	 */
	public Jaiku(Mobibot bot, String sender, String user, String key, String message)
	{
		_bot = bot;
		_user = user;
		_key = key;
		_message = message;
		_sender = sender;
	}

	public final void run()
	{
		try
		{
			final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL("http://api.jaiku.com/xmlrpc"));

			final XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			final Map map = new HashMap(0);
			map.put("user", _user);
			map.put("personal_key", _key);
			map.put("message", _bot.getChannel() + ' ' + _message + " (" + _sender + ')');

			final List params = new ArrayList(0);
			params.add(map);

			client.execute("presence.send", params);
		}
		catch (Exception e)
		{
			_bot.getLogger().warn("Unable to post to Jaiku: " + _message, e);
			_bot.send(_sender, "An error has occurred: " + e.getMessage());
		}
	}
}
