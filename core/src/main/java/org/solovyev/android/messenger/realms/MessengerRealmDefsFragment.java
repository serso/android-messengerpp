package org.solovyev.android.messenger.realms;

import android.os.Bundle;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MessengerRealmDefsFragment extends AbstractMessengerListFragment<RealmDef, RealmDefListItem> {

    @Inject
    @Nonnull
    private RealmService realmService;

    public MessengerRealmDefsFragment() {
        super("RealmDefs", false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    protected MessengerListItemAdapter<RealmDefListItem> createAdapter() {
        final List<RealmDefListItem> listItems = new ArrayList<RealmDefListItem>();
        for (RealmDef realmDef : realmService.getRealmDefs()) {
            listItems.add(new RealmDefListItem(realmDef));
        }
        return new MessengerListItemAdapter<RealmDefListItem>(getActivity(), listItems);
    }

    @Nullable
    @Override
    protected MessengerAsyncTask<Void, Void, List<RealmDef>> createAsyncLoader(@Nonnull MessengerListItemAdapter<RealmDefListItem> adapter, @Nonnull Runnable onPostExecute) {
        return null;
    }

}