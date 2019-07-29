/*
 * Weather2Test.java
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.msg.Message;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The <code>Weather2Test</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-04-09
 * @since 1.0
 */
public class Weather2Test extends LocalProperties {
    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    @Test
    public void testWeather() throws ModuleException {
        List<Message> messages = Weather2.getWeather("98204", LocalProperties.getProperty(Weather2.OWM_API_KEY_PROP));
        assertThat(messages.get(0).getMessage()).as("is Everett").contains("Everett");
        assertThat(messages.get(messages.size() - 1).getMessage()).as("is City Search").endsWith("98204%2CUS");

        messages = Weather2.getWeather("London, UK", LocalProperties.getProperty(Weather2.OWM_API_KEY_PROP));
        assertThat(messages.get(0).getMessage()).as("is UK").contains("UK");
        assertThat(messages.get(messages.size() - 1).getMessage()).as("is City Code").endsWith("4298960");

        try {
            Weather2.getWeather("test", "");
        } catch (Exception e) {
            assertThat(e).as("no API key").isInstanceOf(ModuleException.class);
            assertThat(e).as("no API key exception has no cause").hasNoCause();
        }

        try {
            Weather2.getWeather("", "apikey");
        } catch (Exception e) {
            assertThat(e).as("no query").isInstanceOf(ModuleException.class);
            assertThat(e).as("no query exception has no cause").hasNoCause();
        }
    }

    @Test
    public void testWeather2Impl() {
        AbstractModuleTest.testAbstractModule(new Weather2());
    }
}
