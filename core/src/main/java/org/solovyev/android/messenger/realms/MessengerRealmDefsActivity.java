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
    private RealmDefClickedEventListener realmDefClickedEventListener;

    @Nullable
    private RealmConfigurationEventListener realmConfigurationEventListener;

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

        realmDefClickedEventListener = new RealmDefClickedEventListener();
        eventManager.registerObserver(MessengerRealmDefsFragment.RealmDefClickedEvent.class, realmDefClickedEventListener);

        realmConfigurationEventListener = new RealmConfigurationEventListener();
        eventManager.registerObserver(RealmDefFragmentFinishedEvent.class, realmConfigurationEventListener);

        setFirstFragment(MessengerRealmDefsFragment.class, null, new JPredicate<Fragment>() {
            @Override
            public boolean apply(@Nullable Fragment fragment) {
                return fragment instanceof MessengerRealmDefsFragment;
            }
        });
        if (isDualPane()) {
            emptifySecondFragment();
        }

        if (isTriplePane()) {
            emptifyThirdFragment();
        }
    }

    @Override
    protected void onDestroy() {
        if ( realmDefClickedEventListener != null ) {
            eventManager.unregisterObserver(MessengerRealmDefsFragment.RealmDefClickedEvent.class, realmDefClickedEventListener);
        }

        if ( realmConfigurationEventListener != null ) {
            eventManager.unregisterObserver(RealmDefFragmentFinishedEvent.class, realmConfigurationEventListener);
        }

        super.onDestroy();
    }

    private class RealmDefClickedEventListener implements EventListener<MessengerRealmDefsFragment.RealmDefClickedEvent> {

        @Override
        public void onEvent(@Nonnull MessengerRealmDefsFragment.RealmDefClickedEvent event) {
            final RealmDef realmDef = event.getRealmDef();

            if (isDualPane()) {
                setSecondFragment(realmDef.getConfigurationFragmentClass(), null, new RealmDefFragmentReuseCondition(realmDef));
            } else {
                MessengerRealmConfigurationActivity.startForNewRealm(MessengerRealmDefsActivity.this, realmDef);
            }
        }
    }

    private class RealmConfigurationEventListener implements EventListener<RealmDefFragmentFinishedEvent> {
        @Override
        public void onEvent(@Nonnull RealmDefFragmentFinishedEvent event) {
            if (isDualPane()) {
                emptifySecondFragment();
            } else {
                finish();
            }
        }
    }


    private static class RealmDefFragmentReuseCondition implements JPredicate<Fragment> {

        @Nonnull
        private final RealmDef realmDef;

        public RealmDefFragmentReuseCondition(@Nonnull RealmDef realmDef) {
            this.realmDef = realmDef;
        }

        @Override
        public boolean apply(@Nullable Fragment fragment) {
            if (fragment instanceof BaseRealmConfigurationFragment) {
                final BaseRealmConfigurationFragment oldRealmFragment = ((BaseRealmConfigurationFragment) fragment);
                if (realmDef.equals(oldRealmFragment.getRealmDef())) {
                    // do nothing - configuration fragment for this item is already opened
                    return true;
                }
            }

            return false;
        }
    }
}
