/*
 * @(#)EntryComment.java
 *
 * Copyright (c) 2004, Erik C. Thauvin (erik@thauvin.net)
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
 *
 * $Id$
 *
 */
package net.thauvin.erik.mobibot;

import java.io.Serializable;

import java.util.Calendar;
import java.util.Date;


/**
 * The class used to store comments associated to a specific entry.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 *
 * @created Jan 31, 2004
 * @since 1.0
 */
public class EntryComment implements Serializable
{
	/**
	 * The serial version UID.
	 */
	static final long serialVersionUID = 6957415292233553224L;

	/**
	 * The creation date.
	 */
	private final Date _date = Calendar.getInstance().getTime();
	private String _comment = "";
	private String _nick = "";

	/**
	 * Creates a new comment.
	 *
	 * @param comment The new comment.
	 * @param nick The nickname of the comment's author.
	 */
	public EntryComment(String comment, String nick)
	{
		_comment = comment;
		_nick = nick;
	}

	/**
	 * Creates a new comment.
	 */
	protected EntryComment()
	{
		; // Required for serialization.
	}

	/**
	 * Sets the comment.
	 *
	 * @param comment The actual comment.
	 */
	public final void setComment(String comment)
	{
		_comment = comment;
	}

	/**
	 * Returns the comment.
	 *
	 * @return The comment.
	 */
	public final String getComment()
	{
		return _comment;
	}

	/**
	 * Returns the comment's creation date.
	 *
	 * @return The date.
	 */
	public final Date getDate()
	{
		return _date;
	}

	/**
	 * Sets the nickname of the author of the comment.
	 *
	 * @param nick The new nickname.
	 */
	public final void setNick(String nick)
	{
		_nick = nick;
	}

	/**
	 * Returns the nickname of the author of the comment.
	 *
	 * @return The nickname.
	 */
	public final String getNick()
	{
		return _nick;
	}
}
