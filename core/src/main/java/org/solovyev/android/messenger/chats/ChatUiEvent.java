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

import org.solovyev.android.messenger.messages.Message;
import org.solovyev.common.listeners.JEvent;

import javax.annotation.Nonnull;

public abstract class ChatUiEvent implements JEvent {

	@Nonnull
	public final Chat chat;

	protected ChatUiEvent(@Nonnull Chat chat) {
		this.chat = chat;
	}

	public static final class Clicked extends ChatUiEvent {
		public Clicked(@Nonnull Chat chat) {
			super(chat);
		}
	}

	public static final class Open extends ChatUiEvent {
		public Open(@Nonnull Chat chat) {
			super(chat);
		}
	}

	public static final class ShowParticipants extends ChatUiEvent {
		public ShowParticipants(@Nonnull Chat chat) {
			super(chat);
		}
	}

	public static final class MarkMessageRead extends ChatUiEvent {

		@Nonnull
		public final Message message;

		public MarkMessageRead(@Nonnull Chat chat, @Nonnull Message message) {
			super(chat);
			this.message = message;
		}
	}
}
