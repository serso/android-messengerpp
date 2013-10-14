package org.solovyev.android.messenger.chats;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.common.listeners.AbstractTypedJEvent;

/**
 * User: serso
 * Date: 3/10/13
 * Time: 2:41 PM
 */
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
}
