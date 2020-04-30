/*
 * EntryLink.java
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
import com.rometools.rome.feed.synd.SyndCategoryImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.thauvin.erik.mobibot.commands.links.LinksMgr;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The class used to store link entries.
 *
 * @author <a href="https://erik.thauvin.net" target="_blank">Erik C. Thauvin</a>
 * @created Jan 31, 2004
 * @since 1.0
 */
public class EntryLink implements Serializable {
    // Serial version UID
    static final long serialVersionUID = 1L;

    // Link's comments
    private final List<EntryComment> comments = new CopyOnWriteArrayList<>();

    // Tags/categories
    private final List<SyndCategory> tags = new CopyOnWriteArrayList<>();

    // Channel
    private String channel;

    // Creation date
    private Date date = Calendar.getInstance().getTime();

    // Link's URL
    private String link;

    // Author's login
    private String login = "";

    // Author's nickname
    private String nick;

    // Link's title
    private String title;

    /**
     * Creates a new entry.
     *
     * @param link    The new entry's link.
     * @param title   The new entry's title.
     * @param nick    The nickname of the author of the link.
     * @param login   The login of the author of the link.
     * @param channel The channel.
     * @param tags    The entry's tags/categories.
     */
    public EntryLink(final String link,
                     final String title,
                     final String nick,
                     final String login,
                     final String channel,
                     final List<String> tags) {
        this.link = link;
        this.title = title;
        this.nick = nick;
        this.login = login;
        this.channel = channel;

        setTags(tags);
    }

    /**
     * Creates a new entry.
     *
     * @param link    The new entry's link.
     * @param title   The new entry's title.
     * @param nick    The nickname of the author of the link.
     * @param channel The channel.
     * @param date    The entry date.
     * @param tags    The entry's tags/categories.
     */
    public EntryLink(final String link,
                     final String title,
                     final String nick,
                     final String channel,
                     final Date date,
                     final List<SyndCategory> tags) {
        this.link = link;
        this.title = title;
        this.nick = nick;
        this.channel = channel;
        this.date = new Date(date.getTime());
        this.tags.addAll(tags);
    }

    /**
     * Adds a new comment.
     *
     * @param comment The actual comment.
     * @param nick    The nickname of the author of the comment.
     * @return The total number of comments for this entry.
     */
    public final int addComment(final String comment, final String nick) {
        comments.add(new EntryComment(comment, nick));

        return (comments.size() - 1);
    }

    /**
     * Deletes a specific comment.
     *
     * @param index The index of the comment to delete.
     */
    public final void deleteComment(final int index) {
        if (index < comments.size()) {
            comments.remove(index);
        }
    }

    /**
     * Returns the channel the link was posted on.
     *
     * @return The channel
     */
    public final String getChannel() {
        return channel;
    }

    /**
     * Returns a comment.
     *
     * @param index The comment's index.
     * @return The specific comment.
     */
    public final EntryComment getComment(final int index) {
        return (comments.get(index));
    }

    /**
     * Returns all the comments.
     *
     * @return The comments.
     */
    public final EntryComment[] getComments() {
        return (comments.toArray(new EntryComment[0]));
    }

    /**
     * Returns the total number of comments.
     *
     * @return The count of comments.
     */
    public final int getCommentsCount() {
        return comments.size();
    }

    /**
     * Returns the comment's creation date.
     *
     * @return The date.
     */
    public final Date getDate() {
        return new Date(date.getTime());
    }

    /**
     * Returns the comment's link.
     *
     * @return The link.
     */
    public final String getLink() {
        return link;
    }

    /**
     * Returns the comment's author login.
     *
     * @return The login;
     */
    public final String getLogin() {
        return login;
    }

    /**
     * Returns the comment's author nickname.
     *
     * @return The nickname.
     */
    public final String getNick() {
        return nick;
    }

    /**
     * Returns the tags formatted for pinboard.in
     *
     * @return The tags as a comma-delimited string.
     */
    public final String getPinboardTags() {
        final StringBuilder pinboardTags = new StringBuilder(nick);

        for (final SyndCategory tag : tags) {
            pinboardTags.append(',');
            pinboardTags.append(tag.getName());
        }

        return pinboardTags.toString();
    }

    /**
     * Returns the tags.
     *
     * @return The tags.
     */
    public final List<SyndCategory> getTags() {
        return tags;
    }

    /**
     * Returns the comment's title.
     *
     * @return The title.
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Returns true if the entry has comments.
     *
     * @return true if there are comments, false otherwise.
     */
    public final boolean hasComments() {
        return !comments.isEmpty();
    }

    /**
     * Returns true if the entry has tags.
     *
     * @return true if there are tags, false otherwise.
     */
    public final boolean hasTags() {
        return !tags.isEmpty();
    }

    /**
     * Returns true if a string is contained in the link, title, or nick.
     *
     * @param match The string to match.
     * @return {@code true} if matched, {@code false} otherwise.
     */
    public Boolean matches(final String match) {
        return (StringUtils.containsIgnoreCase(link, match)
                || StringUtils.containsIgnoreCase(title, match)
                || StringUtils.containsIgnoreCase(nick, match));
    }

    /**
     * Sets the channel.
     *
     * @param channel The channel.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final void setChannel(final String channel) {
        this.channel = channel;
    }

    /**
     * /** Sets a comment.
     *
     * @param index   The comment's index.
     * @param comment The actual comment.
     * @param nick    The nickname of the author of the comment.
     */
    public final void setComment(final int index, final String comment, final String nick) {
        if (index < comments.size()) {
            comments.set(index, new EntryComment(comment, nick));
        }
    }

    /**
     * Sets the comment's link.
     *
     * @param link The new link.
     */
    public final void setLink(final String link) {
        this.link = link;
    }

    /**
     * Sets the comment's author login.
     *
     * @param login The new login.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final void setLogin(final String login) {
        this.login = login;
    }

    /**
     * Sets the comment's author nickname.
     *
     * @param nick The new nickname.
     */
    public final void setNick(final String nick) {
        this.nick = nick;
    }

    /**
     * Sets the tags.
     *
     * @param tags The space-delimited tags.
     */
    public final void setTags(final String tags) {
        setTags(Arrays.asList(tags.split(LinksMgr.TAG_MATCH)));
    }

    /**
     * Sets the tags.
     *
     * @param tags The tags list.
     */
    @SuppressFBWarnings("STT_STRING_PARSING_A_FIELD")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public final void setTags(final List<String> tags) {
        if (!tags.isEmpty()) {
            SyndCategoryImpl category;

            for (final String tag : tags) {
                if (StringUtils.isNoneBlank(tag)) {
                    final String t = StringUtils.lowerCase(tag);
                    final char mod = t.charAt(0);
                    if (mod == '-') {
                        // Don't remove the channel tag
                        if (!channel.substring(1).equals(t.substring(1))) {
                            category = new SyndCategoryImpl();
                            category.setName(t.substring(1));
                            this.tags.remove(category);
                        }
                    } else {
                        category = new SyndCategoryImpl();
                        if (mod == '+') {
                            category.setName(t.substring(1));
                        } else {
                            category.setName(t);
                        }
                        if (!this.tags.contains(category)) {
                            this.tags.add(category);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the comment's title.
     *
     * @param title The new title.
     */
    public final void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return "EntryLink{"
               + "channel='" + channel + '\''
               + ", comments=" + comments
               + ", date=" + date
               + ", link='" + link + '\''
               + ", login='" + login + '\''
               + ", nick='" + nick + '\''
               + ", tags=" + tags
               + ", title='" + title + '\''
               + '}';
    }
}
