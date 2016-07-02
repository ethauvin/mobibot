/*
 * Commands.java
 *
 * Copyright (c) 2004-2016, Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot;

/**
 * The <code>commands</code>, <code>keywords</code> and <code>arguments</code>.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created 2014-04-26
 * @since 1.0
 */
final class Commands
{
	/**
	 * The add (back)log command.
	 */
	public static final String ADDLOG_CMD = "addlog";

	/**
	 * The cycle command.
	 */
	public static final String CYCLE_CMD = "cycle";

	/**
	 * Debug command line argument.
	 */
	public static final String DEBUG_ARG = "debug";

	/**
	 * The debug command.
	 */
	public static final String DEBUG_CMD = "debug";

	/**
	 * The die command.
	 */
	public static final String DIE_CMD = "die";

	/**
	 * Help command line argument.
	 */
	public static final String HELP_ARG = "help";

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
	 * The ignore command.
	 */
	public static final String IGNORE_CMD = "ignore";

	/**
	 * The ignore <code>me</code> keyword.
	 */
	public static final String IGNORE_ME_KEYWORD = "me";

	/**
	 * The info command.
	 */
	public static final String INFO_CMD = "info";

	/**
	 * The link command.
	 */
	public static final String LINK_CMD = "L";

	/**
	 * The me command.
	 */
	public static final String ME_CMD = "me";

	/**
	 * The msg command.
	 */
	public static final String MSG_CMD = "msg";

	/**
	 * The nick command.
	 */
	public static final String NICK_CMD = "nick";

	/**
	 * Properties command line argument.
	 */
	public static final String PROPS_ARG = "properties";

	/**
	 * The recap command.
	 */
	public static final String RECAP_CMD = "recap";

	/**
	 * The say command.
	 */
	public static final String SAY_CMD = "say";


	/**
	 * The users command.
	 */
	public static final String USERS_CMD = "users";

	/**
	 * Properties version line argument.
	 */
	public static final String VERSION_ARG = "version";

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
	 * @throws UnsupportedOperationException If the constructor is called.
	 */
	private Commands()
			throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Illegal constructor call.");
	}
}