/*
 * Weather2Test.kt
 *
 * Copyright (c) 2004-2022, Erik C. Thauvin (erik@thauvin.net)
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

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.endsWith
import assertk.assertions.hasNoCause
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import net.aksingh.owmjapis.api.APIException
import net.aksingh.owmjapis.core.OWM
import net.thauvin.erik.mobibot.LocalProperties
import net.thauvin.erik.mobibot.modules.Weather2.Companion.OWM_API_KEY_PROP
import net.thauvin.erik.mobibot.modules.Weather2.Companion.ftoC
import net.thauvin.erik.mobibot.modules.Weather2.Companion.getCountry
import net.thauvin.erik.mobibot.modules.Weather2.Companion.getWeather
import net.thauvin.erik.mobibot.modules.Weather2.Companion.mphToKmh
import net.thauvin.erik.mobibot.msg.Message
import org.testng.annotations.Test

/**
 * The `Weather2Test` class.
 */
class Weather2Test : LocalProperties() {
    @Test(groups = ["modules"])
    fun testFtoC() {
        val t = ftoC(32.0)
        assertThat(t.second, "32 °F is 0 °C").isEqualTo(0)
    }

    @Test(groups = ["modules"])
    fun testGetCountry() {
        assertThat(getCountry("foo"), "foo is not a valid country").isEqualTo(OWM.Country.UNITED_STATES)
        assertThat(getCountry("fr"), "country should France").isEqualTo(OWM.Country.FRANCE)

        val country = OWM.Country.values()
        repeat(3) {
            val rand = country[(country.indices).random()]
            assertThat(getCountry(rand.value), rand.name).isEqualTo(rand)
        }
    }

    @Test(groups = ["modules"])
    fun testMphToKmh() {
        val w = mphToKmh(0.62)
        assertThat(w.second, "0.62 mph is 1 km/h").isEqualTo(1)
    }

    @Test(groups = ["modules"])
    @Throws(ModuleException::class)
    fun testWeather() {
        var query = "98204"
        var messages = getWeather(query, getProperty(OWM_API_KEY_PROP))
        assertThat(messages, "getWeather($query)").index(0).prop(Message::msg).all {
            contains("Everett, United States")
            contains("US")
        }
        assertThat(messages, "getWeather($query)").index(messages.size - 1).prop(Message::msg).endsWith("98204%2CUS")

        query = "San Francisco"
        messages = getWeather(query, getProperty(OWM_API_KEY_PROP))
        assertThat(messages, "getWeather($query)").index(0).prop(Message::msg).all {
            contains("San Francisco")
            contains("US")
        }
        assertThat(messages, "getWeather($query)").index(messages.size - 1).prop(Message::msg).endsWith("5391959")

        query = "London, GB"
        messages = getWeather(query, getProperty(OWM_API_KEY_PROP))
        assertThat(messages, "getWeather($query)").index(0).prop(Message::msg).all {
            contains("London, United Kingdom")
            contains("GB")
        }
        assertThat(messages, "getWeather($query)").index(messages.size - 1).prop(Message::msg).endsWith("2643743")

        try {
            query = "Foo, US"
            getWeather(query, getProperty(OWM_API_KEY_PROP))
        } catch (e: ModuleException) {
            assertThat(e.cause, "getWeather($query)").isNotNull().isInstanceOf(APIException::class.java)
        }

        query = "test"
        assertThat { getWeather(query, "") }.isFailure().isInstanceOf(ModuleException::class.java).hasNoCause()
        assertThat { getWeather(query, null) }.isFailure().isInstanceOf(ModuleException::class.java).hasNoCause()

        messages = getWeather("", "apikey")
        assertThat(messages, "getWeather(empty)").index(0).prop(Message::isError).isTrue()
    }
}
