package org.solovyev.android.messenger.messages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatListItem;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.security.UserIsNotLoggedInException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;
import org.solovyev.android.messenger.users.UserEventType;

import java.util.List;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 10:42 PM
 */
public class MessengerMessagesActivity extends MessengerFragmentActivity implements UserEventListener {

    @Inject
    @Nonnull
    private ImageLoader imageLoader;

    @Nonnull
    private static final String CHAT = "chat";

    public static void startActivity(@Nonnull Activity activity, @Nonnull Chat chat) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerMessagesActivity.class);
        result.putExtra(CHAT, chat.getRealmChat());
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

        final List<User> participants = getChatService().getParticipantsExcept(chat.getRealmChat(), getUser().getRealmUser());
        if (chat.isPrivate()) {
            if (!participants.isEmpty()) {
                contact = participants.get(0);
            }
        }

        setFragment(R.id.content_first_pane, new MessengerMessagesFragment(chat), null);

        setTitle(createTitle());
    }

    private String createTitle() {
        if ( contact != null ) {
            return contact.getDisplayName();
        } else {
            return ChatListItem.getDisplayName(chat, getChatService().getLastMessage(chat.getRealmChat()), getUser());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getUserService().removeListener(this);
    }

    @Override
    public void onUserEvent(@Nonnull final User eventUser, @Nonnull final UserEventType userEventType, @Nullable final Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contact != null) {
                    if (userEventType == UserEventType.contact_online) {
                        final User eventContact = (User) data;
                        if (contact.equals(eventContact)) {
                            //changeOnlineStatus(true);
                            contact = eventContact;
                        }
                    }

                    if (userEventType == UserEventType.contact_offline) {
                        final User eventContact = (User) data;
                        if (contact.equals(eventContact)) {
                            //changeOnlineStatus(false);
                            contact = eventContact;
                        }
                    }

                    if (userEventType == UserEventType.changed) {
                        if (eventUser.equals(contact)) {
                            contact = eventUser;
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
            try {
                user = MessengerApplication.getServiceLocator().getAuthService().getUser(this.chat.getRealmChat().getRealmId());
            } catch (UserIsNotLoggedInException e) {
                // todo serso: continue
            }
        }
        return user;
    }
}

