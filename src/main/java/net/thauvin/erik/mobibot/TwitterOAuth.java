/*
 * TwitterOAuth.java
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

package net.thauvin.erik.mobibot;

import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The <code>TwitterOAuth</code> class.
 * <p>
 * Go to <a href="http://twitter.com/oauth_clients/new">http://twitter.com/oauth_clients/new</a> to register your bot.
 * </p>
 * Then execute:
 * <p>
 * <code>
 * java -cp "mobibot.jar:lib/*" net.thauvin.erik.mobibot.TwitterOAuth &lt;consumerKey&gt; &lt;consumerSecret&gt;
 * </code>
 * </p>
 * and follow the prompts/instructions.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @author <a href="http://twitter4j.org/en/code-examples.html#oauth" target="_blank">Twitter4J</a>
 * @created Sep 13, 2010
 * @since 1.0
 */
@SuppressWarnings("PMD.UseUtilityClass")
public final class TwitterOAuth {
    /**
     * Twitter OAuth Client Registration.
     *
     * @param args The consumerKey and consumerSecret should be passed as arguments.
     * @throws TwitterException If an error occurs.
     * @throws IOException      If an IO error occurs.
     */
    @SuppressWarnings({ "PMD.SystemPrintln" })
    public static void main(final String[] args) throws TwitterException, IOException {
        if (args.length == 2) {
            final twitter4j.Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(args[0], args[1]);
            final RequestToken requestToken = twitter.getOAuthRequestToken();
            AccessToken accessToken = null;
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                while (null == accessToken) {
                    System.out.println("Open the following URL and grant access to your account:");
                    System.out.println(requestToken.getAuthorizationURL());
                    System.out.print("Enter the PIN (if available) or just hit enter.[PIN]:");
                    final String pin = br.readLine();
                    try {
                        if (pin != null && pin.length() > 0) {
                            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                        } else {
                            accessToken = twitter.getOAuthAccessToken();
                        }

                        System.out.println(
                                "Please add the following to the bot's property file:" + "\n\n" + "twitter-consumerKey="
                                + args[0] + '\n' + "twitter-consumerSecret=" + args[1] + '\n' + "twitter-token="
                                + accessToken.getToken() + '\n' + "twitter-tokenSecret=" + accessToken
                                                                                                   .getTokenSecret());
                    } catch (TwitterException te) {
                        if (401 == te.getStatusCode()) {
                            System.out.println("Unable to get the access token.");
                        } else {
                            te.printStackTrace();
                        }
                    }
                }
            }
        } else {
            System.out.println("Usage: " + TwitterOAuth.class.getName() + " <consumerKey> <consumerSecret>");
        }

        System.exit(0);
    }
}
