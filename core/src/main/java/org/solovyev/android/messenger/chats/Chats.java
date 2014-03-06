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

package org.solovyev.android.messenger.chats;

import android.content.Context;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.solovyev.android.messenger.App.*;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.common.text.Strings.isEmpty;

public final class Chats {

	@Nonnull
	public static final String CHATS_FRAGMENT_TAG = "chats";

	private Chats() {
		throw new AssertionError();
	}

	@Nonnull
	static String getDisplayName(@Nonnull Chat chat, @Nullable Message lastMessage, int unreadMessagesCount) {
		String result = getDisplayName(chat, lastMessage);
		if (unreadMessagesCount > 0) {
			result += " (" + unreadMessagesCount + ")";
		}
		return result;
	}

	@Nonnull
	public static String getDisplayName(@Nonnull Chat chat, @Nullable Message message) {
		final String chatTitle = chat.getTitle();
		if (isEmptyTitle(chatTitle)) {
			final String messageTitle = message != null ? message.getTitle() : null;
			if (isEmptyTitle(messageTitle)) {

				if (chat.isPrivate()) {
					return Users.getDisplayNameFor(chat.getSecondUser());
				} else {
					return "";
				}
			} else {
				assert messageTitle != null;
				return messageTitle;
			}
		} else {
			assert chatTitle != null;
			return chatTitle;
		}
	}

	private static boolean isEmptyTitle(@Nullable String title) {
		return isEmpty(title) || title.equals(" ... ") || title.equals("...");
	}

	@Nonnull
	public static MutableChat newChat(@Nonnull Entity entity,
									  @Nonnull Collection<AProperty> properties,
									  @Nullable DateTime lastMessageSyncDate) {
		return new ChatImpl(entity, properties, lastMessageSyncDate);
	}

	@Nonnull
	public static MutableChat newPrivateChat(@Nonnull Entity chat) {
		return ChatImpl.newPrivateChat(chat);
	}

	@Nonnull
	public static MutableChat newEmptyChat(@Nonnull String chatId) {
		return new ChatImpl(newEntityFromEntityId(chatId), false);
	}

	@Nonnull
	public static MutableAccountChat newAccountChat(@Nonnull Entity chat, boolean privateChat) {
		return new AccountChatImpl(chat, privateChat);
	}

	@Nonnull
	public static MutableAccountChat newPrivateAccountChat(@Nonnull Entity chat,
														   @Nonnull User user,
														   @Nonnull User participant,
														   @Nonnull Collection<? extends MutableMessage> messages) {
		final MutableAccountChat result = newAccountChat(chat, true);

		result.addParticipant(user);
		result.addParticipant(participant);

		for (MutableMessage message : messages) {
			result.addMessage(message);
		}

		return result;
	}

	@Nonnull
	public static MutableAccountChat newEmptyAccountChat(@Nonnull MutableChat chat, @Nonnull List<User> participants) {
		return new AccountChatImpl(chat, Collections.<Message>emptyList(), participants);
	}

	@Nonnull
	public static MutableAccountChat newEmptyAccountChat(@Nonnull Chat chat, @Nonnull List<User> participants) {
		if (chat instanceof MutableChat) {
			return newEmptyAccountChat((MutableChat) chat, participants);
		} else {
			return new AccountChatImpl(Chats.newChat(chat.getEntity(), chat.getPropertiesCollection(), chat.getLastMessagesSyncDate()), Collections.<Message>emptyList(), participants);
		}
	}

	public static void openUnreadChat(@Nonnull Context context) {
		final Entity chatId = getUnreadMessagesCounter().getUnreadChat();
		if (chatId != null) {
			openChat(context, chatId);
		}
	}

	public static void openChat(@Nonnull Context context, @Nonnull Entity chatId) {
		final Chat chat = getChatService().getChatById(chatId);
		if (chat != null) {
			getEventManager(context).fire(new ChatUiEvent.Open(chat));
		}
	}
}
