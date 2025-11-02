/*
 * Tell.kt
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpCmdSyntax
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.isChannelOp
import net.thauvin.erik.mobibot.Utils.plural
import net.thauvin.erik.mobibot.Utils.reverseColor
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.Utils.toIntOrDefault
import net.thauvin.erik.mobibot.Utils.toUtcDateTime
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.links.View
import org.pircbotx.PircBotX
import org.pircbotx.hooks.events.MessageEvent
import org.pircbotx.hooks.types.GenericMessageEvent
import org.pircbotx.hooks.types.GenericUserEvent

/**
 * Queues a message to be sent to someone when they join or are active on the channel.
 */
@SuppressFBWarnings("BC_BAD_CAST_TO_ABSTRACT_COLLECTION")
class Tell(private val serialObject: String) : AbstractCommand() {
    // Messages queue
    private val messages: MutableList<TellMessage> = mutableListOf()

    // Maximum number of days to keep messages
    private var maxDays = 7

    // Message maximum queue size
    private var maxSize = 50

    /**
     * The tell command.
     */
    override val name = "tell"

    override val isOpOnly: Boolean = false
    override val isPublic: Boolean = isEnabled()
    override val isVisible: Boolean = isEnabled()

    init {
        addHelp(
            "To send a message to someone when they join the channel:",
            helpFormat("%c $name <nick> <message>"),
            "To view queued and sent messages:",
            helpFormat("%c $name ${View.VIEW_CMD}"),
            "Messages are kept for ${maxDays.bold()}" + " day".plural(maxDays.toLong()) + '.'
        )
    }

