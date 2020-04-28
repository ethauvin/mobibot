/*
 * GoogleSearchTest.java
 *
 * Copyright (c) 2004-2020, Erik C. Thauvin (erik@thauvin.net)
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

package net.thauvin.erik.mobibot.modules;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.msg.Message;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The <code>GoogleSearchTest</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-04-08
 * @since 1.0
 */
public class GoogleSearchTest extends LocalProperties {
    @Test
    public void testGoogleSearchImpl() {
        AbstractModuleTest.testAbstractModule(new GoogleSearch());
    }

    @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE")
    @SuppressWarnings("PMD.PreserveStackTrace")
    @Test
    public void testSearchGoogle() throws ModuleException {
        final String apiKey = getProperty(GoogleSearch.GOOGLE_API_KEY_PROP);
        final String cseKey = getProperty(GoogleSearch.GOOGLE_CSE_KEY_PROP);
        try {
            List<Message> messages = GoogleSearch.searchGoogle("mobibot site:github.com", apiKey, cseKey);
            assertThat(messages).as("mobibot results not empty").isNotEmpty();
            assertThat(messages.get(0).getText()).as("found mobitopia").contains("mobibot");

            messages = GoogleSearch.searchGoogle("aapl", apiKey, cseKey);
            assertThat(messages).as("aapl results not empty").isNotEmpty();
            assertThat(messages.get(0).getText()).as("found apple").containsIgnoringCase("apple");

            assertThatThrownBy(() -> GoogleSearch.searchGoogle("test", "", "apiKey")).as("no API key").isInstanceOf(
                    ModuleException.class).hasNoCause();

            assertThatThrownBy(() -> GoogleSearch.searchGoogle("test", "apiKey", "")).as("no CSE API key").isInstanceOf(
                    ModuleException.class).hasNoCause();

            assertThatThrownBy(() -> GoogleSearch.searchGoogle("", "apikey", "apiKey")).as("no query").isInstanceOf(
                    ModuleException.class).hasNoCause();
        } catch (ModuleException e) {
            // Avoid displaying api keys in CI logs
            if ("true".equals(System.getenv("CI"))) {
                throw new ModuleException(e.getDebugMessage(), e.getSanitizedMessage(apiKey, cseKey));
            } else {
                throw e;
            }
        }
    }
}
