package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.MessengerRealmConfigurationActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.common.JPredicate;
import roboguice.event.EventListener;
import roboguice.event.EventManager;

public class MessengerRealmsActivity extends MessengerFragmentActivity {


    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    @Inject
    @NotNull
    private EventManager eventManager;

    @Nullable
    private RealmClickedEventListener realmClickedEventListener;

    @Nullable
    private RealmConfigurationEventListener realmConfigurationEventListener;

    @Nullable
    private EditRealmEventListener editRealmEventListener;


    public MessengerRealmsActivity() {
        super(R.layout.msg_main, false, true);
    }

    public static void startActivity(@NotNull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerRealmsActivity.class);
        activity.startActivity(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realmClickedEventListener = new RealmClickedEventListener();
        eventManager.registerObserver(MessengerRealmsFragment.RealmClickedEvent.class, realmClickedEventListener);

        realmConfigurationEventListener = new RealmConfigurationEventListener();
        eventManager.registerObserver(RealmFragmentFinishedEvent.class, realmConfigurationEventListener);

        editRealmEventListener = new EditRealmEventListener();
        eventManager.registerObserver(MessengerRealmFragment.EditRealmEvent.class, editRealmEventListener);

        setFirstFragment(MessengerRealmsFragment.class, null, new JPredicate<Fragment>() {
            @Override
            public boolean apply(@Nullable Fragment fragment) {
                return fragment instanceof MessengerRealmsFragment;
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
        if ( realmClickedEventListener != null ) {
            eventManager.unregisterObserver(MessengerRealmsFragment.RealmClickedEvent.class, realmClickedEventListener);
        }

        if ( realmConfigurationEventListener != null ) {
            eventManager.unregisterObserver(RealmFragmentFinishedEvent.class, realmConfigurationEventListener);
        }

        if ( editRealmEventListener != null ) {
            eventManager.unregisterObserver(MessengerRealmFragment.EditRealmEvent.class, editRealmEventListener);
        }

        super.onDestroy();
    }

    private class RealmConfigurationEventListener implements EventListener<RealmFragmentFinishedEvent> {

        @Override
        public void onEvent(@NotNull RealmFragmentFinishedEvent event) {
            if (isDualPane()) {
                if (event.isRemoved()) {
                    emptifySecondFragment();
                } else {
                    final Bundle fragmentArgs = new Bundle();
                    fragmentArgs.putString(MessengerRealmFragment.EXTRA_REALM_ID, event.getRealm().getId());
                    setSecondFragment(MessengerRealmFragment.class, fragmentArgs, new RealmFragmentReuseCondition(event.getRealm()));
                }
            } else {
                finish();
            }
        }
    }

    private class EditRealmEventListener implements EventListener<MessengerRealmFragment.EditRealmEvent> {
        @Override
        public void onEvent(@NotNull MessengerRealmFragment.EditRealmEvent event) {
            final Realm realm = event.getRealm();

            if (isDualPane()) {
                final Bundle fragmentArgs = new Bundle();
                fragmentArgs.putString(BaseRealmConfigurationFragment.EXTRA_REALM_ID, realm.getId());
                setSecondFragment(realm.getRealmDef().getConfigurationFragmentClass(), fragmentArgs, null);
            } else {
                MessengerRealmConfigurationActivity.startForEditRealm(MessengerRealmsActivity.this, realm);
            }
        }
    }

    private class RealmClickedEventListener implements EventListener<MessengerRealmsFragment.RealmClickedEvent> {
        @Override
        public void onEvent(@NotNull MessengerRealmsFragment.RealmClickedEvent event) {
            final Realm realm = event.getRealm();

            if (isDualPane()) {
                final Bundle fragmentArgs = new Bundle();
                fragmentArgs.putString(MessengerRealmFragment.EXTRA_REALM_ID, realm.getId());
                setSecondFragment(MessengerRealmFragment.class, fragmentArgs, new RealmFragmentReuseCondition(realm));
            } else {
                MessengerRealmConfigurationActivity.startForEditRealm(MessengerRealmsActivity.this, realm);
            }
        }
    }

    private static class RealmFragmentReuseCondition implements JPredicate<Fragment> {

        @NotNull
        private final Realm realm;

        public RealmFragmentReuseCondition(@NotNull Realm realm) {
            this.realm = realm;
        }

        @Override
        public boolean apply(@Nullable Fragment fragment) {
            if (fragment instanceof MessengerRealmFragment) {
                final MessengerRealmFragment oldRealmFragment = ((MessengerRealmFragment) fragment);
                if (realm.equals(oldRealmFragment.getRealm())) {
                    // do nothing - configuration fragment for this item is already opened
                    return true;
                }
            }

            return false;
        }

    }
}
