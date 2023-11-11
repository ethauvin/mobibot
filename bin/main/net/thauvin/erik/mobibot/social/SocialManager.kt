/*
 * SocialManager.kt
 *
 * Copyright 2004-2023 Erik C. Thauvin (erik@thauvin.net)
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

package net.thauvin.erik.mobibot.social

import net.thauvin.erik.mobibot.Addons
import net.thauvin.erik.mobibot.Constants
import net.thauvin.erik.mobibot.entries.EntriesUtils.toLinkLabel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Social Manager.
 */
class SocialManager {
    private val entries: MutableSet<Int> = HashSet()
    private val logger: Logger = LoggerFactory.getLogger(SocialManager::class.java)
    private val modules = ArrayList<SocialModule>()
    private val timer = Timer(true)

    /**
     * Adds social modules.
     */
    fun add(addons: Addons, vararg modules: SocialModule) {
        modules.forEach {
            if (addons.add(it)) {
                this.modules.add(it)
            }
        }
    }

    /**
     * Returns the number of entries.
     */
    fun entriesCount(): Int = entries.size

    /**
     * Sends a social notification (dm, etc.)
     */
    fun notification(msg: String) {
        modules.forEach {
            it.notification(msg)
        }
    }

    /**
     * Posts to social media.
     */
    fun postEntry(index: Int) {
        if (entries.contains(index)) {
            modules.forEach {
                it.postEntry(index)
            }
            entries.remove(index)
        }
    }

    /**
     * Queues an entry for posting to social media.
     */
    fun queueEntry(index: Int) {
        if (modules.isNotEmpty()) {
            entries.add(index)
            if (logger.isDebugEnabled) {
                logger.debug("Scheduling {} for posting on social media.", index.toLinkLabel())
            }
            timer.schedule(SocialTimer(this, index), Constants.TIMER_DELAY * 60L * 1000L)
        }
    }

    /**
     * Removes entries from queue.
     */
    fun removeEntry(index: Int) {
        entries.remove(index)
    }

    /**
     * Posts all entries on shutdown.
     */
    fun shutdown() {
        timer.cancel()
        entries.forEach {
            postEntry(it)
        }
    }
}
