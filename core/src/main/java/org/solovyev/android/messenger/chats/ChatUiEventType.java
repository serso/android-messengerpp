package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:02 AM
 */
public enum ChatUiEventType {

	chat_open_requested,
	chat_clicked,
	chat_message_read {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof ChatMessage;
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
