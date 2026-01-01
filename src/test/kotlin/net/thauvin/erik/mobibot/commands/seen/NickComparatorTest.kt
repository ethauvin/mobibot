/*
 * NickComparatorTest.kt
 *
 * Copyright (c) 2004-2026 Erik C. Thauvin (erik@thauvin.net)
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

package net.thauvin.erik.mobibot.commands.seen

import kotlin.test.Test
import kotlin.test.assertEquals

class NickComparatorTest {
    @Test
    fun `Compare should return 0 for equal strings`() {
        val comparator = NickComparator()
        val result = comparator.compare("SameNick", "samenick")
        assertEquals(0, result, "Expected comparison result to be 0 for equal strings ignoring case.")
    }

    @Test
    fun `Compare should return a negative number when first string is less than second string`() {
        val comparator = NickComparator()
        val result = comparator.compare("Alice", "Bob")
        assert(result < 0) { "Expected comparison result to be less than 0 when first string is less than second." }
    }

    @Test
    fun `Compare should return a positive number when first string is greater than second string`() {
        val comparator = NickComparator()
        val result = comparator.compare("Bob", "Alice")
        assert(result > 0) {
            "Expected comparison result to be greater than 0 when first string is greater than second."
        }
    }

    @Test
    fun `Compare should handle strings with different cases correctly`() {
        val comparator = NickComparator()
        val result = comparator.compare("aLiCe", "ALICE")
        assertEquals(0, result, "Expected comparison result to be 0 for strings differing only by case.")
    }

    @Test
    fun `Compare should correctly handle empty strings`() {
        val comparator = NickComparator()
        val result = comparator.compare("", "NonEmpty")
        assert(result < 0) { "Expected comparison result to be less than 0 when first string is empty." }
    }

    @Test
    fun `Compare should correctly handle comparison with null-like but non-null strings`() {
        val comparator = NickComparator()
        val result = comparator.compare("", "")
        assertEquals(0, result, "Expected comparison result to be 0 for two empty strings.")
    }

    @Test
    fun `Compare should correctly handle spaces in strings`() {
        val comparator = NickComparator()
        val result = comparator.compare("Alice ", "Alice")
        assert(result > 0) { "Expected comparison result to be greater than 0 when first string has trailing space." }
    }

    @Test
    fun `Compare should handle strings with symbols or numbers`() {
        val comparator = NickComparator()
        val result = comparator.compare("Alice123", "Alice")
        assert(result > 0) {
            "Expected comparison result to be greater than 0 when first string contains numbers after text."
        }
    }
}
