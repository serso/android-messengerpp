package org.solovyev.android.messenger.messages;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class MessageUiEvent extends AbstractTypedJEvent<Message, MessageUiEventType>{

	MessageUiEvent(@Nonnull Message eventObject, @Nonnull MessageUiEventType type, @Nullable Object data) {
		super(eventObject, type, data);
	}

	@Nonnull
	public Message getMessage() {
		return getEventObject();
	}
}
