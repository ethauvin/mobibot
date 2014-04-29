/*
 * @(#)Utils.java
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

import org.jibble.pircbot.Colors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Miscellaneous utilities class.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created 2014-04-26
 * @since 1.0
 */
public class Utils
{
	/**
	 * The timestamp simple date format.
	 */
	public static final SimpleDateFormat TIMESTAMP_SDF = new SimpleDateFormat("yyyyMMddHHmmss");

	/**
	 * The UTC (yyyy-MM-dd HH:mm) simple date format.
	 */
	static final SimpleDateFormat UTC_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	/**
	 * The ISO (YYYY-MM-DD) simple date format.
	 */
	static final SimpleDateFormat ISO_SDF = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Disables the default constructor.
	 *
	 * @throws UnsupportedOperationException if an error occurred. if the constructor is called.
	 */
	private Utils()
			throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Illegal constructor call.");
	}

	/**
	 * Converts XML/XHTML entities to plain text.
	 *
	 * @param str The string to unescape.
	 *
	 * @return The unescaped string.
	 */
	public static String unescapeXml(String str)
	{
		String s = str.replaceAll("&amp;", "&");
		s = s.replaceAll("&lt;", "<");
		s = s.replaceAll("&gt;", ">");
		s = s.replaceAll("&quot;", "\"");
		s = s.replaceAll("&apos;", "'");
		s = s.replaceAll("&#39;", "'");

		return s;
	}

	/**
	 * Copies a file.
	 *
	 * @param in The source file.
	 * @param out The destination file.
	 *
	 * @throws java.io.IOException If the file could not be copied.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public static void copyFile(File in, File out)
			throws IOException
	{
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		FileInputStream input = null;
		FileOutputStream output = null;

		try
		{
			input = new FileInputStream(in);
			output = new FileOutputStream(out);

			inChannel = input.getChannel();
			outChannel = output.getChannel();

			inChannel.transferTo(0L, inChannel.size(), outChannel);
		}
		finally
		{
			try
			{
				if (inChannel != null)
				{
					inChannel.close();
				}

				if (input != null)
				{
					input.close();
				}
			}
			catch (Exception ignore)
			{
				; // Do nothing
			}

			try
			{
				if (outChannel != null)
				{
					outChannel.close();
				}

				if (output != null)
				{
					output.close();
				}
			}
			catch (Exception ignore)
			{
				; // Do nothing
			}
		}
	}

	/**
	 * Returns the current Internet (beat) Time.
	 *
	 * @return The Internet Time string.
	 */
	public static String internetTime()
	{
		final Calendar gc = Calendar.getInstance();

		final int offset = (gc.get(Calendar.ZONE_OFFSET) / (60 * 60 * 1000));
		int hh = gc.get(Calendar.HOUR_OF_DAY);
		final int mm = gc.get(Calendar.MINUTE);
		final int ss = gc.get(Calendar.SECOND);

		hh -= offset; // GMT
		hh += 1; // BMT

		long beats = Math.round(Math.floor((double) ((((hh * 3600) + (mm * 60) + ss) * 1000) / 86400)));

		if (beats >= 1000)
		{
			beats -= (long) 1000;
		}
		else if (beats < 0)
		{
			beats += (long) 1000;
		}

		if (beats < 10)
		{
			return ("@00" + String.valueOf(beats));
		}
		else if (beats < 100)
		{
			return ("@0" + String.valueOf(beats));
		}

		return ('@' + String.valueOf(beats));
	}

	/**
	 * Returns a property as an int.
	 *
	 * @param property The port property value.
	 * @param def The default property value.
	 *
	 * @return The port or default value if invalid.
	 */
	public static int getIntProperty(String property, int def)
	{
		int prop;

		try
		{
			prop = Integer.parseInt(property);
		}
		catch (NumberFormatException ignore)
		{
			prop = def;
		}

		return prop;
	}

	/**
	 * Ensures that the given location (File/URL) has a trailing slash (<code>/</code>) to indicate a directory.
	 *
	 * @param location The File or URL location.
	 * @param isUrl Set to true if the location is a URL
	 *
	 * @return The location ending with a slash.
	 */
	public static String ensureDir(String location, boolean isUrl)
	{
		if (isUrl)
		{
			if (location.charAt(location.length() - 1) == '/')
			{
				return location;
			}
			else
			{
				return location + '/';
			}
		}
		else
		{
			if (location.charAt(location.length() - 1) == File.separatorChar)
			{
				return location;
			}
			else
			{
				return location + File.separatorChar;
			}
		}
	}

	/**
	 * Returns true if the given string is valid.
	 *
	 * @param s The string to validate.
	 *
	 * @return true if the string is non-empty and not null, false otherwise.
	 */
	public static boolean isValidString(String s)
	{
		return (s != null) && (s.trim().length() > 0);
	}

	/**
	 * Makes the given int bold.
	 *
	 * @param i The int.
	 *
	 * @return The bold string.
	 */
	public static String bold(int i)
	{
		return Colors.BOLD + i + Colors.BOLD;
	}

	/**
	 * Returns today's date.
	 *
	 * @return Today's date in {@link #ISO_SDF ISO} format.
	 */
	public static String today()
	{
		return ISO_SDF.format(Calendar.getInstance().getTime());
	}

	/**
	 * Builds an entry's comment for display on the channel.
	 *
	 * @param entryIndex The entry's index.
	 * @param commentIndex The comment's index.
	 * @param comment The {@link net.thauvin.erik.mobibot.EntryComment comment} object.
	 *
	 * @return The entry's comment.
	 */
	static String buildComment(int entryIndex, int commentIndex, EntryComment comment)
	{
		return (Commands.LINK_CMD + (entryIndex + 1) + '.' + (commentIndex + 1) + ": [" + comment.getNick() + "] "
		        + comment.getComment());
	}

	/**
	 * Builds an entry's link for display on the channel.
	 *
	 * @param index The entry's index.
	 * @param entry The {@link net.thauvin.erik.mobibot.EntryLink entry} object.
	 *
	 * @return The entry's link.
	 *
	 * @see #buildLink(int, net.thauvin.erik.mobibot.EntryLink, boolean)
	 */
	static String buildLink(int index, EntryLink entry)
	{
		return buildLink(index, entry, false);
	}

	/**
	 * Builds an entry's link for display on the channel.
	 *
	 * @param index The entry's index.
	 * @param entry The {@link net.thauvin.erik.mobibot.EntryLink entry} object.
	 * @param isView Set to true to display the number of comments.
	 *
	 * @return The entry's link.
	 */
	static String buildLink(int index, EntryLink entry, boolean isView)
	{
		final StringBuilder buff = new StringBuilder(Commands.LINK_CMD + (index + 1) + ": ");

		buff.append('[').append(entry.getNick()).append(']');

		if (isView && entry.hasComments())
		{
			buff.append("[+").append(entry.getCommentsCount()).append(']');
		}

		buff.append(' ');

		if (Mobibot.NO_TITLE.equals(entry.getTitle()))
		{
			buff.append(bold(entry.getTitle()));
		}
		else
		{
			buff.append(entry.getTitle());
		}

		buff.append(" ( ").append(entry.getLink()).append(" )");

		return buff.toString();
	}

	/**
	 * Makes the given string bold.
	 *
	 * @param s The string.
	 *
	 * @return The bold string.
	 */
	public static String bold(String s)
	{
		return Colors.BOLD + s + Colors.BOLD;
	}

	/**
	 * Build an entry's tags/categories for display on the channel.
	 *
	 * @param entryIndex The entry's index.
	 * @param entry The {@link net.thauvin.erik.mobibot.EntryLink entry} object.
	 *
	 * @return The entry's tags.
	 */
	static String buildTags(int entryIndex, EntryLink entry)
	{
		return (Commands.LINK_CMD + (entryIndex + 1) + "T: " + entry.getDeliciousTags().replaceAll(",", ", "));
	}
}