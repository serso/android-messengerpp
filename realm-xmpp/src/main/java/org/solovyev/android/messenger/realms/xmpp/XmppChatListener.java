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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class XmppChatListener implements ChatManagerListener {

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
    public static ApiChat toApiChat(@Nonnull Chat chat, @Nonnull List<Message> messages, @Nonnull Realm realm) {
        final User participant = toUser(chat.getParticipant(), realm);

        final Entity realmChat;

        final String realmChatId = chat.getThreadID();
        if (Strings.isEmpty(realmChatId) ) {
            realmChat = getChatService().newPrivateChatId(realm.getUser().getEntity(), participant.getEntity());
        } else {
            realmChat = realm.newRealmEntity(realmChatId);
        }

        final List<ChatMessage> chatMessages = toMessages(realm, messages);
        final List<User> participants = Arrays.asList(realm.getUser(), participant);
        return Chats.newPrivateApiChat(realmChat, participants, chatMessages);
    }

    @Nonnull
    public static List<ChatMessage> toMessages(@Nonnull Realm realm, @Nonnull List<Message> messages) {
        final List<ChatMessage> chatMessages = new ArrayList<ChatMessage>(messages.size());
        for (Message message : messages) {
            chatMessages.add(toChatMessage(message, realm));
        }
        return chatMessages;
    }

    @Nonnull
    private static ChatMessage toChatMessage(@Nonnull Message message, @Nonnull Realm realm) {
        final LiteChatMessageImpl liteChatMessage = ChatMessages.newMessage(getChatMessageService().generateEntity(realm));
        liteChatMessage.setBody(message.getBody());
        liteChatMessage.setAuthor(toUser(message.getFrom(), realm));
        liteChatMessage.setRecipient(toUser(message.getTo(), realm));
        liteChatMessage.setSendDate(DateTime.now());
        return ChatMessageImpl.newInstance(liteChatMessage);
    }

    @Nonnull
    private static User toUser(@Nonnull String realmUserId, @Nonnull Realm realm) {
        return Users.newEmptyUser(realm.newUserEntity(realmUserId));
    }
}
