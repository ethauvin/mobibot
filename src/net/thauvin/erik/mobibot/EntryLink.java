/*
 * @(#)EntryLink.java
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * The class used to store link entries.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 *
 * @created Jan 31, 2004
 * @since 1.0
 */
public class EntryLink implements Serializable
{
	/**
	 * The serial version UID.
	 */
	static final long serialVersionUID = 3676245542270899086L;

	/**
	 * The creation date.
	 */
	private Date _date = Calendar.getInstance().getTime();

	/**
	 * The comments.
	 */
	private final List _comments = new ArrayList(0);
	private String _link = "";
	private String _login = "";
	private String _nick = "";
	private String _title = "";


	/**
	 * Creates a new entry.
	 *
	 * @param link The new entry's link.
	 * @param title The new entry's title.
	 * @param nick The nickname of the author of the link.
	 * @param date The entry date.
	 */
	public EntryLink(String link, String title, String nick, Date date)
	{
		_link = link;
		_title = title;
		_nick = nick;
		_date = date;
	}

	/**
	 * Creates a new entry.
	 *
	 * @param link The new entry's link.
	 * @param title The new entry's title.
	 * @param nick The nickname of the author of the link.
	 * @param login The login of the author of the link.
	 */
	public EntryLink(String link, String title, String nick, String login)
	{
		_link = link;
		_title = title;
		_nick = nick;
		_login = login;
	}

	/**
	 * Creates a new EntryLink object.
	 */
	protected EntryLink()
	{
		; // Required for serialization.		
	}

	/**
	 * Sets a comment.
	 *
	 * @param index The comment's index.
	 * @param comment The actual comment.
	 * @param nick The nickname of the author of the comment.
	 */
	public final synchronized void setComment(int index, String comment, String nick)
	{
		if (index < _comments.size())
		{
			_comments.set(index, new EntryComment(comment, nick));
		}
	}

	/**
	 * Returns a comment.
	 *
	 * @param index The comment's index.
	 *
	 * @return The specific comment.
	 */
	public final synchronized EntryComment getComment(int index)
	{
		return ((EntryComment) _comments.get(index));
	}

	/**
	 * Returns all the comments.
	 *
	 * @return The comments.
	 */
	public final synchronized EntryComment[] getComments()
	{
		return ((EntryComment[]) _comments.toArray(new EntryComment[0]));
	}

	/**
	 * Returns the total number of comments.
	 *
	 * @return The count of comments.
	 */
	public final synchronized int getCommentsCount()
	{
		return _comments.size();
	}

	/**
	 * Returns the comment's creation date.
	 *
	 * @return The date.
	 */
	public final synchronized Date getDate()
	{
		return _date;
	}

	/**
	 * Sets the comment's link.
	 *
	 * @param link The new link.
	 */
	public final synchronized void setLink(String link)
	{
		this._link = link;
	}

	/**
	 * Returns the comment's link.
	 *
	 * @return The link.
	 */
	public final synchronized String getLink()
	{
		return _link;
	}

	/**
	 * Set the comment's author login.
	 *
	 * @param login The new login.
	 */
	public final synchronized void setLogin(String login)
	{
		this._login = login;
	}

	/**
	 * Return's the comment's author login.
	 *
	 * @return The login;
	 */
	public final synchronized String getLogin()
	{
		return _login;
	}

	/**
	 * Sets the comment's author nickname.
	 *
	 * @param nick The new nickname.
	 */
	public final synchronized void setNick(String nick)
	{
		this._nick = nick;
	}

	/**
	 * Returns the comment's author nickname.
	 *
	 * @return The nickname.
	 */
	public final synchronized String getNick()
	{
		return _nick;
	}

	/**
	 * Sets the comment's title.
	 *
	 * @param title The new title.
	 */
	public final synchronized void setTitle(String title)
	{
		this._title = title;
	}

	/**
	 * Returns the comment's title.
	 *
	 * @return The title.
	 */
	public final synchronized String getTitle()
	{
		return _title;
	}

	/**
	 * Adds a new comment.
	 *
	 * @param comment The actual comment.
	 * @param nick The nickname of the author of the comment.
	 *
	 * @return The total number of comments for this entry.
	 */
	public final synchronized int addComment(String comment, String nick)
	{
		_comments.add(new EntryComment(comment, nick));

		return (_comments.size() - 1);
	}

	/**
	 * Deletes a specific comment.
	 *
	 * @param index The index of the comment to delete.
	 */
	public final synchronized void deleteComment(int index)
	{
		if (index < _comments.size())
		{
			_comments.remove(index);
		}
	}

	/**
	 * Returns true if the entry has comments.
	 *
	 * @return true if there are comments, false otherwise.
	 */
	public final synchronized boolean hasComments()
	{
		return (_comments.size() > 0);
	}
}
