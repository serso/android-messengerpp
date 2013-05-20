package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatGuiEventType;
import org.solovyev.android.messenger.realms.RealmException;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:54 PM
 */
public final class ContactGuiEventListener implements EventListener<ContactGuiEvent> {

	@Nonnull
	private final MessengerFragmentActivity activity;

	public ContactGuiEventListener(@Nonnull MessengerFragmentActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onEvent(@Nonnull ContactGuiEvent event) {
		final User contact = event.getContact();
		final ContactGuiEventType type = event.getType();

		if (type == ContactGuiEventType.contact_clicked) {

			new MessengerAsyncTask<Void, Void, Chat>() {

				@Override
				protected Chat doWork(@Nonnull List<Void> params) {
					Chat result = null;

					try {
						final User user = activity.getRealmService().getRealmById(contact.getEntity().getRealmId()).getUser();
						result = MessengerApplication.getServiceLocator().getChatService().getPrivateChat(user.getEntity(), contact.getEntity());
					} catch (RealmException e) {
						throwException(e);
					}

					return result;
				}

				@Override
				protected void onSuccessPostExecute(@Nullable Chat chat) {
					if (chat != null) {
						activity.getEventManager().fire(ChatGuiEventType.chat_clicked.newEvent(chat));
					}
				}

			}.execute(null, null);
		}
	}
}
