package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
* User: serso
* Date: 3/12/13
* Time: 8:11 PM
*/
class XmppMessageListener implements MessageListener {

    @Nonnull
    private Realm realm;

    @Nonnull
    private final Entity realmChat;

    XmppMessageListener(@Nonnull Realm realm, @Nonnull Entity realmChat) {
        this.realm = realm;
        this.realmChat = realmChat;
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        Log.i("M++/Xmpp", "Message created: " + message.getBody());
        getChatService().saveChatMessages(realmChat, XmppChatListener.toMessages(realm, Arrays.asList(message)), false);
    }

    @Nonnull
    private static ChatService getChatService() {
        return MessengerApplication.getServiceLocator().getChatService();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XmppMessageListener that = (XmppMessageListener) o;

        if (!realmChat.equals(that.realmChat)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return realmChat.hashCode();
    }
}
