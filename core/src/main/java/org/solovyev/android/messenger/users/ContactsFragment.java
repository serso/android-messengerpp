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

package org.solovyev.android.messenger.users;

import android.util.Log;
import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.UiThreadEventListener.onUiThread;
import static org.solovyev.common.text.Strings.isEmpty;

public class ContactsFragment extends BaseContactsFragment {

	@Nullable
	private JEventListener<ChatEvent> chatEventListener;

	@Nonnull
	@Override
	protected BaseListItemAdapter<ContactListItem> createAdapter() {
		Log.d(tag, "Creating adapter, filter text: " + getFilterText());
		return new ContactsAdapter(getActivity(), false);
	}

	@Nonnull
	@Override
	protected ContactsAdapter getAdapter() {
		return (ContactsAdapter) super.getAdapter();
	}

	@Nonnull
	@Override
	protected MessengerAsyncTask<Void, Void, List<UiContact>> createAsyncLoader(@Nonnull BaseListItemAdapter<ContactListItem> adapter, @Nonnull Runnable onPostExecute) {
		final CharSequence filterText = getFilterText();
		final String query = filterText == null ? null : filterText.toString();
		((BaseContactsAdapter) adapter).setQuery(query);
		return new ContactsAsyncLoader(getActivity(), adapter, onPostExecute, query, getMaxSize());
	}

	@Override
	protected void attachListeners() {
		super.attachListeners();

		chatEventListener = onUiThread(this, new ChatEventListener());
		getChatService().addListener(chatEventListener);
	}

	@Override
	protected void detachListeners() {
		if (chatEventListener != null) {
			getChatService().removeListener(chatEventListener);
			chatEventListener = null;
		}

		super.detachListeners();
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return null;
	}

	private class ChatEventListener extends AbstractJEventListener<ChatEvent> {
		public ChatEventListener() {
			super(ChatEvent.class);
		}

		@Override
		public void onEvent(@Nonnull ChatEvent event) {
			getAdapter().onEvent(event);
		}
	}
}
