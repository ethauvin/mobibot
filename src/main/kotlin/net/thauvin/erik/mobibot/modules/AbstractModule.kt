/*
 * AbstractModule.kt
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
package net.thauvin.erik.mobibot.modules

import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpCmdSyntax
import net.thauvin.erik.mobibot.Utils.sendMessage
import org.pircbotx.hooks.events.PrivateMessageEvent
import org.pircbotx.hooks.types.GenericMessageEvent

/**
 * The `Module` abstract class.
 */
abstract class AbstractModule {
    /**
     * The module name.
     */
    abstract val name: String

    /**
     * The module's commands, if any.
     */
    @JvmField
    val commands: MutableList<String> = mutableListOf()

    @JvmField
    val help: MutableList<String> = mutableListOf()
    val properties: MutableMap<String, String> = mutableMapOf()

    /**
     * Responds to a command.
     */
    abstract fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent)

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
    open fun helpResponse(event: GenericMessageEvent): Boolean {
        for (h in help) {
            event.sendMessage(helpCmdSyntax(h, event.bot().nick, isPrivateMsgEnabled && event is PrivateMessageEvent))
        }
        return true
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
            for (s in properties.keys) {
                if (properties[s].isNullOrBlank()) {
                    return false
                }
            }
            return true
        }

    /**
     * Sets a property key and value.
     */
    fun setProperty(key: String, value: String) {
        if (key.isNotBlank()) {
            properties[key] = value
        }
    }
}
