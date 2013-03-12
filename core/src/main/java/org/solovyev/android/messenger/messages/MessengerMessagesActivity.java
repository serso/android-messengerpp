package org.solovyev.android.messenger.messages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.Chats;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.users.*;
import org.solovyev.common.Builder;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 10:42 PM
 */
public class MessengerMessagesActivity extends MessengerFragmentActivity implements JEventListener<UserEvent> {

    @Inject
    @Nonnull
    private ImageLoader imageLoader;

    @Nonnull
    private static final String CHAT = "chat";

    public static void startActivity(@Nonnull Activity activity, @Nonnull Chat chat) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerMessagesActivity.class);
        result.putExtra(CHAT, chat.getEntity());
        activity.startActivity(result);
    }

    @Nonnull
    private Chat chat;

    @Nullable
    private User contact;

    @Nullable
    private User user;

    public MessengerMessagesActivity() {
        super(R.layout.msg_main, false, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = this.getIntent();
        if (intent != null) {
            final RealmEntity realmChat = intent.getExtras().getParcelable(CHAT);
            if (realmChat != null) {
                final Chat chatFromService = getChatService().getChatById(realmChat);
                if (chatFromService != null) {
                    this.chat = chatFromService;
                } else {
                    this.finish();
                }
            } else {
                this.finish();
            }
        } else {
            this.finish();
        }

        getUserService().addListener(this);

        final List<User> participants = getChatService().getParticipantsExcept(chat.getEntity(), getUser().getEntity());
        if (chat.isPrivate()) {
            if (!participants.isEmpty()) {
                contact = participants.get(0);
            }
        }

        getFragmentService().setFirstFragment(new Builder<Fragment>() {
            @Nonnull
            @Override
            public Fragment build() {
                return new MessengerMessagesFragment(chat);
            }
        }, null, MessengerMessagesFragment.FRAGMENT_TAG);

        setTitle(createTitle());
    }

    private String createTitle() {
        if ( contact != null ) {
            return Users.getDisplayNameFor(contact);
        } else {
            return Chats.getDisplayName(chat, getChatService().getLastMessage(chat.getEntity()), getUser());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getUserService().removeListener(this);
    }

    @Nonnull
    @Override
    public Class<UserEvent> getEventType() {
        return UserEvent.class;
    }

    @Override
    public void onEvent(@Nonnull final UserEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final UserEventType type = event.getType();
                if (contact != null) {
                    if (type == UserEventType.contact_online) {
                        final User eventContact = event.getDataAsUser();
                        if (contact.equals(eventContact)) {
                            //changeOnlineStatus(true);
                            contact = eventContact;
                        }
                    }

                    if (type == UserEventType.contact_offline) {
                        final User eventContact = event.getDataAsUser();
                        if (contact.equals(eventContact)) {
                            //changeOnlineStatus(false);
                            contact = eventContact;
                        }
                    }

                    if (type == UserEventType.changed) {
                        if (event.getUser().equals(contact)) {
                            contact = event.getUser();
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return false;
    }

    @Nonnull
    public User getUser() {
        if ( user == null ) {
            user = getRealmService().getRealmById(chat.getEntity().getRealmId()).getUser();
        }
        return user;
    }
}

