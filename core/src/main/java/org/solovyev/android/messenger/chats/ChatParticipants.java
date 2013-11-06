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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.collections.multimap.ObjectAddedUpdater;
import org.solovyev.common.collections.multimap.ObjectRemovedUpdater;
import org.solovyev.common.collections.multimap.ThreadSafeMultimap;
import org.solovyev.common.collections.multimap.WholeListUpdater;

import static org.solovyev.common.collections.multimap.ThreadSafeMultimap.newThreadSafeMultimap;

class ChatParticipants {

	// key: chat id, value: list of participants
	@Nonnull
	private final ThreadSafeMultimap<Entity, User> participants = newThreadSafeMultimap();

	@Nonnull
	public List<User> get(@Nonnull Entity chat) {
		return participants.get(chat);
	}

	public void put(@Nonnull Entity chat, @Nonnull List<User> participants) {
		this.participants.update(chat, new WholeListUpdater<User>(participants));
	}

	public void onEvent(@Nonnull ChatEvent event) {
		final Chat chat = event.getChat();
		final Object data = event.getData();

		switch (event.getType()) {
			case participant_added:
				// participant added => need to add to list of cached participants
				if (data instanceof User) {
					final User participant = ((User) data);
					this.participants.update(chat.getEntity(), new ObjectAddedUpdater<User>(participant));
				}
				break;
			case participant_removed:
				// participant removed => try to remove from cached participants
				if (data instanceof User) {
					final User participant = ((User) data);
					this.participants.update(chat.getEntity(), new ObjectRemovedUpdater<User>(participant));
				}
				break;
		}
	}
}
