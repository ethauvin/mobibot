/*
 * AbstractModule.kt
 *
 * Copyright (c) 2004-2026 Erik C. Thauvin (erik@thauvin.net)
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
 * Represents an abstract module, which can be extended to implement specific functionality.
 *
 * This class provides a foundation for creating modules with configurable properties, commands, and help features.
 */
abstract class AbstractModule {
    /**
     * The module name.
     */
    abstract val name: String

    /**
     * Initializes the module.
     *
     * Subclasses must implement this to set up commands, help, and required properties.
     */
    abstract fun initialize()

    /**
     * The module's commands, if any.
     */
    private val _commands: MutableList<String> = mutableListOf()
    val commands: List<String>
        get() = _commands.toList()

    private val _help: MutableList<String> = mutableListOf()
    val help: List<String>
        get() = _help.toList()

    private val _properties: MutableMap<String, String> = mutableMapOf()

    /**
     * The module's properties (immutable).
     */
    val properties: Map<String, String>
        get() = _properties.toMap()

    /**
     * Responds to a command.
     */
    abstract fun commandResponse(channel: String, cmd: String, args: String, event: GenericMessageEvent)

    /**
     * Returns the module's property keys.
     */
    val propertyKeys: Set<String> by lazy { _properties.keys.toSet() }

    /**
     * Retrieves the value associated with the specified key from the properties.
     */
    fun getProperty(key: String): String? {
        return _properties[key]
    }

    /**
     * Retrieves the value associated with the specified key from the properties.
     * If the key does not exist, the default value is returned.
     */
    fun getPropertyOrDefault(key: String, defaultValue: String): String {
        return _properties.getOrDefault(key, defaultValue)
    }

    /**
     * Returns `true` if the module has properties.
     */
    fun hasProperties(): Boolean {
        return _properties.isNotEmpty()
    }

    /**
     * Adds a command to the module.
     */
    protected fun addCommand(vararg command: String) {
        _commands.addAll(command)
    }

    /**
     * Adds a help entry to the module.
     */
    protected fun addHelp(vararg help: String) {
        _help.addAll(help)
    }

    /**
     * Responds with the module's help.
     */
    open fun helpResponse(event: GenericMessageEvent): Boolean {
        if (_help.isEmpty()) return false
        for (h in _help) {
            event.sendMessage(
                helpCmdSyntax(
                    h, event.bot().nick, isPrivateMsgEnabled && event is PrivateMessageEvent
                )
            )
        }
        return true
    }

    /**
     * Initializes the properties.
     */
    fun initProperties(vararg keys: String) {
        for (key in keys) {
            _properties[key] = ""
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
            return _properties.values.all { it.isNotBlank() }
        }

    /**
     * Sets a property key and value.
     */
    fun setProperty(key: String, value: String) {
        if (key.isNotBlank()) {
            _properties[key] = value
        }
    }
}
