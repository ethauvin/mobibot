/*
 * Ping.java
 *
 * Copyright (c) 2016 Erik C. Thauvin (http://erik.thauvin.net/)
 * All rights reserved.
 */
package net.thauvin.erik.mobibot.modules;

import net.thauvin.erik.mobibot.Mobibot;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * The <code>Ping</code> class.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created 2016-07-02
 * @since 1.0
 */
public class Ping extends AbstractModule
{
	/**
	 * The ping responses.
	 */
	private static final List<String> PINGS = Arrays.asList("is barely alive.",
	                                                        "is trying to stay awake.",
	                                                        "has gone fishing.",
	                                                        "is somewhere over the rainbow.",
	                                                        "has fallen and can't get up.",
	                                                        "is running. You better go chase it.",
	                                                        "has just spontaneously combusted.",
	                                                        "is talking to itself... don't interrupt. That's rude.",
	                                                        "is bartending at an AA meeting.",
	                                                        "is hibernating.",
	                                                        "is saving energy: apathetic mode activated.",
	                                                        "is busy. Go away!");

	/**
	 * The ping command.
	 */
	private static final String PING_CMD = "ping";

	/**
	 * The default constructor.
	 */
	public Ping()
	{
		commands.add(PING_CMD);
	}

	@Override
	public void commandResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate)
	{
		final Random r = new Random();

		bot.action(PINGS.get(r.nextInt(PINGS.size())));
	}

	@Override
	public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate)
	{
		bot.send(sender, "To ping the bot:");
		bot.send(sender, bot.helpIndent(bot.getNick() + ": " + PING_CMD));
	}
}