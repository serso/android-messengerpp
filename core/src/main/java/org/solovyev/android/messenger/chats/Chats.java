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
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.min;
import static java.util.Collections.sort;
import static org.solovyev.android.messenger.App.getChatService;
import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.App.getUnreadMessagesCounter;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:49 PM
 */
public final class Chats {

	@Nonnull
	public static final String CHATS_FRAGMENT_TAG = "chats";
	static final int MAX_RECENT_CHATS = 20;

	private Chats() {
		throw new AssertionError();
	}

	@Nonnull
	static String getDisplayName(@Nonnull Chat chat, @Nullable Message lastMessage, @Nonnull User user, int unreadMessagesCount) {
		String result = getDisplayName(chat, lastMessage, user);
		if (unreadMessagesCount > 0) {
			result += " (" + unreadMessagesCount + ")";
		}
		return result;
	}

	@Nonnull
	static String getDisplayName(@Nonnull Chat chat, @Nullable Message message, @Nonnull User user) {
		final String title = message != null ? message.getTitle() : null;
		if (Strings.isEmpty(title) || title.equals(" ... ")) {

			if (chat.isPrivate()) {
				return Users.getDisplayNameFor(chat.getSecondUser());
			} else {
				return "";
			}
		} else {
			return title;
		}
	}

	@Nonnull
	public static MutableChat newChat(@Nonnull Entity entity,
							   @Nonnull List<AProperty> properties,
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
	public static MutableAccountChat newEmptyAccountChat(@Nonnull Chat chat, @Nonnull List<User> participants) {
		return new AccountChatImpl(chat, Collections.<Message>emptyList(), participants);
	}

	@Nonnull
	static List<UiChat> getLastChatsByDate(@Nonnull List<UiChat> result, int count) {
		sort(result, new LastMessageDateChatComparator());

		return result.subList(0, min(result.size(), count));
	}

	public static void openUnreadChat(@Nonnull Context context) {
		final Entity chatId = getUnreadMessagesCounter().getUnreadChat();
		if (chatId != null) {
			final Chat chat = getChatService().getChatById(chatId);
			if (chat != null) {
				getEventManager(context).fire(ChatUiEventType.open_chat.newEvent(chat));
			}
		}
	}
}
