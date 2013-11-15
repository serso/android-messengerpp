/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.chats.Chats.MAX_RECENT_CHATS;

public final class ChatsFragment extends BaseChatsFragment {

	private int maxRecentChats = MAX_RECENT_CHATS;

	public ChatsFragment() {
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
	protected ChatsAdapter createAdapter() {
		return new ChatsAdapter(getActivity());
	}

	@Nonnull
	@Override
	protected MessengerAsyncTask<Void, Void, List<UiChat>> createAsyncLoader(@Nonnull BaseListItemAdapter<ChatListItem> adapter, @Nonnull Runnable onPostExecute) {
		return new ChatsAsyncLoader(getActivity(), adapter, onPostExecute, maxRecentChats);
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
