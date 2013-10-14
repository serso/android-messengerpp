package org.solovyev.android.messenger.chats;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:42 PM
 */
public class ApiChatImpl implements ApiChat {

	@Nonnull
	private Chat chat;

	@Nonnull
	private List<Message> messages;

	@Nonnull
	private List<User> participants;

	private ApiChatImpl(@Nonnull Entity entity,
						@Nonnull Integer messagesCount,
						@Nonnull List<AProperty> properties,
						@Nonnull List<Message> messages,
						@Nonnull List<User> chatParticipants,
						@Nullable DateTime lastMessageSyncDate) {
		this.chat = ChatImpl.newInstance(entity, properties, lastMessageSyncDate);
		this.messages = messages;

		this.participants = chatParticipants;
	}

	public ApiChatImpl(@Nonnull Chat chat, @Nonnull List<Message> messages, @Nonnull List<User> participants) {
		this.chat = chat;
		this.messages = messages;
		this.participants = participants;
	}

	private ApiChatImpl(@Nonnull Entity entity,
						@Nonnull Integer messagesCount,
						boolean privateChat) {
		final List<AProperty> properties = new ArrayList<AProperty>();
		properties.add(Properties.newProperty("private", Boolean.toString(privateChat)));
		this.chat = ChatImpl.newInstance(entity, properties, null);

		this.messages = new ArrayList<Message>(20);
		this.participants = new ArrayList<User>(3);
	}

	@Nonnull
	public static ApiChatImpl newInstance(@Nonnull Entity entity,
										  @Nonnull Integer messagesCount,
										  boolean privateChat) {
		return new ApiChatImpl(entity, messagesCount, privateChat);
	}

	@Nonnull
	public static ApiChatImpl newInstance(@Nonnull Entity entity,
										  @Nonnull Integer messagesCount,
										  @Nonnull List<AProperty> properties,
										  @Nonnull List<Message> messages,
										  @Nonnull List<User> chatParticipants,
										  @Nullable DateTime lastMessageSyncDate) {
		return new ApiChatImpl(entity, messagesCount, properties, messages, chatParticipants, lastMessageSyncDate);
	}

	@Nonnull
	public static ApiChat newInstance(@Nonnull Chat chat, @Nonnull List<Message> messages, @Nonnull List<User> participants) {
		return new ApiChatImpl(chat, messages, participants);
	}

	@Nonnull
	public List<Message> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	@Override
	public Message getLastMessage() {
		return messages.isEmpty() ? null : messages.get(messages.size() - 1);
	}

	public void addMessage(@Nonnull Message message) {
		this.messages.add(message);
	}

	@Nonnull
	public List<User> getParticipants() {
		return Collections.unmodifiableList(participants);
	}

	@Nonnull
	@Override
	public List<User> getParticipantsExcept(@Nonnull User user) {
		return Lists.newArrayList(Iterables.filter(participants, not(equalTo(user))));
	}

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
	public ApiChat copyWithNew(@Nonnull Entity newAccountChat) {
		return new ApiChatImpl(chat.copyWithNew(newAccountChat), messages, participants);
	}

}
