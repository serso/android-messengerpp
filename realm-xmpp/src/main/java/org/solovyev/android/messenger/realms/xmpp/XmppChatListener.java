package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
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
        if ( !createdLocally ) {
            final ApiChatImpl newChat = toApiChat(chat, Collections.<Message>emptyList());
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
            final ApiChatImpl apiChat = toApiChat(chat, Arrays.asList(message));
            getChatService().saveChat(realm.getUser().getRealmEntity(), apiChat);
        }
    }

    @Nonnull
    private ApiChatImpl toApiChat(@Nonnull Chat chat, @Nonnull List<Message> messages) {
        final RealmEntity participant = realm.newRealmEntity(chat.getParticipant());

        final RealmEntity realmChat;

        final String realmChatId = chat.getThreadID();
        if (Strings.isEmpty(realmChatId) ) {
            realmChat = getChatService().createPrivateChatId(realm.getUser().getRealmEntity(), participant);
        } else {
            realmChat = realm.newRealmEntity(realmChatId);
        }

        final ApiChatImpl newChat = ApiChatImpl.newInstance(realmChat, messages.size(), true);
        newChat.addParticipant(realm.getUser());
        newChat.addParticipant(Users.newEmptyUser(participant));
        for (Message message : messages) {
            newChat.addMessage(toChatMessage(message));
        }
        return newChat;
    }

    @Nonnull
    private ChatMessage toChatMessage(@Nonnull Message message) {
        LiteChatMessageImpl liteChatMessage = LiteChatMessageImpl.newInstance(message.getPacketID());
        liteChatMessage.setBody(message.getBody());
        liteChatMessage.setAuthor(toUser(message.getFrom()));
        liteChatMessage.setRecipient(toUser(message.getTo()));
        return ChatMessageImpl.newInstance(liteChatMessage);
    }

    @Nonnull
    private User toUser(@Nonnull String realmUserId) {
        return Users.newEmptyUser(realm.newRealmEntity(realmUserId));
    }
}
