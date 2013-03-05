package org.solovyev.android.messenger.chats;

import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.messages.MessengerMessagesFragment;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* User: serso
* Date: 3/5/13
* Time: 2:00 PM
*/
public final class MessagesFragmentReuseCondition implements JPredicate<Fragment> {

    @Nonnull
    private final Chat chat;

    public MessagesFragmentReuseCondition(@Nonnull Chat chat) {
        this.chat = chat;
    }

    @Nonnull
    public static MessagesFragmentReuseCondition forChat(@Nonnull Chat chat) {
        return new MessagesFragmentReuseCondition(chat);
    }

    @Override
    public boolean apply(@Nullable Fragment fragment) {
        boolean reuse = false;

        if ( fragment instanceof MessengerMessagesFragment) {
            final MessengerMessagesFragment mmf = (MessengerMessagesFragment) fragment;
            if ( chat.equals(mmf.getChat()) ) {
                reuse = true;
            }

        }
        return reuse;
    }
}
