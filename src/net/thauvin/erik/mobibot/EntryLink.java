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

import com.sun.syndication.feed.synd.SyndCategoryImpl;

import java.io.Serializable;

import java.util.*;


/**
 * The class used to store link entries.
 *
 * @author  Erik C. Thauvin
 * @version $Revision$, $Date$
 * @created Jan 31, 2004
 * @since   1.0
 */
public class EntryLink implements Serializable
{
	/**
	 * The serial version UID.
	 */
	static final long serialVersionUID = 3676245542270899086L;

	// The channel
	private String _channel = "";

	// The link's comments
	private final List _comments = new ArrayList(0);

	// The creation date
	private Date _date = Calendar.getInstance().getTime();

	// The link's URL
	private String _link = "";

	// The author's login
	private String _login = "";

	// The author's nickname
	private String _nick = "";

	// The tags/categories
	private final List _tags = new ArrayList(0);

	// The link's title
	private String _title = "";

	/**
	 * Creates a new entry.
	 *
	 * @param link    The new entry's link.
	 * @param title   The new entry's title.
	 * @param nick    The nickname of the author of the link.
	 * @param login   The login of the author of the link.
	 * @param channel The channel.
	 * @param tags    The entry's tags/categories.
	 */
	public EntryLink(String link, String title, String nick, String login, String channel, String tags)
	{
		_link = link;
		_title = title;
		_nick = nick;
		_login = login;
		_channel = channel;

		setTags(tags);
	}


	/**
	 * Creates a new entry.
	 *
	 * @param link    The new entry's link.
	 * @param title   The new entry's title.
	 * @param nick    The nickname of the author of the link.
	 * @param channel The channel.
	 * @param date    The entry date.
	 * @param tags    The entry's tags/categories.
	 */
	public EntryLink(String link, String title, String nick, String channel, Date date, List tags)
	{
		_link = link;
		_title = title;
		_nick = nick;
		_channel = channel;
		_date = date;


		setTags(tags);
	}

	/**
	 * Creates a new EntryLink object.
	 */
	protected EntryLink()
	{
		; // Required for serialization.
	}

	/**
	 * Adds a new comment.
	 *
	 * @param  comment The actual comment.
	 * @param  nick    The nickname of the author of the comment.
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
	 * Returns the channel the link was posted on.
	 *
	 * @return The channel
	 */
	public final synchronized String getChannel()
	{
		return _channel;
	}

	/**
	 * Returns a comment.
	 *
	 * @param  index The comment's index.
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
		return ((EntryComment[]) _comments.toArray(new EntryComment[_comments.size()]));
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
	 * Returns the tags formatted for del.icio.us.
	 *
	 * @return The tags as a comma-delimited string.
	 */
	public final synchronized String getDeliciousTags()
	{
		final StringBuffer tags = new StringBuffer(_nick);

		for (int i = 0; i < _tags.size(); i++)
		{
			tags.append(',');
			tags.append(((SyndCategoryImpl) _tags.get(i)).getName());
		}

		return tags.toString();
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
	 * Return's the comment's author login.
	 *
	 * @return The login;
	 */
	public final synchronized String getLogin()
	{
		return _login;
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
	 * Returns the tags.
	 *
	 * @return The tags.
	 */
	public final synchronized List getTags()
	{
		return _tags;
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
	 * Returns true if the entry has comments.
	 *
	 * @return true if there are comments, false otherwise.
	 */
	public final synchronized boolean hasComments()
	{
		return (!_comments.isEmpty());
	}

	/**
	 * Returns true if the entry has tags.
	 *
	 * @return true if there are tags, false otherwise.
	 */
	public final synchronized boolean hasTags()
	{
		return (!_tags.isEmpty());
	}

	/**
	 * Sets the channel.
	 *
	 * @param channel The channel.
	 */
	public final synchronized void setChannel(String channel)
	{
		_channel = channel;
	}

	/**
	 * /** Sets a comment.
	 *
	 * @param index   The comment's index.
	 * @param comment The actual comment.
	 * @param nick    The nickname of the author of the comment.
	 */
	public final synchronized void setComment(int index, String comment, String nick)
	{
		if (index < _comments.size())
		{
			_comments.set(index, new EntryComment(comment, nick));
		}
	}

	/**
	 * Sets the comment's link.
	 *
	 * @param link The new link.
	 */
	public final synchronized void setLink(String link)
	{
		_link = link;
	}

	/**
	 * Set the comment's author login.
	 *
	 * @param login The new login.
	 */
	public final synchronized void setLogin(String login)
	{
		_login = login;
	}

	/**
	 * Sets the comment's author nickname.
	 *
	 * @param nick The new nickname.
	 */
	public final synchronized void setNick(String nick)
	{
		_nick = nick;
	}

	/**
	 * Sets the tags.
	 *
	 * @param tags The tags.
	 */
	public final synchronized void setTags(List tags)
	{
		_tags.addAll(tags);
	}

	/**
	 * Sets the tags.
	 *
	 * @param tags The space-delimited tags.
	 */
	public final synchronized void setTags(String tags)
	{
		if (tags != null)
		{
			final String[] parts = tags.replaceAll(", ", " ").replaceAll(",", " ").split(" ");

			SyndCategoryImpl tag;
			String part;
			char mod;

			for (int i = 0; i < parts.length; i++)
			{
				part = parts[i].trim();

				if (part.length() >= 2)
				{
					tag = new SyndCategoryImpl();
					tag.setName(part.substring(1).toLowerCase());

					mod = part.charAt(0);

					if (mod == '-')
					{
						// Don't remove the channel tag, if any.
						if (!tag.getName().equals(_channel.substring(1)))
						{
							_tags.remove(tag);
						}
					}
					else if (mod == '+')
					{
						if (!_tags.contains(tag))
						{
							_tags.add(tag);
						}
					}
					else
					{
						tag.setName(part.trim().toLowerCase());

						if (!_tags.contains(tag))
						{
							_tags.add(tag);
						}
					}
				}
			}
		}
	}

	/**
	 * Sets the comment's title.
	 *
	 * @param title The new title.
	 */
	public final synchronized void setTitle(String title)
	{
		_title = title;
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return A string representation of the object.
	 */
	public final String toString()
	{
		final StringBuffer sb = new StringBuffer(super.toString());
		sb.append("[ channel -> '").append(_channel).append('\'');
		sb.append(", comments -> ").append(_comments);
		sb.append(", date -> ").append(_date);
		sb.append(", link -> '").append(_link).append('\'');
		sb.append(", login -> '").append(_login).append('\'');
		sb.append(", nick -> '").append(_nick).append('\'');
		sb.append(", tags -> ").append(_tags);
		sb.append(", title -> '").append(_title).append('\'');
		sb.append(" ]");

		return sb.toString();
	}
}
