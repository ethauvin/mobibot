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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * The class used to store link entries.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 * @created Jan 31, 2004
 * @since 1.0
 */
public class EntryLink implements Serializable
{
	/**
	 * The serial version UID.
	 *
	 * @noinspection UnusedDeclaration
	 */
	static final long serialVersionUID = 3676245542270899086L;

	// The link's comments
	private final List<EntryComment> comments = new ArrayList<EntryComment>(0);

	// The tags/categories
	private final List<SyndCategoryImpl> tags = new ArrayList<SyndCategoryImpl>(0);

	// The channel
	private String channel = "";

	// The creation date
	private Date date = Calendar.getInstance().getTime();

	// The link's URL
	private String link = "";

	// The author's login
	private String login = "";

	// The author's nickname
	private String nick = "";

	// The link's title
	private String title = "";

	/**
	 * Creates a new entry.
	 *
	 * @param link The new entry's link.
	 * @param title The new entry's title.
	 * @param nick The nickname of the author of the link.
	 * @param login The login of the author of the link.
	 * @param channel The channel.
	 * @param tags The entry's tags/categories.
	 */
	public EntryLink(String link, String title, String nick, String login, String channel, String tags)
	{
		this.link = link;
		this.title = title;
		this.nick = nick;
		this.login = login;
		this.channel = channel;

		setTags(tags);
	}

	/**
	 * Creates a new entry.
	 *
	 * @param link The new entry's link.
	 * @param title The new entry's title.
	 * @param nick The nickname of the author of the link.
	 * @param channel The channel.
	 * @param date The entry date.
	 * @param tags The entry's tags/categories.
	 */
	public EntryLink(String link, String title, String nick, String channel, Date date, List<SyndCategoryImpl> tags)
	{
		this.link = link;
		this.title = title;
		this.nick = nick;
		this.channel = channel;
		this.date = date;

		setTags(tags);
	}

	/**
	 * Creates a new EntryLink object.
	 * @noinspection UnusedDeclaration
	 */
	protected EntryLink()
	{
		; // Required for serialization.
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
		comments.add(new EntryComment(comment, nick));

		return (comments.size() - 1);
	}

	/**
	 * Deletes a specific comment.
	 *
	 * @param index The index of the comment to delete.
	 */
	public final synchronized void deleteComment(int index)
	{
		if (index < comments.size())
		{
			comments.remove(index);
		}
	}

	/**
	 * Returns the channel the link was posted on.
	 *
	 * @return The channel
	 */
	public final synchronized String getChannel()
	{
		return channel;
	}

	/**
	 * Sets the channel.
	 *
	 * @param channel The channel.
	 * @noinspection UnusedDeclaration
	 */
	public final synchronized void setChannel(String channel)
	{
		this.channel = channel;
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
		return (comments.get(index));
	}

	/**
	 * Returns all the comments.
	 *
	 * @return The comments.
	 */
	public final synchronized EntryComment[] getComments()
	{
		return (comments.toArray(new EntryComment[comments.size()]));
	}

	/**
	 * Returns the total number of comments.
	 *
	 * @return The count of comments.
	 */
	public final synchronized int getCommentsCount()
	{
		return comments.size();
	}

	/**
	 * Returns the comment's creation date.
	 *
	 * @return The date.
	 */
	public final synchronized Date getDate()
	{
		return date;
	}

	/**
	 * Returns the tags formatted for del.icio.us.
	 *
	 * @return The tags as a comma-delimited string.
	 */
	public final synchronized String getDeliciousTags()
	{
		final StringBuilder tags = new StringBuilder(nick);

		for (final Object tag : this.tags)
		{
			tags.append(',');
			tags.append(((SyndCategoryImpl) tag).getName());
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
		return link;
	}

	/**
	 * Sets the comment's link.
	 *
	 * @param link The new link.
	 */
	public final synchronized void setLink(String link)
	{
		this.link = link;
	}

	/**
	 * Return's the comment's author login.
	 *
	 * @return The login;
	 */
	public final synchronized String getLogin()
	{
		return login;
	}

	/**
	 * Set the comment's author login.
	 *
	 * @param login The new login.
	 * @noinspection UnusedDeclaration
	 */
	public final synchronized void setLogin(String login)
	{
		this.login = login;
	}

	/**
	 * Returns the comment's author nickname.
	 *
	 * @return The nickname.
	 */
	public final synchronized String getNick()
	{
		return nick;
	}

	/**
	 * Sets the comment's author nickname.
	 *
	 * @param nick The new nickname.
	 */
	public final synchronized void setNick(String nick)
	{
		this.nick = nick;
	}

	/**
	 * Returns the tags.
	 *
	 * @return The tags.
	 */
	public final synchronized List getTags()
	{
		return tags;
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

			for (final String rawPart : parts)
			{
				part = rawPart.trim();

				if (part.length() >= 2)
				{
					tag = new SyndCategoryImpl();
					tag.setName(part.substring(1).toLowerCase());

					mod = part.charAt(0);

					if (mod == '-')
					{
						// Don't remove the channel tag, if any.
						if (!tag.getName().equals(channel.substring(1)))
						{
							this.tags.remove(tag);
						}
					}
					else if (mod == '+')
					{
						if (!this.tags.contains(tag))
						{
							this.tags.add(tag);
						}
					}
					else
					{
						tag.setName(part.trim().toLowerCase());

						if (!this.tags.contains(tag))
						{
							this.tags.add(tag);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the comment's title.
	 *
	 * @return The title.
	 */
	public final synchronized String getTitle()
	{
		return title;
	}

	/**
	 * Sets the comment's title.
	 *
	 * @param title The new title.
	 */
	public final synchronized void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Returns true if the entry has comments.
	 *
	 * @return true if there are comments, false otherwise.
	 */
	public final synchronized boolean hasComments()
	{
		return (!comments.isEmpty());
	}

	/**
	 * Returns true if the entry has tags.
	 *
	 * @return true if there are tags, false otherwise.
	 */
	public final synchronized boolean hasTags()
	{
		return (!tags.isEmpty());
	}

	/**
	 * /** Sets a comment.
	 *
	 * @param index The comment's index.
	 * @param comment The actual comment.
	 * @param nick The nickname of the author of the comment.
	 */
	public final synchronized void setComment(int index, String comment, String nick)
	{
		if (index < comments.size())
		{
			comments.set(index, new EntryComment(comment, nick));
		}
	}

	/**
	 * Sets the tags.
	 *
	 * @param tags The tags.
	 */
	public final synchronized void setTags(List<SyndCategoryImpl> tags)
	{
		this.tags.addAll(tags);
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return A string representation of the object.
	 */
	public final String toString()
	{

		return super.toString() + "[ channel -> '" + channel + '\'' + ", comments -> " + comments + ", date -> "
		       + date + ", link -> '" + link + '\'' + ", login -> '" + login + '\'' + ", nick -> '" + nick + '\''
		       + ", tags -> " + tags + ", title -> '" + title + '\'' + " ]";
	}
}
