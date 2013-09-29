package org.solovyev.android.messenger.chats;

import android.widget.Toast;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:37 PM
 */
public final class MessengerChatsFragment extends AbstractChatsFragment {

	public MessengerChatsFragment() {
		super();
	}

	@Nonnull
	@Override
	protected ChatsAdapter createAdapter() {
		return new ChatsAdapter(getActivity());
	}

	@Nonnull
	@Override
	protected MessengerAsyncTask<Void, Void, List<UiChat>> createAsyncLoader(@Nonnull MessengerListItemAdapter<ChatListItem> adapter, @Nonnull Runnable onPostExecute) {
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
				Toast.makeText(getActivity(), "Chats sync started!", Toast.LENGTH_SHORT).show();
			} catch (TaskIsAlreadyRunningException e) {
				e.showMessage(getActivity());
			}
		}
	}
}
