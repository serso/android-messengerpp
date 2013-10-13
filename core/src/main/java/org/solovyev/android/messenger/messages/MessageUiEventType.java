package org.solovyev.android.messenger.messages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum MessageUiEventType {

	quote;

	@Nonnull
	public final MessageUiEvent newEvent(@Nonnull ChatMessage message) {
		return newEvent(message, null);
	}

	@Nonnull
	public final MessageUiEvent newEvent(@Nonnull ChatMessage message, @Nullable Object data) {
		checkData(data);
		return new MessageUiEvent(message, this, data);
	}

	protected void checkData(@Nullable Object data) {
		assert data == null;
	}


}
