/*
 * LinksManager.kt
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

package net.thauvin.erik.mobibot.commands.links

import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.Pinboard
import net.thauvin.erik.mobibot.Utils.bold
import net.thauvin.erik.mobibot.Utils.bot
import net.thauvin.erik.mobibot.Utils.helpFormat
import net.thauvin.erik.mobibot.Utils.sendMessage
import net.thauvin.erik.mobibot.Utils.today
import net.thauvin.erik.mobibot.commands.AbstractCommand
import net.thauvin.erik.mobibot.commands.Ignore.Companion.isNotIgnored
import net.thauvin.erik.mobibot.entries.Entries
import net.thauvin.erik.mobibot.entries.EntriesUtils.printLink
import net.thauvin.erik.mobibot.entries.EntriesUtils.toLinkLabel
import net.thauvin.erik.mobibot.entries.EntryLink
import net.thauvin.erik.mobibot.social.SocialManager
import org.jsoup.Jsoup
import org.pircbotx.hooks.types.GenericMessageEvent
import java.io.IOException

/**
 * Processes a URL, fetch its metadata, and register it with associated details.
 *
 * It checks for duplicate entries, retrieves or assigns a title, associates tags, and adds the URL to a collection of
 * entries for further processing.
 */
class LinksManager : AbstractCommand() {
    private val defaultTags: MutableList<String> = mutableListOf()
    private val keywords: MutableList<String> = mutableListOf()

    override val name = Constants.LINK_CMD
    override val isOpOnly = false
    override val isPublic = false
    override val isVisible = false

    companion object {
        const val KEYWORDS_PROP = "tags-keywords"
        const val TAGS_PROP = "tags"

        @JvmStatic
        val LINK_MATCH by lazy { "^[hH][tT][tT][pP](|[sS])://.*".toRegex() }

        @JvmStatic
        val TAG_MATCH by lazy { ", *| +".toRegex() }

        /**
         * Entries array
         */
        @JvmField
        val entries = Entries()

        /**
         * Pinboard handler.
         */
        @JvmField
        val pinboard = Pinboard()

        /**
         * Social Manager handler.
         */
        @JvmField
        val socialManager = SocialManager()

        /**
         * Let the user know if the entries are too old to be modified.
         */
        @JvmStatic
        fun isUpToDate(event: GenericMessageEvent): Boolean {
            if (entries.lastPubDate != today()) {
                event.sendMessage("The links are too old to be updated.")
                return false
            }
            return true
        }
    }

    init {
        initProperties(TAGS_PROP, KEYWORDS_PROP)
    }

    override fun commandResponse(channel: String, args: String, event: GenericMessageEvent) {
        val cmds = args.split(" ".toRegex(), 2)
        val sender = event.user.nick
        val botNick = event.bot().nick
        val login = event.user.login

        if (isNotIgnored(sender) && (cmds.size == 1 || !cmds[1].contains(botNick))) {
            val link = cmds[0].trim()
            if (!isDupEntry(link, event)) {
                var title = ""
                val tags = ArrayList<String>(defaultTags)
                if (cmds.size == 2) {
                    val data = cmds[1].trim().split("${Tags.COMMAND}:", limit = 2)
                    title = data[0].trim()
                    if (data.size > 1) {
                        tags.addAll(data[1].split(TAG_MATCH))
                    }
                }

                if (title.isBlank()) {
                    title = fetchPageTitle(link)
                }

                if (title != Constants.NO_TITLE) {
                    // Add keywords as tags if found in the title
                    matchTagKeywords(title, tags)
                }

                // Links are old, clear them
                if (entries.lastPubDate != today()) {
                    entries.clear()
                }

                val entry = EntryLink(link, title, sender, login, channel, tags)
                entries.add(entry)
                val index = entries.links.lastIndexOf(entry)
                event.sendMessage(printLink(index, entry))

                pinboard.addPin(event.bot().serverHostname, entry)

                // Queue the entry for posting to social media.
                socialManager.queueEntry(index)

                entries.save()

                if (Constants.NO_TITLE == entry.title) {
                    event.sendMessage("Please specify a title, by typing:")
                    event.sendMessage(helpFormat("${index.toLinkLabel()}:|This is the title"))
                }
            }
        }
    }

    override fun helpResponse(channel: String, topic: String, event: GenericMessageEvent): Boolean = false

    override fun matches(message: String): Boolean {
        return message.matches(LINK_MATCH)
    }

    /**
     * Fetches and returns the page title of the given URL.
     *
     * If the title cannot be fetched or is blank, [Constants.NO_TITLE] is returned.
     */
    internal fun fetchPageTitle(link: String): String {
        try {
            val html = Jsoup.connect(link)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/74.0")
                .get()
            val title = html.title()
            if (title.isNotBlank()) {
                return title
            }
        } catch (_: IOException) {
            // Do nothing
        }
        return Constants.NO_TITLE
    }

    private fun isDupEntry(link: String, event: GenericMessageEvent): Boolean {
        val match = entries.findDuplicateLink(link)
        return if (match != null) {
            event.sendMessage(
                "Duplicate".bold() + " >> " + printLink(entries.links.indexOf(match), match)
            )
            true
        } else {
            false
        }
    }

    /**
     * Matches [keywords] in the given title and adds them to the provided tag list.
     */
    internal fun matchTagKeywords(title: String, tags: MutableList<String>) {
        for (match in keywords) {
            val m = Regex.escape(match)
            if (title.matches("(?i).*\\b$m\\b.*".toRegex())) {
                tags.add(match)
            }
        }
    }

    override fun setProperty(key: String, value: String) {
        super.setProperty(key, value)
        if (KEYWORDS_PROP == key) {
            keywords.addAll(value.split(TAG_MATCH))
        } else if (TAGS_PROP == key) {
            defaultTags.addAll(value.split(TAG_MATCH))
        }
    }
}
