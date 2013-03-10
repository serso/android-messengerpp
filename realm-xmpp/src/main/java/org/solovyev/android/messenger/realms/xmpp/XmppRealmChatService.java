package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmEntityImpl;
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

    public XmppRealmChatService(@Nonnull XmppRealm realm, @Nonnull XmppConnectionAware connectionAware) {
        super(realm, connectionAware);
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
        return doOnConnection(new MessengerSender(chat, message));
    }

    @Nonnull
    @Override
    public Chat newPrivateChat(@Nonnull String realmUserId1, @Nonnull final String realmUserId2) {
        return doOnConnection(new XmppConnectedCallable<Chat>() {
            @Override
            public Chat call(@Nonnull Connection connection) throws RealmIsNotConnectedException, XMPPException {
                org.jivesoftware.smack.Chat smackChat = connection.getChatManager().createChat(realmUserId2, null);
                return XmppChatListener.toApiChat(smackChat, Collections.<Message>emptyList(), getRealm()).getChat();
            }
        });
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
        public String call(@Nonnull Connection connection) throws RealmIsNotConnectedException, XMPPException {
            final ChatManager chatManager = connection.getChatManager();

            final RealmEntity realmChat = chat.getRealmEntity();
            org.jivesoftware.smack.Chat smackChat = chatManager.getThreadChat(realmChat.getRealmEntityId());
            if ( smackChat == null ) {
                // smack forget about chat ids after restart => need to create chat here
                smackChat = chatManager.createChat(chat.getSecondUser().getRealmEntityId(), null);
                final String threadID = smackChat.getThreadID();
                if ( threadID != null && !realmChat.getRealmEntityId().equals(threadID) ) {
                    // check if thread id was created and update chat if it is not the same
                    final Chat newChat = chat.copyWithNew(RealmEntityImpl.newInstance(realmChat.getRealmId(), threadID, realmChat.getEntityId()));
                    MessengerApplication.getServiceLocator().getChatService().updateChat(newChat);
                }
            }

            smackChat.sendMessage(message.getBody());

            return null;
        }
    }
}
