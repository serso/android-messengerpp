package org.solovyev.android.messenger.realms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.common.JPredicate;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:50 PM
 */
public class RealmGuiEventListener implements EventListener<RealmGuiEvent> {

    @Nonnull
    private static final String TAG = RealmGuiEventListener.class.getSimpleName();

    @Nonnull
    private final MessengerFragmentActivity activity;

    public RealmGuiEventListener(@Nonnull MessengerFragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onEvent(@Nonnull RealmGuiEvent event) {
        final Realm realm = event.getRealm();

        switch (event.getType()) {
            case realm_clicked:
                handleRealmClickedEvent(realm);
                break;
            case realm_edit_requested:
                handleRealmEditRequested(realm);
                break;
            case realm_edit_finished:
                handleRealmEditFinishedEvent(event);
                break;
        }
    }

    private void handleRealmEditRequested(@Nonnull Realm realm) {
        if (activity.isDualPane()) {
            final Bundle fragmentArgs = new Bundle();
            fragmentArgs.putString(BaseRealmConfigurationFragment.EXTRA_REALM_ID, realm.getId());
            activity.getFragmentService().setSecondFragment(realm.getRealmDef().getConfigurationFragmentClass(), fragmentArgs, null, BaseRealmConfigurationFragment.FRAGMENT_TAG);
        } else {
            MessengerRealmConfigurationActivity.startForEditRealm(activity, realm);
        }
    }

    private void handleRealmClickedEvent(@Nonnull Realm realm) {
        if (activity.isDualPane()) {
            final Bundle fragmentArgs = new Bundle();
            fragmentArgs.putString(MessengerRealmFragment.EXTRA_REALM_ID, realm.getId());
            activity.getFragmentService().setSecondFragment(MessengerRealmFragment.class, fragmentArgs, RealmFragmentReuseCondition.forRealm(realm), MessengerRealmFragment.FRAGMENT_TAG);
            if ( activity.isTriplePane() ) {
                activity.getFragmentService().emptifyThirdFragment();
            }
        } else {
            MessengerRealmConfigurationActivity.startForEditRealm(activity, realm);
        }
    }

    private void handleRealmEditFinishedEvent(@Nonnull RealmGuiEvent event) {
        if (activity.isDualPane()) {
            final Object data = event.getData();
            if (data instanceof Boolean) {
                final Boolean removed = (Boolean) data;
                if (removed) {
                    activity.getFragmentService().emptifySecondFragment();
                } else {
                    final Bundle fragmentArgs = new Bundle();
                    fragmentArgs.putString(MessengerRealmFragment.EXTRA_REALM_ID, event.getRealm().getId());
                    activity.getFragmentService().setSecondFragment(MessengerRealmFragment.class, fragmentArgs, RealmFragmentReuseCondition.forRealm(event.getRealm()), MessengerRealmFragment.FRAGMENT_TAG);
                }
            } else {
                Log.e(TAG, "Data is not instance of Boolean in " + event.getType() + " event!");
            }
        } else {
            activity.finish();
        }
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    /**
     * Fragment will be reused if it's instance of {@link MessengerRealmFragment} and
     * contains same realm as one passed in constructor
     */
    private static class RealmFragmentReuseCondition extends AbstractFragmentReuseCondition<MessengerRealmFragment> {

        @Nonnull
        private final Realm realm;

        private RealmFragmentReuseCondition(@Nonnull Realm realm) {
            super(MessengerRealmFragment.class);
            this.realm = realm;
        }

        @Nonnull
        public static JPredicate<Fragment> forRealm(@Nonnull Realm realm) {
            return new RealmFragmentReuseCondition(realm);
        }

        @Override
        protected boolean canReuseFragment(@Nonnull MessengerRealmFragment fragment) {
            return realm.equals(fragment.getRealm());
        }
    }
}
