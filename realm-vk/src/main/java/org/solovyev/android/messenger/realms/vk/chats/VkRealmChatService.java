package org.solovyev.android.messenger.realms.vk.chats;

import android.util.Log;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.http.HttpTransaction;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.RealmConnectionException;
import org.solovyev.android.messenger.realms.vk.VkRealm;
import org.solovyev.android.messenger.realms.vk.messages.VkMessagesSendHttpTransaction;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:30 PM
 */
public class VkRealmChatService implements RealmChatService {

	@Nonnull
	private static final String TAG = VkRealmChatService.class.getSimpleName();

	@Nonnull
	private static final String CHAT_DELIMITER = ":";

	@Nonnull
	private final VkRealm realm;

	public VkRealmChatService(@Nonnull VkRealm realm) {
		this.realm = realm;
	}

    /*@Nonnull
	@Override
    public List<Chat> getUserChats(@Nonnull Integer userId) {
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
	public List<ChatMessage> getChatMessages(@Nonnull String realmUserId) throws RealmConnectionException {
		try {
			return HttpTransactions.execute(new VkMessagesGetHttpTransaction(realm, getUser(realmUserId)));
		} catch (HttpRuntimeIoException e) {
			throw new RealmConnectionException(e);
		} catch (IOException e) {
			throw new RealmConnectionException(e);
		}
	}

	@Nonnull
	@Override
	public List<ChatMessage> getNewerChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId) throws RealmConnectionException {
		return getChatMessagesForChat(realmChatId, realmUserId, new VkHttpTransactionForMessagesForChatProvider() {
			@Nonnull
			@Override
			public List<? extends HttpTransaction<List<ChatMessage>>> getForPrivateChat(@Nonnull User user, @Nonnull String secondUserId) {
				return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forUser(realm, secondUserId, user));
			}

			@Nonnull
			@Override
			public List<? extends HttpTransaction<List<ChatMessage>>> getForChat(@Nonnull User user, @Nonnull String chatId) {
				return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forChat(realm, chatId, user));
			}
		});
	}

	private List<ChatMessage> getChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId, @Nonnull VkHttpTransactionForMessagesForChatProvider p) throws RealmConnectionException {
		final Chat chat = getChatService().getChatById(realm.newChatEntity(realmChatId));

		if (chat != null) {
			try {
				if (chat.isPrivate()) {
					final int index = realmChatId.indexOf(":");
					if (index >= 0) {

						final String secondUserId = realmChatId.substring(index + 1, realmChatId.length());
						final List<ChatMessage> result = new ArrayList<ChatMessage>(100);
						for (List<ChatMessage> messages : HttpTransactions.execute(p.getForPrivateChat(getUser(realmUserId), secondUserId))) {
							result.addAll(messages);
						}
						return result;

					} else {
						Log.e(TAG, "Chat is private but don't have ':', chat id: " + realmChatId);
						return Collections.emptyList();
					}

				} else {
					final List<ChatMessage> result = new ArrayList<ChatMessage>(100);
					for (List<ChatMessage> messages : HttpTransactions.execute(p.getForChat(getUser(realmUserId), realmChatId))) {
						result.addAll(messages);
					}
					return result;
				}
			} catch (HttpRuntimeIoException e) {
				throw new RealmConnectionException(e);
			} catch (IOException e) {
				throw new RealmConnectionException(e);
			}
		} else {
			Log.e(TAG, "Chat is not found for chat id: " + realmChatId);
			return Collections.emptyList();
		}
	}

	@Nonnull
	@Override
	public List<ChatMessage> getOlderChatMessagesForChat(@Nonnull String realmChatId, @Nonnull String realmUserId, @Nonnull final Integer offset) throws RealmConnectionException {
		return getChatMessagesForChat(realmChatId, realmUserId, new VkHttpTransactionForMessagesForChatProvider() {
			@Nonnull
			@Override
			public List<? extends HttpTransaction<List<ChatMessage>>> getForPrivateChat(@Nonnull User user, @Nonnull String secondUserId) {
				return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forUser(realm, secondUserId, user, offset));
			}

			@Nonnull
			@Override
			public List<? extends HttpTransaction<List<ChatMessage>>> getForChat(@Nonnull User user, @Nonnull String chatId) {
				return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forChat(realm, chatId, user, offset));
			}
		});
	}

	private static interface VkHttpTransactionForMessagesForChatProvider {
		@Nonnull
		List<? extends HttpTransaction<List<ChatMessage>>> getForPrivateChat(@Nonnull User user, @Nonnull String secondUserId);

		@Nonnull
		List<? extends HttpTransaction<List<ChatMessage>>> getForChat(@Nonnull User user, @Nonnull String chatId);

	}

	@Nonnull
	private User getUser(@Nonnull String realmUserId) {
		return getUserService().getUserById(realm.newUserEntity(realmUserId));
	}

	@Nonnull
	private UserService getUserService() {
		return MessengerApplication.getServiceLocator().getUserService();
	}

	@Nonnull
	private ChatService getChatService() {
		return MessengerApplication.getServiceLocator().getChatService();
	}


	@Nonnull
	@Override
	public List<ApiChat> getUserChats(@Nonnull String realmUserId) throws RealmConnectionException {
		try {
			final User user = MessengerApplication.getServiceLocator().getUserService().getUserById(realm.newUserEntity(realmUserId));
			return HttpTransactions.execute(VkMessagesGetDialogsHttpTransaction.newInstance(realm, user));
		} catch (HttpRuntimeIoException e) {
			throw new RealmConnectionException(e);
		} catch (IOException e) {
			throw new RealmConnectionException(e);
		}
	}

	@Nonnull
	@Override
	public String sendChatMessage(@Nonnull Chat chat, @Nonnull ChatMessage message) throws RealmConnectionException {
		try {
			return HttpTransactions.execute(new VkMessagesSendHttpTransaction(realm, message, chat));
		} catch (HttpRuntimeIoException e) {
			throw new RealmConnectionException(e);
		} catch (IOException e) {
			throw new RealmConnectionException(e);
		}
	}

	@Nonnull
	@Override
	public Chat newPrivateChat(@Nonnull Entity chat, @Nonnull String user1, @Nonnull String user2) {
		return Chats.newPrivateChat(realm.newChatEntity(user1 + CHAT_DELIMITER + user2));
	}
}
