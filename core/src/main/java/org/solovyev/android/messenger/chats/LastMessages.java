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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MessageService;

import static org.solovyev.android.messenger.chats.ChatEventType.last_message_changed;

class LastMessages {

	// key: chat id, value: last message
	@Nonnull
	private final Map<Entity, Message> lastMessagesCache = new HashMap<Entity, Message>();

	@Nonnull
	private final ChatService chatService;

	@Nonnull
	private final MessageService messageService;

	LastMessages(@Nonnull ChatService chatService, @Nonnull MessageService messageService) {
		this.chatService = chatService;
		this.messageService = messageService;
	}

	public void onEvent(@Nonnull ChatEvent event) {
		final Chat chat = event.getChat();
		final Object data = event.getData();

		final Map<Chat, Message> changedLastMessages = new HashMap<Chat, Message>();
		synchronized (lastMessagesCache) {
			switch (event.getType()) {
				case message_added: {
					final Message message = event.getDataAsMessage();
					tryPutNewLastMessage(chat, changedLastMessages, message);
				}
				break;
				case messages_added: {
					final List<Message> messages = event.getDataAsMessages();

					Message newestMessage = null;
					for (Message message : messages) {
						if (newestMessage == null) {
							newestMessage = message;
						} else if (message.getSendDate().isAfter(newestMessage.getSendDate())) {
							newestMessage = message;
						}
					}

					tryPutNewLastMessage(chat, changedLastMessages, newestMessage);
				}
				break;
				case message_changed: {
					if (data instanceof Message) {
						final Message message = (Message) data;
						final Message messageFromCache = lastMessagesCache.get(chat.getEntity());
						if (messageFromCache == null || messageFromCache.equals(message)) {
							lastMessagesCache.put(chat.getEntity(), message);
							changedLastMessages.put(chat, message);
						}
					}
				}
				break;
			}
		}

		for (Map.Entry<Chat, Message> entry : changedLastMessages.entrySet()) {
			chatService.fireEvent(last_message_changed.newEvent(entry.getKey(), entry.getValue()));
		}
	}

	private void tryPutNewLastMessage(@Nonnull Chat chat,
									  @Nonnull Map<Chat, Message> changedLastMessages,
									  @Nullable Message message) {
		if (message != null) {
			final Message messageFromCache = lastMessagesCache.get(chat.getEntity());
			if (messageFromCache == null || message.getSendDate().isAfter(messageFromCache.getSendDate())) {
				lastMessagesCache.put(chat.getEntity(), message);
				changedLastMessages.put(chat, message);
			}
		}
	}

	public Message getLastMessage(Entity chat) {
		Message result;

		synchronized (lastMessagesCache) {
			result = lastMessagesCache.get(chat);
			if (result == null) {
				result = messageService.getLastMessage(chat.getEntityId());
				if (result != null) {
					lastMessagesCache.put(chat, result);
				}
			}
		}

		return result;
	}
}
