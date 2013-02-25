package org.solovyev.android.messenger.chats;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;
import org.solovyev.android.messenger.users.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:42 PM
 */
public class ApiChatImpl implements ApiChat {

    @NotNull
    private Chat chat;

    @NotNull
    private List<ChatMessage> messages;

    @NotNull
    private List<User> participants;

    private ApiChatImpl(@NotNull RealmEntity realmEntity,
                        @NotNull Integer messagesCount,
                        @NotNull List<AProperty> properties,
                        @NotNull List<ChatMessage> chatMessages,
                        @NotNull List<User> chatParticipants,
                        @Nullable DateTime lastMessageSyncDate) {
        this.chat = ChatImpl.newInstance(realmEntity, messagesCount, properties, lastMessageSyncDate);
        this.messages = chatMessages;

        this.participants = chatParticipants;
    }

    private ApiChatImpl(@NotNull RealmEntity realmEntity,
                        @NotNull Integer messagesCount,
                        boolean privateChat) {
        final List<AProperty> properties = new ArrayList<AProperty>();
        properties.add(APropertyImpl.newInstance("private", Boolean.toString(privateChat)));
        this.chat = ChatImpl.newInstance(realmEntity, messagesCount, properties, null);

        this.messages = new ArrayList<ChatMessage>(20);
        this.participants = new ArrayList<User>(3);
    }

    @NotNull
    public static ApiChatImpl newInstance(@NotNull RealmEntity realmEntity,
                                          @NotNull Integer messagesCount,
                                          boolean privateChat) {
        return new ApiChatImpl(realmEntity, messagesCount, privateChat);
    }

    @NotNull
    public static ApiChatImpl newInstance(@NotNull RealmEntity realmEntity,
                                          @NotNull Integer messagesCount,
                                          @NotNull List<AProperty> properties,
                                          @NotNull List<ChatMessage> chatMessages,
                                          @NotNull List<User> chatParticipants,
                                          @Nullable DateTime lastMessageSyncDate) {
        return new ApiChatImpl(realmEntity, messagesCount, properties, chatMessages, chatParticipants, lastMessageSyncDate);
    }

    @NotNull
    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public ChatMessage getLastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    public void addMessage(@NotNull ChatMessage message) {
        this.messages.add(message);
    }

    @NotNull
    public List<User> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    @NotNull
    @Override
    public List<User> getParticipantsExcept(@NotNull User user) {
        return Lists.newArrayList(Iterables.filter(participants, not(equalTo(user))));
    }

    public boolean addParticipant(@NotNull User participant) {
        if (!participants.contains(participant)) {
            if (this.chat.isPrivate()) {
                if (participants.size() == 2) {
                    throw new IllegalArgumentException("Only 2 participants can be in private chat!");
                }
            }
            return participants.add(participant);
        }

        return false;
    }

    @Override
    @NotNull
    public Chat getChat() {
        return chat;
    }
}
