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
import net.aksingh.owmjapis.core.OWM
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.modules.Weather2.Companion.OWM_API_KEY_PROP
import net.thauvin.erik.mobibot.modules.Weather2.Companion.ftoC
import net.thauvin.erik.mobibot.modules.Weather2.Companion.getCountry
import net.thauvin.erik.mobibot.modules.Weather2.Companion.getWeather
import net.thauvin.erik.mobibot.modules.Weather2.Companion.mphToKmh
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.testng.annotations.Test
import kotlin.random.Random

/**
 * The `Weather2Test` class.
 */
class Weather2Test : LocalProperties() {
    @Test
    fun testFtoC() {
        val t = ftoC(32.0)
        assertThat(t.second).describedAs("32 °F is 0 °C").isEqualTo(0)
    }

    @Test
    fun testGetCountry() {
        assertThat(getCountry("foo")).describedAs("not a country").isEqualTo(OWM.Country.UNITED_STATES)
        assertThat(getCountry("fr")).describedAs("fr is france").isEqualTo(OWM.Country.FRANCE)

        val country = OWM.Country.values()
        repeat(3) {
            val rand = country[Random.nextInt(0, country.size - 1)]
            assertThat(getCountry(rand.value)).describedAs(rand.name).isEqualTo(rand)
        }
    }

    @Test
    fun testMphToKmh() {
        val w = mphToKmh(0.62)
        assertThat(w.second).describedAs("0.62 mph is 1 km/h").isEqualTo(1)
    }

    @Test
    @Throws(ModuleException::class)
    fun testWeather() {
        var messages = getWeather("98204", getProperty(OWM_API_KEY_PROP))
        assertThat(messages[0].msg).describedAs("is Everett").contains("Everett").contains("US")
        assertThat(messages[messages.size - 1].msg).describedAs("is Everett zip code").endsWith("98204%2CUS")

        messages = getWeather("San Francisco", getProperty(OWM_API_KEY_PROP))
        assertThat(messages[0].msg).describedAs("is San Francisco").contains("San Francisco").contains("US")
        assertThat(messages[messages.size - 1].msg).describedAs("is San Fran city code").endsWith("5391959")

        messages = getWeather("London, UK", getProperty(OWM_API_KEY_PROP))
        assertThat(messages[0].msg).describedAs("is UK").contains("London").contains("UK")
        assertThat(messages[messages.size - 1].msg).describedAs("is London city code").endsWith("4517009")

        assertThatThrownBy { getWeather("Foo, US", getProperty(OWM_API_KEY_PROP)) }
            .describedAs("foo not found").hasCauseInstanceOf(APIException::class.java)
        assertThatThrownBy { getWeather("test", "") }
            .describedAs("no API key").isInstanceOf(ModuleException::class.java).hasNoCause()
        assertThatThrownBy { getWeather("test", null) }
            .describedAs("null API key").isInstanceOf(ModuleException::class.java).hasNoCause()

        messages = getWeather("", "apikey")
        assertThat(messages[0].isError).describedAs("no query").isTrue
    }
}
