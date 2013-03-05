package org.solovyev.android.messenger.chats;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmEntityImpl;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:59 PM
 */
public class ChatImpl extends JObject implements Chat {

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    private boolean privateChat;

    @Nonnull
    private Integer messagesCount = 0;

    @Nonnull
    private List<AProperty> properties;

    @Nullable
    private DateTime lastMessageSyncDate;

    @Nonnull
    private RealmEntity realmEntity;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

    private ChatImpl(@Nonnull RealmEntity realmEntity,
                     @Nonnull Integer messagesCount,
                     @Nonnull List<AProperty> properties,
                     @Nullable DateTime lastMessageSyncDate) {
        this.realmEntity = realmEntity;
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

    private ChatImpl(@Nonnull RealmEntity realmEntity,
                     @Nonnull Integer messagesCount,
                     boolean privateChat) {
        this.realmEntity = realmEntity;
        this.messagesCount = messagesCount;
        this.privateChat = privateChat;
        this.properties = new ArrayList<AProperty>();
        properties.add(APropertyImpl.newInstance("private", Boolean.toString(privateChat)));
    }



    @Nonnull
    public static Chat newFakeChat(@Nonnull String chatId) {
        return new ChatImpl(RealmEntityImpl.fromEntityId(chatId), 0, false);
    }

    @Nonnull
    public static Chat newInstance(@Nonnull RealmEntity realmEntity,
                                   @Nonnull Integer messagesCount,
                                   @Nonnull List<AProperty> properties,
                                   @Nullable DateTime lastMessageSyncDate) {
        return new ChatImpl(realmEntity, messagesCount, properties, lastMessageSyncDate);
    }

    @Nonnull
    public static Chat newPrivate(@Nonnull RealmEntity realmEntity) {
        final List<AProperty> properties = new ArrayList<AProperty>();
        properties.add(APropertyImpl.newInstance("private", Boolean.toString(true)));
        return new ChatImpl(realmEntity, 0, properties, null);
    }

    /*
    **********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */

    @Nonnull
    public List<AProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Nonnull
    public Integer getMessagesCount() {
        return messagesCount;
    }

    @Nonnull
    @Override
    public ChatImpl updateMessagesSyncDate() {
        final ChatImpl clone = clone();

        clone.lastMessageSyncDate = DateTime.now();

        return clone;
    }

    @Nonnull
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
        clone.realmEntity = realmEntity.clone();

        return clone;
    }

    @Nonnull
    @Override
    public RealmEntity getRealmChat() {
        return this.realmEntity;
    }

    @Override
    public boolean isPrivate() {
        return privateChat;
    }

    @Nonnull
    @Override
    public RealmEntity getSecondUser() {
        assert isPrivate();

        return MessengerApplication.getServiceLocator().getChatService().getSecondUser(this);
    }

    @Override
    public DateTime getLastMessagesSyncDate() {
        return this.lastMessageSyncDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatImpl)) return false;

        ChatImpl that = (ChatImpl) o;

        if (!this.realmEntity.equals(that.realmEntity)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return this.realmEntity.hashCode();
    }

    @Override
    public String toString() {
        return "ChatImpl{" +
                "id=" + realmEntity.getEntityId() +
                ", privateChat=" + privateChat +
                '}';
    }
}
