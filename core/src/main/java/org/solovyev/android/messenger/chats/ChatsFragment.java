package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import java.util.List;

import static org.solovyev.android.messenger.App.showToast;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:37 PM
 */
public final class ChatsFragment extends BaseChatsFragment {

	public ChatsFragment() {
		super();
	}

	@Nonnull
	@Override
	protected ChatsAdapter createAdapter() {
		return new ChatsAdapter(getActivity());
	}

	@Nonnull
	@Override
	protected MessengerAsyncTask<Void, Void, List<UiChat>> createAsyncLoader(@Nonnull BaseListItemAdapter<ChatListItem> adapter, @Nonnull Runnable onPostExecute) {
		return new ChatsAsyncLoader(getActivity(), adapter, onPostExecute);
	}

	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return new ChatSyncRefreshListener();
	}

	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return new ChatSyncRefreshListener();
	}

	private class ChatSyncRefreshListener extends AbstractOnRefreshListener {
		@Override
		public void onRefresh() {
			try {
				getSyncService().sync(SyncTask.user_chats, new Runnable() {
					@Override
					public void run() {
						completeRefresh();
					}
				});
				showToast(R.string.mpp_updating_chat);
			} catch (TaskIsAlreadyRunningException e) {
				e.showMessage();
			}
		}
	}
}
