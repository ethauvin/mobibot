/*
 * LocalProperties.kt
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
package net.thauvin.erik.mobibot

import org.testng.annotations.BeforeSuite
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Access to `local.properties`.
 */
open class LocalProperties {
    @BeforeSuite(alwaysRun = true)
    fun loadProperties() {
        val localPath = Paths.get("local.properties")
        if (Files.exists(localPath)) {
            try {
                Files.newInputStream(localPath).use { stream -> localProps.load(stream) }
            } catch (ignore: IOException) {
                // Do nothing
            }
        }
    }

    companion object {
        private val localProps = Properties()

        fun getHostName(): String {
            val ciName = System.getenv("CI_NAME")
            return ciName ?: try {
                InetAddress.getLocalHost().hostName
            } catch (ignore: UnknownHostException) {
                "Unknown Host"
            }
        }

        fun getProperty(key: String): String {
            return if (localProps.containsKey(key)) {
                localProps.getProperty(key)
            } else {
                val env = System.getenv(keyToEnv(key))
                env?.let {
                    localProps.setProperty(key, env)
                }
                env
            }
        }

        private fun keyToEnv(key: String): String {
            return key.replace('-', '_').uppercase()
        }
    }
}
