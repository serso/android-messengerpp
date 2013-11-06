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

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.common.listeners.AbstractTypedJEvent;

public class ChatEvent extends AbstractTypedJEvent<Chat, ChatEventType> {

	ChatEvent(@Nonnull Chat chat, @Nonnull ChatEventType type, Object data) {
		super(chat, type, data);
	}

	@Nonnull
	public Chat getChat() {
		return getEventObject();
	}

	@Nonnull
	public Message getDataAsMessage() {
		return (Message) getData();
	}

	@Nonnull
	public List<Message> getDataAsMessages() {
		return (List<Message>) getData();
	}

	@Nonnull
	public Integer getDataAsInteger() {
		return (Integer) getData();
	}

	@Nonnull
	public Entity getDataAsEntity() {
		return (Entity) getData();
	}

	@Override
	public String toString() {
		return "ChatEvent{chat=" + getChat().getId() + ", type=" + getType() + ", data=" + getData() + "}";
	}
}
