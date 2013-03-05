package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.solovyev.android.messenger.chats.ChatGuiEvent;
import org.solovyev.android.messenger.chats.ChatGuiEventListener;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.RealmGuiEvent;
import org.solovyev.android.messenger.realms.RealmGuiEventListener;
import org.solovyev.android.messenger.users.ContactGuiEvent;
import org.solovyev.android.messenger.users.ContactGuiEventListener;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 3:52 PM
 */
public class MessengerMainActivity extends MessengerFragmentActivity {

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    @Nullable
    private EventListener<RealmGuiEvent> realmGuiEventListener;

    @Nullable
    private EventListener<ContactGuiEvent> contactGuiEventListener;

    @Nullable
    private EventListener<ChatGuiEvent> chatGuiEventListener;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

    public MessengerMainActivity() {
        super(R.layout.msg_main);
    }

    public static void startActivity(@Nonnull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerMainActivity.class);
        activity.startActivity(result);
    }

    /*
    **********************************************************************
    *
    *                           ACTIVITY LIFECYCLE METHODS
    *
    **********************************************************************
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realmGuiEventListener = new RealmGuiEventListener(this);
        getEventManager().registerObserver(RealmGuiEvent.class, realmGuiEventListener);

        contactGuiEventListener = new ContactGuiEventListener(this);
        getEventManager().registerObserver(ContactGuiEvent.class, contactGuiEventListener);

        chatGuiEventListener = new ChatGuiEventListener(this);
        getEventManager().registerObserver(ChatGuiEvent.class, chatGuiEventListener);

        if (isDualPane()) {
            emptifySecondFragment();
        }

        if (isTriplePane()) {
            emptifyThirdFragment();
        }
    }


    @Override
    protected void onDestroy() {
        if (realmGuiEventListener != null) {
            getEventManager().unregisterObserver(RealmGuiEvent.class, realmGuiEventListener);
        }

        if (contactGuiEventListener != null) {
            getEventManager().unregisterObserver(ContactGuiEvent.class, contactGuiEventListener);
        }

        if (chatGuiEventListener != null) {
            getEventManager().unregisterObserver(ChatGuiEvent.class, chatGuiEventListener);
        }

        super.onDestroy();
    }

}
