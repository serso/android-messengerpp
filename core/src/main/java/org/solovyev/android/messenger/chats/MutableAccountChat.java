package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.users.User;

public interface MutableAccountChat extends AccountChat {

	void addMessage(@Nonnull Message message);

	boolean addParticipant(@Nonnull User participant);
}
