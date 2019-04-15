/*
 * CurrencyConvertTest.java
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The <code>CurrencyConvertTest</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-04-07
 * @since 1.0
 */
public class CurrentConverterTest {
    @Test
    public void testCurrencyConvertererImpl() {
        AbstractModuleTest.testAbstractModule(new CurrencyConverter());
    }

    @Test(expectedExceptions = ModuleException.class)
    public void testException() throws ModuleException {
        CurrencyConverter.convertCurrency("100 BLA to USD");
    }

    @Test
    public void testConvertCurrency() throws ModuleException {
        assertThat(CurrencyConverter.convertCurrency("100 USD to EUR").getMessage())
            .as("100 USD to EUR").startsWith("100.00 USD = ");
        assertThat(CurrencyConverter.convertCurrency(CurrencyConverter.CURRENCY_RATES_KEYWORD).isNotice())
            .as(CurrencyConverter.CURRENCY_RATES_KEYWORD + " is notice").isTrue();
        assertThat(CurrencyConverter.convertCurrency(CurrencyConverter.CURRENCY_RATES_KEYWORD).getMessage())
            .as(CurrencyConverter.CURRENCY_RATES_KEYWORD).contains("USD: ");
    }
}
