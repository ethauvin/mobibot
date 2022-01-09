/*
 * Addons.kt
 *
 * Copyright (c) 2004-2022, Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot

import net.thauvin.erik.mobibot.Utils.notContains
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.links.LinksMgr
import net.thauvin.erik.mobibot.modules.AbstractModule
import org.pircbotx.hooks.events.PrivateMessageEvent
import org.pircbotx.hooks.types.GenericMessageEvent
import java.util.Properties

/**
 * Modules and Commands addons.
 */
class Addons(private val props: Properties) {
    private val disabledModules = props.getProperty("disabled-modules", "").split(LinksMgr.TAG_MATCH.toRegex())
    private val disableCommands = props.getProperty("disabled-commands", "").split(LinksMgr.TAG_MATCH.toRegex())

    val commands: MutableList<AbstractCommand> = mutableListOf()
    val modules: MutableList<AbstractModule> = mutableListOf()
    val modulesNames: MutableList<String> = mutableListOf()
    val names: MutableList<String> = mutableListOf()
    val ops: MutableList<String> = mutableListOf()

    /**
     * Add a module with properties.
     */
    fun add(module: AbstractModule) {
        with(module) {
            if (disabledModules.notContains(name, true)) {
                if (hasProperties()) {
                    propertyKeys.forEach {
                        setProperty(it, props.getProperty(it, ""))
                    }
                }

                if (isEnabled) {
                    modules.add(this)
                    modulesNames.add(name)
                    names.addAll(commands)
                }
            }
        }
    }

    /**
     * Add a command with properties.
     */
    fun add(command: AbstractCommand) {
        with(command) {
            if (disableCommands.notContains(name, true)) {
                if (properties.isNotEmpty()) {
                    properties.keys.forEach {
                        setProperty(it, props.getProperty(it, ""))
                    }
                }
                if (isEnabled()) {
                    commands.add(this)
                    if (isVisible) {
                        if (isOpOnly) {
                            ops.add(name)
                        } else {
                            names.add(name)
                        }
                    }
                }
            }
        }
    }

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
     * Sort commands and modules names.
     */
    fun sort() {
        names.sort()
        ops.sort()
        modulesNames.sort()
    }
}
