package org.solovyev.android.messenger.chats;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;

class ChatCache {

	// key: chat id, value: chat
	@Nonnull
	private final Map<Entity, Chat> chats = new HashMap<Entity, Chat>();

	public void put(@Nonnull Chat chat) {
		synchronized (chats) {
			chats.put(chat.getEntity(), chat);
		}
	}

	@Nullable
	public Chat get(@Nonnull Entity chat) {
		synchronized (chats) {
			return chats.get(chat);
		}
	}

	public void onEvent(@Nonnull ChatEvent event) {
		switch (event.getType()) {
			case changed:
				put(event.getChat());
				break;
		}
	}
}
