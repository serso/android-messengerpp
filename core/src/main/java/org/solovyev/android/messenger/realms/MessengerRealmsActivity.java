package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.inject.Inject;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.MessengerPrimaryFragment;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessengerRealmsActivity extends MessengerFragmentActivity {


    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private EventManager eventManager;

    @Nullable
    private RealmGuiEventListener realmGuiEventListener;

    public MessengerRealmsActivity() {
        super(R.layout.msg_main, false, true);
    }

    public static void startActivity(@Nonnull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerRealmsActivity.class);
        activity.startActivity(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realmGuiEventListener = new RealmGuiEventListener(this);
        eventManager.registerObserver(RealmGuiEvent.class, realmGuiEventListener);

        getFragmentService().setPrimaryFragment(MessengerPrimaryFragment.realms);
        if (isDualPane()) {
            getFragmentService().emptifySecondFragment();
        }

        if (isTriplePane()) {
            getFragmentService().emptifyThirdFragment();
        }
    }

    @Override
    protected void onDestroy() {
        if ( realmGuiEventListener != null ) {
            eventManager.unregisterObserver(RealmGuiEvent.class, realmGuiEventListener);
        }

        super.onDestroy();
    }
}
