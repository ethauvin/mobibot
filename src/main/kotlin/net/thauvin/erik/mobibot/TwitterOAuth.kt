/*
 * TwitterOAuth.kt
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

import twitter4j.AccessToken
import twitter4j.OAuthAuthorization
import twitter4j.TwitterException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.exitProcess


/**
 * The `TwitterOAuth` class.
 *
 * Go to [https://developer.twitter.com/en/apps](https://developer.twitter.com/en/apps) to register your bot.
 *
 * Then execute:
 *
 * `java -cp mobibot.jar net.thauvin.erik.mobibot.TwitterOAuth <consumerKey> <consumerSecret>`
 *
 * and follow the prompts/instructions.
 *
 * @author [Erik C. Thauvin](https://erik.thauvin.net)
 * @author [Yusuke Yamamoto](https://github.com/Twitter4J/Twitter4J/blob/main/twitter4j-examples/src/main/java/examples/oauth/GetAccessToken.java)
 */
object TwitterOAuth {
    /**
     * Twitter OAuth Client Registration.
     *
     * @param args The consumerKey and consumerSecret should be passed as arguments.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size == 2) {
            try {
                val oAuthAuthorization = OAuthAuthorization.getInstance(args[0], args[1])
                val requestToken = oAuthAuthorization.oAuthRequestToken
                var accessToken: AccessToken? = null
                val br = BufferedReader(InputStreamReader(System.`in`))
                while (null == accessToken) {
                    print(
                        """
                        Open the following URL and grant access to your account:
                    
                        ${requestToken.authorizationURL}

                        Enter the PIN (if available) or just hit enter. [PIN]: """.trimIndent()
                    )
                    val pin = br.readLine()
                    try {
                        accessToken = if (!pin.isNullOrEmpty()) {
                            oAuthAuthorization.getOAuthAccessToken(requestToken, pin)
                        } else {
                            oAuthAuthorization.getOAuthAccessToken(requestToken)
                        }
                    } catch (te: TwitterException) {
                        if (401 == te.statusCode) {
                            println("Unable to get the access token.")
                        } else {
                            te.printStackTrace()
                        }
                    }
                }
                println(
                    """
                    Please add the following to the bot's property file:
                    
                    twitter-consumerKey=${args[0]}
                    twitter-consumerSecret=${args[1]}
                    twitter-token=${accessToken.token}
                    twitter-tokenSecret=${accessToken.tokenSecret}
                    """.trimIndent()
                )
            } catch (te: TwitterException) {
                te.printStackTrace()
                println("Failed to get accessToken: " + te.message)
                exitProcess(-1)
            } catch (ioe: IOException) {
                ioe.printStackTrace()
                println("Failed to read the system input.")
                exitProcess(-1)
            }
        } else {
            println("Usage: ${TwitterOAuth::class.java.name} <consumerKey> <consumerSecret>")
        }
        exitProcess(0)
    }
}
