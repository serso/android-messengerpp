package org.solovyev.android.messenger.xmpp;

import org.jetbrains.annotations.NotNull;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.solovyev.android.messenger.AbstractMessengerApplication;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmEntityImpl;

class XmppChatListener implements ChatManagerListener {

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        if ( !createdLocally ) {
            final RealmEntity participant = RealmEntityImpl.newInstance(XmppRealm.REALM_ID, chat.getParticipant());
            //getChatService().syncChat();
        }
    }

    @NotNull
    private ChatService getChatService() {
        return AbstractMessengerApplication.getServiceLocator().getChatService();
    }
}
