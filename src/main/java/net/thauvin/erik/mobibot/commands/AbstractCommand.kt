/*
 * AbstractCommand.kt
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

package net.thauvin.erik.mobibot.commands

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractCommand(val bot: Mobibot) {
    abstract val name: String
    abstract val help: List<String>
    abstract val isOp: Boolean
    abstract val isPublic: Boolean
    abstract val isVisible: Boolean

    private val properties: MutableMap<String, String> = ConcurrentHashMap()

    abstract fun commandResponse(
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    )

    open fun helpResponse(command: String, sender: String, isOp: Boolean, isPrivate: Boolean): Boolean {
        if (!this.isOp || this.isOp == isOp) {
            for (h in help) {
                bot.send(sender, Utils.helpFormat(h, bot.nick, isPrivate), isPrivate)
            }
            return true
        }
        return false
    }

    open fun getProperty(key: String) : String? {
        return properties[key]
    }
    open fun getPropertyKeys(): Set<String> {
        return properties.keys
    }

    open fun hasProperties(): Boolean {
        return properties.isNotEmpty()
    }

    open fun initProperties(vararg keys: String) {
        keys.forEach {
            properties[it] = ""
        }
    }

    open fun isEnabled(): Boolean {
        return true
    }

    open fun matches(message: String): Boolean {
        return false
    }

    open fun setProperty(key: String, value: String) {
        properties[key] = value
    }
}
