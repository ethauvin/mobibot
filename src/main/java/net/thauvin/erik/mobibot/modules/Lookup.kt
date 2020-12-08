/*
 * Lookup.kt
 *
 * Copyright (c) 2004-2020, Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot.modules

import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import org.apache.commons.net.whois.WhoisClient
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * The Lookup module.
 */
class Lookup(bot: Mobibot) : AbstractModule(bot) {
    override fun commandResponse(
        sender: String,
        cmd: String,
        args: String,
        isPrivate: Boolean
    ) {
        if (args.matches("(\\S.)+(\\S)+".toRegex())) {
            with(bot) {
                try {
                    lookup(args).split(',').forEach {
                        send(it.trim())
                    }
                } catch (ignore: UnknownHostException) {
                    if (args.matches(
                            ("(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)").toRegex()
                        )
                    ) {
                        try {
                            val lines = whois(args)
                            if (lines.isNotEmpty()) {
                                var line: String
                                for (rawLine in lines) {
                                    line = rawLine.trim()
                                    if (line.isNotEmpty() && line[0] != '#') {
                                        send(line)
                                    }
                                }
                            } else {
                                send("Unknown host.")
                            }
                        } catch (ioe: IOException) {
                            if (logger.isDebugEnabled) {
                                logger.debug("Unable to perform whois IP lookup: $args", ioe)
                            }
                            send("Unable to perform whois IP lookup: ${ioe.message}")
                        }
                    } else {
                        send("Unknown host.")
                    }
                }
            }
        } else {
            helpResponse(sender, true)
        }
    }

    companion object {
        /**
         * The whois default host.
         */
        const val WHOIS_HOST = "whois.arin.net"

        // Lookup command
        private const val LOOKUP_CMD = "lookup"

        /**
         * Performs a DNS lookup on the specified query.
         */
        @JvmStatic
        @Throws(UnknownHostException::class)
        fun lookup(query: String): String {
            val buffer = StringBuilder()
            val results = InetAddress.getAllByName(query)
            var hostInfo: String
            for (result in results) {
                if (result.hostAddress == query) {
                    hostInfo = result.hostName
                    if (hostInfo == query) {
                        throw UnknownHostException()
                    }
                } else {
                    hostInfo = result.hostAddress
                }
                if (buffer.isNotEmpty()) {
                    buffer.append(", ")
                }
                buffer.append(hostInfo)
            }
            return buffer.toString()
        }

        /**
         * Performs a whois IP query.
         */
        @Throws(IOException::class)
        private fun whois(query: String): List<String> {
            return whois(query, WHOIS_HOST)
        }

        /**
         * Performs a whois IP query.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun whois(query: String, host: String): List<String> {
            val whoisClient = WhoisClient()
            val lines: List<String>
            with(whoisClient) {
                try {
                    defaultTimeout = Constants.CONNECT_TIMEOUT
                    connect(host)
                    soTimeout = Constants.CONNECT_TIMEOUT
                    setSoLinger(false, 0)
                    lines = if (WHOIS_HOST == host) {
                        query("n - $query").split("\n")
                    } else {
                        query(query).split("\n")
                    }
                } finally {
                    disconnect()
                }
            }
            return lines
        }
    }

    init {
        commands.add(LOOKUP_CMD)
        help.add("To perform a DNS lookup query:")
        help.add(Utils.helpFormat("%c $LOOKUP_CMD <ip address or hostname>"))
    }
}
