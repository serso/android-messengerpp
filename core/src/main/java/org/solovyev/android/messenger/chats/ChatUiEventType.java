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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:02 AM
 */
public enum ChatUiEventType {

	open_chat,
	chat_clicked,
	show_participants,
	chat_message_read {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Message;
		}
	};

	@Nonnull
	public final ChatUiEvent newEvent(@Nonnull Chat chat) {
		return newEvent(chat, null);
	}

	@Nonnull
	public final ChatUiEvent newEvent(@Nonnull Chat chat, @Nullable Object data) {
		checkData(data);
		return new ChatUiEvent(chat, this, data);
	}

	protected void checkData(@Nullable Object data) {
		assert data == null;
	}
}
