package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

public interface MutableAccountChat extends AccountChat {

	void addMessage(@Nonnull MutableMessage message);

	boolean addParticipant(@Nonnull User participant);
}
