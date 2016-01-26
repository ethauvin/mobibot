/*
 * Lookup.java
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

import org.apache.commons.net.WhoisClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Processes the {@link Commands#LOOKUP_CMD} command.
 *
 * @author <a href="mailto:erik@thauvin.net">Erik C. Thauvin</a>
 * @created 2014-04-26
 * @since 1.0
 */
final class Lookup
{

	/**
	 * The whois host.
	 */
	private static final String WHOIS_HOST = "whois.arin.net";

	/**
	 * Disables the default constructor.
	 *
	 * @throws UnsupportedOperationException If the constructor is called.
	 */
	private Lookup()
			throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Illegal constructor call.");
	}

	/**
	 * Performs a DNS lookup on the specified query.
	 *
	 * @param query The IP address or hostname.
	 *
	 * @return The lookup query result string.
	 *
	 * @throws java.net.UnknownHostException If the host is unknown.
	 */
	public static String lookup(final String query)
			throws UnknownHostException
	{
		final StringBuilder buffer = new StringBuilder("");

		final InetAddress[] results = InetAddress.getAllByName(query);
		String hostInfo;

		for (final InetAddress result : results)
		{
			if (result.getHostAddress().equals(query))
			{
				hostInfo = result.getHostName();

				if (hostInfo.equals(query))
				{
					throw new UnknownHostException();
				}
			}
			else
			{
				hostInfo = result.getHostAddress();
			}

			if (buffer.length() > 0)
			{
				buffer.append(", ");
			}

			buffer.append(hostInfo);
		}

		return buffer.toString();
	}

	/**
	 * Performs a whois IP query.
	 *
	 * @param query The IP address.
	 *
	 * @return The IP whois data, if any.
	 *
	 * @throws java.io.IOException If a connection error occurs.
	 */
	public static String[] whois(final String query)
			throws IOException
	{
		return whois(query, WHOIS_HOST);
	}

	/**
	 * Performs a whois IP query.
	 *
	 * @param query The IP address.
	 * @param host The whois host.
	 *
	 * @return The IP whois data, if any.
	 *
	 * @throws java.io.IOException If a connection error occurs.
	 */
	@SuppressWarnings("WeakerAccess, SameParameterValue")
	public static String[] whois(final String query, final String host)
			throws IOException
	{
		final WhoisClient whois = new WhoisClient();
		String[] lines;

		try
		{
			whois.setDefaultTimeout(Mobibot.CONNECT_TIMEOUT);
			whois.connect(host);
			whois.setSoTimeout(Mobibot.CONNECT_TIMEOUT);
			whois.setSoLinger(false, 0);

			lines = whois.query('-' + query).split("\n");
		}
		finally
		{
			whois.disconnect();
		}

		return lines;
	}
}