package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.packet.Message;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmException;

import javax.annotation.Nonnull;
import java.util.Collections;

final class XmppChatListener implements ChatManagerListener {

	@Nonnull
	private Realm realm;

	public XmppChatListener(@Nonnull Realm realm) {
		this.realm = realm;
	}

	@Override
	public void chatCreated(@Nonnull Chat chat, boolean createdLocally) {
		Log.i("M++/Xmpp", "Chat created!");

		if (!createdLocally) {
			ApiChat newChat = XmppRealm.toApiChat(chat, Collections.<Message>emptyList(), realm);
			try {
				newChat = getChatService().saveChat(realm.getUser().getEntity(), newChat);
				chat.addMessageListener(new XmppMessageListener(realm, newChat.getChat().getEntity()));
			} catch (RealmException e) {
				MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
			}
		}
	}

	@Nonnull
	private static ChatService getChatService() {
		return MessengerApplication.getServiceLocator().getChatService();
	}
}
