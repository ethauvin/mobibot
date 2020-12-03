/*
 * Tell.java
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

import net.thauvin.erik.mobibot.Mobibot;
import net.thauvin.erik.mobibot.Utils;
import net.thauvin.erik.mobibot.commands.AbstractCommand;
import net.thauvin.erik.mobibot.commands.links.View;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The <code>Tell</code> command.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created 2016-07-02
 * @since 1.0
 */
public class Tell extends AbstractCommand {
    /**
     * Max days property.
     */
    public static final String MAX_DAYS_PROP = "tell-max-days";
    /**
     * Max size proeprty.
     */
    public static final String MAX_SIZE_PROP = "tell-max-size";
    /**
     * The tell command.
     */
    public static final String TELL_CMD = "tell";
    // Arrow
    private static final String ARROW = " --> ";
    // Serialized object file extension
    private static final String SER_EXT = ".ser";
    // All keyword
    private static final String TELL_ALL_KEYWORD = "all";
    //T he delete command.
    private static final String TELL_DEL_KEYWORD = "del";
    // Messages queue
    private final List<TellMessage> messages = new CopyOnWriteArrayList<>();
    // Serialized object file
    private final String serializedObject;
    // Maximum number of days to keep messages
    private int maxDays = 7;
    // Message maximum queue size
    private int maxSize = 50;

