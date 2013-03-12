package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.AbstractMessengerEntity;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 2:04 PM
 */
public class LiteChatMessageImpl extends AbstractMessengerEntity implements LiteChatMessage {

    @Nonnull
    private User author;

    @Nullable
    private User recipient;

    @Nonnull
    private DateTime sendDate;

    @Nonnull
    private String title = "";

    @Nonnull
    private String body = "";

    private LiteChatMessageImpl(@Nonnull RealmEntity entity) {
        super(entity);
    }

    @Nonnull
    static LiteChatMessageImpl newInstance(@Nonnull RealmEntity entity) {
        return new LiteChatMessageImpl(entity);
    }

    @Nonnull
    public User getAuthor() {
        return author;
    }

    public void setAuthor(@Nonnull User author) {
        this.author = author;
    }

    @Nonnull
    public DateTime getSendDate() {
        return sendDate;
    }

    public void setSendDate(@Nonnull DateTime sendDate) {
        this.sendDate = sendDate;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nonnull String title) {
        this.title = title;
    }

    @Nonnull
    public String getBody() {
        return body;
    }

    @Nonnull
    @Override
    public LiteChatMessageImpl clone() {
        final LiteChatMessageImpl clone = (LiteChatMessageImpl) super.clone();

        clone.author = this.author.clone();

        if (this.recipient != null) {
            clone.recipient = this.recipient.clone();
        }

        return clone;
    }

    public void setBody(@Nonnull String body) {
        this.body = body;
    }

    @Nullable
    public User getRecipient() {
        return recipient;
    }

    @Override
    public boolean isPrivate() {
        return recipient != null && !recipient.equals(author);
    }

    @Override
    public User getSecondUser(@Nonnull User user) {
        if (author.equals(user)) {
            return recipient;
        } else if (user.equals(recipient)) {
            return author;
        }

        return null;
    }

    public void setRecipient(@Nullable User recipient) {
        this.recipient = recipient;
    }
}
