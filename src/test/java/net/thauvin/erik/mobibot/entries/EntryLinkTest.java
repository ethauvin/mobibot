/*
 * EntryLinkTest.java
 *
 * Copyright (c) 2004-2020, Erik C. Thauvin (erik@thauvin.net)
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

package net.thauvin.erik.mobibot.entries;

import com.rometools.rome.feed.synd.SyndCategory;
import org.testng.annotations.Test;

import java.security.SecureRandom;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The <code>EntryUtilsTest</code> class.
 *
 * @author <a href="https://erik.thauvin.net/" target="_blank">Erik C. Thauvin</a>
 * @created 2019-04-19
 * @since 1.0
 */
public class EntryLinkTest {
    private final EntryLink entryLink = new EntryLink("https://www.mobitopia.org/", "Mobitopia", "Skynx",
        "JimH", "#mobitopia", List.of("tag1", "tag2", "tag3", "TAG4", "Tag5"));


    @Test
    public void testAddDeleteComment() {
        int i = 0;
        for (; i < 5; i++) {
            entryLink.addComment("c" + i, "u" + i);
        }

        i = 0;
        for (final EntryComment comment : entryLink.getComments()) {
            assertThat(comment.getComment()).as("getComment(" + i + ')').isEqualTo("c" + i);
            assertThat(comment.getNick()).as("getNick(" + i + ')').isEqualTo("u" + i);
            i++;
        }

        final SecureRandom r = new SecureRandom();
        while (entryLink.getCommentsCount() > 0) {
            entryLink.deleteComment(r.nextInt(entryLink.getCommentsCount()));
        }
        assertThat(entryLink.hasComments()).as("hasComments()").isFalse();

        entryLink.addComment("nothing", "nobody");
        entryLink.setComment(0, "something", "somebody");
        assertThat(entryLink.getComment(0).getNick()).as("getNick(somebody)").isEqualTo("somebody");
        assertThat(entryLink.getComment(0).getComment()).as("getComment(something)").isEqualTo("something");

    }

    @Test
    public void testTags() {
        final List<SyndCategory> tags = entryLink.getTags();

        int i = 0;
        for (final SyndCategory tag : tags) {
            assertThat(tag.getName()).as("tag.getName(" + i + ')').isEqualTo("tag" + (i + 1));
            i++;
        }

        entryLink.setTags("-tag5");
        entryLink.setTags("+mobitopia");
        entryLink.setTags("tag4");
        entryLink.setTags("-mobitopia");

        assertThat(entryLink.getPinboardTags()).as("getPinboardTags()")
            .isEqualTo(entryLink.getNick() + ",tag1,tag2,tag3,tag4,mobitopia");
    }
}
