package org.solovyev.android.messenger.messages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.http.RemoteFileService;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatListItem;
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
    @NotNull
    private RemoteFileService remoteFileService;

    @NotNull
    private static final String CHAT_ID = "chat_id";

    public static void startActivity(@NotNull Activity activity, @NotNull Chat chat) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerMessagesActivity.class);
        result.putExtra(CHAT_ID, chat.getId());
        activity.startActivity(result);
    }

    @NotNull
    private Chat chat;

    @Nullable
    private User contact;

    public MessengerMessagesActivity() {
        super(R.layout.msg_main, false, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = this.getIntent();
        if (intent != null) {
            final String chatId = intent.getExtras().getString(CHAT_ID);
            if (chatId != null) {
                final Chat chatFromService = getChatService().getChatById(chatId, this);
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

        getUserService().addUserEventListener(this);

        final List<User> participants = getChatService().getParticipantsExcept(chat.getId(), getUser().getId(), this);
        if (chat.isPrivate()) {
            if (!participants.isEmpty()) {
                contact = participants.get(0);
            }
        }

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        final MessengerMessagesFragment fragment = new MessengerMessagesFragment(chat);
        fragmentTransaction.add(R.id.content_first_pane, fragment);
        fragmentTransaction.commit();

        setTitle(createTitle());
    }

    private String createTitle() {
        if ( contact != null ) {
            return contact.getDisplayName();
        } else {
            return ChatListItem.getDisplayName(chat, getChatService().getLastMessage(chat.getId(), this), getUser());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getUserService().removeUserEventListener(this);
    }

    @Override
    public void onUserEvent(@NotNull final User eventUser, @NotNull final UserEventType userEventType, @Nullable final Object data) {
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
}

