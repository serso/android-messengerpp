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

package org.solovyev.android.messenger.messages;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.MessengerEventType;
import org.solovyev.android.messenger.MessengerListeners;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountEvent;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.PersistenceLock;
import org.solovyev.common.listeners.AbstractJEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;

@Singleton
public final class UnreadMessagesCounter {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	private static final int DELAY_LONG = 2500;
	private static final int DELAY_SHORT = 500;

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private ChatService chatService;

	@Inject
	@Nonnull
	private MessageService messageService;

	@Inject
	@Nonnull
	private MessengerListeners messengerListeners;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	@Nonnull
	private final Object lock;

	@Nonnull
	private final AtomicInteger counter = new AtomicInteger(0);

	@GuardedBy("counter")
	@Nonnull
	private final Map<Entity, AtomicInteger> countersByChats = new HashMap<Entity, AtomicInteger>();

	@Nonnull
	private final AtomicInteger runnablesCounter = new AtomicInteger(0);

	@Nonnull
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	@Inject
	public UnreadMessagesCounter(@Nonnull PersistenceLock lock) {
		this.lock = lock;
	}

	public void init() {
		updateCounters();

		chatService.addListener(new ChatEventListener());
		accountService.addListener(new AccountEventListener());
	}

	private void updateCounters() {
		synchronized (lock) {
			countersByChats.clear();
			counter.set(0);

			for (Map.Entry<Entity, Integer> entry : chatService.getUnreadChats().entrySet()) {
				final Integer unreadInChat = entry.getValue();
				if (unreadInChat > 0) {
					final Entity chat = entry.getKey();
					final Account account = accountService.getAccountByEntity(chat);
					if (account.isEnabled()) {
						countersByChats.put(chat, new AtomicInteger(unreadInChat));
						counter.addAndGet(unreadInChat);
					}
				}
			}

			fireCounterChanged(false);
		}
	}

	private void onMessageRead(@Nonnull Chat chat) {
		changeCounter(chat, -1);
		// many messages can be read at once (e.g. chat was opened => wait a little bit for them, too)
		fireCounterChanged(false);
	}

	private void changeCounter(@Nonnull Chat chat, int increment) {
		synchronized (counter) {
			counter.addAndGet(increment);
			if (counter.get() < 0) {
				counter.set(0);
			}

			AtomicInteger counterByChat = countersByChats.get(chat.getEntity());
			if (counterByChat == null) {
				counterByChat = new AtomicInteger();
				countersByChats.put(chat.getEntity(), counterByChat);
			}

			counterByChat.addAndGet(increment);
			if (counterByChat.get() < 0) {
				counterByChat.set(0);
			}
		}
	}

	private void onMessageAdded(@Nonnull Chat chat, @Nonnull List<Message> messages) {
		int unread = 0;
		for (Message message : messages) {
			if (message.canRead()) {
				unread++;
			}
		}

		if (unread > 0) {
			changeCounter(chat, unread);
			fireCounterChanged(true);
		}
	}

	private void fireCounterChanged(boolean longDelay) {
		final int runnableIndex = runnablesCounter.incrementAndGet();

		// a little delay for performance improvement
		executorService.schedule(new Runnable() {
			@Override
			public void run() {
				if (runnableIndex == runnablesCounter.get()) {

					final int unreadMessagesCount;
					final Map<Entity, Integer> countersByChatsCopy = new HashMap<Entity, Integer>(countersByChats.size());
					synchronized (counter) {
						unreadMessagesCount = counter.get();

						for (Map.Entry<Entity, AtomicInteger> entry : countersByChats.entrySet()) {
							countersByChatsCopy.put(entry.getKey(), entry.getValue().get());
						}
					}

					// no new runnables scheduled => can continue
					messengerListeners.fireEvent(MessengerEventType.unread_messages_count_changed.newEvent(unreadMessagesCount));

					for (Map.Entry<Entity, Integer> entry : countersByChatsCopy.entrySet()) {
						final Entity chatEntity = entry.getKey();
						final Integer counter = entry.getValue();

						// fire chat event
						chatService.onUnreadMessagesCountChanged(chatEntity, counter);
					}
				}
			}
		}, longDelay ? DELAY_LONG : DELAY_SHORT, TimeUnit.MILLISECONDS);
	}

	public int getUnreadMessagesCount() {
		return counter.get();
	}

	@Nullable
	public Entity getUnreadChat() {
		synchronized (counter) {
			for (Map.Entry<Entity, AtomicInteger> entry : countersByChats.entrySet()) {
				final AtomicInteger counterByChat = entry.getValue();
				if (counterByChat != null && counterByChat.get() > 0) {
					return entry.getKey();
				}
			}
		}

		return null;
	}

	public int getUnreadMessagesCountForChat(@Nonnull Entity chat) {
		synchronized (counter) {
			final AtomicInteger counterByChat = countersByChats.get(chat);
			if (counterByChat == null) {
				return 0;
			} else {
				return counterByChat.get();
			}
		}
	}

	private final class ChatEventListener extends AbstractJEventListener<ChatEvent> {

		private ChatEventListener() {
			super(ChatEvent.class);
		}

		@Override
		public void onEvent(@Nonnull ChatEvent event) {
			switch (event.getType()) {
				case message_read:
					onMessageRead(event.getChat());
					break;
				case message_added:
					onMessageAdded(event.getChat(), asList(event.getDataAsMessage()));
					break;
				case messages_added:
					onMessageAdded(event.getChat(), event.getDataAsMessages());
					break;
			}
		}
	}

	private final class AccountEventListener extends AbstractJEventListener<AccountEvent> {

		private AccountEventListener() {
			super(AccountEvent.class);
		}

		@Override
		public void onEvent(@Nonnull AccountEvent event) {
			final Account account = event.getAccount();
			switch (event.getType()) {
				case state_changed:
					switch (account.getState()) {
						case removed:
						case disabled_by_app:
						case disabled_by_user:
							updateCounters();
							break;
					}
					break;
			}
		}
	}
}
