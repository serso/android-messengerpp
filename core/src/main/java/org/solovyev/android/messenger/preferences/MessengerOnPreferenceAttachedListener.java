package org.solovyev.android.messenger.preferences;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.MessengerSyncAllAsyncTask;
import org.solovyev.android.messenger.sync.SyncService;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 7:35 PM
 */
public final class MessengerOnPreferenceAttachedListener implements PreferenceListFragment.OnPreferenceAttachedListener {

    @Nonnull
    private final Context context;

    @Nonnull
    private final SyncService syncService;

    public MessengerOnPreferenceAttachedListener(@Nonnull Context context, @Nonnull SyncService syncService) {
        this.context = context;
        this.syncService = syncService;
    }

    @Override
    public void onPreferenceAttached(PreferenceScreen preferenceScreen, int preferenceResId) {
        if (preferenceResId == R.xml.mpp_preferences_others) {
            onOtherPreferencesAttached(preferenceScreen);
        }
    }

    private void onOtherPreferencesAttached(PreferenceScreen preferenceScreen) {
        final Preference reloadData = preferenceScreen.findPreference("reload_data");

        reloadData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // todo serso: show user message that action has been started
                MessengerSyncAllAsyncTask.newForAllRealms(context, syncService).execute((Void) null);
                return true;
            }
        });
    }
}
