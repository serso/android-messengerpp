package org.solovyev.android.messenger.chats;

import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.messages.MessengerMessagesActivity;
import org.solovyev.android.messenger.messages.MessengerMessagesFragment;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.ContactFragmentReuseCondition;
import org.solovyev.android.messenger.users.MessengerContactFragment;
import org.solovyev.android.messenger.users.MessengerContactsInfoFragment;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.Builder;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:59 PM
 */
public class ChatGuiEventListener implements EventListener<ChatGuiEvent> {

    @Nonnull
    private static final String TAG = ChatGuiEventListener.class.getSimpleName();

    @Nonnull
    private final MessengerFragmentActivity activity;

    public ChatGuiEventListener(@Nonnull MessengerFragmentActivity activity) {
        this.activity = activity;
    }


    @Override
    public void onEvent(ChatGuiEvent event) {
        final Chat chat = event.getChat();
        final ChatGuiEventType type = event.getType();

        if (type == ChatGuiEventType.chat_clicked) {

            if (activity.isDualPane()) {
                activity.setSecondFragment(new Builder<Fragment>() {
                    @Nonnull
                    @Override
                    public Fragment build() {
                        return new MessengerMessagesFragment(chat);
                    }
                }, MessagesFragmentReuseCondition.forChat(chat));

                if (activity.isTriplePane()) {
                    if (chat.isPrivate()) {
                        activity.setThirdFragment(new Builder<Fragment>() {
                            @Nonnull
                            @Override
                            public Fragment build() {
                                return new MessengerContactFragment(chat.getSecondUser());
                            }
                        }, ContactFragmentReuseCondition.forContact(chat.getSecondUser()));
                    } else {
                        activity.setThirdFragment(new Builder<Fragment>() {
                            @Nonnull
                            @Override
                            public Fragment build() {
                                final List<User> participants = new ArrayList<User>();
                                for (Realm realm : activity.getRealmService().getRealms()) {
                                    final User user = realm.getUser();
                                    participants.addAll(activity.getChatService().getParticipantsExcept(chat.getRealmChat(), user.getRealmUser()));

                                }
                                return new MessengerContactsInfoFragment(participants);
                            }
                        }, null);
                    }
                }

            } else {
                MessengerMessagesActivity.startActivity(activity, chat);
            }
        }
    }
}
