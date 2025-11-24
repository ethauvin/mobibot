/*
 * Seen.kt
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

package net.thauvin.erik.mobibot.commands.seen

import com.google.common.collect.ImmutableSortedSet
import net.thauvin.erik.mobibot.Utils
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.isChannelOp
import net.thauvin.erik.mobibot.Utils.loadSerialData
import net.thauvin.erik.mobibot.Utils.saveSerialData
import net.thauvin.erik.mobibot.Utils.sendList
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.Info.Companion.toUptime
import org.pircbotx.User
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Displays when a user was last seen, or all seen nicks.
 */
class Seen(private val serialObject: String) : AbstractCommand() {
    private val logger: Logger = LoggerFactory.getLogger(Seen::class.java)
    private val seenNicks = TreeMap<String, SeenNick>(NickComparator())

    override val name = "seen"
    private val helpOp = help.plus(
        arrayOf("To view all ${"seen".bold()} nicks:", helpFormat("%c $name $ALL_KEYWORD"))
    )
    override val isOpOnly = false
    override val isPublic = true
    override val isVisible = true

    init {
        addHelp("To view when a nickname was last seen:", helpFormat("%c $name <nick>"))
        load()
    }

    companion object {
        private const val ALL_KEYWORD = "all"
    }

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        if (isEnabled()) {
            if (args.isNotBlank() && !args.contains(' ')) {
                val ch = event.bot().userChannelDao.getChannel(channel)
                if (args == ALL_KEYWORD && ch.isOp(event.user) && seenNicks.isNotEmpty()) {
                    event.sendMessage("The ${"seen".bold()} nicks are:")
                    event.sendList(seenNicks.keys.toList(), 7, separator = ", ", isIndent = true)
                    return
                }
                ch.users.forEach {
                    if (args.equals(it.nick, true)) {
                        event.sendMessage("${it.nick} is on ${channel}.")
                        return
                    }
                }
                if (seenNicks.containsKey(args)) {
                    val seenNick = seenNicks.getValue(args)
                    val lastSeen = System.currentTimeMillis() - seenNick.lastSeen
                    event.sendMessage("${seenNick.nick} was last seen on $channel ${lastSeen.toUptime()} ago.")
                    return
                }
                event.sendMessage("I haven't seen $args on $channel lately.")
            } else {
                helpResponse(channel, args, event)
            }
        }
    }

    fun add(nick: String) {
        if (isEnabled()) {
            seenNicks[nick] = SeenNick(nick, System.currentTimeMillis())
            save()
        }
    }

    fun add(users: ImmutableSortedSet<User>) {
        if (isEnabled()) {
            users.forEach {
                seenNicks[it.nick] = SeenNick(it.nick, System.currentTimeMillis())
            }
            save()
        }
    }

    fun clear() {
        seenNicks.clear()
    }

    fun count(): Int = seenNicks.size

    override fun helpResponse(channel: String, topic: String, event: GenericMessageEvent): Boolean {
        return if (event.isChannelOp(channel)) {
            for (h in helpOp) {
                event.sendMessage(Utils.helpCmdSyntax(h, event.bot().nick, true))
            }
            true
        } else {
            super.helpResponse(channel, topic, event)
        }
    }

    // Provide read-only access via a getter that returns a copy
    fun getSeenNicks(): Map<String, SeenNick> {
        return seenNicks.toMap() // Immutable snapshot
    }

    fun getSeenNick(nick: String): SeenNick? {
        return seenNicks[nick]
    }

    fun load() {
        if (isEnabled()) {
            @Suppress("UNCHECKED_CAST")
            seenNicks.putAll(
                loadSerialData(
                    serialObject,
                    TreeMap<String, SeenNick>(),
                    logger,
                    "seen nicknames"
                ) as TreeMap<String, SeenNick>
            )
        }
    }

    fun save() {
        saveSerialData(serialObject, seenNicks, logger, "seen nicknames")
    }
}
