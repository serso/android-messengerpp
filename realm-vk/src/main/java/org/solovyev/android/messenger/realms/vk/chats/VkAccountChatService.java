package org.solovyev.android.messenger.realms.vk.chats;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.http.HttpTransaction;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.Chats;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.messages.VkMessagesSendHttpTransaction;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:30 PM
 */
public class VkAccountChatService implements AccountChatService {

	@Nonnull
	private static final String TAG = VkAccountChatService.class.getSimpleName();

	@Nonnull
	private static final String CHAT_DELIMITER = ":";

	@Nonnull
	private final VkAccount account;

	public VkAccountChatService(@Nonnull VkAccount account) {
		this.account = account;
	}

    /*@Nonnull
	@Override
    public List<Chat> getChats(@Nonnull Integer userId) {
        try {
            final List<Chat> result = new ArrayList<Chat>();
            for (VkMessagesGetDialogsHttpTransaction vkMessagesGetDialogsHttpTransaction : VkMessagesGetDialogsHttpTransaction.newInstances(100)) {
                result.addAll(HttpTransactions.execute(vkMessagesGetDialogsHttpTransaction));
            }
            return result;
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }*/

	@Nonnull
	@Override
	public List<Message> getMessages(@Nonnull String accountUserId) throws AccountConnectionException {
		try {
			return HttpTransactions.execute(new VkMessagesGetHttpTransaction(account, getUser(accountUserId)));
		} catch (HttpRuntimeIoException e) {
			throw new AccountConnectionException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountConnectionException(account.getId(), e);
		}
	}

	@Nonnull
	@Override
	public List<Message> getNewerMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId) throws AccountConnectionException {
		return getMessagesForChat(accountChatId, accountUserId, new VkHttpTransactionForMessagesForChatProvider() {
			@Nonnull
			@Override
			public List<? extends HttpTransaction<List<Message>>> getForPrivateChat(@Nonnull User user, @Nonnull String secondUserId) {
				return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forUser(account, secondUserId, user));
			}

			@Nonnull
			@Override
			public List<? extends HttpTransaction<List<Message>>> getForChat(@Nonnull User user, @Nonnull String chatId) {
				return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forChat(account, chatId, user));
			}
		});
	}

	private List<Message> getMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId, @Nonnull VkHttpTransactionForMessagesForChatProvider p) throws AccountConnectionException {
		final Chat chat = getChatService().getChatById(account.newChatEntity(realmChatId));

		if (chat != null) {
			try {
				if (chat.isPrivate()) {
					final int index = realmChatId.indexOf(":");
					if (index >= 0) {

						final String secondUserId = realmChatId.substring(index + 1, realmChatId.length());
						final List<Message> result = new ArrayList<Message>(100);
						for (List<Message> messages : HttpTransactions.execute(p.getForPrivateChat(getUser(realmUserId), secondUserId))) {
							result.addAll(messages);
						}
						return result;

					} else {
						Log.e(TAG, "Chat is private but don't have ':', chat id: " + realmChatId);
						return Collections.emptyList();
					}

				} else {
					final List<Message> result = new ArrayList<Message>(100);
					for (List<Message> messages : HttpTransactions.execute(p.getForChat(getUser(realmUserId), realmChatId))) {
						result.addAll(messages);
					}
					return result;
				}
			} catch (HttpRuntimeIoException e) {
				throw new AccountConnectionException(account.getId(), e);
			} catch (IOException e) {
				throw new AccountConnectionException(account.getId(), e);
			}
		} else {
			Log.e(TAG, "Chat is not found for chat id: " + realmChatId);
			return Collections.emptyList();
		}
	}

	@Nonnull
	@Override
	public List<Message> getOlderMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId, @Nonnull final Integer offset) throws AccountConnectionException {
		return getMessagesForChat(accountChatId, accountUserId, new VkHttpTransactionForMessagesForChatProvider() {
			@Nonnull
			@Override
			public List<? extends HttpTransaction<List<Message>>> getForPrivateChat(@Nonnull User user, @Nonnull String secondUserId) {
				return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forUser(account, secondUserId, user, offset));
			}

			@Nonnull
			@Override
			public List<? extends HttpTransaction<List<Message>>> getForChat(@Nonnull User user, @Nonnull String chatId) {
				return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forChat(account, chatId, user, offset));
			}
		});
	}

	private static interface VkHttpTransactionForMessagesForChatProvider {
		@Nonnull
		List<? extends HttpTransaction<List<Message>>> getForPrivateChat(@Nonnull User user, @Nonnull String secondUserId);

		@Nonnull
		List<? extends HttpTransaction<List<Message>>> getForChat(@Nonnull User user, @Nonnull String chatId);

	}

	@Nonnull
	private User getUser(@Nonnull String realmUserId) {
		return getUserService().getUserById(account.newUserEntity(realmUserId));
	}

	@Nonnull
	private UserService getUserService() {
		return App.getUserService();
	}

	@Nonnull
	private ChatService getChatService() {
		return App.getChatService();
	}


	@Nonnull
	@Override
	public List<AccountChat> getChats(@Nonnull String accountUserId) throws AccountConnectionException {
		try {
			final User user = App.getUserService().getUserById(account.newUserEntity(accountUserId));
			return HttpTransactions.execute(VkMessagesGetDialogsHttpTransaction.newInstance(account, user));
		} catch (HttpRuntimeIoException e) {
			throw new AccountConnectionException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountConnectionException(account.getId(), e);
		}
	}

	@Nonnull
	@Override
	public String sendMessage(@Nonnull Chat chat, @Nonnull Message message) throws AccountConnectionException {
		try {
			return HttpTransactions.execute(new VkMessagesSendHttpTransaction(account, message, chat));
		} catch (HttpRuntimeIoException e) {
			throw new AccountConnectionException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountConnectionException(account.getId(), e);
		}
	}

	@Override
	public void beforeSendMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableMessage message) throws AccountConnectionException {
	}

	@Nonnull
	@Override
	public Chat newPrivateChat(@Nonnull Entity accountChat, @Nonnull String accountUserId1, @Nonnull String accountUserId2) {
		return Chats.newPrivateChat(account.newChatEntity(accountUserId1 + CHAT_DELIMITER + accountUserId2));
	}
}
