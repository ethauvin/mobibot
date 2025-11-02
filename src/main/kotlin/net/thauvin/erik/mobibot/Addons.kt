/*
 * Addons.kt
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.thauvin.erik.mobibot.Utils.notContains
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.links.LinksManager
import net.thauvin.erik.mobibot.modules.AbstractModule
import org.pircbotx.hooks.events.PrivateMessageEvent
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Registers and manages commands and modules.
 */
@SuppressFBWarnings("BC_BAD_CAST_TO_ABSTRACT_COLLECTION")
class Addons(props: Properties) {
    private val props: Properties = Properties().apply { putAll(props) }

    private val commands: MutableList<AbstractCommand> = mutableListOf()
    private val disableCommands = props.getProperty("disabled-commands", "")
        .split(LinksManager.TAG_MATCH)
    private val disabledModules = props.getProperty("disabled-modules", "")
        .split(LinksManager.TAG_MATCH)
    private val logger: Logger = LoggerFactory.getLogger(Addons::class.java)
    private val modules: MutableList<AbstractModule> = mutableListOf()
    private val names = Names

    /**
     * Add a command with properties.
     */
    fun add(command: AbstractCommand): Boolean {
        var enabled = false
        with(command) {
            if (disableCommands.notContains(name, true)) {
                val properties = getProperties()
                if (properties.isNotEmpty()) {
                    properties.keys.forEach {
                        setProperty(it, props.getProperty(it, ""))
                    }
                }
                if (isEnabled()) {
                    commands.add(this)
                    if (isVisible) {
                        if (isOpOnly) {
                            names.ops.add(name)
                        } else {
                            names.commands.add(name)
                        }
                    }
                    enabled = true
                } else {
                    if (logger.isDebugEnabled) {
                        logger.debug("Command $name is disabled.")
                    }
                    names.disabledCommands.add(name)
                }
            } else {
                names.disabledCommands.add(name)
            }
        }
        return enabled
    }

    /**
     * Add a module with properties.
     */
    fun add(module: AbstractModule): Boolean {
        var enabled = false
        with(module) {
            if (disabledModules.notContains(name, true)) {
                if (hasProperties()) {
                    propertyKeys.forEach {
                        setProperty(it, props.getProperty(it, ""))
                    }
                }

                if (isEnabled) {
                    modules.add(this)
                    names.modules.add(name)
                    names.commands.addAll(commands)
                    enabled = true
                } else {
                    if (logger.isDebugEnabled) {
                        logger.debug("Module $name is disabled.")
                    }
                    names.disabledModules.add(name)
                }
            } else {
                names.disabledModules.add(name)
            }
        }
        return enabled
    }

    /**
     * Retrieves the list of commands names.
     */
    fun commands() = names.commands.toList()

    /**
     * Retrieves the list of command names that are currently disabled.
     **/
    fun disabledCommands() = names.disabledCommands.toList()

    /**
     * Retrieves a list of currently disabled module names.
     */
    fun disabledModules() = names.disabledModules.toList()

    /**
     * Execute a command or module.
     */
    fun exec(channel: String, cmd: String, args: String, event: GenericMessageEvent): Boolean {
        val cmds = if (event is PrivateMessageEvent) commands else commands.filter { it.isPublic }
        for (command in cmds) {
            if (command.name.startsWith(cmd)) {
                command.commandResponse(channel, args, event)
                return true
            }
        }
        val mods = if (event is PrivateMessageEvent) modules.filter { it.isPrivateMsgEnabled } else modules
        for (module in mods) {
            if (module.commands.contains(cmd)) {
                module.commandResponse(channel, cmd, args, event)
                return true
            }
        }
        return false
    }

    /**
     * Commands and Modules help.
     */
    fun help(channel: String, topic: String, event: GenericMessageEvent): Boolean {
        for (command in commands) {
            if (command.isVisible && command.name.startsWith(topic)) {
                return command.helpResponse(channel, topic, event)
            }
        }
        for (module in modules) {
            if (module.commands.contains(topic)) {
                return module.helpResponse(event)
            }
        }
        return false
    }

    /**
     * Match a command.
     */
    fun match(channel: String, event: GenericMessageEvent): Boolean {
        for (command in commands) {
            if (command.matches(event.message)) {
                command.commandResponse(channel, event.message, event)
                return true
            }
        }
        return false
    }

    /**
     * Retrieves the list of module names.
     */
    fun modules() = names.modules.toList()

    /**
     * Retrieves the list of operator names.
     */
    fun ops() = names.ops.toList()

    /**
     * Sorts the commands and modules names.
     */
    fun sort() = names.sort()

    // Holds commands and modules names.
    private object Names {
        val modules: MutableList<String> = mutableListOf()
        val disabledModules: MutableList<String> = mutableListOf()
        val commands: MutableList<String> = mutableListOf()
        val disabledCommands: MutableList<String> = mutableListOf()
        val ops: MutableList<String> = mutableListOf()

        fun sort() {
            modules.sort()
            disabledModules.sort()
            commands.sort()
            disabledCommands.sort()
            ops.sort()
        }
    }
}
