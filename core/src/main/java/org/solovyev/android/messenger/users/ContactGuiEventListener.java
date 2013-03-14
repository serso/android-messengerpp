package org.solovyev.android.messenger.users;

import android.os.AsyncTask;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatGuiEventType;
import org.solovyev.android.messenger.realms.UnsupportedRealmException;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:54 PM
 */
public class ContactGuiEventListener implements EventListener<ContactGuiEvent> {

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

            new AsyncTask<Void, Void, Chat>() {

                @Override
                protected Chat doInBackground(Void... params) {
                    try {
                        final User user = activity.getRealmService().getRealmById(contact.getEntity().getRealmId()).getUser();
                        return MessengerApplication.getServiceLocator().getChatService().getPrivateChat(user.getEntity(), contact.getEntity());
                    } catch (UnsupportedRealmException e) {
                        throw new AssertionError(e);
                    }
                }

                @Override
                protected void onPostExecute(@Nonnull Chat chat) {
                    super.onPostExecute(chat);

                    activity.getEventManager().fire(ChatGuiEventType.chat_clicked.newEvent(chat));
                }

            }.execute(null, null);
        }
    }
}
