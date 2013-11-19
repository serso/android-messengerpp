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

package org.solovyev.android.messenger.realms.vk.longpoll;

import java.lang.reflect.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.MutableAccountChat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import static org.solovyev.android.messenger.chats.Chats.newAccountChat;

public interface LongPollUpdate {

	void doUpdate(@Nonnull Account account) throws AccountException;

	public static class Adapter implements JsonDeserializer<LongPollUpdate> {

		@Override
		public LongPollUpdate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

			if (json.isJsonArray()) {

				final JsonArray jsonArray = json.getAsJsonArray();

				switch (jsonArray.get(0).getAsInt()) {
					case 0:
						return new RemoveMessage(jsonArray.get(1).getAsInt());
					//case 3: todo serso: implement ONE message update
					case 4:
						int flags = jsonArray.get(2).getAsInt();
						int chatUserId = jsonArray.get(3).getAsInt();
						// todo serso: uncomment after answer
						if (/*MessageFlag.chat.isApplied(flags) && */chatUserId >= 2000000000) {

							// MAGIC MAGIC MAGIC
							int chatId = chatUserId - 2000000000;

							return MessageAdded.forChat(String.valueOf(chatId));

						} else {
							int userId = chatUserId;
							return MessageAdded.forFriend(String.valueOf(userId));
						}
					case 8:
						return new FriendOnline(String.valueOf(-jsonArray.get(1).getAsInt()), true);
					case 9:
						return new FriendOnline(String.valueOf(-jsonArray.get(1).getAsInt()), false);
					case 51:
						return new ChatChanged(jsonArray.get(1).getAsString());
					case 61:
						return new UserStartTypingInPrivateChat(jsonArray.get(1).getAsString());
					case 62:
						return new UserStartTypingInChat(jsonArray.get(1).getAsString(), jsonArray.get(2).getAsString());
				}

				return new EmptyLongPollUpdate();


                /*final JsonArray responseArray = response.getAsJsonArray("response");

                boolean first = true;

                result.response = new ArrayList<JsonMessage>();
                for (JsonElement e : responseArray.getAsJsonArray()) {
                    if (first) {
                        result.count = e.getAsInt();
                        first = false;
                    } else {
                        result.response.add((JsonMessage) context.deserialize(e, JsonMessage.class));
                    }
                }*/

			} else {
				throw new JsonParseException("Unexpected JSON type: " + json.getClass());
			}
		}
	}

	static class UserStartTypingInChat implements LongPollUpdate {

		@Nonnull
		private final String accountUserId;

		@Nonnull
		private final String accountChatId;

		public UserStartTypingInChat(@Nonnull String accountUserId, @Nonnull String accountChatId) {
			this.accountUserId = accountUserId;
			this.accountChatId = accountChatId;
		}

		@Override
		public void doUpdate(@Nonnull Account account) {
			// not self
			if (!account.getUser().getEntity().getAccountEntityId().equals(accountUserId)) {
				Chat chat = getChatService().getChatById(account.newChatEntity(accountChatId));
				if (chat != null) {
					getChatService().fireEvent(ChatEventType.user_is_typing.newEvent(chat, account.newUserEntity(accountUserId)));
				}
			}
		}


		@Nonnull
		private static ChatService getChatService() {
			return App.getChatService();
		}
	}

	static class UserStartTypingInPrivateChat implements LongPollUpdate {

		@Nonnull
		private String accountUserId;

		public UserStartTypingInPrivateChat(@Nonnull String accountUserId) {
			this.accountUserId = accountUserId;
		}

		@Override
		public void doUpdate(@Nonnull Account account) {
			// not self
			final User accountUser = account.getUser();
			if (!accountUser.getEntity().getAccountEntityId().equals(accountUserId)) {
				final Entity secondAccountUser = account.newUserEntity(accountUserId);

				final Entity chatId = getChatService().getPrivateChatId(accountUser.getEntity(), secondAccountUser);
				final Chat chat = getChatService().getChatById(chatId);
				if (chat != null) {
					getChatService().fireEvent(ChatEventType.user_is_typing.newEvent(chat, secondAccountUser));
				}
			}
		}


		@Nonnull
		private static ChatService getChatService() {
			return App.getChatService();
		}
	}

	static class ChatChanged implements LongPollUpdate {

		@Nonnull
		private final String accountChatId;

		public ChatChanged(@Nonnull String accountChatId) {
			this.accountChatId = accountChatId;
		}

		@Override
		public void doUpdate(@Nonnull Account account) throws AccountException {
			getChatService().syncChat(account.newChatEntity(accountChatId), account.getUser().getEntity());
		}


		@Nonnull
		private ChatService getChatService() {
			return App.getChatService();
		}
	}

	static class EmptyLongPollUpdate implements LongPollUpdate {

		@Override
		public void doUpdate(@Nonnull Account account) {
			// do nothing
		}


	}

	static class MessageAdded implements LongPollUpdate {

		@Nullable
		private String accountFriendId;

		@Nullable
		private String accountChatId;

		private MessageAdded() {
		}

		public static MessageAdded forChat(@Nonnull String accountChatId) {
			final MessageAdded result = new MessageAdded();

			result.accountFriendId = null;
			result.accountChatId = accountChatId;

			return result;
		}

		public static MessageAdded forFriend(@Nonnull String accountFriendId) {
			final MessageAdded result = new MessageAdded();

			result.accountFriendId = accountFriendId;
			result.accountChatId = null;

			return result;
		}

		@Override
		public void doUpdate(@Nonnull Account account) throws AccountException {
			final Chat chat;
			if (this.accountChatId != null) {
				final Entity chatId = account.newChatEntity(this.accountChatId);
				final Chat oldChat = getChatService().getChatById(chatId);
				if (oldChat == null) {
					final MutableAccountChat newChat = newAccountChat(account.newChatEntity(accountChatId), false);
					newChat.addParticipant(account.getUser());
					chat = getChatService().saveChat(account.getUser().getEntity(), newChat);
				} else {
					chat = oldChat;
				}
			} else {
				assert accountFriendId != null;
				chat = getChatService().getOrCreatePrivateChat(account.getUser().getEntity(), account.newUserEntity(accountFriendId));
			}

			if (chat != null) {
				getChatService().syncNewerMessagesForChat(chat.getEntity());
			}
		}

		@Nonnull
		private ChatService getChatService() {
			return App.getChatService();
		}
	}

	static class FriendOnline implements LongPollUpdate {

		@Nonnull
		private final String accountFriendId;

		private final boolean online;

		public FriendOnline(@Nonnull String accountFriendId, boolean online) {
			this.accountFriendId = accountFriendId;
			this.online = online;
		}

		@Override
		public void doUpdate(@Nonnull Account account) {
			final User contact = getUserService().getUserById(account.newUserEntity(accountFriendId), true).cloneWithNewStatus(online);
			getUserService().onContactPresenceChanged(account.getUser(), contact, online);
		}

		private UserService getUserService() {
			return App.getUserService();
		}
	}

	static class RemoveMessage implements LongPollUpdate {

		@Nonnull
		private final Integer messageId;

		public RemoveMessage(@Nonnull Integer messageId) {
			this.messageId = messageId;
		}

		@Override
		public void doUpdate(@Nonnull Account account) {
			// todo serso: implement
		}
	}

}
