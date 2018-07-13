/*
 * Tell.java
 *
 * Copyright (c) 2004-2018, Erik C. Thauvin (erik@thauvin.net)
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The <code>Tell</code> command.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2016-07-02
 * @since 1.0
 */
public class Tell {
    /**
     * The all keyword.
     */
    public static final String TELL_ALL_KEYWORD = "all";

    /**
     * The tell command.
     */
    public static final String TELL_CMD = "tell";

    /**
     * The delete command.
     */
    public static final String TELL_DEL_KEYWORD = "del";

    // The default maximum number of days to keep messages.
    private static final int DEFAULT_TELL_MAX_DAYS = 7;

    // The default message max queue size.
    private static final int DEFAULT_TELL_MAX_SIZE = 50;

    // The serialized object file extension.
    private static final String SER_EXT = ".ser";

    // The bot instance.
    final private Mobibot bot;

    // The maximum number of days to keep messages.
    final private int maxDays;

    // The message maximum queue size.
    final private int maxSize;

    // The messages queue.
    private final List<TellMessage> messages = new CopyOnWriteArrayList<>();

    // The serialized object file.
    private final String serializedObject;

    public Tell(final Mobibot bot, final String maxDays, final String maxSize) {
        this.bot = bot;
        this.maxDays = Utils.getIntProperty(maxDays, DEFAULT_TELL_MAX_DAYS);
        this.maxSize = Utils.getIntProperty(maxSize, DEFAULT_TELL_MAX_SIZE);

        // Load the message queue.
        serializedObject = bot.getLogsDir() + bot.getName() + SER_EXT;
        messages.addAll(TellMessagesMgr.load(serializedObject, bot.getLogger()));

        if (clean()) {
            save();
        }
    }

    /**
     * Cleans the messages queue.
     *
     * @return <code>true</code> if the queue was cleaned.
     */
    private boolean clean() {
        if (bot.getLogger().isDebugEnabled()) {
            bot.getLogger().debug("Cleaning the messages.");
        }

        return TellMessagesMgr.clean(messages, maxDays);
    }

