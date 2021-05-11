/*
 * Weather2Test.kt
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
package net.thauvin.erik.mobibot.modules

import net.aksingh.owmjapis.api.APIException
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.modules.Weather2.Companion.getWeather
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.testng.annotations.Test

/**
 * The `Weather2Test` class.
 */
class Weather2Test : LocalProperties() {
    @Test
    @Throws(ModuleException::class)
    fun testWeather() {
        var messages = getWeather("98204", getProperty(Weather2.OWM_API_KEY_PROP))
        assertThat(messages[0].msg).describedAs("is Everett").contains("Everett").contains("US")
        assertThat(messages[messages.size - 1].msg).describedAs("is City Search").endsWith("98204%2CUS")
        messages = getWeather("London, UK", getProperty(Weather2.OWM_API_KEY_PROP))
        assertThat(messages[0].msg).describedAs("is UK").contains("London").contains("UK")
        assertThat(messages[messages.size - 1].msg).describedAs("is City Code").endsWith("4517009")
        assertThatThrownBy { getWeather("Montpellier, FR", getProperty(Weather2.OWM_API_KEY_PROP)) }
            .describedAs("Montpellier not found").hasCauseInstanceOf(APIException::class.java)
        assertThatThrownBy { getWeather("test", "") }
            .describedAs("no API key").isInstanceOf(ModuleException::class.java).hasNoCause()
        messages = getWeather("", "apikey")
        assertThat(messages[0].isError).describedAs("no query").isTrue
    }
}
