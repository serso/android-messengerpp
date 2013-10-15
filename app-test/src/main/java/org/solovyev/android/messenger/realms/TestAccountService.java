package org.solovyev.android.messenger.realms;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 5:15 PM
 */
public class TestAccountService implements AccountUserService, AccountChatService {
	@Nullable
	@Override
	public User getUserById(@Nonnull String accountUserId) {
		return null;
	}

	@Nonnull
	@Override
	public List<User> getUserContacts(@Nonnull String accountUserId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<User> checkOnlineUsers(@Nonnull List<User> users) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public User saveUser(@Nonnull User user) {
		return user;
	}

	@Nonnull
	@Override
	public List<Message> getMessages(@Nonnull String accountUserId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getNewerMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getOlderMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId, @Nonnull Integer offset) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<AccountChat> getChats(@Nonnull String accountUserId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public String sendMessage(@Nonnull Chat chat, @Nonnull Message message) {
		return "test_message_id";
	}

	@Override
	public void beforeSendMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableMessage message) throws AccountConnectionException {
	}

	@Nonnull
	@Override
	public Chat newPrivateChat(@Nonnull Entity accountChat, @Nonnull String accountUserId1, @Nonnull String accountUserId2) {
		throw new UnsupportedOperationException();
	}
}
