package org.solovyev.android.messenger.chats;

import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.Threads2;
import org.solovyev.android.messenger.ToggleFilterInputMenuItem;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:37 PM
 */
public final class MessengerRecentChatsFragment extends AbstractChatsFragment {

	public MessengerRecentChatsFragment() {
		super();
	}

	@Nonnull
	@Override
	protected RecentChatsAdapter createAdapter() {
		return new RecentChatsAdapter(getActivity());
	}

	@Nullable
	@Override
	protected MessengerAsyncTask<Void, Void, List<UiChat>> createAsyncLoader(@Nonnull MessengerListItemAdapter<ChatListItem> adapter, @Nonnull Runnable onPostExecute) {
		return new RecentChatsAsyncLoader(getActivity(), adapter, onPostExecute);
	}
}
