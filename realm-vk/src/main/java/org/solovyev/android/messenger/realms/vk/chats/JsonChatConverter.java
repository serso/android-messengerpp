package org.solovyev.android.messenger.realms.vk.chats;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.ApiChatImpl;
import org.solovyev.android.messenger.messages.ChatMessage;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.realms.vk.messages.JsonMessage;
import org.solovyev.android.messenger.realms.vk.messages.JsonMessageTypedAttachment;
import org.solovyev.android.messenger.realms.vk.messages.JsonMessages;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Converter;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.text.Strings;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:33 PM
 */
public class JsonChatConverter implements Converter<String, List<ApiChat>> {

	@Nonnull
	private final User user;

	@Nullable
	private final String explicitChatId;

	@Nullable
	private final String explicitUserId;

	@Nonnull
	private final UserService userService;

	@Nonnull
	private final Account account;

	public JsonChatConverter(@Nonnull User user,
							 @Nullable String explicitChatId,
							 @Nullable String explicitUserId,
							 @Nonnull UserService userService,
							 @Nonnull Account account) {
		this.user = user;
		this.explicitChatId = explicitChatId;
		this.explicitUserId = explicitUserId;
		this.userService = userService;
		this.account = account;
	}

	@Nonnull
	@Override
	public List<ApiChat> convert(@Nonnull String json) {
		final Gson gson = new GsonBuilder()
				.registerTypeAdapter(JsonMessages.class, new JsonMessages.Adapter())
				.registerTypeAdapter(JsonMessageTypedAttachment.class, new JsonMessageTypedAttachment.Adapter())
				.create();

		final JsonMessages jsonMessagesResult = gson.fromJson(json, JsonMessages.class);

		final List<JsonMessage> jsonMessages = jsonMessagesResult.getResponse();

		// key: chat id, value: chat
		final Map<String, ApiChatImpl> chats = new HashMap<String, ApiChatImpl>();

		// key: id of second user, value: chat
		final Map<String, ApiChatImpl> fakeChats = new HashMap<String, ApiChatImpl>();

		try {
			final Splitter splitter = Splitter.on(",");

			if (!Collections.isEmpty(jsonMessages)) {
				for (JsonMessage jsonMessage : jsonMessages) {
					final ChatMessage message = jsonMessage.toChatMessage(user, explicitUserId, account);

					final Integer apiChatId = jsonMessage.getChat_id();
					if (apiChatId == null && explicitChatId == null) {

						// fake chat (message from user to another without explicitly created chat)
						final Entity secondUser = message.getSecondUser(user.getEntity());

						if (secondUser != null) {
							// vk allows to have messages sent to person self himself - we don't
							if (!secondUser.getAccountEntityId().equals(user.getEntity().getAccountEntityId())) {
								final Entity realmUser = user.getEntity();
								final Entity realmChat = App.getChatService().getPrivateChatId(realmUser, secondUser);

								ApiChatImpl chat = fakeChats.get(realmChat.getEntityId());
								if (chat == null) {
									chat = ApiChatImpl.newInstance(realmChat, jsonMessagesResult.getCount(), true);

									chat.addParticipant(user);
									chat.addParticipant(userService.getUserById(secondUser));

									fakeChats.put(realmChat.getEntityId(), chat);
								}

								chat.addMessage(message);
							}
						} else {
							Log.e(this.getClass().getSimpleName(), "Recipient is null for message " + message);
						}

					} else {
						// real chat
						final String realmChatId = apiChatId == null ? explicitChatId : String.valueOf(apiChatId);

						ApiChatImpl chat = chats.get(realmChatId);
						if (chat == null) {
							// create new chat object
							chat = ApiChatImpl.newInstance(account.newChatEntity(realmChatId), jsonMessagesResult.getCount(), false);

							final String participantsStr = jsonMessage.getChat_active();
							if (!Strings.isEmpty(participantsStr)) {
								for (Integer participantId : Iterables.transform(splitter.split(participantsStr), ToIntFunction.getInstance())) {
									chat.addParticipant(userService.getUserById(account.newUserEntity(String.valueOf(participantId))));
								}
							}

							chat.addParticipant(user);

							chats.put(realmChatId, chat);
						}

						chat.addMessage(message);
					}
				}
			}
		} catch (IllegalJsonException e) {
			throw new IllegalJsonRuntimeException(e);
		}

		final List<ApiChat> result = new ArrayList<ApiChat>(chats.size() + fakeChats.size());
		result.addAll(chats.values());
		result.addAll(fakeChats.values());
		return result;
	}

	private static class ToIntFunction implements Function<String, Integer> {

		@Nonnull
		private static final ToIntFunction instance = new ToIntFunction();

		private ToIntFunction() {
		}

		@Nonnull
		public static ToIntFunction getInstance() {
			return instance;
		}

		@Override
		public Integer apply(@Nullable String input) {
			return Integer.valueOf(input);
		}
	}
}
