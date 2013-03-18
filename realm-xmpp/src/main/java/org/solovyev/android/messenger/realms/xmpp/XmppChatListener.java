package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.packet.Message;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.ChatMessages;
import org.solovyev.android.messenger.messages.LiteChatMessageImpl;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class XmppChatListener implements ChatManagerListener {

    @Nonnull
    private Realm realm;

    public XmppChatListener(@Nonnull Realm realm) {
        this.realm = realm;
    }

    @Override
    public void chatCreated(@Nonnull Chat chat, boolean createdLocally) {
        Log.i("M++/Xmpp", "Chat created!");


        if (!createdLocally) {
            ApiChat newChat = toApiChat(chat, Collections.<Message>emptyList(), realm);
            newChat = getChatService().saveChat(realm.getUser().getEntity(), newChat);
            chat.addMessageListener(new XmppMessageListener(realm, newChat.getChat().getEntity()));
        }
    }

    @Nonnull
    private static ChatService getChatService() {
        return MessengerApplication.getServiceLocator().getChatService();
    }

    @Nonnull
    private static ChatMessageService getChatMessageService() {
        return MessengerApplication.getServiceLocator().getChatMessageService();
    }

    @Nonnull
    public static ApiChat toApiChat(@Nonnull Chat smackChat, @Nonnull List<Message> messages, @Nonnull Realm realm) {
        final User participant = toUser(smackChat.getParticipant(), realm);

        final Entity chat;

        final String realmChatId = smackChat.getThreadID();
        if (Strings.isEmpty(realmChatId) ) {
            chat = getChatService().newPrivateChatId(realm.getUser().getEntity(), participant.getEntity());
        } else {
            chat = realm.newRealmEntity(realmChatId);
        }

        final List<ChatMessage> chatMessages = toMessages(realm, messages);
        final List<User> participants = Arrays.asList(realm.getUser(), participant);
        return Chats.newPrivateApiChat(chat, participants, chatMessages);
    }

    @Nonnull
    public static List<ChatMessage> toMessages(@Nonnull Realm realm, @Nonnull List<Message> messages) {
        final List<ChatMessage> chatMessages = new ArrayList<ChatMessage>(messages.size());
        for (Message message : messages) {
            final ChatMessage chatMessage = toChatMessage(message, realm);
            if (chatMessage != null) {
                chatMessages.add(chatMessage);
            }
        }
        return chatMessages;
    }

    @Nullable
    private static ChatMessage toChatMessage(@Nonnull Message message, @Nonnull Realm realm) {
        final String body = message.getBody();
        if (!Strings.isEmpty(body)) {
            final LiteChatMessageImpl liteChatMessage = ChatMessages.newMessage(getChatMessageService().generateEntity(realm));
            liteChatMessage.setBody(body);
            liteChatMessage.setAuthor(realm.newUserEntity(message.getFrom()));
            liteChatMessage.setRecipient(realm.newUserEntity(message.getTo()));
            liteChatMessage.setSendDate(DateTime.now());
            return ChatMessageImpl.newInstance(liteChatMessage);
        } else {
            return null;
        }
    }

    @Nonnull
    private static User toUser(@Nonnull String realmUserId, @Nonnull Realm realm) {
        return Users.newEmptyUser(realm.newUserEntity(realmUserId));
    }
}
