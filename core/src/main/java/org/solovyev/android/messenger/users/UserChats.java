package org.solovyev.android.messenger.users;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAwareRemovedUpdater;
import org.solovyev.common.collections.multimap.ObjectAddedUpdater;
import org.solovyev.common.collections.multimap.ObjectChangedMapUpdater;
import org.solovyev.common.collections.multimap.ObjectsAddedUpdater;
import org.solovyev.common.collections.multimap.ThreadSafeMultimap;
import org.solovyev.common.collections.multimap.WholeListUpdater;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import static org.solovyev.common.collections.multimap.ThreadSafeMultimap.newThreadSafeMultimap;

@ThreadSafe
class UserChats {

	// key: user entity, value: list of user chats
	@Nonnull
	private final ThreadSafeMultimap<Entity, Chat> chats = newThreadSafeMultimap();

	@Nonnull
	public List<Chat> getChats(@Nonnull Entity user) {
		return chats.get(user);
	}

	void init() {
		App.getChatService().addListener(new ChatEventListener());
	}

	public void updateChats(@Nonnull Entity user, @Nonnull List<Chat> chats) {
		this.chats.update(user, new WholeListUpdater<Chat>(chats));
	}

	void onEvent(@Nonnull UserEvent event) {
		final User eventUser = event.getUser();

		switch (event.getType()) {
			case chat_added:
				final Chat chat = event.getDataAsChat();
				this.chats.update(eventUser.getEntity(), new ObjectAddedUpdater<Chat>(chat));
				break;
			case chat_added_batch:
				final List<Chat> chats = event.getDataAsChats();
				this.chats.update(eventUser.getEntity(), new ObjectsAddedUpdater<Chat>(chats));
				break;
			case chat_removed:
				final Chat removedChat = event.getDataAsChat();
				this.chats.update(eventUser.getEntity(), new EntityAwareRemovedUpdater<Chat>(removedChat.getId()));
				break;
		}
	}

	private class ChatEventListener extends AbstractJEventListener<ChatEvent> {

		protected ChatEventListener() {
			super(ChatEvent.class);
		}

		@Override
		public void onEvent(@Nonnull ChatEvent event) {
			switch (event.getType()) {
				case changed:
					chats.update(new ObjectChangedMapUpdater<Chat>(event.getChat()));
					break;

			}
		}
	}
}
