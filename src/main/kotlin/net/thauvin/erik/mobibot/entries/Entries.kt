/*
 * Entries.kt
 *
 * Copyright (c) 2004-2026 Erik C. Thauvin (erik@thauvin.net)
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

package net.thauvin.erik.mobibot.entries

import net.thauvin.erik.mobibot.Utils.today
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

/**
 * Holds [EntryLink] entries.
 *
 * Optimized for concurrent read-mostly access:
 * - Uses CopyOnWriteArrayList for links so reads are lock-free and fast.
 * - Uses AtomicReference for lastPubDate to ensure visibility with low overhead.
 *
 * If your workload is write-heavy, consider switching to a ReentrantReadWriteLock
 * protected mutable list instead.
 */
class Entries(
    var channel: String = "",
    var ircServer: String = "",
    var logsDir: String = "",
    var backlogs: String = ""
) {
    private val _links = CopyOnWriteArrayList<EntryLink>()

    // AtomicReference gives cheap thread-safe visibility semantics.
    private val lastPubDateRef = AtomicReference(today())

    var lastPubDate: String
        get() = lastPubDateRef.get()
        set(value) = lastPubDateRef.set(value)

    val links: List<EntryLink>
        get() = Collections.unmodifiableList(_links)

    fun add(link: EntryLink): Boolean = _links.add(link)

    fun clear() = _links.clear()

    fun count(): Int = _links.size

    fun findDuplicateLink(link: String): EntryLink? = _links.find { it.link == link }

    fun load() {
        val date = FeedsManager.loadFeed(this)
        lastPubDate = date
    }

    fun remove(index: Int): EntryLink = _links.removeAt(index)

    fun save() {
        lastPubDate = today()
        FeedsManager.saveFeed(this)
    }
}
