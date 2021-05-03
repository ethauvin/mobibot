/*
 * Tell.kt
 *
 * Copyright (c) 2004-2021, Erik C. Thauvin (erik@thauvin.net)
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
package net.thauvin.erik.mobibot.commands.tell

import net.thauvin.erik.mobibot.Mobibot
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.buildCmdSyntax
import net.thauvin.erik.mobibot.Utils.getIntProperty
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.plural
import net.thauvin.erik.mobibot.Utils.reverseColor
import net.thauvin.erik.mobibot.Utils.utcDateTime
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.links.View

/**
 * The `Tell` command.
 */
class Tell(bot: Mobibot) : AbstractCommand(bot) {
    // Messages queue
    private val messages: MutableList<TellMessage> = mutableListOf()

    // Serialized object file
    private val serializedObject: String

    // Maximum number of days to keep messages
    @Suppress("MagicNumber")
    private var maxDays = 7

    // Message maximum queue size
    @Suppress("MagicNumber")
    private var maxSize = 50

    /**
     * Cleans the messages queue.
     */
    private fun clean(): Boolean {
        if (bot.logger.isDebugEnabled) bot.logger.debug("Cleaning the messages.")
        return TellMessagesMgr.clean(messages, maxDays.toLong())
    }

    // Delete message.
    private fun deleteMessage(sender: String, args: String, isOp: Boolean, isPrivate: Boolean) {
        val split = args.split(" ")
        if (split.size == 2) {
            val id = split[1]
            var deleted = false
            if (TELL_ALL_KEYWORD.equals(id, ignoreCase = true)) {
                for (message in messages) {
                    if (message.sender.equals(sender, ignoreCase = true) && message.isReceived) {
                        messages.remove(message)
                        deleted = true
                    }
                }
                if (deleted) {
                    save()
                    bot.send(sender, "Delivered messages have been deleted.", isPrivate)
                } else {
                    bot.send(sender, "No delivered messages were found.", isPrivate)
                }
            } else {
                var found = false
                for (message in messages) {
                    found = (message.id == id)
                    if (found && (message.sender.equals(sender, ignoreCase = true) || bot.isOp(sender))) {
                        messages.remove(message)
                        save()
                        bot.send(sender, "Your message was deleted from the queue.", isPrivate)
                        deleted = true
                        break
                    }
                }
                if (!deleted) {
                    if (found) {
                        bot.send(sender, "Only messages that you sent can be deleted.", isPrivate)
                    } else {
                        bot.send(sender, "The specified message [ID $id] could not be found.", isPrivate)
                    }
                }
            }
        } else {
            helpResponse(args, sender, isOp, isPrivate)
        }
    }

    /**
     * The tell command.
     */
    override val name = "tell"

    override val help: List<String>
        get() = listOf(
            "To send a message to someone when they join the channel:",
            helpFormat("%c $name <nick> <message>"),
            "To view queued and sent messages:",
            helpFormat("%c $name ${View.VIEW_CMD}"),
            "Messages are kept for " + bold(maxDays)
                + plural(maxDays, " day.", " days.")
        )
    override val isOp: Boolean
        get() = false
    override val isPublic: Boolean
        get() = isEnabled()
    override val isVisible: Boolean
        get() = isEnabled()

    override fun commandResponse(
        sender: String,
        login: String,
        args: String,
        isOp: Boolean,
        isPrivate: Boolean
    ) {
        if (isEnabled()) {
            if (args.isBlank()) {
                helpResponse(args, sender, isOp, isPrivate)
            } else if (args.startsWith(View.VIEW_CMD)) {
                if (bot.isOp(sender) && "${View.VIEW_CMD} $TELL_ALL_KEYWORD" == args) {
                    viewAll(sender, isPrivate)
                } else {
                    viewMessages(sender, isPrivate)
                }
            } else if (args.startsWith("$TELL_DEL_KEYWORD ")) {
                deleteMessage(sender, args, isOp, isPrivate)
            } else {
                newMessage(sender, args, isOp, isPrivate)
            }
            if (clean()) {
                save()
            }
        }
    }

    override fun isEnabled(): Boolean {
        return maxSize > 0 && maxDays > 0
    }

    override fun setProperty(key: String, value: String) {
        super.setProperty(key, value)
        if (MAX_DAYS_PROP == key) {
            maxDays = getIntProperty(value, maxDays)
        } else if (MAX_SIZE_PROP == key) {
            maxSize = getIntProperty(value, maxSize)
        }
    }

    // New message.
    private fun newMessage(sender: String, args: String, isOp: Boolean, isPrivate: Boolean) {
        val split = args.split(" ".toRegex(), 2)
        if (split.size == 2 && split[1].isNotBlank() && split[1].contains(" ")) {
            if (messages.size < maxSize) {
                val message = TellMessage(sender, split[0], split[1].trim())
                messages.add(message)
                save()
                bot.send(
                    sender, "Message [ID ${message.id}] was queued for ${bold(message.recipient)}", isPrivate
                )
            } else {
                bot.send(sender, "Sorry, the messages queue is currently full.", isPrivate)
            }
        } else {
            helpResponse(args, sender, isOp, isPrivate)
        }
    }

