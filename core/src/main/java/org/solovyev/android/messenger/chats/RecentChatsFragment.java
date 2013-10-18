package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.chats.Chats.MAX_RECENT_CHATS;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:37 PM
 */
public final class RecentChatsFragment extends BaseChatsFragment {

	private int maxRecentChats = MAX_RECENT_CHATS;

	public RecentChatsFragment() {
		super();
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
	protected RecentChatsAdapter createAdapter() {
		return new RecentChatsAdapter(getActivity());
	}

	@Nullable
	@Override
	protected MessengerAsyncTask<Void, Void, List<UiChat>> createAsyncLoader(@Nonnull BaseListItemAdapter<ChatListItem> adapter, @Nonnull Runnable onPostExecute) {
		return new RecentChatsAsyncLoader(getActivity(), adapter, onPostExecute, maxRecentChats);
	}

	@Override
	public void onBottomReached() {
		super.onBottomReached();

		final int count = getAdapter().getCount();
		if (count < maxRecentChats) {
			// no more chats
		} else {
			maxRecentChats += MAX_RECENT_CHATS;
			reloadRecentChats();
		}
	}

	private void reloadRecentChats() {
		createAsyncLoader(getAdapter()).executeInParallel();
	}
}
