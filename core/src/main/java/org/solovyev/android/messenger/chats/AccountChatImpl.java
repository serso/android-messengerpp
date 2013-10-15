package org.solovyev.android.messenger.chats;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
		properties.add(newProperty("private", Boolean.toString(privateChat)));
		this.chat = Chats.newChat(chat, properties, null);

		this.messages = new ArrayList<Message>(20);
		this.participants = new ArrayList<User>(3);
	}

	@Nonnull
	public List<Message> getMessages() {
		return unmodifiableList(messages);
	}

	@Override
	public void addMessage(@Nonnull Message message) {
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

}
