package org.solovyev.android.messenger.accounts;

import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.ChatMessage;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

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
	public List<ChatMessage> getChatMessages(@Nonnull String accountUserId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<ChatMessage> getNewerChatMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<ChatMessage> getOlderChatMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId, @Nonnull Integer offset) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<ApiChat> getUserChats(@Nonnull String accountUserId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public String sendChatMessage(@Nonnull Chat chat, @Nonnull ChatMessage message) {
		return "test_message_id";
	}

	@Nonnull
	@Override
	public Chat newPrivateChat(@Nonnull Entity accountChat, @Nonnull String accountUserId1, @Nonnull String accountUserId2) {
		return Chats.newPrivateChat(accountChat);
	}
}
