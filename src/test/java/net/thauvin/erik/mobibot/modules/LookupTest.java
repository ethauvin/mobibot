/*
 * LookupTest.java
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

import org.testng.annotations.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The <code>Lookup Test</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2017-05-30
 * @since 1.0
 */
public class LookupTest {
    @Test
    public void testLookupImpl() {
        AbstractModuleTest.testAbstractModule(new Lookup());
    }

    @Test
    public void testLookup() throws Exception {
        final String result = Lookup.lookup("erik.thauvin.net");
        assertThat(result).as("lookup(erik.thauvin.net/104.31.77.12)").contains("104.31.77.12");
    }

    @Test
    public void testWhois() throws Exception {
        final String[] result = Lookup.whois("17.178.96.59", Lookup.WHOIS_HOST);

        assertThat(Arrays.stream(result).anyMatch(m -> m.contains("Apple Inc.")))
            .as("whois(17.178.96.59/Apple Inc.").isTrue();
    }
}
