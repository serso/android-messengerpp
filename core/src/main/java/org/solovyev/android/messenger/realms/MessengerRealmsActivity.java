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
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.messages.MessengerEmptyFragment;
import org.solovyev.common.JPredicate;
import roboguice.event.EventListener;
import roboguice.event.EventManager;

public class MessengerRealmsActivity extends MessengerFragmentActivity {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */
    @NotNull
    private static final String REALM_CONFIGURATION_FRAGMENT_TAG = "realm-configuration";

    @NotNull
    private static final String REALM_LIST_FRAGMENT_TAG = "realm-list";

    @NotNull
    private static final String EMPTY_FRAGMENT_TAG = "empty-fragment";

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
        eventManager.registerObserver(BaseRealmConfigurationFragment.FinishedEvent.class, realmConfigurationEventListener);

        setFragment(R.id.content_first_pane, MessengerRealmsFragment.class, REALM_LIST_FRAGMENT_TAG, null);
        if ( isDualPane() ) {
            setFragment(R.id.content_second_pane, MessengerEmptyFragment.class, EMPTY_FRAGMENT_TAG, null);
        }
    }

    @Override
    protected void onDestroy() {
        if ( realmClickedEventListener != null ) {
            eventManager.unregisterObserver(MessengerRealmsFragment.RealmClickedEvent.class, realmClickedEventListener);
        }

        if ( realmConfigurationEventListener != null ) {
            eventManager.unregisterObserver(BaseRealmConfigurationFragment.FinishedEvent.class, realmConfigurationEventListener);
        }

        super.onDestroy();
    }

    private class RealmConfigurationEventListener implements EventListener<BaseRealmConfigurationFragment.FinishedEvent> {

        @Override
        public void onEvent(@NotNull BaseRealmConfigurationFragment.FinishedEvent event) {
            if (isDualPane()) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        }
    }

    private class RealmClickedEventListener implements EventListener<MessengerRealmsFragment.RealmClickedEvent> {
        @Override
        public void onEvent(@NotNull MessengerRealmsFragment.RealmClickedEvent event) {
            final Realm realm = event.getRealm();

            if (isDualPane()) {

                final Bundle fragmentArgs = new Bundle();
                fragmentArgs.putString(BaseRealmConfigurationFragment.EXTRA_REALM_ID, realm.getId());
                trySetFragment(R.id.content_second_pane, realm.getRealmDef().getConfigurationFragmentClass(), REALM_CONFIGURATION_FRAGMENT_TAG, fragmentArgs, new JPredicate<Fragment>() {
                    @Override
                    public boolean apply(@Nullable Fragment fragment) {
                        if (fragment instanceof BaseRealmConfigurationFragment) {
                            final BaseRealmConfigurationFragment oldRealmFragment = ((BaseRealmConfigurationFragment) fragment);
                            if (realm.equals(oldRealmFragment.getEditedRealm())) {
                                // do nothing - configuration fragment for this item is already opened
                                return true;
                            }
                        }

                        return false;
                    }
                }, true, EMPTY_FRAGMENT_TAG);

            } else {
                MessengerRealmConfigurationActivity.startForEditRealm(MessengerRealmsActivity.this, realm);
            }
        }
    }
}
