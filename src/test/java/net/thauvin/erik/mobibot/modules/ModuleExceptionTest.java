/*
 * ModuleExceptionTest.java
 *
 * Copyright (c) 2004-2019, Erik C. Thauvin (erik@thauvin.net)
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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The <code>ModuleExceptionTest</code> class.
 */
public class ModuleExceptionTest {
    static final String debugMessage = "debugMessage";
    static final String message = "message";

    @DataProvider(name = "dp")
    Object[][] createData(final Method m) {
        return new Object[][]{ new Object[]{ new ModuleException(debugMessage,
                                                                 message,
                                                                 new IOException("URL http://foobar.com")) },
                               new Object[]{ new ModuleException(debugMessage,
                                                                 message,
                                                                 new IOException("URL http://foobar.com?")) },
                               new Object[]{ new ModuleException(debugMessage, message) } };
    }

    @Test(dataProvider = "dp")
    final void testGetDebugMessage(final ModuleException e) {
        assertThat(e.getDebugMessage()).as("get debug message").isEqualTo(debugMessage);
    }

    @Test(dataProvider = "dp")
    final void testGetMessage(final ModuleException e) {
        assertThat(e.getMessage()).as("get message").isEqualTo(message);
    }

    @Test
    final void testGetSanitizedMessage() {
        final String apiKey = "1234567890";
        final ModuleException e = new ModuleException(debugMessage,
                                                      message,
                                                      new IOException(
                                                              "URL http://foo.com?apiKey=" + apiKey + "&userID=me"));
        assertThat(e.getSanitizedMessage(apiKey)).as("sanitized url").contains("xxxxxxxxxx").doesNotContain(apiKey);
    }
}
