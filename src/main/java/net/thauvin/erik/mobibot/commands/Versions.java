/*
 * Version.java
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

package net.thauvin.erik.mobibot.commands;

import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.ReleaseInfo;
import net.thauvin.erik.mobibot.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Versions extends AbstractCommand {
    private final List<String> verList =
            List.of("Version: " + ReleaseInfo.VERSION + " (" + Utils.isoLocalDate(ReleaseInfo.BUILDDATE) + ')',
                    "Platform: " + System.getProperty("os.name") + ' ' + System.getProperty("os.version")
                    + " (" + System.getProperty("os.arch") + ')',
                    "Runtime: " + System.getProperty("java.runtime.name")
                    + ' ' + System.getProperty("java.runtime.version"));

    public Versions(@NotNull final Mobibot bot) {
        super(bot);
    }

    @NotNull
    @Override
    public String getName() {
        return "versions";
    }


    @NotNull
    @Override
    public List<String> getHelp() {
        return List.of("To view the versions data (bot, platform, java, etc.):", Utils.helpFormat("%c " + getName()));
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void commandResponse(@NotNull final String sender,
                                @NotNull final String login,
                                @NotNull final String args,
                                final boolean isOp,
                                final boolean isPrivate) {
        if (isOp) {
            for (final String v : verList) {
                getBot().send(sender, v, isPrivate);
            }
        } else {
            getBot().helpDefault(sender, false, isPrivate);
        }

    }
}
