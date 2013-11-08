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
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.getAccountService;

public class RecentChatsAdapter extends BaseChatsAdapter {

	public RecentChatsAdapter(@Nonnull Context context) {
		super(context);
	}

	@Override
	protected boolean canAddChat(@Nonnull Chat chat) {
		return true;
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
