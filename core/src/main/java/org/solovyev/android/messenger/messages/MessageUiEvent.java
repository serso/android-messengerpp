package org.solovyev.android.messenger.messages;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class MessageUiEvent extends AbstractTypedJEvent<ChatMessage, MessageUiEventType>{

	MessageUiEvent(@Nonnull ChatMessage eventObject, @Nonnull MessageUiEventType type, @Nullable Object data) {
		super(eventObject, type, data);
	}

	@Nonnull
	public ChatMessage getMessage() {
		return getEventObject();
	}
}
