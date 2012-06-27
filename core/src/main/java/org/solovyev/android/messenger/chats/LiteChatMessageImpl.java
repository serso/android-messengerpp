package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.VersionedEntity;
import org.solovyev.android.VersionedEntityImpl;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.JObject;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 2:04 PM
 */
public class LiteChatMessageImpl extends JObject implements LiteChatMessage {

    @NotNull
    private VersionedEntity versionedEntity;

    @NotNull
    private User author;

    @Nullable
    private User recipient;

    @NotNull
    private DateTime sendDate;

    @NotNull
    private String title = "";

    @NotNull
    private String body = "";

    private LiteChatMessageImpl(@NotNull VersionedEntity versionedEntity) {
        this.versionedEntity = versionedEntity;
    }

    @NotNull
    public static LiteChatMessageImpl newInstance(@NotNull Integer id) {
        return new LiteChatMessageImpl(new VersionedEntityImpl(id));
    }

    @NotNull
    public User getAuthor() {
        return author;
    }

    public void setAuthor(@NotNull User author) {
        this.author = author;
    }

    @NotNull
    public DateTime getSendDate() {
        return sendDate;
    }

    public void setSendDate(@NotNull DateTime sendDate) {
        this.sendDate = sendDate;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    @NotNull
    public String getBody() {
        return body;
    }

    @NotNull
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

    public void setBody(@NotNull String body) {
        this.body = body;
    }

    @Override
    @NotNull
    public Integer getId() {
        return versionedEntity.getId();
    }

    @Override
    @NotNull
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
    public User getSecondUser(@NotNull User user) {
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
