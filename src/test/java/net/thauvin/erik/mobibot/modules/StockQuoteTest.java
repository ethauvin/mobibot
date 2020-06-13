/*
 * StockQuoteTest.java
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
 * The <code>StockQuoteTest</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-04-07
 * @since 1.0
 */

public class StockQuoteTest extends LocalProperties {
    @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE")
    @SuppressWarnings("PMD.PreserveStackTrace")
    @Test
    public void testGetQuote() throws ModuleException {
        final String apiKey = LocalProperties.getProperty(StockQuote.ALPHAVANTAGE_API_KEY_PROP);
        try {
            final List<Message> messages = StockQuote.getQuote("apple inc", apiKey);
            assertThat(messages).as("response not empty").isNotEmpty();
            assertThat(messages.get(0).getMsg()).as("same stock symbol").contains("AAPL").contains("Apple Inc.");

            try {
                StockQuote.getQuote("blahfoo", apiKey);
            } catch (ModuleException e) {
                assertThat(e.getMessage()).as("invalid symbol").containsIgnoringCase(StockQuote.INVALID_SYMBOL);
            }

            assertThatThrownBy(() -> StockQuote.getQuote("test", "")).as("no API key").isInstanceOf(
                ModuleException.class).hasNoCause();

            assertThatThrownBy(() -> StockQuote.getQuote("", "apikey")).as("no symbol").isInstanceOf(
                ModuleException.class).hasNoCause();

        } catch (ModuleException e) {
            // Avoid displaying api keys in CI logs
            if ("true".equals(System.getenv("CI"))) {
                throw new ModuleException(e.getDebugMessage(), e.getSanitizedMessage(apiKey));
            } else {
                throw e;
            }
        }
    }

    @Test
    public void testStockQuoteImpl() {
        AbstractModuleTest.testAbstractModule(new StockQuote(null));
    }
}
