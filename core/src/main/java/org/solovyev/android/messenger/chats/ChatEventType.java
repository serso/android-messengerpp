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

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public enum ChatEventType {
	added,
	changed,

	participant_added,
	participant_removed,

	message_added {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Message;
		}
	},

	messages_added {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof List;
		}
	},

	// data == changed message for chat
	message_state_changed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Message;
		}
	},

	// data == changed message for chat
	message_changed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Message;
		}
	},

	// data == new last message for chat
	last_message_changed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Message;
		}
	},

	// data == user which start typing in chat
	user_is_typing {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Entity;
		}
	},

	// data == user which stop typing in chat
	user_is_not_typing {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Entity;
		}
	},

	// data == number of unread messages
	unread_message_count_changed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Integer;
		}
	},

	message_read {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Message;
		}
	};

	public final boolean isEvent(@Nonnull ChatEventType chatEventType, @Nonnull Chat eventChat, @Nonnull Chat chat) {
		return this == chatEventType && eventChat.equals(chat);
	}

	@Nonnull
	public final ChatEvent newEvent(@Nonnull Chat chat) {
		return newEvent(chat, null);
	}

	@Nonnull
	public final ChatEvent newEvent(@Nonnull Chat chat, @Nullable Object data) {
		checkData(data);
		return new ChatEvent(chat, this, data);
	}

	protected void checkData(@Nullable Object data) {
		assert data == null;
	}

}