    /**
     * Creates a new instance.
     *
     * @param bot The bot.
     */
    public Tell(final Mobibot bot) {
        super(bot);
        initProperties(MAX_DAYS_PROP, MAX_SIZE_PROP);

        // Load the message queue
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
    @SuppressWarnings("WeakerAccess")
    final boolean clean() {
        if (getBot().getLogger().isDebugEnabled()) {
            getBot().getLogger().debug("Cleaning the messages.");
        }
        return TellMessagesMgr.clean(messages, maxDays);
    }

    // Delete message.
    private void deleteMessage(final String sender, final String args, final boolean isOp, final boolean isPrivate) {
        final String[] split = args.split(" ");

        if (split.length == 2) {
            final String id = split[1];
            boolean deleted = false;

            if (TELL_ALL_KEYWORD.equalsIgnoreCase(id)) {
                for (final TellMessage message : messages) {
                    if (message.getSender().equalsIgnoreCase(sender) && message.isReceived()) {
                        messages.remove(message);
                        deleted = true;
                    }
                }

                if (deleted) {
                    save();
                    getBot().send(sender, "Delivered messages have been deleted.", isPrivate);
                } else {
                    getBot().send(sender, "No delivered messages were found.", isPrivate);
                }

            } else {
                boolean found = false;

                for (final TellMessage message : messages) {
                    found = message.isMatchId(id);

                    if (found && (message.getSender().equalsIgnoreCase(sender) || getBot().isOp(sender))) {
                        messages.remove(message);

                        save();
                        getBot().send(sender, "Your message was deleted from the queue.", isPrivate);
                        deleted = true;
                        break;
                    }
                }

                if (!deleted) {
                    if (found) {
                        getBot().send(sender, "Only messages that you sent can be deleted.", isPrivate);
                    } else {
                        getBot().send(sender, "The specified message [ID " + id + "] could not be found.", isPrivate);
                    }
                }
            }
        } else {
            helpResponse(args, sender, isOp, isPrivate);
        }
    }

    @NotNull
    @Override
    public String getName() {
        return TELL_CMD;
    }

    @NotNull
    @Override
    public List<String> getHelp() {
        return List.of("To send a message to someone when they join the channel:",
                       Utils.helpIndent("%c " + TELL_CMD + " <nick> <message>"),
                       "To view queued and sent messages:",
                       Utils.helpIndent("%c " + TELL_CMD + ' ' + View.VIEW_CMD),
                       "Messages are kept for " + Utils.bold(maxDays)
                       + Utils.plural(maxDays, " day.", " days."));
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return isEnabled();
    }

    @Override
    public boolean isVisible() {
        return isEnabled();
    }

    @Override
    public void commandResponse(@NotNull final String sender,
                                @NotNull final String login,
                                @NotNull final String args,
                                final boolean isOp,
                                final boolean isPrivate) {
        if (isEnabled()) {
            if (StringUtils.isBlank(args)) {
                helpResponse(args, sender, isOp, isPrivate);
            } else if (args.startsWith(View.VIEW_CMD)) {
                if (getBot().isOp(sender) && (View.VIEW_CMD + ' ' + TELL_ALL_KEYWORD).equals(args)) {
                    viewAll(sender, isPrivate);
                } else {
                    viewMessages(sender, isPrivate);
                }
            } else if (args.startsWith(TELL_DEL_KEYWORD + ' ')) {
                deleteMessage(sender, args, isOp, isPrivate);
            } else {
                newMessage(sender, args, isOp, isPrivate);
            }

            if (clean()) {
                save();
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return maxSize > 0 && maxDays > 0;
    }

    @Override
    public void setProperty(@NotNull final String key, @NotNull final String value) {
        super.setProperty(key, value);
        if (MAX_DAYS_PROP.equals(key)) {
            this.maxDays = Utils.getIntProperty(value, maxDays);
        } else if (MAX_SIZE_PROP.equals(key)) {
            this.maxSize = Utils.getIntProperty(value, maxSize);
        }
    }

    // New message.
    private void newMessage(final String sender, final String args, final boolean isOp, final boolean isPrivate) {
        final String[] split = args.split(" ", 2);

        if (split.length == 2 && (StringUtils.isNotBlank(split[1]) && split[1].contains(" "))) {
            if (messages.size() < maxSize) {
                final TellMessage message = new TellMessage(sender, split[0], split[1].trim());

                messages.add(message);

                save();

                getBot().send(sender, "Message [ID " + message.getId() + "] was queued for "
                                      + Utils.bold(message.getRecipient()), isPrivate);
            } else {
                getBot().send(sender, "Sorry, the messages queue is currently full.", isPrivate);
            }
        } else {
            helpResponse(args, sender, isOp, isPrivate);
        }
    }

    /**
     * Saves the messages queue.
     */
    @SuppressWarnings("WeakerAccess")
    final void save() {
        TellMessagesMgr.save(serializedObject, messages, getBot().getLogger());
    }

    /**
     * Checks and sends messages.
     *
     * @param nickname  The user's nickname.
     * @param isMessage The message flag.
     */
    public void send(final String nickname, final boolean isMessage) {
        if (isEnabled() && !nickname.equals(getBot().getNick())) {
            messages.stream().filter(message -> message.isMatch(nickname)).forEach(message -> {
                if (message.getRecipient().equalsIgnoreCase(nickname) && !message.isReceived()) {
                    if (message.getSender().equals(nickname)) {
                        if (!isMessage) {
                            getBot().send(nickname, Utils.bold("You") + " wanted me to remind you: "
                                                    + Utils.reverseColor(message.getMessage()),
                                          true);

                            message.setIsReceived();
                            message.setIsNotified();

                            save();
                        }
                    } else {
                        getBot().send(nickname, message.getSender() + " wanted me to tell you: "
                                                + Utils.reverseColor(message.getMessage()),
                                      true);

                        message.setIsReceived();

                        save();
                    }
                } else if (message.getSender().equalsIgnoreCase(nickname) && message.isReceived()
                           && !message.isNotified()) {
                    getBot().send(nickname,
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

    // View all messages.
    private void viewAll(final String sender, final boolean isPrivate) {
        if (!messages.isEmpty()) {
            for (final TellMessage message : messages) {
                getBot().send(sender, Utils.bold(message.getSender()) + ARROW + Utils.bold(message.getRecipient())
                                      + " [ID: " + message.getId() + ", "
                                      + (message.isReceived() ? "DELIVERED" : "QUEUED") + ']',
                              isPrivate);
            }
        } else {
            getBot().send(sender, "There are no messages in the queue.", isPrivate);
        }
    }

    // View messages.
    private void viewMessages(final String sender, final boolean isPrivate) {
        boolean hasMessage = false;

        for (final TellMessage message : messages) {
            if (message.isMatch(sender)) {
                if (!hasMessage) {
                    hasMessage = true;
                    getBot().send(sender, "Here are your messages: ", isPrivate);
                }

                if (message.isReceived()) {
                    getBot().send(sender,
                                  Utils.bold(message.getSender()) + ARROW + Utils.bold(message.getRecipient())
                                  + " [" + Utils.utcDateTime(message.getReceived()) + ", ID: "
                                  + Utils.bold(message.getId()) + ", DELIVERED]",
                                  isPrivate);

                } else {
                    getBot().send(sender,
                                  Utils.bold(message.getSender()) + ARROW + Utils.bold(message.getRecipient())
                                  + " [" + Utils.utcDateTime(message.getQueued()) + ", ID: "
                                  + Utils.bold(message.getId()) + ", QUEUED]",
                                  isPrivate);
                }

                getBot().send(sender, Utils.helpIndent(message.getMessage()), isPrivate);
            }
        }

        if (!hasMessage) {
            getBot().send(sender, "You have no messages in the queue.", isPrivate);
        } else {
            getBot().send(sender, "To delete one or all delivered messages:", isPrivate);
            getBot().send(sender,
                          Utils.helpIndent(Utils.helpFormat(
                                  "%c " + TELL_CMD + ' ' + TELL_DEL_KEYWORD + " <id|" + TELL_ALL_KEYWORD + '>',
                                  getBot().getNick(),
                                  isPrivate)),
                          isPrivate);
            getBot().send(sender,
                          "Messages are kept for " + Utils.bold(maxDays)
                          + Utils.plural(maxDays, " day.", " days."), isPrivate);
        }
    }
}
