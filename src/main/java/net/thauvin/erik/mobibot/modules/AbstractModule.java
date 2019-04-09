/*
 * AbstractModule.java
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

import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.Utils;

import java.util.*;

/**
 * The <code>Module</code> abstract class.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2016-07-01
 * @since 1.0
 */
public abstract class AbstractModule {
    final List<String> commands = new ArrayList<>();
    final Map<String, String> properties = new HashMap<>();

    /**
     * Responds to a command.
     *
     * @param bot       The bot's instance.
     * @param sender    The sender.
     * @param args      The command arguments.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    public abstract void commandResponse(final Mobibot bot,
                                         final String sender,
                                         final String args,
                                         final boolean isPrivate);

    /**
     * Returns the module's commands, if any.
     *
     * @return The commands.
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * Returns the module's property keys.
     *
     * @return The keys.
     */
    public Set<String> getPropertyKeys() {
        return properties.keySet();
    }

    /**
     * Returns <code>true</code> if the module has properties.
     *
     * @return <code>true</code> or <code>false</code> .
     */
    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    /**
     * Responds with the module's help.
     *
     * @param bot       The bot's instance.
     * @param sender    The sender.
     * @param args      The help arguments.
     * @param isPrivate Set to <code>true</code> if the response should be sent as a private message.
     */
    public abstract void helpResponse(final Mobibot bot,
                                      final String sender,
                                      final String args,
                                      final boolean isPrivate);

    /**
     * Returns <code>true</code> if the module is enabled.
     *
     * @return <code>true</code> or <code>false</code>
     */
    public boolean isEnabled() {
        if (hasProperties()) {
            return isValidProperties();
        } else {
            return true;
        }
    }

    /**
     * Returns <code>true</code> if the module responds to private messages.
     *
     * @return <code>true</code> or <code>false</code>
     */
    public boolean isPrivateMsgEnabled() {
        return false;
    }

    /**
     * Ensures that all properties have values.
     *
     * @return <code>true</code> if the properties are valid, <code>false</code> otherwise.
     */
    boolean isValidProperties() {
        for (final String s : getPropertyKeys()) {
            if (!Utils.isValidString(properties.get(s))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets a property key and value.
     *
     * @param key   The key.
     * @param value The value.
     */
    public void setProperty(final String key, final String value) {
        if (Utils.isValidString(key)) {
            properties.put(key, value);
        }
    }
}
