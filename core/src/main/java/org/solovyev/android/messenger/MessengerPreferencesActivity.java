package org.solovyev.android.messenger;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.sync.SyncAllTaskIsAlreadyRunning;
import org.solovyev.android.messenger.sync.SyncService;
import roboguice.inject.InjectPreference;

import java.util.List;

/**
 * User: serso
 * Date: 8/14/12
 * Time: 12:31 AM
 */
public class MessengerPreferencesActivity extends SherlockPreferenceActivity {

    @InjectPreference("reload_data")
    @NotNull
    private Preference reloadData;

    @Inject
    @NotNull
    private SyncService syncService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        reloadData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new MessengerAsyncTask<Void, Void, Void>(MessengerPreferencesActivity.this) {

                    @Override
                    protected Void doWork(@NotNull List<Void> voids) {
                        Context context = getContext();
                        if (context != null) {
                            try {
                                syncService.syncAll(context);
                            } catch (SyncAllTaskIsAlreadyRunning e) {
                                throwException(e);
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onSuccessPostExecute(@Nullable Void result) {
                    }

                    @Override
                    protected void onFailurePostExecute(@NotNull Exception e) {
                        if (e instanceof SyncAllTaskIsAlreadyRunning) {
                            final Context context = getContext();
                            if (context != null) {
                                Toast.makeText(context, getString(R.string.sync_task_is_running), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            super.onFailurePostExecute(e);
                        }
                    }
                }.execute((Void)null);

                return true;
            }
        });
    }
}
