/*
 * AbstractModule.kt
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

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * The `Module` abstract class.
 */
abstract class AbstractModule(val bot: Mobibot) {
    /**
     * The module's commands, if any.
     */
    @JvmField
    val commands: MutableList<String> = ArrayList()

    @JvmField
    val help: MutableList<String> = ArrayList()
    val properties: MutableMap<String, String> = ConcurrentHashMap()

    /**
     * Responds to a command.
     */
    abstract fun commandResponse(
        sender: String,
        cmd: String,
        args: String,
        isPrivate: Boolean
    )

    /**
     * Returns the module's property keys.
     */
    val propertyKeys: Set<String>
        get() = properties.keys

    /**
     * Returns `true` if the module has properties.
     */
    fun hasProperties(): Boolean {
        return properties.isNotEmpty()
    }

    /**
     * Responds with the module's help.
     */
    open fun helpResponse(sender: String, isPrivate: Boolean) {
        for (h in help) {
            bot.send(sender, Utils.helpFormat(h, bot.nick, isPrivateMsgEnabled && isPrivate), isPrivate)
        }
    }

    /**
     * Initializes the properties.
     */
    fun initProperties(vararg keys: String) {
        for (key in keys) {
            properties[key] = ""
        }
    }

    /**
     * Returns `true` if the module is enabled.
     */
    val isEnabled: Boolean
        get() = if (hasProperties()) {
            isValidProperties
        } else {
            true
        }

    /**
     * Returns `true` if the module responds to private messages.
     */
    open val isPrivateMsgEnabled: Boolean = false

    /**
     * Ensures that all properties have values.
     */
    open val isValidProperties: Boolean
        get() {
            for (s in propertyKeys) {
                if (StringUtils.isBlank(properties[s])) {
                    return false
                }
            }
            return true
        }

    /**
     * Sets a property key and value.
     */
    fun setProperty(key: String, value: String) {
        if (StringUtils.isNotBlank(key)) {
            properties[key] = value
        }
    }
}
