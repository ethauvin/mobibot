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
package net.thauvin.erik.mobibot.modules;

import net.thauvin.erik.mobibot.Mobibot;
import org.apache.commons.net.whois.WhoisClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The Lookup module.
 *
 * @author <a href="mailto:erik@thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2014-04-26
 * @since 1.0
 */
final public class Lookup extends AbstractModule {
    /**
     * The lookup command.
     */
    public static final String LOOKUP_CMD = "lookup";

    // The whois host.
    private static final String WHOIS_HOST = "whois.arin.net";

    /**
     * The default constructor
     */
    public Lookup() {
        commands.add(LOOKUP_CMD);
    }

    /**
     * Performs a DNS lookup on the specified query.
     *
     * @param query The IP address or hostname.
     * @return The lookup query result string.
     * @throws java.net.UnknownHostException If the host is unknown.
     */
    private static String lookup(final String query)
            throws UnknownHostException {
        final StringBuilder buffer = new StringBuilder("");

        final InetAddress[] results = InetAddress.getAllByName(query);
        String hostInfo;

        for (final InetAddress result : results) {
            if (result.getHostAddress().equals(query)) {
                hostInfo = result.getHostName();

                if (hostInfo.equals(query)) {
                    throw new UnknownHostException();
                }
            } else {
                hostInfo = result.getHostAddress();
            }

            if (buffer.length() > 0) {
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
     * @return The IP whois data, if any.
     * @throws java.io.IOException If a connection error occurs.
     */
    private static String[] whois(final String query)
            throws IOException {
        return whois(query, WHOIS_HOST);
    }

    /**
     * Performs a whois IP query.
     *
     * @param query The IP address.
     * @param host  The whois host.
     * @return The IP whois data, if any.
     * @throws java.io.IOException If a connection error occurs.
     */
    @SuppressWarnings("WeakerAccess, SameParameterValue")
    public static String[] whois(final String query, final String host)
            throws IOException {
        final WhoisClient whois = new WhoisClient();
        String[] lines;

        try {
            whois.setDefaultTimeout(Mobibot.CONNECT_TIMEOUT);
            whois.connect(host);
            whois.setSoTimeout(Mobibot.CONNECT_TIMEOUT);
            whois.setSoLinger(false, 0);

            lines = whois.query('-' + query).split("\n");
        } finally {
            whois.disconnect();
        }

        return lines;
    }

    /**
     * Process a command.
     *
     * @param bot       The bot's instance.
     * @param sender    The sender.
     * @param args      The command arguments.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    @Override
    public void commandResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        if (args.matches("(\\S.)+(\\S)+")) {
            try {
                bot.send(bot.getChannel(), Lookup.lookup(args));
            } catch (UnknownHostException ignore) {
                if (args.matches(
                        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)")) {
                    try {
                        final String[] lines = Lookup.whois(args);

                        if ((lines != null) && (lines.length > 0)) {
                            String line;

                            for (final String rawLine : lines) {
                                line = rawLine.trim();

                                if ((line.length() > 0) && (line.charAt(0) != '#')) {
                                    bot.send(bot.getChannel(), line);
                                }
                            }
                        } else {
                            bot.send(bot.getChannel(), "Unknown host.");
                        }
                    } catch (IOException ioe) {
                        if (bot.getLogger().isDebugEnabled()) {
                            bot.getLogger().debug("Unable to perform whois IP lookup: " + args, ioe);
                        }

                        bot.send(bot.getChannel(), "Unable to perform whois IP lookup: " + ioe.getMessage());
                    }
                } else {
                    bot.send(bot.getChannel(), "Unknown host.");
                }
            }
        } else {
            helpResponse(bot, sender, args, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helpResponse(final Mobibot bot, final String sender, final String args, final boolean isPrivate) {
        bot.send(sender, "To perform a DNS lookup query:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + LOOKUP_CMD + " <ip address or hostname>"));
    }
}