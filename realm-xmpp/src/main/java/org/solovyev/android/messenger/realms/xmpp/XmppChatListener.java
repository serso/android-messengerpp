package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.ApiChatImpl;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.users.UserImpl;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;

public class XmppChatListener implements ChatManagerListener {

    @Nonnull
    private XmppRealm realm;

    public XmppChatListener(@Nonnull XmppRealm realm) {
        this.realm = realm;
    }

    @Override
    public void chatCreated(@Nonnull Chat chat, boolean createdLocally) {
        if ( !createdLocally ) {
            final RealmEntity participant = realm.newRealmEntity(chat.getParticipant());

            final RealmEntity realmChat;

            final String realmChatId = chat.getThreadID();
            if (Strings.isEmpty(realmChatId) ) {
                realmChat = getChatService().createPrivateChatId(realm.getUser().getRealmEntity(), participant);
            } else {
                realmChat = realm.newRealmEntity(realmChatId);
            }

            final ApiChatImpl newChat = ApiChatImpl.newInstance(realmChat, 0, true);
            newChat.addParticipant(realm.getUser());
            newChat.addParticipant(UserImpl.newFakeInstance(participant));
            getChatService().saveChat(realm.getUser().getRealmEntity(), newChat);
        }
    }

    @Nonnull
    private ChatService getChatService() {
        return MessengerApplication.getServiceLocator().getChatService();
    }
}
