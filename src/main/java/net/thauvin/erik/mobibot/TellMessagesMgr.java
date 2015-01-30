/*
 * TellMessagesMgr.java
 *
 * Copyright (c) 2004-2015, Erik C. Thauvin (erik@thauvin.net)
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

import org.apache.commons.logging.impl.Log4JLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Managers the {@link Commands#TELL_CMD} messages.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created 2014-04-26
 * @since 1.0
 */
public class TellMessagesMgr
{
	/**
	 * Disables the default constructor.
	 *
	 * @throws UnsupportedOperationException If the constructor is called.
	 */
	private TellMessagesMgr()
			throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Illegal constructor call.");
	}

	/**
	 * Cleans the messages queue
	 *
	 * @param tellMessages The messages list.
	 * @param tellMaxDays The maximum number of days to keep messages for.
	 *
	 * @return <code>True</code> if the queue was cleaned.
	 */
	public static boolean cleanTellMessages(List<TellMessage> tellMessages, int tellMaxDays)
	{
		final Calendar maxDate = Calendar.getInstance();
		final Date today = new Date();
		boolean cleaned = false;

		for (final TellMessage message : tellMessages)
		{
			maxDate.setTime(message.getQueued());
			maxDate.add(Calendar.DATE, tellMaxDays);

			if (maxDate.getTime().before(today))
			{
				tellMessages.remove(message);
				cleaned = true;
			}
		}

		return cleaned;
	}

	/**
	 * Loads the messages.
	 *
	 * @param file The serialized objects file.
	 * @param logger The logger.
	 *
	 * @return The {@link net.thauvin.erik.mobibot.TellMessage} array.
	 */
	@SuppressWarnings("unchecked")
	public static List<TellMessage> load(String file, Log4JLogger logger)
	{
		try
		{
			final ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

			try
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Loading the messages.");
				}

				return ((List<TellMessage>) input.readObject());
			}
			finally
			{
				input.close();
			}
		}
		catch (FileNotFoundException ignore)
		{
			; // Do nothing.
		}
		catch (IOException e)
		{
			logger.error("An IO error occurred loading the messages queue.", e);
		}
		catch (Exception e)
		{
			logger.getLogger().error("An error occurred loading the messages queue.", e);
		}

		return (List<TellMessage>) new ArrayList();
	}

	/**
	 * Saves the messages.
	 *
	 * @param file The serialized objects file.
	 * @param messages The {@link net.thauvin.erik.mobibot.TellMessage} array.
	 * @param logger The logger.
	 */
	public static void save(String file, List<TellMessage> messages, Log4JLogger logger)
	{
		try
		{
			final ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

			try
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Saving the messages.");
				}

				output.writeObject(messages);
			}
			finally
			{
				output.close();
			}
		}
		catch (IOException e)
		{
			logger.error("Unable to save messages queue.", e);
		}
	}
}