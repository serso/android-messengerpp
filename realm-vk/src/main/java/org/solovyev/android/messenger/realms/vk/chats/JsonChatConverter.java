/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.realms.vk.chats;

import android.util.Log;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.MutableAccountChat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.realms.vk.messages.JsonMessage;
import org.solovyev.android.messenger.realms.vk.messages.JsonMessageTypedAttachment;
import org.solovyev.android.messenger.realms.vk.messages.JsonMessages;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Converter;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.solovyev.android.messenger.chats.Chats.newAccountChat;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:33 PM
 */
public class JsonChatConverter implements Converter<String, List<AccountChat>> {

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
	public List<AccountChat> convert(@Nonnull String json) {
		// todo serso: we need to save title for chat somewhere
		final Gson gson = new GsonBuilder()
				.registerTypeAdapter(JsonMessages.class, new JsonMessages.Adapter())
				.registerTypeAdapter(JsonMessageTypedAttachment.class, new JsonMessageTypedAttachment.Adapter())
				.create();

		final JsonMessages jsonMessagesResult = gson.fromJson(json, JsonMessages.class);

		final List<JsonMessage> jsonMessages = jsonMessagesResult.getResponse();

		// key: chat id, value: chat
		final Map<String, MutableAccountChat> chats = new HashMap<String, MutableAccountChat>();

		// key: id of second user, value: chat
		final Map<String, MutableAccountChat> fakeChats = new HashMap<String, MutableAccountChat>();

		try {
			final Splitter splitter = Splitter.on(",");

			if (!Collections.isEmpty(jsonMessages)) {
				for (JsonMessage jsonMessage : jsonMessages) {
					final MutableMessage message = jsonMessage.toMessage(user, explicitUserId, account);

					final Integer apiChatId = jsonMessage.getChat_id();
					if (apiChatId == null && explicitChatId == null) {

						// fake chat (message from user to another without explicitly created chat)
						final Entity secondUser = message.getSecondUser(user.getEntity());

						if (secondUser != null) {
							// vk allows to have messages sent to person self himself - we don't
							if (!secondUser.getAccountEntityId().equals(user.getEntity().getAccountEntityId())) {
								final Entity userId = user.getEntity();
								final Entity chatId = App.getChatService().getPrivateChatId(userId, secondUser);

								MutableAccountChat chat = fakeChats.get(chatId.getEntityId());
								if (chat == null) {
									chat = newAccountChat(chatId, true);

									chat.addParticipant(user);
									chat.addParticipant(userService.getUserById(secondUser, true));

									fakeChats.put(chatId.getEntityId(), chat);
								}

								chat.addMessage(message);
							}
						} else {
							Log.e(this.getClass().getSimpleName(), "Recipient is null for message " + message);
						}

					} else {
						// real chat
						final String realmChatId = apiChatId == null ? explicitChatId : String.valueOf(apiChatId);

						MutableAccountChat chat = chats.get(realmChatId);
						if (chat == null) {
							// create new chat object
							chat = newAccountChat(account.newChatEntity(realmChatId), false);

							final String participantsStr = jsonMessage.getChat_active();
							if (!Strings.isEmpty(participantsStr)) {
								for (Integer participantId : Iterables.transform(splitter.split(participantsStr), ToIntFunction.getInstance())) {
									chat.addParticipant(userService.getUserById(account.newUserEntity(String.valueOf(participantId)), true));
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

		final List<AccountChat> result = new ArrayList<AccountChat>(chats.size() + fakeChats.size());
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
