/*
 * InfoJ.java
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
import net.thauvin.erik.mobibot.commands.links.LinksMgr;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.util.List;

public class Info extends AbstractCommand {
    private final List<String> allVersions = List.of(
            StringUtils.capitalize(ReleaseInfo.PROJECT) + " " + ReleaseInfo.VERSION
            + " (" + Utils.green(ReleaseInfo.WEBSITE) + ')',
            "Written by " + ReleaseInfo.AUTHOR + " (" + Utils.green(ReleaseInfo.AUTHOR_URL) + ')');

    public Info(final Mobibot bot) {
        super(bot);
    }

    @NotNull
    @Override
    public String getName() {
        return "info";
    }

    @NotNull
    @Override
    public List<String> getHelp() {
        return List.of("To view information about the bot:", Utils.helpFormat("%c " + getName()));
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return true;
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
        getBot().sendList(sender, allVersions, 1, isPrivate, false, false);

        final StringBuilder info = new StringBuilder("Uptime: ");

        info.append(Utils.uptime(ManagementFactory.getRuntimeMXBean().getUptime()))
            .append(" [Entries: ")
            .append(LinksMgr.entries.size());

        if (isOp) {
            if (getBot().getTell().isEnabled()) {
                info.append(", Messages: ").append(getBot().getTell().size());
            }
            if (getBot().getTwitter().isAutoPost()) {
                info.append(", Twitter: ").append(getBot().getTwitter().entriesCount());
            }
        }

        info.append(", Recap: ").append(Recap.recaps.size()).append(']');

        getBot().send(sender, info.toString(), isPrivate);
    }
}
