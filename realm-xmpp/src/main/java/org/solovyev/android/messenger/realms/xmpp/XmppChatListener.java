package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmEntity;
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
    private XmppRealm realm;

    public XmppChatListener(@Nonnull XmppRealm realm) {
        this.realm = realm;
    }

    @Override
    public void chatCreated(@Nonnull Chat chat, boolean createdLocally) {
        Log.i("M++/Xmpp", "Chat created!");

        if (!createdLocally) {
            final ApiChat newChat = toApiChat(chat, Collections.<Message>emptyList(), realm);
            getChatService().saveChat(realm.getUser().getRealmEntity(), newChat);
        }

        chat.addMessageListener(new XmppMessageListener());
    }

    @Nonnull
    private static ChatService getChatService() {
        return MessengerApplication.getServiceLocator().getChatService();
    }

    private class XmppMessageListener implements MessageListener {

        @Override
        public void processMessage(Chat chat, Message message) {
            Log.i("M++/Xmpp", "Message created: " + message.getBody());
            final ApiChat apiChat = toApiChat(chat, Arrays.asList(message), XmppChatListener.this.realm);
            getChatService().saveChat(realm.getUser().getRealmEntity(), apiChat);
        }
    }

    @Nonnull
    public static ApiChat toApiChat(@Nonnull Chat chat, @Nonnull List<Message> messages, @Nonnull XmppRealm realm) {
        final RealmEntity participant = realm.newRealmEntity(chat.getParticipant());

        final RealmEntity realmChat;

        final String realmChatId = chat.getThreadID();
        if (Strings.isEmpty(realmChatId) ) {
            realmChat = getChatService().newPrivateChatId(realm.getUser().getRealmEntity(), participant);
        } else {
            realmChat = realm.newRealmEntity(realmChatId);
        }

        final List<ChatMessage> chatMessages = new ArrayList<ChatMessage>(messages.size());
        for (Message message : messages) {
            chatMessages.add(toChatMessage(message, realm));
        }
        final List<User> participants = Arrays.asList(realm.getUser(), Users.newEmptyUser(participant));
        return Chats.newPrivateApiChat(realmChat, participants, chatMessages);
    }

    @Nonnull
    private static ChatMessage toChatMessage(@Nonnull Message message, @Nonnull Realm realm) {
        LiteChatMessageImpl liteChatMessage = LiteChatMessageImpl.newInstance(message.getPacketID());
        liteChatMessage.setBody(message.getBody());
        liteChatMessage.setAuthor(toUser(message.getFrom(), realm));
        liteChatMessage.setRecipient(toUser(message.getTo(), realm));
        return ChatMessageImpl.newInstance(liteChatMessage);
    }

    @Nonnull
    private static User toUser(@Nonnull String realmUserId, @Nonnull Realm realm) {
        return Users.newEmptyUser(realm.newRealmEntity(realmUserId));
    }
}
