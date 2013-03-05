package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.os.Bundle;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import org.solovyev.android.AThreads;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MessengerRealmsFragment extends AbstractMessengerListFragment<Realm, RealmListItem> {

    @Inject
    @Nonnull
    private RealmService realmService;

    private ActivityMenu<Menu, MenuItem> menu;

    @Nullable
    private JEventListener<RealmEvent> realmEventListener;

    public MessengerRealmsFragment() {
        super("Realms");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        realmEventListener = new UiRealmEventListener();
        realmService.addListener(realmEventListener);
    }

    @Override
    public void onDestroyView() {
        if ( realmEventListener != null ) {
            realmService.removeListener(realmEventListener);
        }
        super.onDestroyView();
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

    @Nonnull
    @Override
    protected MessengerListItemAdapter<RealmListItem> createAdapter() {
        final List<RealmListItem> listItems = new ArrayList<RealmListItem>();
        for (Realm realm : realmService.getRealms()) {
            listItems.add(new RealmListItem(realm));
        }
        return new RealmsAdapter(getActivity(), listItems);
    }

    @Nullable
    @Override
    protected MessengerAsyncTask<Void, Void, List<Realm>> createAsyncLoader(@Nonnull MessengerListItemAdapter<RealmListItem> adapter, @Nonnull Runnable onPostExecute) {
        return null;
    }

    /*
    **********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.menu == null) {
            this.menu = ListActivityMenu.fromResource(R.menu.mpp_realms_menu, RealmsMenu.class, SherlockMenuHelper.getInstance());
        }

        this.menu.onCreateOptionsMenu(this.getActivity(), menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return this.menu.onOptionsItemSelected(this.getActivity(), item) || super.onOptionsItemSelected(item);
    }

    private static enum RealmsMenu implements IdentifiableMenuItem<MenuItem> {
        realm_add(R.id.mpp_realm_add) {
            @Override
            public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
                MessengerRealmDefsActivity.startActivity(context);
            }
        };

        private final int menuItemId;

        RealmsMenu(int menuItemId) {
            this.menuItemId = menuItemId;
        }

        @Nonnull
        @Override
        public Integer getItemId() {
            return this.menuItemId;
        }
    }

    @Nonnull
    @Override
    protected RealmsAdapter getAdapter() {
        return (RealmsAdapter)super.getAdapter();
    }

    private class UiRealmEventListener extends AbstractJEventListener<RealmEvent> {

        public UiRealmEventListener() {
            super(RealmEvent.class);
        }

        @Override
        public void onEvent(@Nonnull final RealmEvent e) {
            AThreads.tryRunOnUiThread(getActivity(), new Runnable() {
                @Override
                public void run() {
                    getAdapter().onRealmEvent(e);
                }
            });
        }
    }
}
