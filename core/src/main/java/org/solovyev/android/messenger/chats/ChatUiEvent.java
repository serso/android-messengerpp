package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.messages.ChatMessage;
import org.solovyev.common.listeners.AbstractTypedJEvent;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:02 AM
 */
public final class ChatUiEvent extends AbstractTypedJEvent<Chat, ChatUiEventType> {

	public ChatUiEvent(@Nonnull Chat chat, @Nonnull ChatUiEventType type, @Nullable Object data) {
		super(chat, type, data);
	}

	@Nonnull
	public Chat getChat() {
		return getEventObject();
	}

	@Nonnull
	public ChatMessage getDataAsChatMessage() {
		return (ChatMessage) getData();
	}
}
