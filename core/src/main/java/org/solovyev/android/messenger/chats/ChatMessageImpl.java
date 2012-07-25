package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.JObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:01 PM
 */
public class ChatMessageImpl extends JObject implements ChatMessage {

    @NotNull
    private LiteChatMessage liteChatMessage;

    private boolean read = false;

    @NotNull
    private List<LiteChatMessage> fwdMessages = new ArrayList<LiteChatMessage>();

    @NotNull
    private MessageDirection direction = MessageDirection.in;

    public ChatMessageImpl(@NotNull LiteChatMessage liteChatMessage) {
        this.liteChatMessage = liteChatMessage;
    }

    @NotNull
    public static ChatMessageImpl newInstance(@NotNull LiteChatMessage liteChatMessage) {
        return new ChatMessageImpl(liteChatMessage);
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @NotNull
    @Override
    public MessageDirection getDirection() {
        return this.direction;
    }

    @NotNull
    @Override
    public List<LiteChatMessage> getFwdMessages() {
        return Collections.unmodifiableList(fwdMessages);
    }

    @NotNull
    @Override
    public ChatMessageImpl clone() {
        final ChatMessageImpl clone = (ChatMessageImpl) super.clone();

        clone.liteChatMessage = this.liteChatMessage.clone();
        clone.fwdMessages = new ArrayList<LiteChatMessage>(this.fwdMessages.size());
        for (LiteChatMessage fwdMessage : this.fwdMessages) {
            clone.fwdMessages.add(fwdMessage.clone());
        }

        return clone;
    }

    public boolean addFwdMessage(@NotNull LiteChatMessage fwdMessage) {
        return fwdMessages.add(fwdMessage);
    }

    public void setDirection(@NotNull MessageDirection direction) {
        this.direction = direction;
    }

    @Override
    @NotNull
    public User getAuthor() {
        return liteChatMessage.getAuthor();
    }

    @Override
    @Nullable
    public User getRecipient() {
        return liteChatMessage.getRecipient();
    }

    @Override
    @NotNull
    public DateTime getSendDate() {
        return liteChatMessage.getSendDate();
    }

    @Override
    @NotNull
    public String getTitle() {
        return liteChatMessage.getTitle();
    }

    @Override
    @NotNull
    public String getBody() {
        return liteChatMessage.getBody();
    }

    @Override
    @NotNull
    public String getId() {
        return liteChatMessage.getId();
    }

    @Override
    @NotNull
    public Integer getVersion() {
        return liteChatMessage.getVersion();
    }

    @Override
    @Nullable
    public User getSecondUser(@NotNull User user) {
        return liteChatMessage.getSecondUser(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessageImpl)) return false;

        ChatMessageImpl that = (ChatMessageImpl) o;

        if (!liteChatMessage.equals(that.liteChatMessage)) return false;

        return true;
    }

    @Override
    public boolean equalsVersion(Object that) {
        return this.equals(that) && this.liteChatMessage.equalsVersion(((ChatMessageImpl) that).liteChatMessage);
    }

    @Override
    public int hashCode() {
        return liteChatMessage.hashCode();
    }

    @Override
    public String toString() {
        return "ChatMessageImpl{" +
                "liteChatMessage=" + liteChatMessage +
                '}';
    }

    @Override
    public boolean isPrivate() {
        return liteChatMessage.isPrivate();
    }
}
