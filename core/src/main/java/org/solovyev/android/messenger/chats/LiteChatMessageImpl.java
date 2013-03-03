package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.JObject;
import org.solovyev.common.VersionedEntity;
import org.solovyev.common.VersionedEntityImpl;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 2:04 PM
 */
public class LiteChatMessageImpl extends JObject implements LiteChatMessage {

    @Nonnull
    private VersionedEntity<String> versionedEntity;

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

    private LiteChatMessageImpl(@Nonnull VersionedEntity<String> versionedEntity) {
        this.versionedEntity = versionedEntity;
    }

    @Nonnull
    public static LiteChatMessageImpl newInstance(@Nonnull String id) {
        return new LiteChatMessageImpl(new VersionedEntityImpl<String>(id));
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

        clone.versionedEntity = this.versionedEntity.clone();

        clone.author = this.author.clone();

        if (this.recipient != null) {
            clone.recipient = this.recipient.clone();
        }

        return clone;
    }

    public void setBody(@Nonnull String body) {
        this.body = body;
    }

    @Override
    @Nonnull
    public String getId() {
        return versionedEntity.getId();
    }

    @Override
    @Nonnull
    public Integer getVersion() {
        return versionedEntity.getVersion();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiteChatMessageImpl)) return false;

        LiteChatMessageImpl that = (LiteChatMessageImpl) o;

        if (!versionedEntity.equals(that.versionedEntity)) return false;

        return true;
    }

    @Override
    public boolean equalsVersion(Object that) {
        return this.equals(that) && this.versionedEntity.equalsVersion(((LiteChatMessageImpl) that).versionedEntity);
    }

    @Override
    public int hashCode() {
        return versionedEntity.hashCode();
    }

    @Override
    public String toString() {
        return "LiteChatMessageImpl{" +
                "versionedEntity=" + versionedEntity +
                '}';
    }
}
