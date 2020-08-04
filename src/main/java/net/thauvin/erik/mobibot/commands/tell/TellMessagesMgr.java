/*
 * TellMessagesMgr.java
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

package net.thauvin.erik.mobibot.commands.tell;

import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The Tell Messages Manager.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2014-04-26
 * @since 1.0
 */
final class TellMessagesMgr {
    /**
     * Disables the default constructor.
     *
     * @throws UnsupportedOperationException If the constructor is called.
     */
    private TellMessagesMgr() {
        throw new UnsupportedOperationException("Illegal constructor call.");
    }

    /**
     * Cleans the messages queue.
     *
     * @param tellMessages The messages list.
     * @param tellMaxDays  The maximum number of days to keep messages for.
     * @return <code>True</code> if the queue was cleaned.
     */
    static boolean clean(final List<TellMessage> tellMessages, final int tellMaxDays) {
        final LocalDateTime today = LocalDateTime.now(Clock.systemUTC());

        return tellMessages.removeIf(o -> o.getQueued().plusDays(tellMaxDays).isBefore(today));
    }

    /**
     * Loads the messages.
     *
     * @param file   The serialized objects file.
     * @param logger The logger.
     * @return The {@link TellMessage} array.
     */
    @SuppressWarnings("unchecked")
    public static List<TellMessage> load(final String file, final Logger logger) {
        try {
            try (final ObjectInput input = new ObjectInputStream(
                    new BufferedInputStream(Files.newInputStream(Paths.get(file))))) {
                logger.debug("Loading the messages.");

                return ((List<TellMessage>) input.readObject());
            }
        } catch (FileNotFoundException ignore) {
            // Do nothing
        } catch (IOException | ClassNotFoundException e) {
            logger.error("An error occurred loading the messages queue.", e);
        }

        return new ArrayList<>();
    }

    /**
     * Saves the messages.
     *
     * @param file     The serialized objects file.
     * @param messages The {@link TellMessage} array.
     * @param logger   The logger.
     */
    public static void save(final String file, final List<TellMessage> messages, final Logger logger) {
        try {
            try (final BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(file)))) {
                try (final ObjectOutput output = new ObjectOutputStream(bos)) {
                    logger.debug("Saving the messages.");
                    output.writeObject(messages);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to save messages queue.", e);
        }
    }
}
