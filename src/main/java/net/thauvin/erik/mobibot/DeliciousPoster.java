/*
 * DeliciousPoster.java
 *
 * Copyright (c) 2004-2017, Erik C. Thauvin (erik@thauvin.net)
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

import del.icio.us.Delicious;

import javax.swing.*;

/**
 * The class to handle posts to del.icio.us.
 *
 * @author <a href="http://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created Mar 5, 2005
 * @since 1.0
 */
class DeliciousPoster {
    private final Delicious delicious;
    private final String ircServer;

    /**
     * Creates a new {@link DeliciousPoster} instance.
     *
     * @param username  The del.icio.us user name.
     * @param password  The del.icio.us password.
     * @param ircServer The IRC server.
     */
    public DeliciousPoster(final String username, final String password, final String ircServer) {
        delicious = new Delicious(username, password);
        this.ircServer = ircServer;
    }

    /**
     * Adds a post to del.icio.us.
     *
     * @param entry The entry to add.
     */
    public final void addPost(final EntryLink entry) {
        final SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground()
                    throws Exception {
                return delicious.addPost(entry.getLink(),
                        entry.getTitle(),
                        postedBy(entry),
                        entry.getDeliciousTags(),
                        entry.getDate());
            }
        };

        worker.execute();
    }

    /**
     * Deletes a post to del.icio.us.
     *
     * @param entry The entry to delete.
     */
    public final void deletePost(final EntryLink entry) {
        final String link = entry.getLink();

        final SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground()
                    throws Exception {
                return delicious.deletePost(link);
            }
        };

        worker.execute();
    }

    /**
     * Returns he del.icio.us extended attribution line.
     *
     * @param entry The entry.
     * @return The extended attribution line.
     */
    private String postedBy(final EntryLink entry) {
        return "Posted by " + entry.getNick() + " on " + entry.getChannel() + " (" + ircServer + ')';
    }

    /**
     * Updates a post to del.icio.us.
     *
     * @param oldUrl The old post URL.
     * @param entry  The entry to add.
     */
    public final void updatePost(final String oldUrl, final EntryLink entry) {
        final SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground()
                    throws Exception {
                if (!oldUrl.equals(entry.getLink())) {
                    delicious.deletePost(oldUrl);

                    return delicious.addPost(entry.getLink(),
                            entry.getTitle(),
                            postedBy(entry),
                            entry.getDeliciousTags(),
                            entry.getDate());
                } else {
                    return delicious.addPost(entry.getLink(),
                            entry.getTitle(),
                            postedBy(entry),
                            entry.getDeliciousTags(),
                            entry.getDate(),
                            true,
                            true);
                }
            }
        };

        worker.execute();
    }
}
