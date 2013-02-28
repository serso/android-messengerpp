package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.RealmConfigurationActivity;
import org.solovyev.android.messenger.messages.MessengerEmptyFragment;
import org.solovyev.android.messenger.messages.MessengerMessagesFragment;
import org.solovyev.common.Builder;
import roboguice.event.EventListener;
import roboguice.event.EventManager;

public class MessengerRealmsActivity extends MessengerFragmentActivity {

    @NotNull
    private static final String REALM_CONFIGURATION_FRAGMENT_TAG = "realm-configuration";

    @NotNull
    private static final String EMPTY_FRAGMENT_TAG = "empty-fragment";

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

        setFragment(R.id.content_first_pane, new MessengerRealmsFragment(), null);
        if ( isDualPane() ) {
            setFragment(R.id.content_second_pane, new MessengerEmptyFragment(), EMPTY_FRAGMENT_TAG);
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
                final FragmentManager fm = getSupportFragmentManager();
                final FragmentTransaction ft = fm.beginTransaction();

                try {
                    final Fragment oldRealmConfigurationFragment = fm.findFragmentByTag(REALM_CONFIGURATION_FRAGMENT_TAG);
                    final Fragment oldEmptyFragment = fm.findFragmentByTag(EMPTY_FRAGMENT_TAG);

                    if ( oldRealmConfigurationFragment != null && oldRealmConfigurationFragment.isAdded() ) {
                        ft.remove(oldRealmConfigurationFragment);
                    }

                    if ( oldEmptyFragment != null ) {
                        ft.add(oldEmptyFragment, EMPTY_FRAGMENT_TAG);
                    } else {
                        setFragment(R.id.content_second_pane, new MessengerEmptyFragment(), EMPTY_FRAGMENT_TAG);
                    }

                } finally {
                    ft.commit();
                }
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
                final FragmentManager fm = getSupportFragmentManager();

                final Fragment oldRealmConfigurationFragment = fm.findFragmentByTag(REALM_CONFIGURATION_FRAGMENT_TAG);
                final Fragment oldEmptyFragment = fm.findFragmentByTag(EMPTY_FRAGMENT_TAG);

                final FragmentTransaction ft = fm.beginTransaction();
                try {
                    boolean replaceFragment = true;
                    if ( oldRealmConfigurationFragment instanceof BaseRealmConfigurationFragment) {
                        final BaseRealmConfigurationFragment oldRealmFragment = ((BaseRealmConfigurationFragment) oldRealmConfigurationFragment);
                        if ( realm.equals(oldRealmFragment.getEditedRealm()) ) {
                            // do nothing - configuration fragment for this item is already opened
                            replaceFragment = false;
                        }
                    }

                    if (replaceFragment) {
                        if (oldRealmConfigurationFragment != null && oldRealmConfigurationFragment.isAdded()) {
                            ft.remove(oldRealmConfigurationFragment);
                        }

                        if ( oldEmptyFragment != null && oldEmptyFragment.isAdded() ) {
                            ft.remove(oldEmptyFragment);
                        }

                        final Bundle fragmentArgs = new Bundle();
                        fragmentArgs.putString(BaseRealmConfigurationFragment.EXTRA_REALM_ID, realm.getId());
                        ft.add(R.id.content_second_pane, Fragment.instantiate(MessengerRealmsActivity.this, realm.getRealmDef().getConfigurationFragmentClass().getName(), fragmentArgs), REALM_CONFIGURATION_FRAGMENT_TAG);
                    }
                } finally {
                    ft.commit();
                }

            } else {
                RealmConfigurationActivity.startForEditRealm(MessengerRealmsActivity.this, realm);
            }
        }
    }

    private void replaceFragment(@NotNull String tag,
                                 int parentViewId,
                                 @NotNull Builder<Fragment> builder) {
        final FragmentManager fm = getSupportFragmentManager();

        final Fragment oldFragment = fm.findFragmentByTag(tag);
        final FragmentTransaction ft = fm.beginTransaction();

        try {
            final Fragment newMessagesFragment;

            if (oldFragment instanceof MessengerEmptyFragment) {
                if (oldFragment.isAdded()) {
                    ft.remove(oldFragment);
                }

                newMessagesFragment = builder.build();
            } else if (oldFragment instanceof MessengerMessagesFragment) {
                final MessengerMessagesFragment oldMessagesFragment = (MessengerMessagesFragment) oldFragment;

                /*if (chat.equals(oldMessagesFragment.getChat())) {
                    // same fragment
                    if (oldMessagesFragment.isDetached()) {
                        ft.attach(oldMessagesFragment);
                    }

                    newMessagesFragment = null;

                } else {*/
                // another fragment
                if (oldMessagesFragment.isAdded()) {
                    ft.remove(oldMessagesFragment);
                }

                newMessagesFragment = builder.build();
               /* }*/
            } else {
                if (oldFragment != null && oldFragment.isAdded()) {
                    ft.remove(oldFragment);
                }

                newMessagesFragment = builder.build();
            }

            if (newMessagesFragment != null) {
                ft.add(parentViewId, newMessagesFragment, tag);
            }
        } finally {
            ft.commit();
        }
    }
}
