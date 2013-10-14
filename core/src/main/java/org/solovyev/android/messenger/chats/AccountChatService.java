package org.solovyev.android.messenger.chats;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:29 PM
 */
public interface AccountChatService {

	@Nonnull
	List<Message> getMessages(@Nonnull String accountUserId) throws AccountConnectionException;

	@Nonnull
	List<Message> getNewerMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId) throws AccountConnectionException;

	@Nonnull
	List<Message> getOlderMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId, @Nonnull Integer offset) throws AccountConnectionException;

	@Nonnull
	List<ApiChat> getChats(@Nonnull String accountUserId) throws AccountConnectionException;

	/**
	 * Method sends message to the account and, if possible, returns message id. If message id could not be returned
	 * (due, for example, to the asynchronous nature of realm) - null is returned (in that case realm connection must receive message id)
	 *
	 * @param chat    chat in which message was created
	 * @param message message to be sent
	 * @return message id of sent message if possible
	 */
	@Nullable
	String sendMessage(@Nonnull Chat chat, @Nonnull Message message) throws AccountConnectionException;

	void beforeSendMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableMessage message) throws AccountConnectionException;

	@Nonnull
	Chat newPrivateChat(@Nonnull Entity accountChat, @Nonnull String accountUserId1, @Nonnull String accountUserId2) throws AccountConnectionException;
}
