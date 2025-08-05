/*
 * CommentTest.kt
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
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

package net.thauvin.erik.mobibot.commands.links

import net.thauvin.erik.mobibot.Constants
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommentTest {
    @Test
    fun `Matches with valid comment syntax`() {
        val comment = Comment()
        val validMessage = Constants.LINK_CMD + "1.2:This is a comment"
        assertTrue(comment.matches(validMessage), "Message should match valid comment format.")
    }

    @Test
    fun `Matches with valid edit comment syntax`() {
        val comment = Comment()
        val validEditMessage = Constants.LINK_CMD + "1.10:This is an edited comment"
        assertTrue(comment.matches(validEditMessage), "Message should match valid comment edit format.")
    }

    @Test
    fun `Matches with valid delete comment syntax`() {
        val comment = Comment()
        val validDeleteMessage = Constants.LINK_CMD + "4.5:-"
        assertTrue(comment.matches(validDeleteMessage), "Message should match valid delete comment format.")
    }

    @Test
    fun `Matches with changing author syntax`() {
        val comment = Comment()
        val changeAuthorMessage = Constants.LINK_CMD + "2.1:?newAuthor"
        assertTrue(comment.matches(changeAuthorMessage), "Message should match author change format.")
    }

    @Test
    fun `Matches with no command but similar syntax`() {
        val comment = Comment()
        assertFalse(comment.matches(Constants.LINK_CMD), "Message should not match when command format is incomplete.")
    }

    @Test
    fun `Matches with number-only command`() {
        val comment = Comment()
        val nonMatchingNumberMessage = Constants.LINK_CMD + "2.3"
        assertFalse(comment.matches(nonMatchingNumberMessage), "Message should not match when missing colon and text.")
    }

    @Test
    fun `Matches with completely invalid command`() {
        val comment = Comment()
        val invalidMessage = "foo"
        assertFalse(comment.matches(invalidMessage), "Message should not match when format is unrelated.")
    }
}
