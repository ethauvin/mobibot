/*
 * Addons.java
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
package net.thauvin.erik.mobibot

import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.modules.AbstractModule
import java.util.*

/**
 * Modules and Commands addons.
 */
class Addons {
    val commands: MutableList<AbstractCommand> = mutableListOf()
    val modules: MutableList<AbstractModule> = mutableListOf()
    val modulesNames: MutableList<String> = mutableListOf()
    val names: MutableList<String> = mutableListOf()
    val ops: MutableList<String> = mutableListOf()

    /**
     * Add a module with properties.
     */
    fun add(module: AbstractModule, props: Properties) {
        module.apply {
            if (hasProperties()) {
                propertyKeys.forEach {
                    setProperty(it, props.getProperty(it, ""))
                }
            }

            if (isEnabled) {
                modules.add(this)
                modulesNames.add(this.javaClass.simpleName)
                names.addAll(this.commands)
            }
        }
    }

    /**
     * Add a command with properties.
     */
    fun add(command: AbstractCommand, props: Properties) {
        command.apply {
            if (properties.isNotEmpty()) {
                properties.keys.forEach {
                    setProperty(it, props.getProperty(it, ""))
                }
            }
            if (isEnabled()) {
                commands.add(this)
                if (isVisible) {
                    if (isOp) {
                        ops.add(name)
                    } else {
                        names.add(name)
                    }
                }
            }
        }
    }

    /**
     * Execute a command or module.
     */
    fun exec(sender: String, login: String, cmd: String, args: String, isOp: Boolean, isPrivate: Boolean): Boolean {
        for (command in commands) {
            if (command.name.startsWith(cmd)) {
                command.commandResponse(sender, login, args, isOp, isPrivate)
                return true
            }
        }
        for (module in modules) {
            if ((isPrivate && module.isPrivateMsgEnabled) || !isPrivate) {
                if (module.commands.contains(cmd)) {
                    module.commandResponse(sender, cmd, args, isPrivate)
                    return true
                }
            }
        }
        return false
    }

    /**
     * Match a command.
     */
    fun match(sender: String, login: String, message: String, isOp: Boolean, isPrivate: Boolean): Boolean {
        for (command in commands) {
            if (command.matches(message)) {
                command.commandResponse(sender, login, message, isOp, isPrivate)
                return true
            }
        }
        return false
    }

    /**
     * Commands and Modules help.
     */
    fun help(sender: String, topic: String, isOp: Boolean, isPrivate: Boolean): Boolean {
        for (command in commands) {
            if (command.isVisible && command.name.startsWith(topic)) {
                return command.helpResponse(topic, sender, isOp, isPrivate)
            }
        }
        for (module in modules) {
            if (module.commands.contains(topic)) {
                return module.helpResponse(sender, isPrivate)
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
