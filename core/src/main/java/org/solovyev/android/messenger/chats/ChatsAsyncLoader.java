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

import android.content.Context;
import org.solovyev.android.list.ListItemAdapter;
import org.solovyev.android.messenger.BaseAsyncLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.App.getChatService;

public class ChatsAsyncLoader extends BaseAsyncLoader<UiChat, ChatListItem> {

	@Nullable
	private final String query;

	private final int maxCount;

	public ChatsAsyncLoader(@Nonnull Context context,
							@Nonnull ListItemAdapter<ChatListItem> adapter,
							@Nullable Runnable onPostExecute,
							@Nullable String query,
							int maxCount) {
		super(context, adapter, onPostExecute);
		this.query = query;
		this.maxCount = maxCount;
	}

	@Nonnull
	@Override
	protected List<UiChat> getElements(@Nonnull Context context) {
		return getChatService().getLastChats(query, maxCount);
	}

	@Nonnull
	@Override
	protected ChatListItem createListItem(@Nonnull UiChat uiChat) {
		return ChatListItem.newInstance(uiChat);
	}
}
