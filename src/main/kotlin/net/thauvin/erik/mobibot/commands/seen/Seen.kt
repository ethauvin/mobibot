/*
 * Seen.kt
 *
 * Copyright (c) 2004-2022, Erik C. Thauvin (erik@thauvin.net)
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

package net.thauvin.erik.mobibot.commands.seen

import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.loadData
import net.thauvin.erik.mobibot.Utils.saveData
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.Info.Companion.toUptime
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Seen(private val serialObject: String) : AbstractCommand() {
    private val logger: Logger = LoggerFactory.getLogger(Seen::class.java)
    val seenNicks: MutableList<SeenNick> = mutableListOf()

    override val name = "seen"
    override val help = listOf("To view when a nickname was last seen:", helpFormat("%c $name <nick>"))
    override val isOpOnly = false
    override val isPublic = true
    override val isVisible = true

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        if (isEnabled()) {
            if (args.isNotBlank() && !args.contains(' ')) {
                val ch = event.bot().userChannelDao.getChannel(channel)
                ch.users.forEach {
                    if (args.equals(it.nick, true)) {
                        event.sendMessage("${it.nick} is on ${channel}.")
                        return
                    }
                }
                seenNicks.forEach {
                    if (it.nick.equals(args, true)) {
                        val lastSeen = System.currentTimeMillis() - it.last
                        event.sendMessage("${it.nick} was last seen on $channel ${lastSeen.toUptime()} ago.")
                        return
                    }
                }
                event.sendMessage("I haven't seen $args on $channel lately.")
            } else {
                helpResponse(channel, args, event)
            }
        }
    }

    fun add(nick: String) {
        if (isEnabled()) {
            seenNicks.forEach {
                if (it.nick.equals(nick, true)) {
                    if (it.nick != nick) it.nick = nick
                    it.last = System.currentTimeMillis()
                    save()
                    return
                }
            }
            seenNicks.add(SeenNick(nick))
            save()
        }
    }

    fun clear() {
        seenNicks.clear()
    }

    fun load() {
        if (isEnabled()) {
            @Suppress("UNCHECKED_CAST")
            seenNicks += loadData(
                serialObject,
                mutableListOf<SeenNick>(),
                logger,
                "seen nicknames"
            ) as MutableList<SeenNick>
        }
    }

    fun save() {
        saveData(serialObject, seenNicks, logger, "seen nicknames")
    }

    init {
        load()
    }
}
