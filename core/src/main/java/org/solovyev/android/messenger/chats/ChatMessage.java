package org.solovyev.android.messenger.chats;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.messages.LiteChatMessage;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 12:56 PM
 */
public interface ChatMessage extends LiteChatMessage {

	boolean isRead();

	@Nonnull
	MessageDirection getDirection();

	@Nonnull
	List<LiteChatMessage> getFwdMessages();

	@Nonnull
	ChatMessage clone();

	@Nonnull
	ChatMessage cloneRead();
}
