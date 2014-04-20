/*
 * @(#)DeliciousPoster.java
 *
 * Copyright (c) 2005, Erik C. Thauvin (erik@thauvin.net)
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

import del.icio.us.Delicious;

/**
 * The class to handle posts to del.icio.us.
 *
 * @author Erik C. Thauvin
 * @version $Revision$, $Date$
 * @created Mar 5, 2005
 * @noinspection UnnecessaryBoxing
 * @since 1.0
 */
public class DeliciousPoster
{
	private final Delicious _delicious;

	private final String _ircServer;

	/**
	 * Creates a new DeliciousPoster instance.
	 *
	 * @param username The del.icio.us username.
	 * @param password The del.icio.us password.
	 * @param ircServer The IRC server.
	 */
	public DeliciousPoster(String username, String password, String ircServer)
	{
		_delicious = new Delicious(username, password);
		_ircServer = ircServer;
	}

	/**
	 * Adds a post to del.icio.us.
	 *
	 * @param entry The entry to add.
	 */
	public final void addPost(final EntryLink entry)
	{
		final SwingWorker worker = new SwingWorker()
		{
			public Object construct()
			{
				return Boolean.valueOf(_delicious.addPost(entry.getLink(),
				                                          entry.getTitle(),
				                                          postedBy(entry),
				                                          entry.getDeliciousTags(),
				                                          entry.getDate()));
			}
		};

		worker.start();
	}

	/**
	 * Deletes a post to del.icio.us.
	 *
	 * @param entry The entry to delete.
	 */
	public final void deletePost(EntryLink entry)
	{
		final String link = entry.getLink();

		final SwingWorker worker = new SwingWorker()
		{
			public Object construct()
			{
				return Boolean.valueOf(_delicious.deletePost(link));
			}
		};

		worker.start();
	}

	/**
	 * Updates a post to del.icio.us.
	 *
	 * @param oldUrl The old post URL.
	 * @param entry The entry to add.
	 */
	public final void updatePost(final String oldUrl, final EntryLink entry)
	{
		final SwingWorker worker = new SwingWorker()
		{
			public Object construct()
			{
				if (!oldUrl.equals(entry.getLink()))
				{
					_delicious.deletePost(oldUrl);

					return Boolean.valueOf(_delicious.addPost(entry.getLink(),
					                                          entry.getTitle(),
					                                          postedBy(entry),
					                                          entry.getDeliciousTags(),
					                                          entry.getDate()));
				}
				else
				{
					return Boolean.valueOf(_delicious.addPost(entry.getLink(),
					                                          entry.getTitle(),
					                                          postedBy(entry),
					                                          entry.getDeliciousTags(),
					                                          entry.getDate(),
					                                          true,
					                                          true));
				}
			}
		};

		worker.start();
	}

	/**
	 * Returns he del.icio.us extended attribution line.
	 *
	 * @param entry The entry.
	 *
	 * @return The extended attribution line.
	 */
	private String postedBy(EntryLink entry)
	{
		return "Posted by " + entry.getNick() + " on " + entry.getChannel() + " (" + _ircServer + ')';
	}
}
