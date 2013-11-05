package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

import java.util.List;

public interface AccountChat {

	@Nonnull
	List<Message> getMessages();

	@Nonnull
	List<User> getParticipants();

	@Nonnull
	List<User> getParticipantsExcept(@Nonnull User user);

	@Nonnull
	Chat getChat();

	@Nonnull
	AccountChat copyWithNewId(@Nonnull Entity id);
}
