package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.realms.ConfiguredRealm;
import org.solovyev.android.messenger.realms.ConfiguredRealmListItem;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class MessengerRealmsActivity extends MessengerFragmentActivity {

    public MessengerRealmsActivity() {
        super(R.layout.msg_main);
    }

    public static void startActivity(@NotNull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerRealmsActivity.class);
        activity.startActivity(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFragment(R.id.content_first_pane, new MessengerRealmsListFragment());
    }

    public static class MessengerRealmsListFragment extends AbstractMessengerListFragment<ConfiguredRealm, ConfiguredRealmListItem> {

        @Inject
        @NotNull
        private RealmService realmService;

        public MessengerRealmsListFragment() {
            super("ConfiguredRealms");
        }

        @Override
        protected boolean isFilterEnabled() {
            return false;
        }

        @Nullable
        @Override
        protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
            return null;
        }

        @Nullable
        @Override
        protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
            return null;
        }

        @NotNull
        @Override
        protected AbstractMessengerListItemAdapter<ConfiguredRealmListItem> createAdapter() {
            final List<ConfiguredRealmListItem> listItems = new ArrayList<ConfiguredRealmListItem>();
            for (ConfiguredRealm configuredRealm : realmService.getConfiguredRealms()) {
                listItems.add(new ConfiguredRealmListItem(configuredRealm));
            }
            return new MessengerConfiguredRealmListItemAdapter(getActivity(), listItems, getUser());
        }

        @Nullable
        @Override
        protected MessengerAsyncTask<Void, Void, List<ConfiguredRealm>> createAsyncLoader(@NotNull AbstractMessengerListItemAdapter<ConfiguredRealmListItem> adapter, @NotNull Runnable onPostExecute) {
            return null;
        }
    }
}
