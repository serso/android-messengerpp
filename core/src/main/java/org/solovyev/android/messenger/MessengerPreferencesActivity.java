package org.solovyev.android.messenger;

import android.os.Bundle;
import android.preference.Preference;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.MessengerSyncAllAsyncTask;
import org.solovyev.android.messenger.sync.SyncService;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/14/12
 * Time: 12:31 AM
 */
public class MessengerPreferencesActivity extends SherlockPreferenceActivity {

    @Nonnull
    private Preference reloadData;

    @Nonnull
    private SyncService syncService = MessengerApplication.getServiceLocator().getSyncService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        reloadData = findPreference("reload_data");

        reloadData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MessengerSyncAllAsyncTask.newForAllRealms(MessengerPreferencesActivity.this, syncService).execute((Void) null);
                return true;
            }
        });
    }

}
