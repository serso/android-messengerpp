package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.common.JObject;
import org.solovyev.common.VersionedEntity;
import org.solovyev.common.VersionedEntityImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:59 PM
 */
public class ChatImpl extends JObject implements Chat {

    @NotNull
    private VersionedEntity<String> versionedEntity;

    private boolean privateChat;

    @NotNull
    private Integer messagesCount = 0;

    @NotNull
    private List<AProperty> properties;

    @Nullable
    private DateTime lastMessageSyncDate;

    public ChatImpl(@NotNull String id,
                    @NotNull Integer messagesCount,
                    @NotNull List<AProperty> properties,
                    @Nullable DateTime lastMessageSyncDate) {
        this.versionedEntity = new VersionedEntityImpl<String>(id);
        this.messagesCount = messagesCount;
        this.lastMessageSyncDate = lastMessageSyncDate;

        this.properties = properties;

        this.privateChat = true;
        for (AProperty property : properties) {
            if (property.getName().equals("private")) {
                this.privateChat = Boolean.valueOf(property.getValue());
                break;
            }
        }
    }

    public ChatImpl(@NotNull String id,
                    @NotNull Integer messagesCount,
                    boolean privateChat) {
        this.versionedEntity = new VersionedEntityImpl<String>(id);
        this.messagesCount = messagesCount;
        this.privateChat = privateChat;
        this.properties = new ArrayList<AProperty>();
        properties.add(APropertyImpl.newInstance("private", Boolean.toString(privateChat)));
    }


    @NotNull
    public List<AProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @NotNull
    public Integer getMessagesCount() {
        return messagesCount;
    }

    @NotNull
    @Override
    public ChatImpl updateMessagesSyncDate() {
        final ChatImpl clone = clone();

        clone.lastMessageSyncDate = DateTime.now();

        return clone;
    }

    @NotNull
    @Override
    public ChatImpl clone() {
        final ChatImpl clone = (ChatImpl) super.clone();

        /*clone.messages = new ArrayList<ChatMessage>(this.messages.size());
        for (ChatMessage message : this.messages) {
            clone.messages.add(message.clone());
        }

        clone.participants = new ArrayList<User>(this.participants.size());
        for (User participant : this.participants) {
            clone.participants.add(participant.clone());
        }*/

        // properties cannot be changed themselves but some can be removed or added
        clone.properties = new ArrayList<AProperty>(this.properties);

        return clone;
    }

    @Override
    public boolean isPrivate() {
        return privateChat;
    }

    @NotNull
    @Override
    public String getSecondUserId() {
        assert isPrivate();

        return MessengerApplication.getServiceLocator().getChatService().getSecondUserId(this);
    }

    @Override
    public DateTime getLastMessagesSyncDate() {
        return this.lastMessageSyncDate;
    }

    @Override
    @NotNull
    public String getId() {
        return this.versionedEntity.getId();
    }

    @NotNull
    @Override
    public Integer getVersion() {
        return this.versionedEntity.getVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatImpl)) return false;

        ChatImpl that = (ChatImpl) o;

        if (!this.versionedEntity.equals(that.versionedEntity)) return false;

        return true;
    }

    @Override
    public boolean equalsVersion(Object that) {
        return this.equals(that) && this.versionedEntity.equalsVersion(((ChatImpl) that).versionedEntity);
    }

    @Override
    public int hashCode() {
        return this.versionedEntity.hashCode();
    }

    @Override
    public String toString() {
        return "ChatImpl{" +
                "versionedEntity=" + versionedEntity +
                ", privateChat=" + privateChat +
                '}';
    }
}
