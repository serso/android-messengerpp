package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.google.inject.Inject;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.MessengerRealmConfigurationActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.common.JPredicate;
import roboguice.event.EventListener;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessengerRealmDefsActivity extends MessengerFragmentActivity {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */


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
    private EventListener<RealmDefGuiEvent> realmDefGuiEventListener;

    public MessengerRealmDefsActivity() {
        super(R.layout.msg_main, false, true);
    }

    public static void startActivity(@Nonnull Context context) {
        final Intent result = new Intent();
        result.setClass(context, MessengerRealmDefsActivity.class);
        context.startActivity(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realmDefGuiEventListener = new RealmDefClickedEventListener();
        eventManager.registerObserver(RealmDefGuiEvent.class, realmDefGuiEventListener);

        getFragmentService().setFirstFragment(MessengerRealmDefsFragment.class, null, new JPredicate<Fragment>() {
            @Override
            public boolean apply(@Nullable Fragment fragment) {
                return fragment instanceof MessengerRealmDefsFragment;
            }
        }, "realm-defs");

        if (isDualPane()) {
            getFragmentService().emptifySecondFragment();
        }

        if (isTriplePane()) {
            getFragmentService().emptifyThirdFragment();
        }
    }

    @Override
    protected void onDestroy() {
        if ( realmDefGuiEventListener != null ) {
            eventManager.unregisterObserver(RealmDefGuiEvent.class, realmDefGuiEventListener);
        }

        super.onDestroy();
    }

    private class RealmDefClickedEventListener implements EventListener<RealmDefGuiEvent> {

        @Override
        public void onEvent(@Nonnull RealmDefGuiEvent event) {
            final RealmDef realmDef = event.getRealmDef();

            switch (event.getType()) {
                case realm_def_clicked:
                    if (isDualPane()) {
                        getFragmentService().setSecondFragment(realmDef.getConfigurationFragmentClass(), null, new RealmDefFragmentReuseCondition(realmDef), BaseRealmConfigurationFragment.FRAGMENT_TAG);
                    } else {
                        MessengerRealmConfigurationActivity.startForNewRealm(MessengerRealmDefsActivity.this, realmDef);
                    }
                    break;
                case realm_def_edit_finished:
                    if (isDualPane()) {
                        getFragmentService().emptifySecondFragment();
                    } else {
                        finish();
                    }
                    break;
            }
        }
    }

}