    /**
     * Responds with help.
     *
     * @param sender The sender.
     */
    public void helpResponse(final String sender) {
        bot.send(sender, "To send a message to someone when they join the channel:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + TELL_CMD + " <nick> <message>"));

        bot.send(sender, "To view queued and sent messages:");
        bot.send(sender, bot.helpIndent(bot.getNick() + ": " + TELL_CMD + ' ' + Commands.VIEW_CMD));

        bot.send(sender, "Messages are kept for " + Utils.bold(maxDays) + Utils.plural(maxDays, " day.", " days."));
    }

    /**
     * Returns <code>true</code> if enabled.
     *
     * @return <code>true</code> or <code>false</code>
     */
    public boolean isEnabled() {
        return maxSize > 0 && maxDays > 0;
    }

    /**
     * Processes the commands.
     *
     * @param sender The sender's nick.
     * @param cmds   The commands string.
     */
    public void response(final String sender, final String cmds) {
        if (!Utils.isValidString(cmds)) {
            helpResponse(sender);
        } else if (cmds.startsWith(Commands.VIEW_CMD)) {
            if (bot.isOp(sender) && cmds.equals(Commands.VIEW_CMD + ' ' + TELL_ALL_KEYWORD)) {
                if (messages.size() > 0) {
                    for (final TellMessage message : messages) {
                        bot.send(sender, Utils.bold(message.getSender()) + " --> " + Utils.bold(message.getRecipient())
                                + " [ID: " + message.getId() + ", "
                                + (message.isReceived() ? "DELIVERED" : "QUEUED") + ']',
                            true);
                    }
                } else {
                    bot.send(sender, "There are no messages in the queue.", true);
                }
            } else {
                boolean hasMessage = false;

                for (final TellMessage message : messages) {
                    if (message.isMatch(sender)) {
                        if (!hasMessage) {
                            hasMessage = true;
                            bot.send(sender, "Here are your messages: ", true);
                        }

                        if (message.isReceived()) {
                            bot.send(sender,
                                Utils.bold(message.getSender()) + " --> " + Utils.bold(message.getRecipient())
                                    + " [" + Utils.utcDateTime(message.getReceived()) + ", ID: "
                                    + message.getId() + ", DELIVERED]",
                                true);

                        } else {
                            bot.send(sender,
                                Utils.bold(message.getSender()) + " --> " + Utils.bold(message.getRecipient())
                                    + " [" + Utils.utcDateTime(message.getQueued()) + ", ID: "
                                    + message.getId() + ", QUEUED]",
                                true);
                        }

                        bot.send(sender, bot.helpIndent(message.getMessage(), false), true);
                    }
                }

                if (!hasMessage) {
                    bot.send(sender, "You have no messages in the queue.", true);
                } else {
                    bot.send(sender, "To delete one or all delivered messages:");
                    bot.send(sender,
                        bot.helpIndent(bot.getNick() + ": " + TELL_CMD + ' ' + TELL_DEL_KEYWORD + " <id|"
                            + TELL_ALL_KEYWORD + '>'));
                    bot.send(sender, "Messages are kept for " + Utils.bold(maxDays)
                        + Utils.plural(maxDays, " day.", " days."));
                }
            }
        } else if (cmds.startsWith(TELL_DEL_KEYWORD + ' ')) {
            final String[] split = cmds.split(" ");

            if (split.length == 2) {
                final String id = split[1];
                boolean deleted = false;

                if (id.equalsIgnoreCase(TELL_ALL_KEYWORD)) {
                    for (final TellMessage message : messages) {
                        if (message.getSender().equalsIgnoreCase(sender) && message.isReceived()) {
                            messages.remove(message);
                            deleted = true;
                        }
                    }

                    if (deleted) {
                        save();
                        bot.send(sender, "Delivered messages have been deleted.", true);
                    } else {
                        bot.send(sender, "No delivered messages were found.", true);
                    }

                } else {
                    boolean found = false;

                    for (final TellMessage message : messages) {
                        found = message.isMatchId(id);

                        if (found && (message.getSender().equalsIgnoreCase(sender) || bot.isOp(sender))) {
                            messages.remove(message);

                            save();
                            bot.send(sender, "Your message was deleted from the queue.", true);
                            deleted = true;
                            break;
                        }
                    }

                    if (!deleted) {
                        if (found) {
                            bot.send(sender, "Only messages that you sent can be deleted.", true);
                        } else {
                            bot.send(sender, "The specified message [ID " + id + "] could not be found.", true);
                        }
                    }
                }
            } else {
                helpResponse(sender);
            }
        } else {
            final String[] split = cmds.split(" ", 2);

            if (split.length == 2 && (Utils.isValidString(split[1]) && split[1].contains(" "))) {
                if (messages.size() < maxSize) {
                    final TellMessage message = new TellMessage(sender, split[0], split[1].trim());

                    messages.add(message);

                    save();

                    bot.send(sender, "Message [ID " + message.getId() + "] was queued for "
                        + Utils.bold(message.getRecipient()), true);
                } else {
                    bot.send(sender, "Sorry, the messages queue is currently full.", true);
                }
            } else {
                helpResponse(sender);
            }
        }

        if (clean()) save();
    }

    /**
     * Saves the messages queue.
     */
    private void save() {
        TellMessagesMgr.save(serializedObject, messages, bot.getLogger());
    }

    /**
     * Checks and sends messages.
     *
     * @param nickname  The user's nickname.
     * @param isMessage The message flag.
     */
    public void send(final String nickname, final boolean isMessage) {
        if (!nickname.equals(bot.getNick()) && isEnabled()) {
            messages.stream().filter(message -> message.isMatch(nickname)).forEach(
                message -> {
                    if (message.getRecipient().equalsIgnoreCase(nickname) && !message.isReceived()) {
                        if (message.getSender().equals(nickname)) {
                            if (!isMessage) {
                                bot.send(nickname, Utils.bold("You") + " wanted me to remind you: "
                                        + Utils.reverseColor(message.getMessage()),
                                    true);

                                message.setIsReceived();
                                message.setIsNotified();

                                save();
                            }
                        } else {
                            bot.send(nickname, message.getSender() + " wanted me to tell you: "
                                    + Utils.reverseColor(message.getMessage()),
                                true);

                            message.setIsReceived();

                            save();
                        }
                    } else if (message.getSender().equalsIgnoreCase(nickname) && message.isReceived()
                        && !message.isNotified()) {
                        bot.send(nickname,
                            "Your message "
                                + Utils.reverseColor("[ID " + message.getId() + ']') + " was sent to "
                                + Utils.bold(message.getRecipient()) + " on "
                                + Utils.utcDateTime(message.getReceived()),
                            true);

                        message.setIsNotified();

                        save();
                    }
                });
        }
    }

    /**
     * Checks and sends messages.
     *
     * @param nickname The user's nickname.
     */
    public void send(final String nickname) {
        send(nickname, false);
    }

    /**
     * Returns the messages queue size.
     *
     * @return The size.
     */
    public int size() {
        return messages.size();
    }
}
