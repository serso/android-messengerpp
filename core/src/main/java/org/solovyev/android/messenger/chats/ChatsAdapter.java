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

import javax.annotation.Nonnull;

import org.solovyev.android.list.PrefixFilter;
import org.solovyev.android.messenger.users.User;

import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.common.Objects.areEqual;

public class ChatsAdapter extends BaseChatsAdapter {

	@Nonnull
	private String filterQuery = "";

	@Nonnull
	private PrefixFilter<CharSequence> filter = new PrefixFilter<CharSequence>(filterQuery);

	public ChatsAdapter(@Nonnull Context context) {
		super(context);
	}

	@Override
	protected boolean canAddChat(@Nonnull ChatListItem chatListItem) {
		final String query = getQuery();
		if (!areEqual(filterQuery, query)) {
			filterQuery = query;
			filter = new PrefixFilter<CharSequence>(filterQuery);
		}
		return filter.apply(chatListItem.getDisplayName());
	}

	@Override
	public void onEvent(@Nonnull ChatEvent event) {
		super.onEvent(event);

		final Chat eventChat = event.getChat();

		switch (event.getType()) {
			case last_message_changed:
				final User user = getAccountService().getAccountById(eventChat.getEntity().getAccountId()).getUser();
				final ChatListItem chatListItem = findInAllElements(user, eventChat);
				if (chatListItem == null) {
					addListItem(user, eventChat);
				}
				break;
		}
	}
}
