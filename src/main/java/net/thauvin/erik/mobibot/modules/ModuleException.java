/*
 * ModuleException.java
 *
 * Copyright (c) 2004-2019, Erik C. Thauvin (erik@thauvin.net)
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

import okhttp3.HttpUrl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The <code>ModuleException</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-04-07
 * @since 1.0
 */
public class ModuleException extends Exception {
    private static final long serialVersionUID = -3036774290621088107L;

    private final String debugMessage;
    private final Pattern urlPattern = Pattern.compile("(https?://\\S+)(\\?\\S+)");

    /**
     * Creates a new exception.
     *
     * @param message The exception message.
     */
    ModuleException(final String message) {
        super(message);
        this.debugMessage = message;
    }

    /**
     * Creates a new exception.
     *
     * @param debugMessage The debug message.
     * @param message      The exception message.
     * @param cause        The cause.
     */
    ModuleException(final String debugMessage, final String message, final Throwable cause) {
        super(message, cause);
        this.debugMessage = debugMessage;
    }

    /**
     * Creates a new exception.
     *
     * @param debugMessage The debug message.
     * @param message      The exception message.
     */
    ModuleException(final String debugMessage, final String message) {
        super(message);
        this.debugMessage = debugMessage;
    }

    /**
     * Returns the debug message.
     *
     * @return The debug message.
     */
    String getDebugMessage() {
        return debugMessage;
    }

    /**
     * Return the sanitized (URL query parameters are replaced by char count) message.
     *
     * @return The sanitized message.
     */
    String getSanitizedMessage() {
        if (hasCause()) {
            final String causeMessage = getCause().getMessage();
            final Matcher matcher = urlPattern.matcher(causeMessage);
            if (matcher.find()) {
                final HttpUrl url = HttpUrl.parse(matcher.group(1) + matcher.group(2));
                if (url != null) {
                    final StringBuilder query = new StringBuilder("?");
                    final int size = url.querySize();
                    for (int i = 0; i < size; i++) {
                        if (i > 0) {
                            query.append('&');
                        }
                        query.append(url.queryParameterName(i)).append("=[")
                            .append(url.queryParameterValue(i).length()).append(']');
                    }
                    return getDebugMessage() + "\nCaused by: " + getCause().getClass().getName() + ": "
                        + causeMessage.replace(matcher.group(2), query);
                }
            }
        }
        return getDebugMessage() + "\nCaused by: " + getCause().getClass().getName() + ": " + getCause().getMessage();
    }

    /**
     * Return <code>true</code> if the exception has a cause.
     *
     * @return <code>true</code> or <code>false</code>
     */
    boolean hasCause() {
        return getCause() != null;
    }
}
