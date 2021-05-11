/*
 * ModuleException.kit
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
package net.thauvin.erik.mobibot.modules

import net.thauvin.erik.mobibot.Utils.obfuscate
import org.apache.commons.lang3.StringUtils

/**
 * The `ModuleException` class.
 */
class ModuleException : Exception {
    /**
     * Returns the debug message.
     */
    val debugMessage: String?

    /**
     * Creates a new exception.
     */
    constructor(message: String?) : super(message) {
        debugMessage = message
    }

    /**
     * Creates a new exception.
     */
    constructor(debugMessage: String?, message: String?, cause: Throwable?) : super(message, cause) {
        this.debugMessage = debugMessage
    }

    /**
     * Creates a new exception.
     */
    constructor(debugMessage: String?, message: String?) : super(message) {
        this.debugMessage = debugMessage
    }

    /**
     * Return the sanitized message (e.g. remove API keys, etc.)
     */
    fun getSanitizedMessage(vararg sanitize: String): String {
        val obfuscate = sanitize.map { obfuscate(it) }.toTypedArray()
        return when {
            cause != null -> {
                cause.javaClass.name + ": " + StringUtils.replaceEach(cause.message, sanitize, obfuscate)
            }
            message != null -> {
                message.javaClass.name + ": " + StringUtils.replaceEach(message, sanitize, obfuscate)
            }
            else -> ""
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
