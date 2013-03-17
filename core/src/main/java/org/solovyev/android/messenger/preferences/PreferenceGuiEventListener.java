package org.solovyev.android.messenger.preferences;

import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.MessengerFragmentService;
import org.solovyev.common.Builder;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 6:28 PM
 */
public final class PreferenceGuiEventListener implements EventListener<PreferenceGuiEvent> {

    @Nonnull
    private final MessengerFragmentActivity activity;

    public PreferenceGuiEventListener(@Nonnull MessengerFragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onEvent(@Nonnull PreferenceGuiEvent event) {
        final MessengerFragmentService fragmentService = activity.getFragmentService();
        final PreferenceGroup preferenceGroup = event.getPreferenceScreen();

        if (event.isOfType(PreferenceGuiEventType.preference_group_clicked)) {
            final int preferencesResId = preferenceGroup.getPreferencesResId();

            if (activity.isDualPane()) {
                fragmentService.setSecondFragment(new Builder<Fragment>() {
                    @Nonnull
                    @Override
                    public Fragment build() {
                        return new MessengerPreferenceListFragment(preferencesResId, R.layout.mpp_preferences_fragment);
                    }
                }, PreferenceListFragmentReuseCondition.newInstance(preferencesResId), PreferenceListFragment.FRAGMENT_TAG);
            } else {
                fragmentService.setFirstFragment(new Builder<Fragment>() {
                    @Nonnull
                    @Override
                    public Fragment build() {
                        return new MessengerPreferenceListFragment(preferencesResId, R.layout.mpp_preferences_fragment);
                    }
                }, PreferenceListFragmentReuseCondition.newInstance(preferencesResId), PreferenceListFragment.FRAGMENT_TAG, true);
            }
        }
    }
}
