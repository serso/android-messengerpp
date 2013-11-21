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

import javax.annotation.Nonnull;
import java.util.List;

public final class ChatsFragment extends BaseChatsFragment {

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
		return new ChatsAsyncLoader(getActivity(), adapter, onPostExecute, query, getMaxSize());
	}
}
