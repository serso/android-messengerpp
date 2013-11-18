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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.solovyev.android.messenger.EntityAwareByIdFinder;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;
import static org.solovyev.android.properties.Properties.newProperty;

class AccountChatImpl implements MutableAccountChat {

	@Nonnull
	private Chat chat;

	@Nonnull
	private List<Message> messages;

	@Nonnull
	private List<User> participants;

	AccountChatImpl(@Nonnull Chat chat, @Nonnull List<Message> messages, @Nonnull List<User> participants) {
		this.chat = chat;
		this.messages = messages;
		this.participants = participants;
	}

	AccountChatImpl(@Nonnull Entity chat,
					boolean privateChat) {
		final List<AProperty> properties = new ArrayList<AProperty>();
		properties.add(newProperty(Chat.PROPERTY_PRIVATE, Boolean.toString(privateChat)));
		this.chat = Chats.newChat(chat, properties, null);

		this.messages = new ArrayList<Message>(20);
		this.participants = new ArrayList<User>(3);
	}

	@Nonnull
	public List<Message> getMessages() {
		return unmodifiableList(messages);
	}

	@Override
	public void addMessage(@Nonnull MutableMessage message) {
		message.setChat(this.chat.getEntity());
		addParticipant(message.getAuthor());
		final Entity recipient = message.getRecipient();
		if (recipient != null) {
			addParticipant(recipient);
		}
		this.messages.add(message);
	}

	@Nonnull
	public List<User> getParticipants() {
		return unmodifiableList(participants);
	}

	@Nonnull
	@Override
	public List<User> getParticipantsExcept(@Nonnull User user) {
		return newArrayList(filter(participants, not(equalTo(user))));
	}

	@Override
	public boolean addParticipant(@Nonnull User participant) {
		if (!participants.contains(participant)) {
			if (this.chat.isPrivate()) {
				if (participants.size() == 2) {
					throw new IllegalArgumentException("Only 2 participants can be in private chat!");
				}
			}
			return participants.add(participant);
		}

		return false;
	}

	private boolean addParticipant(@Nonnull Entity participant) {
		final boolean contains = Iterables.find(participants, new EntityAwareByIdFinder(participant.getEntityId()), null) != null;
		if (!contains) {
			if (this.chat.isPrivate()) {
				if (participants.size() == 2) {
					throw new IllegalArgumentException("Only 2 participants can be in private chat!");
				}
			}
			return participants.add(Users.newEmptyUser(participant));
		}

		return false;
	}

	@Override
	@Nonnull
	public Chat getChat() {
		return chat;
	}

	@Nonnull
	@Override
	public AccountChat copyWithNewId(@Nonnull final Entity id) {
		final Chat chat = this.chat.copyWithNewId(id);
		final List<Message> messages = newArrayList(transform(this.messages, new Function<Message, Message>() {
			@Override
			public Message apply(Message message) {
				return message.cloneWithNewChat(id);
			}
		}));
		return new AccountChatImpl(chat, messages, participants);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AccountChatImpl)) return false;

		AccountChatImpl that = (AccountChatImpl) o;

		if (!chat.equals(that.chat)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return chat.hashCode();
	}
}
