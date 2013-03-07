package org.solovyev.android.messenger.users;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.ToggleFilterInputMenuItem;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 4:09 PM
 */
public class MessengerContactsFragment extends AbstractMessengerContactsFragment {

    @Nonnull
    private static final String MODE = "mode";

    @Nonnull
    private MessengerContactsMode mode = MessengerContactsMode.all_contacts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            final Object mode = savedInstanceState.getSerializable(MODE);
            if (mode instanceof MessengerContactsMode) {
                changeMode((MessengerContactsMode) mode);
            }
        }
    }

    @Nonnull
    protected AbstractAsyncLoader<UserContact, ContactListItem> createAsyncLoader(@Nonnull MessengerListItemAdapter<ContactListItem> adapter, @Nonnull Runnable onPostExecute) {
        return new ContactsAsyncLoader(getActivity(), adapter, onPostExecute, getRealmService());
    }

    @Override
    protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
        return new AbstractOnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    getSyncService().sync(SyncTask.user_contacts, new Runnable() {
                        @Override
                        public void run() {
                            completeRefresh();
                        }
                    });
                    Toast.makeText(getActivity(), "User contacts sync started!", Toast.LENGTH_SHORT).show();
                } catch (TaskIsAlreadyRunningException e) {
                    e.showMessage(getActivity());
                }
            }
        };
    }

    @Nonnull
    protected AbstractContactsAdapter createAdapter() {
        return new ContactsAdapter(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(MODE, mode);
    }

    private void changeMode(@Nonnull MessengerContactsMode newMode) {
        mode = newMode;
        ((AbstractContactsAdapter) getAdapter()).setMode(newMode);
    }

    /*
    **********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

    private ActivityMenu<Menu, MenuItem> menu;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return this.menu.onOptionsItemSelected(this.getActivity(), item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();

        menuItems.add(new ToggleContactsMenuItem());
        menuItems.add(new ToggleFilterInputMenuItem(this));

        this.menu = ListActivityMenu.fromResource(R.menu.contacts, menuItems, SherlockMenuHelper.getInstance());
        this.menu.onCreateOptionsMenu(this.getActivity(), menu);
    }

    private class ToggleContactsMenuItem implements IdentifiableMenuItem<MenuItem> {

        @Nonnull
        @Override
        public Integer getItemId() {
            return R.id.toggle_contacts;
        }

        @Override
        public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
            changeMode(mode == MessengerContactsMode.only_online_contacts ? MessengerContactsMode.all_contacts : MessengerContactsMode.only_online_contacts);
        }
    }
}