    /**
     * Saves the messages queue.
     */
    private fun save() {
        TellMessagesMgr.save(serializedObject, messages, bot.logger)
    }

    /**
     * Checks and sends messages.
     */
    @JvmOverloads
    fun send(nickname: String, isMessage: Boolean = false) {
        if (isEnabled() && nickname != bot.nick) {
            messages.stream().filter { message: TellMessage -> message.isMatch(nickname) }
                .forEach { message: TellMessage ->
                    if (message.recipient.equals(nickname, ignoreCase = true) && !message.isReceived) {
                        if (message.sender == nickname) {
                            if (!isMessage) {
                                bot.send(
                                    nickname,
                                    "${bold("You")} wanted me to remind you: ${reverseColor(message.message)}",
                                    true
                                )
                                message.isReceived = true
                                message.isNotified = true
                                save()
                            }
                        } else {
                            bot.send(
                                nickname,
                                "${message.sender} wanted me to tell you: ${reverseColor(message.message)}",
                                true
                            )
                            message.isReceived = true
                            save()
                        }
                    } else if (message.sender.equals(nickname, ignoreCase = true) && message.isReceived
                        && !message.isNotified) {
                        bot.send(
                            nickname,
                            "Your message ${reverseColor("[ID " + message.id + ']')} was sent to " +
                                "${bold(message.recipient)} on ${utcDateTime(message.receptionDate)}",
                            true
                        )
                        message.isNotified = true
                        save()
                    }
                }
        }
    }

    /**
     * Returns the messages queue size.
     *
     * @return The size.
     */
    fun size(): Int {
        return messages.size
    }

    // View all messages.
    private fun viewAll(sender: String, isPrivate: Boolean) {
        if (messages.isNotEmpty()) {
            for (message in messages) {
                bot.send(
                    sender, bold(message.sender) + ARROW + bold(message.recipient)
                        + " [ID: " + message.id + ", "
                        + (if (message.isReceived) "DELIVERED" else "QUEUED") + ']',
                    isPrivate
                )
            }
        } else {
            bot.send(sender, "There are no messages in the queue.", isPrivate)
        }
    }

    // View messages.
    private fun viewMessages(sender: String, isPrivate: Boolean) {
        var hasMessage = false
        for (message in messages) {
            if (message.isMatch(sender)) {
                if (!hasMessage) {
                    hasMessage = true
                    bot.send(sender, "Here are your messages: ", isPrivate)
                }
                if (message.isReceived) {
                    bot.send(
                        sender,
                        bold(message.sender) + ARROW + bold(message.recipient)
                            + " [" + utcDateTime(message.receptionDate) + ", ID: "
                            + bold(message.id) + ", DELIVERED]",
                        isPrivate
                    )
                } else {
                    bot.send(
                        sender,
                        bold(message.sender) + ARROW + bold(message.recipient)
                            + " [" + utcDateTime(message.queued) + ", ID: "
                            + bold(message.id) + ", QUEUED]",
                        isPrivate
                    )
                }
                bot.send(sender, helpFormat(message.message), isPrivate)
            }
        }
        if (!hasMessage) {
            bot.send(sender, "You have no messages in the queue.", isPrivate)
        } else {
            bot.send(sender, "To delete one or all delivered messages:", isPrivate)
            bot.send(
                sender,
                helpFormat(
                    buildCmdSyntax(
                        "%c $name $TELL_DEL_KEYWORD <id|$TELL_ALL_KEYWORD>",
                        bot.nick,
                        isPrivate
                    )
                ),
                isPrivate
            )
            bot.send(
                sender,
                "Messages are kept for ${bold(maxDays)}${plural(maxDays, " day.", " days.")}",
                isPrivate
            )
        }
    }

    companion object {
        /**
         * Max days property.
         */
        const val MAX_DAYS_PROP = "tell-max-days"

        /**
         * Max size proeprty.
         */
        const val MAX_SIZE_PROP = "tell-max-size"

        // Arrow
        private const val ARROW = " --> "

        // Serialized object file extension
        private const val SER_EXT = ".ser"

        // All keyword
        private const val TELL_ALL_KEYWORD = "all"

        //T he delete command.
        private const val TELL_DEL_KEYWORD = "del"
    }

    /**
     * Creates a new instance.
     */
    init {
        initProperties(MAX_DAYS_PROP, MAX_SIZE_PROP)

        // Load the message queue
        serializedObject = bot.logsDir + bot.name + SER_EXT
        messages.addAll(TellMessagesMgr.load(serializedObject, bot.logger))
        if (clean()) {
            save()
        }
    }
}
