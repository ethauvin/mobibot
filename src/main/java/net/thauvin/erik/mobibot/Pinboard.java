/*
 * Pinboard.java
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

package net.thauvin.erik.mobibot;

import net.thauvin.erik.mobibot.entries.EntryLink;
import net.thauvin.erik.pinboard.PinboardPoster;

import javax.swing.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class to handle posts to pinboard.in.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2017-05-17
 * @since 1.0
 */
class Pinboard {
    private final String ircServer;
    private final PinboardPoster pinboard;

    /**
     * Creates a new {@link Pinboard} instance.
     *
     * @param bot       The bot's instance.
     * @param apiToken  The API end point.
     */
    Pinboard(final Mobibot bot, final String apiToken) {
        pinboard = new PinboardPoster(apiToken);
        ircServer = bot.getIrcServer();

        if (bot.getLogger().isDebugEnabled()) {
            final ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.FINE);
            final Logger logger = pinboard.getLogger();
            logger.addHandler(consoleHandler);
            logger.setLevel(Level.FINE);
        }
    }

    /**
     * Adds a post to pinboard.in.
     *
     * @param entry The entry to add.
     */
    final void addPost(final EntryLink entry) {
        final SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return pinboard.addPin(entry.getLink(),
                                       entry.getTitle(),
                                       postedBy(entry),
                                       entry.getPinboardTags(),
                                       formatDate(entry.getDate()));
            }
        };

        worker.execute();
    }

    /**
     * Deletes a post to pinboard.in.
     *
     * @param entry The entry to delete.
     */
    final void deletePost(final EntryLink entry) {
        final String link = entry.getLink();

        final SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return pinboard.deletePin(link);
            }
        };

        worker.execute();
    }

    /**
     * Format a date to a UTC timestamp.
     *
     * @param date The date.
     * @return The date in {@link DateTimeFormatter#ISO_INSTANT} format.
     */
    private String formatDate(final Date date) {
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT);
    }

    /**
     * Returns he pinboard.in extended attribution line.
     *
     * @param entry The entry.
     * @return The extended attribution line.
     */
    private String postedBy(final EntryLink entry) {
        return "Posted by " + entry.getNick() + " on " + entry.getChannel() + " (" + ircServer + ')';
    }

    /**
     * Updates a post to pinboard.in.
     *
     * @param oldUrl The old post URL.
     * @param entry  The entry to add.
     */
    final void updatePost(final String oldUrl, final EntryLink entry) {
        final SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                if (!oldUrl.equals(entry.getLink())) {
                    pinboard.deletePin(oldUrl);

                    return pinboard.addPin(entry.getLink(),
                                           entry.getTitle(),
                                           postedBy(entry),
                                           entry.getPinboardTags(),
                                           formatDate(entry.getDate()));
                } else {
                    return pinboard.addPin(entry.getLink(),
                                           entry.getTitle(),
                                           postedBy(entry),
                                           entry.getPinboardTags(),
                                           formatDate(entry.getDate()),
                                           true,
                                           true);
                }
            }
        };

        worker.execute();
    }
}
