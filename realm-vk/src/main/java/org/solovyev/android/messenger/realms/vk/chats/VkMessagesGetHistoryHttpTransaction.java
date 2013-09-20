package org.solovyev.android.messenger.realms.vk.chats;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.solovyev.android.http.HttpTransaction;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.http.AbstractVkHttpTransaction;
import org.solovyev.android.messenger.realms.vk.users.ApiUserField;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: serso
 * Date: 6/10/12
 * Time: 10:15 PM
 */

public class VkMessagesGetHistoryHttpTransaction extends AbstractVkHttpTransaction<List<ChatMessage>> {

	@Nonnull
	private static final Integer MAX_COUNT = 100;

	@Nullable
	private Integer count;

	@Nullable
	private String chatId;

	@Nullable
	private String userId;

	@Nonnull
	private User user;

	@Nullable
	private Integer offset;

	private VkMessagesGetHistoryHttpTransaction(@Nonnull VkAccount realm) {
		super(realm, "messages.getHistory");
	}

	@Nonnull
	public static HttpTransaction<List<ChatMessage>> forChat(@Nonnull VkAccount realm, @Nonnull String chatId, @Nonnull User user) {
		final VkMessagesGetHistoryHttpTransaction result = new VkMessagesGetHistoryHttpTransaction(realm);

		result.chatId = chatId;
		result.user = user;

		return result;
	}

	@Nonnull
	public static HttpTransaction<List<ChatMessage>> forChat(@Nonnull VkAccount realm, @Nonnull String chatId, @Nonnull User user, @Nonnull Integer offset) {
		final VkMessagesGetHistoryHttpTransaction result = new VkMessagesGetHistoryHttpTransaction(realm);

		result.chatId = chatId;
		result.user = user;
		result.offset = offset;

		return result;
	}

	@Nonnull
	public static HttpTransaction<List<ChatMessage>> forUser(@Nonnull VkAccount realm, @Nonnull String userId, @Nonnull User user) {
		final VkMessagesGetHistoryHttpTransaction result = new VkMessagesGetHistoryHttpTransaction(realm);

		result.userId = userId;
		result.user = user;

		return result;
	}

	@Nonnull
	public static HttpTransaction<List<ChatMessage>> forUser(@Nonnull VkAccount realm, @Nonnull String userId, @Nonnull User user, @Nonnull Integer offset) {
		final VkMessagesGetHistoryHttpTransaction result = new VkMessagesGetHistoryHttpTransaction(realm);

		result.userId = userId;
		result.user = user;
		result.offset = offset;

		return result;
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> requestParameters = super.getRequestParameters();

		if (count != null) {
			requestParameters.add(new BasicNameValuePair("count", String.valueOf(count)));
		}

		if (userId != null) {
			requestParameters.add(new BasicNameValuePair("uid", String.valueOf(userId)));
		}

		if (chatId != null) {
			requestParameters.add(new BasicNameValuePair("chat_id", chatId));
		}

		if (offset != null) {
			requestParameters.add(new BasicNameValuePair("offset", String.valueOf(offset)));
		}

		requestParameters.add(new BasicNameValuePair("fields", Strings.getAllValues(Arrays.asList(ApiUserField.uid, ApiUserField.last_name))));

		return requestParameters;
	}

	@Override
	protected List<ChatMessage> getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
		final List<ApiChat> chats = new JsonChatConverter(user, chatId, userId, MessengerApplication.getServiceLocator().getUserService(), getRealm()).convert(json);

		// todo serso: optimize - convert json to the messages directly
		final List<ChatMessage> messages = new ArrayList<ChatMessage>(chats.size() * 10);
		for (ApiChat chat : chats) {
			messages.addAll(chat.getMessages());
		}

		return messages;
	}
}