    /**
     * Cleans the messages queue.
     */
    private fun clean(): Boolean {
        return TellManager.clean(messages, maxDays.toLong())
    }

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        if (isEnabled()) {
            when {
                args.isBlank() -> {
                    helpResponse(channel, args, event)
                }

                args.startsWith(View.VIEW_CMD) -> {
                    if (event.isChannelOp(channel) && "${View.VIEW_CMD} $TELL_ALL_KEYWORD" == args) {
                        viewAll(event)
                    } else {
                        viewMessages(event)
                    }
                }

                args.startsWith("$TELL_DEL_KEYWORD ") -> {
                    deleteMessage(channel, args, event)
                }

                else -> {
                    newMessage(channel, args, event)
                }
            }
            if (clean()) {
                save()
            }
        }
    }

    // Delete message.
    private fun deleteMessage(channel: String, args: String, event: GenericMessageEvent) {
        val split = args.split(" ")
        if (split.size == 2) {
            val id = split[1]
            if (TELL_ALL_KEYWORD.equals(id, ignoreCase = true)) {
                if (messages.removeIf { it.sender.equals(event.user.nick, true) && it.isReceived }) {
                    save()
                    event.sendMessage("Delivered messages have been deleted.")
                } else {
                    event.sendMessage("No delivered messages were found.")
                }
            } else {
                if (messages.removeIf {
                        it.id == id && (it.sender.equals(event.user.nick, true)
                                || event.isChannelOp(channel))
                    }) {
                    save()
                    event.sendMessage("The message was deleted from the queue.")
                } else {
                    event.sendMessage("The specified message [ID $id] could not be found.")
                }
            }
        } else {
            helpResponse(channel, args, event)
        }
    }

    override fun isEnabled(): Boolean {
        return maxSize > 0 && maxDays > 0
    }

    override fun setProperty(key: String, value: String) {
        super.setProperty(key, value)
        if (MAX_DAYS_PROP == key) {
            maxDays = value.toIntOrDefault(maxDays)
        } else if (MAX_SIZE_PROP == key) {
            maxSize = value.toIntOrDefault(maxSize)
        }
    }

    // New message.
    private fun newMessage(channel: String, args: String, event: GenericMessageEvent) {
        val split = args.split(" ".toRegex(), 2)
        if (split.size == 2 && split[1].isNotBlank() && split[1].contains(" ")) {
            if (messages.size < maxSize) {
                val message = TellMessage(event.user.nick, split[0], split[1].trim())
                messages.add(message)
                save()
                event.sendMessage("Message [ID ${message.id}] was queued for ${message.recipient.bold()}")
            } else {
                event.sendMessage("Sorry, the messages queue is currently full.")
            }
        } else {
            helpResponse(channel, args, event)
        }
    }

    /**
     * Saves the messages queue.
     */
    private fun save() {
        TellManager.save(serialObject, messages)
    }

    /**
     * Checks and sends messages.
     */
    fun send(event: GenericUserEvent) {
        val nickname = event.user.nick
        if (!isEnabled() || nickname == event.getBot<PircBotX>().nick) return

        messages.filter { it.isMatch(nickname) }.forEach { message ->
            when {
                // Deliver a message to the recipient
                message.recipient.equals(nickname, ignoreCase = true) && !message.isReceived -> {
                    val messageText = if (message.sender == nickname) {
                        // Self-reminder (but not from MessageEvent to avoid loops)
                        if (event is MessageEvent) return@forEach
                        "${"You".bold()} wanted me to remind you: ${message.message.reverseColor()}"
                    } else {
                        // Message from someone else
                        "${message.sender} wanted me to tell you: ${message.message.reverseColor()}"
                    }

                    event.user.send().message(messageText)
                    message.isReceived = true
                    if (message.sender == nickname) message.isNotified = true
                    save()
                }

                // Notify sender that message was delivered
                message.sender.equals(nickname, ignoreCase = true) && message.isReceived
                        && !message.isNotified -> {
                    event.user.send().message(
                        "Your message ${"[ID ${message.id}]".reverseColor()} was sent to " +
                                "${message.recipient.bold()} on ${message.receptionDate}"
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
    fun size(): Int = messages.size

    // View all messages.
    private fun viewAll(event: GenericMessageEvent) {
        if (messages.isNotEmpty()) {
            for (message in messages) {
                event.sendMessage(
                    "${message.sender.bold()}$ARROW${message.recipient.bold()} [ID: ${message.id}, " +
                            (if (message.isReceived) "DELIVERED]" else "QUEUED]")
                )
            }
        } else {
            event.sendMessage("There are no messages in the queue.")
        }
    }

    // View messages.
    private fun viewMessages(event: GenericMessageEvent) {
        var hasMessage = false
        for (message in messages.filter { it.isMatch(event.user.nick) }) {
            if (!hasMessage) {
                hasMessage = true
                event.sendMessage("Here are your messages: ")
            }
            if (message.isReceived) {
                event.sendMessage(
                    message.sender.bold() + ARROW + message.recipient.bold() +
                            " [${message.receptionDate.toUtcDateTime()}, ID: ${message.id.bold()}, DELIVERED]"
                )
            } else {
                event.sendMessage(
                    message.sender.bold() + ARROW + message.recipient.bold() +
                            " [${message.queued.toUtcDateTime()}, ID: ${message.id.bold()}, QUEUED]"
                )
            }
            event.sendMessage(helpFormat(message.message))
        }
        if (!hasMessage) {
            event.sendMessage("You have no messages in the queue.")
        } else {
            event.sendMessage("To delete one or all delivered messages:")
            event.sendMessage(
                helpFormat(
                    helpCmdSyntax(
                        "%c $name $TELL_DEL_KEYWORD <id|$TELL_ALL_KEYWORD>",
                        event.bot().nick, true
                    )
                )
            )
            event.sendMessage(help.last())
        }
    }

    companion object {
        /**
         * Max number of days property.
         */
        const val MAX_DAYS_PROP = "tell-max-days"

        /**
         * Max size property.
         */
        const val MAX_SIZE_PROP = "tell-max-size"

        // Arrow
        private const val ARROW = " --> "

        // The `all` keyword
        private const val TELL_ALL_KEYWORD = "all"

        // The delete command.
        private const val TELL_DEL_KEYWORD = "del"
    }

    /**
     * Creates a new instance.
     */
    init {
        initProperties(MAX_DAYS_PROP, MAX_SIZE_PROP)

        // Load the message queue
        messages.addAll(TellManager.load(serialObject))
        if (clean()) {
            save()
        }
    }
}
