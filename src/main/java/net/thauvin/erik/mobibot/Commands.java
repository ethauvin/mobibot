/*
 * @(#)Commands.java
 *
 * Copyright (c) 2004-2014, Erik C. Thauvin (erik@thauvin.net)
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

/**
 * The <code>commands</code>, <code>keywords</code> and <code>arguments</code>.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created 2014-04-26
 * @since 1.0
 */
public class Commands
{
	/**
	 * The currency command.
	 */
	public static final String CURRENCY_CMD = "currency";

	/**
	 * The rates keyword.
	 */
	public static final String CURRENCY_RATES_KEYWORD = "rates";

	/**
	 * The weather command.
	 */
	public static final String WEATHER_CMD = "weather";

	/**
	 * Debug command line argument.
	 */
	public static final String DEBUG_ARG = "debug";

	/**
	 * Help command line argument.
	 */
	public static final String HELP_ARG = "help";

	/**
	 * Properties command line argument.
	 */
	public static final String PROPS_ARG = "properties";

	/**
	 * Properties version line argument.
	 */
	public static final String VERSION_ARG = "version";

	/**
	 * The add (back)log command.
	 */
	public static final String ADDLOG_CMD = "addlog";

	/**
	 * The debug command.
	 */
	public static final String DEBUG_CMD = "debug";

	/**
	 * The dices command.
	 */
	public static final String DICE_CMD = "dice";

	/**
	 * The say command.
	 */
	public static final String SAY_CMD = "say";

	/**
	 * The die command.
	 */
	public static final String DIE_CMD = "die";

	/**
	 * The cycle command.
	 */
	public static final String CYCLE_CMD = "cycle";

	/**
	 * The msg command.
	 */
	public static final String MSG_CMD = "msg";

	/**
	 * The ignore command.
	 */
	public static final String IGNORE_CMD = "ignore";

	/**
	 * The ignore <code>me</code> keyword.
	 */
	public static final String IGNORE_ME_KEYWORD = "me";

	/**
	 * The help command.
	 */
	public static final String HELP_CMD = "help";

	/**
	 * The help on posting keyword.
	 */
	public static final String HELP_POSTING_KEYWORD = "posting";

	/**
	 * The help on tags keyword.
	 */
	public static final String HELP_TAGS_KEYWORD = "tags";

	/**
	 * The Google command.
	 */
	public static final String GOOGLE_CMD = "google";

	/**
	 * The Twitter command.
	 */
	public static final String TWITTER_CMD = "twitter";

	/**
	 * The math command.
	 */
	public static final String CALC_CMD = "calc";

	/**
	 * The me command.
	 */
	public static final String ME_CMD = "me";

	/**
	 * The nick command.
	 */
	public static final String NICK_CMD = "nick";

	/**
	 * The link command.
	 */
	public static final String LINK_CMD = "L";

	/**
	 * The lookup command.
	 */
	public static final String LOOKUP_CMD = "lookup";

	/**
	 * The ping command.
	 */
	public static final String PING_CMD = "ping";

	/**
	 * The pong command.
	 */
	public static final String PONG_CMD = "pong";

	/**
	 * The quote command.
	 */
	public static final String QUOTE_CMD = "quote";

	/**
	 * The recap command.
	 */
	public static final String RECAP_CMD = "recap";

	/**
	 * The stock command.
	 */
	public static final String STOCK_CMD = "stock";

	/**
	 * The time command.
	 */
	public static final String TIME_CMD = "time";

	/**
	 * The tell command.
	 */
	public static final String TELL_CMD = "tell";

	/**
	 * The {@link #TELL_CMD} delete command.
	 */
	public static final String TELL_DEL_CMD = "del";

	/**
	 * The {@link #TELL_CMD} all command.
	 */
	public static final String TELL_ALL_CMD = "all";

	/**
	 * The war command.
	 */
	public static final String WAR_CMD = "war";

	/**
	 * The users command.
	 */
	public static final String USERS_CMD = "users";

	/**
	 * The info command.
	 */
	public static final String INFO_CMD = "info";

	/**
	 * The version command.
	 */
	public static final String VERSION_CMD = "version";

	/**
	 * The view command.
	 */
	public static final String VIEW_CMD = "view";

	/**
	 * Disables the default constructor.
	 *
	 * @throws UnsupportedOperationException if an error occurred. if the constructor is called.
	 */
	private Commands()
			throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Illegal constructor call.");
	}
}