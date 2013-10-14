package org.solovyev.android.messenger.messages;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.chats.MessageDirection;
import org.solovyev.android.properties.MutableAProperties;

public interface MutableChatMessage extends ChatMessage {

	@Nonnull
	MutableChatMessage clone();

	@Nonnull
	MutableChatMessage cloneRead();

	@Nonnull
	MutableAProperties getProperties();

}
