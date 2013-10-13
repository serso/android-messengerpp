package org.solovyev.android.messenger.messages;

import org.solovyev.android.messenger.chats.MessageDirection;
import org.solovyev.android.properties.AProperties;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 12:56 PM
 */
public interface ChatMessage extends Message {

	boolean isRead();

	@Nonnull
	MessageDirection getDirection();

	@Nonnull
	List<Message> getFwdMessages();

	@Nonnull
	ChatMessage clone();

	@Nonnull
	ChatMessage cloneRead();

	@Nonnull
	AProperties getProperties();
}
