package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.realms.RealmIsNotConnectedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 9:17 PM
 */
class XmppRealmChatService extends AbstractXmppRealmService implements RealmChatService {

    public XmppRealmChatService(@Nonnull XmppRealm realm) {
        super(realm);
    }

    @Nonnull
    @Override
    public List<ChatMessage> getChatMessages(@Nonnull String realmUserId) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<ChatMessage> getNewerChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<ChatMessage> getOlderChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId, @Nonnull Integer offset) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<ApiChat> getUserChats(@Nonnull String realmUserId) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public String sendChatMessage(@Nonnull Chat chat, @Nonnull ChatMessage message) {
        return doConnected(new MessengerSender(chat, message));
    }

    private static final class MessengerSender implements XmppConnectedCallable<String> {

        @Nonnull
        private final Chat chat;

        @Nonnull
        private final ChatMessage message;

        private MessengerSender(@Nonnull Chat chat, @Nonnull ChatMessage message) {
            this.chat = chat;
            this.message = message;
        }

        @Override
        public String call(@Nonnull XMPPConnection connection) throws RealmIsNotConnectedException, XMPPException {
            final ChatManager chatManager = connection.getChatManager();

            final org.jivesoftware.smack.Chat smackChat = chatManager.getThreadChat(chat.getRealmChat().getRealmEntityId());
            smackChat.sendMessage(message.getBody());

            return null;
        }
    }
}
