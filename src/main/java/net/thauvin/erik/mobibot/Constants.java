/*
 * Constants.java
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

package net.thauvin.erik.mobibot;

import java.util.Locale;

/**
 * The <code>Constants</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-04-19
 * @since 1.0
 */
public final class Constants {
    /**
     * The connect/read timeout in ms.
     */
    public static final int CONNECT_TIMEOUT = 5000;
    /**
     * Debug command line argument.
     */
    public static final String DEBUG_ARG = "debug";
    /**
     * The debug command.
     */
    public static final String DEBUG_CMD = "debug";
    /**
     * The die command.
     */
    public static final String DIE_CMD = "die";
    /**
     * Help command line argument.
     */
    public static final String HELP_ARG = "help";
    /**
     * The help command.
     */
    public static final String HELP_CMD = "help";
    /**
     * The link command.
     */
    public static final String LINK_CMD = "L";
    /**
     * Default locale.
     */
    public static final Locale LOCALE = Locale.getDefault();
    /**
     * The empty title string.
     */
    public static final String NO_TITLE = "No Title";
    /**
     * Properties command line argument.
     */
    public static final String PROPS_ARG = "properties";
    /**
     * The timer delay in minutes.
     */
    public static final long TIMER_DELAY = 10L;
    /**
     * The twitter post flag property key.
     */
    public static final String TWITTER_AUTOPOST_PROP = "twitter-auto-post";
    /**
     * The Twitter handle property key.
     */
    public static final String TWITTER_HANDLE_PROP = "twitter-handle";
    /**
     * Properties version line argument.
     */
    public static final String VERSION_ARG = "version";

    /**
     * Disables the default constructor.
     */
    private Constants() {
        throw new UnsupportedOperationException("Illegal constructor call.");
    }
}
