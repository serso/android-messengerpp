package org.solovyev.android.messenger.chats;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.LiteChatMessage;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:01 PM
 */
public class ChatMessageImpl extends JObject implements ChatMessage {

    @Nonnull
    private LiteChatMessage liteChatMessage;

    private boolean read = false;

    @Nonnull
    private List<LiteChatMessage> fwdMessages = new ArrayList<LiteChatMessage>();

    @Nonnull
    private MessageDirection direction = MessageDirection.in;

    public ChatMessageImpl(@Nonnull LiteChatMessage liteChatMessage) {
        this.liteChatMessage = liteChatMessage;
    }

    @Nonnull
    public static ChatMessageImpl newInstance(@Nonnull LiteChatMessage liteChatMessage) {
        return new ChatMessageImpl(liteChatMessage);
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Nonnull
    @Override
    public MessageDirection getDirection() {
        return this.direction;
    }

    @Nonnull
    @Override
    public List<LiteChatMessage> getFwdMessages() {
        return Collections.unmodifiableList(fwdMessages);
    }

    @Nonnull
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

    public boolean addFwdMessage(@Nonnull LiteChatMessage fwdMessage) {
        return fwdMessages.add(fwdMessage);
    }

    public void setDirection(@Nonnull MessageDirection direction) {
        this.direction = direction;
    }

    @Nonnull
    @Override
    public Entity getEntity() {
        return this.liteChatMessage.getEntity();
    }

    @Override
    @Nonnull
    public Entity getAuthor() {
        return liteChatMessage.getAuthor();
    }

    @Override
    @Nullable
    public Entity getRecipient() {
        return liteChatMessage.getRecipient();
    }

    @Override
    @Nonnull
    public DateTime getSendDate() {
        return liteChatMessage.getSendDate();
    }

    @Override
    @Nonnull
    public String getTitle() {
        return liteChatMessage.getTitle();
    }

    @Override
    @Nonnull
    public String getBody() {
        return liteChatMessage.getBody();
    }

    @Override
    @Nullable
    public Entity getSecondUser(@Nonnull Entity user) {
        return liteChatMessage.getSecondUser(user);
    }

    @Nonnull
    @Override
    public String getId() {
        return liteChatMessage.getId();
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
