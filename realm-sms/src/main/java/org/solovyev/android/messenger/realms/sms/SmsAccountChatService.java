package org.solovyev.android.messenger.realms.sms;

import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 9:23 PM
 */
final class SmsAccountChatService implements AccountChatService {

	@Nonnull
	@Override
	public List<ChatMessage> getChatMessages(@Nonnull String realmUserId) throws AccountConnectionException {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<ChatMessage> getNewerChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId) throws AccountConnectionException {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<ChatMessage> getOlderChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId, @Nonnull Integer offset) throws AccountConnectionException {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<ApiChat> getUserChats(@Nonnull String realmUserId) throws AccountConnectionException {
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public String sendChatMessage(@Nonnull Chat chat, @Nonnull ChatMessage message) throws AccountConnectionException {
		return null;
	}

	@Nonnull
	@Override
	public Chat newPrivateChat(@Nonnull Entity realmChat, @Nonnull String realmUserId1, @Nonnull String realmUserId2) throws AccountConnectionException {
		return Chats.newPrivateChat(realmChat);
	}
}
