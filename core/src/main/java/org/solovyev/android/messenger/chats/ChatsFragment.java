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

import android.os.Handler;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.chats.Chats.MAX_RECENT_CHATS;
import static org.solovyev.common.text.Strings.isEmpty;

public final class ChatsFragment extends BaseChatsFragment {

	private int maxChats = MAX_RECENT_CHATS;

	private static final long SEARCH_DELAY_MILLIS = 500;

	private int loadingStartedForTotal = 0;

	@Nonnull
	private final FindChatsRunnable runnable = new FindChatsRunnable();

	@Nonnull
	@Override
	protected ChatsAdapter createAdapter() {
		return new ChatsAdapter(getActivity());
	}

	@Nonnull
	@Override
	protected MessengerAsyncTask<Void, Void, List<UiChat>> createAsyncLoader(@Nonnull BaseListItemAdapter<ChatListItem> adapter, @Nonnull Runnable onPostExecute) {
		final CharSequence filterText = getFilterText();
		final String query = filterText == null ? null : filterText.toString();
		((BaseChatsAdapter) adapter).setQuery(query);
		return new ChatsAsyncLoader(getActivity(), adapter, onPostExecute, query, maxChats);
	}

	@Override
	public void filter(@Nullable CharSequence filterText) {
		if (isInitialLoadingDone()) {
			if (isEmpty(filterText)) {
				// in case of empty query we need to reset maxChats
				maxChats = MAX_RECENT_CHATS;
				loadingStartedForTotal = 0;
			}
			final Handler handler = getUiHandler();
			handler.removeCallbacks(runnable);
			handler.postDelayed(runnable, SEARCH_DELAY_MILLIS);
		}
	}

	@Override
	public void onItemReachedFromTop(int position, int total) {
		super.onItemReachedFromTop(position, total);

		final float rate = (float) position / (float) total;
		if (rate > 0.75f) {
			if (loadingStartedForTotal != total) {
				loadingStartedForTotal = total;
				maxChats = 2 * maxChats;
				getUiHandler().post(runnable);
			}
		}
	}

	private class FindChatsRunnable implements Runnable {
		@Override
		public void run() {
			final BaseListItemAdapter<ChatListItem> adapter = getAdapter();
			adapter.unselect();
			createAsyncLoader(adapter).executeInParallel();
		}
	}
}
