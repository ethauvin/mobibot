/*
 * Constants.kt
 *
 * Copyright 2004-2023 Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot

/**
 * The `Constants`.
 */
object Constants {
    /**
     * The connect/read timeout in ms.
     */
    const val CONNECT_TIMEOUT = 5000

    /**
     * Debug command line argument.
     */
    const val DEBUG_ARG = "debug"

    /**
     * Default IRC Port.
     */
    const val DEFAULT_PORT = 6667

    /**
     * Default IRC Server.
     */
    const val DEFAULT_SERVER = "irc.libera.chat"

    /**
     * CLI command for usage.
     */
    const val CLI_CMD = "java -jar ${ReleaseInfo.PROJECT}.jar"

    /**
     * User-Agent
     */
    const val USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36"

    /**
     * The help command.
     */
    const val HELP_CMD = "help"

    /**
     * The link command.
     */
    const val LINK_CMD = "L"

    /**
     * The empty title string.
     */
    const val NO_TITLE = "No Title"

    /**
     * Properties command line argument.
     */
    const val PROPS_ARG = "properties"

    /**
     * The tag command
     */
    const val TAG_CMD = "T"

    /**
     * The timer delay in minutes.
     */
    const val TIMER_DELAY = 10L

    /**
     * Properties version line argument.
     */
    const val VERSION_ARG = "version"
}
