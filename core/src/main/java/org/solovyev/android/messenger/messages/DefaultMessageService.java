package org.solovyev.android.messenger.messages;

import android.widget.ImageView;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.joda.time.DateTime;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.PersistenceLock;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.List;

import static java.util.Arrays.asList;
import static org.solovyev.android.messenger.accounts.AccountService.NO_ACCOUNT_ID;
import static org.solovyev.android.messenger.messages.Messages.copySentMessage;

@Singleton
public class DefaultMessageService implements MessageService {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private ImageLoader imageLoader;

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private ChatService chatService;

	@GuardedBy("lock")
	@Inject
	@Nonnull
	private MessageDao dao;

	@Nonnull
	private final PersistenceLock lock;

	@Inject
	public DefaultMessageService(@Nonnull PersistenceLock lock) {
		this.lock = lock;
	}

	@Override
	public void init() {
	}

	@Nonnull
	@Override
	public List<Message> getMessages(@Nonnull Entity chat) {
		// todo serso: think about lock
		/*synchronized (lock) {*/
			return dao.readMessages(chat.getEntityId());
		/*}*/
	}

	@Nullable
	@Override
	public Message getSameMessage(@Nonnull String body, @Nonnull DateTime sendTime, @Nonnull Entity author, @Nonnull Entity recipient) {
		synchronized (lock) {
			return dao.readSameMessage(body, sendTime, author, recipient);
		}
	}

	@Nullable
	@Override
	public Message getMessage(@Nonnull String messageId) {
		return dao.read(messageId);
	}

	@Override
	public void setMessageIcon(@Nonnull Message message, @Nonnull ImageView imageView) {
		final Entity author = message.getAuthor();
		userService.getIconsService().setUserIcon(userService.getUserById(author), imageView);
	}

	@Nonnull
	@Override
	public Message sendMessage(@Nonnull Chat chat, @Nonnull Message message) throws AccountException {
		final Account account = getAccountByUser(chat.getEntity());
		final AccountChatService acs = account.getAccountChatService();

		// id returned by account
		final String accountMessageId = sendMessage(chat, message, acs);

		final MutableMessage result = copySentMessage(message, account, accountMessageId);

		chatService.saveMessages(chat.getEntity(), asList(result));

		return result;
	}

	@Nonnull
	private String sendMessage(@Nonnull Chat chat, @Nonnull Message message, @Nonnull AccountChatService acs) throws AccountConnectionException {
		final String accountMessageId = acs.sendMessage(chat, message);
		return accountMessageId == null ? NO_ACCOUNT_ID : accountMessageId;
	}

	@Nullable
	@Override
	public Message getLastMessage(@Nonnull String chatId) {
		synchronized (lock) {
			return this.dao.readLastMessage(chatId);
		}
	}

	@Override
	public int getUnreadMessagesCount() {
		synchronized (lock) {
			return this.dao.getUnreadMessagesCount();
		}
	}

	@Nonnull
	private Account getAccountByUser(@Nonnull Entity userEntity) throws UnsupportedAccountException {
		return accountService.getAccountById(userEntity.getAccountId());
	}
}
