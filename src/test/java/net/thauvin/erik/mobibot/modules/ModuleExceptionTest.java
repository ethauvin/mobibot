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
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-04-09
 * @since 1.0
 */
public class ModuleExceptionTest {
    @DataProvider(name = "dp")
    Object[][] createData(Method m) {
        System.out.println(m.getName());  // print test method name
        return new Object[][]{new Object[]{new ModuleException("debugMessage", "message",
            new IOException("Secret URL http://foo.com?apiKey=sec&userID=me"))},
            new Object[]{new ModuleException("debugMessage", "message")}
        };
    }

    @Test(dataProvider = "dp")
    final void testGetDebugMessage(ModuleException e) {
        assertThat(e.getDebugMessage()).as("get debug message").isEqualTo("debugMessage");
    }

    @Test(dataProvider = "dp")
    final void testGetMessage(ModuleException e) {
        assertThat(e.getMessage()).as("get message").isEqualTo("message");
    }

    @Test(dataProvider = "dp")
    final void testGetStanitizedMessage(ModuleException e) {
        if (e.hasCause()) {
            assertThat(e.getSanitizedMessage()).as("get sanitzed url")
                .contains("http://foo.com?apiKey=[3]&userID=[2]");
        }
    }
}
