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

import android.app.Application;
import android.util.Log;
import android.widget.ImageView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.list.PrefixFilter;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.*;
import org.solovyev.android.messenger.users.*;
import org.solovyev.common.collections.multimap.ThreadSafeMultimap;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.*;
import java.util.concurrent.Executor;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.solovyev.android.messenger.chats.Chat.PROPERTY_DRAFT_MESSAGE;
import static org.solovyev.android.messenger.chats.Chats.newEmptyAccountChat;
import static org.solovyev.android.messenger.chats.UiChat.loadUiChat;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.android.properties.Properties.newProperty;
import static org.solovyev.common.text.Strings.isEmpty;

@Singleton
public class DefaultChatService implements ChatService {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	@Nonnull
	private static final Character PRIVATE_CHAT_DELIMITER = ':';

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private AccountService accountService;

	@GuardedBy("lock")
	@Inject
	@Nonnull
	private ChatDao chatDao;

	@Inject
	@Nonnull
	private MessageService messageService;

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private Application context;

	@Inject
	@Nonnull
	private MessageDao messageDao;

	@Inject
	@Nonnull
	private UnreadMessagesCounter unreadMessagesCounter;


	/*
	**********************************************************************
	*
	*                           OWN FIELDS
	*
	**********************************************************************
	*/

	@Nonnull
	private final JEventListeners<JEventListener<? extends ChatEvent>, ChatEvent> listeners;

	@Nonnull
	private final ChatParticipants participants = new ChatParticipants();

	@Nonnull
	private LastMessages lastMessages;

	@Nonnull
	private final ChatCache cache = new ChatCache();

	@Nonnull
	private final Object lock;

	@Inject
	public DefaultChatService(@Nonnull PersistenceLock lock, @Nonnull Executor eventExecutor) {
		this.listeners = Listeners.newEventListenersBuilderFor(ChatEvent.class).withHardReferences().withExecutor(eventExecutor).create();
		this.listeners.addListener(new ChatEventListener());
		this.lock = lock;
	}

	@Override
	public void init() {
		this.lastMessages = new LastMessages(this, messageService);
	}

	@Nonnull
	@Override
	public Chat updateChat(@Nonnull Chat chat) {
		final boolean changed;
		synchronized (lock) {
			changed = chatDao.update(chat) >= 0;
		}

		if (changed) {
			fireEvent(ChatEventType.changed.newEvent(chat));
		}

		return chat;
	}

	@Nonnull
	private Chat newPrivateChat(@Nonnull Entity user1, @Nonnull Entity user2) throws AccountException {
		final Account account = getAccountByEntity(user1);

		Chat result;

		final Entity accountChat = getPrivateChatId(user1, user2);
		synchronized (lock) {
			result = getChatById(accountChat);
			if (result == null) {
				// no private chat exists => create one
				final AccountChatService acs = account.getAccountChatService();

				MutableChat chat = acs.newPrivateChat(accountChat, user1.getAccountEntityId(), user2.getAccountEntityId());

				chat = preparePrivateChat(chat, user1, user2);

				final List<User> participants = new ArrayList<User>(2);
				participants.add(userService.getUserById(user1));
				participants.add(userService.getUserById(user2));
				final AccountChat apiChat = newEmptyAccountChat(chat, participants);

				userService.mergeChats(account, asList(apiChat));

				result = apiChat.getChat();
			}
		}

		return result;
	}

	/**
	 * Method prepares private chat for inserting into database.
	 *
	 * @param chat  chat to be prepared
	 * @param user1 first participant
	 * @param user2 second participant
	 * @return prepared chat
	 */
	@Nonnull
	private MutableChat preparePrivateChat(@Nonnull MutableChat chat, @Nonnull Entity user1, @Nonnull Entity user2) throws UnsupportedAccountException {
		final Account account = getAccountByEntity(user1);
		final Entity chatEntity = getPrivateChatId(user1, user2);

		if (!chatEntity.getAccountEntityId().equals(chat.getEntity().getAccountEntityId())) {
			/**
			 * chat id that was created by realm (may differ from one created in {@link org.solovyev.android.messenger.chats.ChatService#getPrivateChatId(org.solovyev.android.messenger.entities.Entity, org.solovyev.android.messenger.entities.Entity)) method)
			 */
			final String realmChatId = chat.getEntity().getAccountEntityId();

			// copy with new id
			chat = chat.copyWithNewId(account.newEntity(realmChatId, chatEntity.getEntityId()));
		}

		return chat;
	}

	@Nonnull
	private AccountChat prepareChat(@Nonnull AccountChat accountChat) throws UnsupportedAccountException {
		final Account account = accountService.getAccountById(accountChat.getChat().getEntity().getAccountId());
		final User user = account.getUser();
		final List<User> participants = accountChat.getParticipantsExcept(user);

		// let's check if all participants are saved in the app
		for (User participant : participants) {
			try {
				userService.getUserById(participant.getEntity(), false, false);
			} catch (NoSuchElementException e) {
				userService.saveUser(participant);
			}
		}

		if (accountChat.getChat().isPrivate()) {
			if (participants.size() == 1) {
				final Entity participant1 = user.getEntity();
				final Entity participant2 = participants.get(0).getEntity();

				final Entity chat = getPrivateChatId(participant1, participant2);

				if (!chat.getAccountEntityId().equals(accountChat.getChat().getEntity().getAccountEntityId())) {
					/**
					 * chat id that was created by account (may differ from one created in {@link org.solovyev.android.messenger.chats.ChatService#getPrivateChatId(org.solovyev.android.messenger.entities.Entity, org.solovyev.android.messenger.entities.Entity)) method)
					 */
					final String accountChatId = accountChat.getChat().getEntity().getAccountEntityId();

					// copy with new id
					accountChat = accountChat.copyWithNewId(account.newEntity(accountChatId, chat.getEntityId()));
				}
			}
		}

		return accountChat;
	}

	@Nonnull
	@Override
	public List<Chat> loadChats(@Nonnull Entity user) {
		synchronized (lock) {
			return chatDao.readChatsByUserId(user.getEntityId());
		}
	}

	@Nullable
	@Override
	public Chat saveChat(@Nonnull Entity user, @Nonnull AccountChat chat) {
		final MergeDaoResult<Chat, String> mergeResult = mergeChats(user, asList(chat));

		Chat result = getFirst(mergeResult.getAddedObjects(), null);
		if (result == null) {
			result = getFirst(mergeResult.getUpdatedObjects(), null);
		}

		return result;
	}

	@Nonnull
	@Override
	public Map<Entity, Integer> getUnreadChats() {
		synchronized (lock) {
			return chatDao.getUnreadChats();
		}
	}

	@Override
	public void onUnreadMessagesCountChanged(@Nonnull Entity chatEntity, @Nonnull Integer unreadMessagesCount) {
		final Chat chat = getChatById(chatEntity);
		if (chat != null) {
			fireEvent(ChatEventType.unread_message_count_changed.newEvent(chat, unreadMessagesCount));

			if (chat.isPrivate()) {
				final Entity secondUser = getSecondUser(chat);
				if (secondUser != null) {
					userService.onUnreadMessagesCountChanged(secondUser, unreadMessagesCount);
				}
			}
		}
	}

	@Override
	public int getUnreadMessagesCount(@Nonnull Entity chat) {
		return unreadMessagesCounter.getUnreadMessagesCountForChat(chat);
	}

	@Nonnull
	@Override
	public MergeDaoResult<Chat, String> mergeChats(@Nonnull final Entity user, @Nonnull List<? extends AccountChat> chats) {
		final ChatMergeDaoResult result;

		synchronized (lock) {
			result = chatDao.mergeChats(user.getEntityId(), prepareChats(chats));
		}

		for (final Chat chat : result.getUpdatedObjects()) {
			final AccountChat accountChat = find(chats, new Predicate<AccountChat>() {
				@Override
				public boolean apply(AccountChat accountChat) {
					return accountChat.getChat().equals(chat);
				}
			}, null);

			if (accountChat != null) {
				saveMessages(chat, accountChat.getMessages(), false);
			}
		}

		fireChatEvents(userService.getUserById(user), result);

		return result;

	}

	@Nonnull
	private List<AccountChat> prepareChats(List<? extends AccountChat> chats) {
		return newArrayList(transform(chats, new Function<AccountChat, AccountChat>() {
			@Override
			public AccountChat apply(AccountChat chat) {
				return prepareChat(chat);
			}
		}));
	}

	private void fireChatEvents(@Nonnull User user, @Nonnull ChatMergeDaoResult mergeResult) {
		final List<UserEvent> userEvents = new ArrayList<UserEvent>();
		final List<ChatEvent> chatEvents = new ArrayList<ChatEvent>();

		final List<Chat> addedChats = mergeResult.getAddedObjects();
		for (Chat addedChat : addedChats) {
			chatEvents.add(ChatEventType.added.newEvent(addedChat));
		}

		if (!addedChats.isEmpty()) {
			userEvents.add(UserEventType.chats_added.newEvent(user, addedChats));
		}

		for (Map.Entry<Chat, Collection<Message>> entry : mergeResult.getNewMessages().entrySet()) {
			chatEvents.add(ChatEventType.messages_added.newEvent(entry.getKey(), entry.getValue()));
		}

		for (String removedChatId : mergeResult.getRemovedObjectIds()) {
			userEvents.add(UserEventType.chat_removed.newEvent(user, removedChatId));
		}

		for (Chat updatedChat : mergeResult.getUpdatedObjects()) {
			chatEvents.add(ChatEventType.changed.newEvent(updatedChat));
		}

		userService.fireEvents(userEvents);
		fireEvents(chatEvents);
	}

	@Nullable
	@Override
	public Chat getChatById(@Nonnull Entity chat) {
		Chat result = cache.get(chat);

		if (result == null) {
			synchronized (lock) {
				result = chatDao.read(chat.getEntityId());
			}

			if (result != null) {
				cache.put(result);
			}
		}

		return result;
	}


	@Nonnull
	private Account getAccountByEntity(@Nonnull Entity entity) throws UnsupportedAccountException {
		return accountService.getAccountById(entity.getAccountId());
	}

	@Nonnull
	@Override
	public List<Message> syncMessages(@Nonnull Account<?> account) throws AccountException {
		final Entity user = account.getUser().getEntity();
		final List<? extends Message> messages = account.getAccountChatService().getMessages();

		final Multimap<Chat, Message> messagesByChats = ArrayListMultimap.create();

		for (Message message : messages) {
			final Entity chatEntity = message.getChat();
			Chat chat = getChatById(chatEntity);
			if (chat == null) {
				if (message.isPrivate()) {
					final Entity participant = message.getSecondUser(user);
					assert participant != null;
					chat = getOrCreatePrivateChat(user, participant);
				} else {
					// todo serso: we need to create MUC here
				}
			}

			if (chat != null) {
				messagesByChats.put(chat, message);
			}
		}

		for (Chat chat : messagesByChats.keySet()) {
			saveMessages(chat, messagesByChats.get(chat), true);
		}

		return unmodifiableList(messages);
	}

	@Nonnull
	@Override
	public List<Message> syncNewerMessagesForChat(@Nonnull Entity chat) throws AccountException {
		final Account account = getAccountByEntity(chat);
		final AccountChatService accountChatService = account.getAccountChatService();

		final List<Message> messages = accountChatService.getNewerMessagesForChat(chat.getAccountEntityId());

		saveMessages(chat, messages, true);

		return unmodifiableList(messages);

	}

	@Override
	public void saveMessages(@Nonnull Entity chat, @Nonnull Collection<? extends Message> messages) {
		saveMessages(chat, messages, false);
	}

	@Override
	public void saveMessages(@Nonnull Entity chatId, @Nonnull Collection<? extends Message> messages, boolean updateChatSyncDate) {
		Chat chat = this.getChatById(chatId);

		if (chat != null) {
			saveMessages(chat, messages, updateChatSyncDate);
		} else {
			Log.e(this.getClass().getSimpleName(), "No chat found - chat id: " + chatId.getEntityId());
		}
	}

	private void saveMessages(@Nonnull Chat chat, @Nonnull Collection<? extends Message> messages, boolean updateMessagesSyncDate) {
		final MutableAccountChat accountChat = newEmptyAccountChat(chat, getParticipants(chat.getEntity()));

		boolean added = false;
		for (Message message : messages) {
			added |= accountChat.addParticipant(newEmptyUser(message.getAuthor()));
			final Entity recipient = message.getRecipient();
			if (recipient != null) {
				added |= accountChat.addParticipant(newEmptyUser(recipient));
			}
		}

		if (added) {
			saveChat(getAccountByEntity(chat.getEntity()).getUser().getEntity(), accountChat);
		}

		final MergeDaoResult<Message, String> result;
		synchronized (lock) {
			result = getMessageDao().mergeMessages(chat.getId(), messages);

			// update sync data
			if (updateMessagesSyncDate) {
				chat = chat.updateMessagesSyncDate();
				updateChat(chat);
			}
		}

		final List<ChatEvent> events = new ArrayList<ChatEvent>(messages.size());

		events.add(ChatEventType.messages_added.newEvent(chat, result.getAddedObjects()));

		for (Message updatedMessage : result.getUpdatedObjects()) {
			events.add(ChatEventType.message_changed.newEvent(chat, updatedMessage));
		}

		fireEvents(events);
	}

	@Override
	public void markMessageRead(@Nonnull Chat chat, @Nonnull Message message) throws AccountConnectionException {
		if (!message.isRead()) {
			message = message.cloneRead();
		}

		final Account account = getAccountByEntity(message.getEntity());

		if (account.getAccountChatService().markMessageRead(message)) {
			final boolean changed;
			synchronized (lock) {
				changed = messageDao.changeReadStatus(message.getId(), true);
			}

			if (changed) {
				fireEvent(ChatEventType.message_changed.newEvent(chat, message));
				fireEvent(ChatEventType.message_read.newEvent(chat, message));
			}
		}
	}

	@Override
	public void removeMessage(@Nonnull Message message) {
		final Chat chat = getChatById(message.getChat());
		if (chat != null) {
			updateMessageState(chat, message, MessageState.removed);
		}
	}

	@Override
	public void updateMessageState(@Nonnull Message message) {
		final Chat chat = getChatById(message.getChat());
		if (chat != null) {
			updateMessageState(chat, message, message.getState());
		}
	}

	private void updateMessageState(@Nonnull Chat chat, @Nonnull Message message, @Nonnull MessageState newState) {
		message = message.cloneWithNewState(newState);

		final boolean changed;
		synchronized (lock) {
			changed = messageDao.changeMessageState(message.getId(), message.getState());
		}

		if (changed) {
			fireEvent(ChatEventType.message_state_changed.newEvent(chat, message));
		}
	}

	@Nonnull
	@Override
	public List<UiChat> getLastUiChats(@Nonnull User user, @Nullable String query, int count) {
		final List<String> chatIds = chatDao.readLastChatIds(user.getId(), false, isEmpty(query) ? count : Integer.MAX_VALUE);
		return toUiChats(user, query, chatIds);
	}

	@Nonnull
	@Override
	public List<UiChat> getLastUiChats(@Nullable String query, int count) {
		final List<String> chatIds = chatDao.readLastChatIds(null, false, isEmpty(query) ? count : Integer.MAX_VALUE);
		return toUiChats(null, query, chatIds);
	}

	@Nonnull
	@Override
	public List<Chat> getLastChats(boolean privateChat, int count) {
		final List<String> chatIds = chatDao.readLastChatIds(null, privateChat, count);
		return Lists.transform(chatIds, new Function<String, Chat>() {
			@Override
			public Chat apply(String chatId) {
				return getChatById(newEntityFromEntityId(chatId));
			}
		});
	}

	@Nonnull
	private List<UiChat> toUiChats(@Nullable User user, @Nullable String query, @Nonnull List<String> chatIds) {
		final List<UiChat> result = new ArrayList<UiChat>(chatIds.size());

		final PrefixFilter<String> chatFilter = new PrefixFilter<String>(query == null ? "" : query);

		for (String chatId : chatIds) {
			final Chat chat = getChatById(newEntityFromEntityId(chatId));
			if (chat != null) {
				final Message lastMessage = getLastMessage(chat.getEntity());
				if (lastMessage != null) {
					final UiChat uiChat;
					if (user != null) {
						uiChat = loadUiChat(user, chat);
					} else {
						uiChat = loadUiChat(chat);
					}

					if (chatFilter.apply(uiChat.getDisplayName())) {
						result.add(uiChat);
					}
				}
			}
		}

		return result;
	}

	@Override
	public void removeEmptyChats(@Nonnull User user) {
		final List<Chat> chats = userService.getChats(user.getEntity());

		for (Chat chat : chats) {
			final Message lastMessage = getLastMessage(chat.getEntity());
			if (lastMessage == null) {
				chatDao.delete(user, chat);
			}
		}
	}

	@Override
	public void removeChat(@Nonnull Entity chat) {
		chatDao.deleteById(chat.getEntityId());
	}

	@Override
	public void saveDraftMessage(@Nonnull Chat chat, @Nullable String message) {
		if (!Strings.isEmpty(message)) {
			updateChat(chat.cloneWithNewProperty(newProperty(PROPERTY_DRAFT_MESSAGE, message)));
		} else {
			// empty draft message => try to remove it from the chat
			removeDraftMessage(chat);
		}
	}

	@Override
	public void removeDraftMessage(@Nonnull Chat chat) {
		final String draftMessage = chat.getPropertyValueByName(PROPERTY_DRAFT_MESSAGE);
		if (!Strings.isEmpty(draftMessage)) {
			updateChat(chat.cloneWithoutProperty(PROPERTY_DRAFT_MESSAGE));
		}
	}

	@Nullable
	@Override
	public String getDraftMessage(@Nonnull Chat chat) {
		return chat.getPropertyValueByName(PROPERTY_DRAFT_MESSAGE);
	}

	@Nonnull
	private MessageDao getMessageDao() {
		return messageDao;
	}

	@Nonnull
	@Override
	public List<Message> syncOlderMessagesForChat(@Nonnull Entity chat, @Nonnull Entity user) throws AccountException {
		final Integer offset = messageService.getMessages(chat).size();

		final List<Message> messages = getAccountByEntity(user).getAccountChatService().getOlderMessagesForChat(chat.getAccountEntityId(), offset);
		saveMessages(chat, messages);

		return unmodifiableList(messages);
	}

	@Override
	public void syncChat(@Nonnull Entity chat, @Nonnull Entity user) throws AccountException {
		syncNewerMessagesForChat(chat);
	}

	@Nullable
	@Override
	public Entity getSecondUser(@Nonnull Chat chat) {
		boolean first = true;

		if (chat.isPrivate()) {
			for (String userId : Splitter.on(PRIVATE_CHAT_DELIMITER).split(chat.getEntity().getAppAccountEntityId())) {
				if (first) {
					first = false;
				} else {
					return newEntity(chat.getEntity().getAccountId(), userId);
				}
			}
		}

		return null;
	}

	@Override
	public void setChatIcon(@Nonnull Chat chat, @Nonnull ImageView imageView) {
		final Account account = getAccountByEntity(chat.getEntity());

		final List<User> otherParticipants = this.getParticipantsExcept(chat.getEntity(), account.getUser().getEntity());

		if (!otherParticipants.isEmpty()) {
			if (otherParticipants.size() == 1) {
				final User participant = otherParticipants.get(0);
				userService.getIconsService().setUserIcon(participant, imageView);
			} else {
				imageView.setImageDrawable(App.getIconGenerator().getIcon(chat));
			}
		} else {
			// just in case...
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_user_red));
		}
	}

	@Nonnull
	@Override
	public Entity getPrivateChatId(@Nonnull Entity user1, @Nonnull Entity user2) {
		String firstPart = user1.getAccountEntityId();
		if (!user1.isAccountEntityIdSet()) {
			firstPart = user1.getAppAccountEntityId();
		}

		String secondPart = user2.getAccountEntityId();
		if (!user2.isAccountEntityIdSet()) {
			secondPart = user2.getAppAccountEntityId();
		}

		if (firstPart.equals(secondPart)) {
			Log.e(TAG, "Same user in private chat " + Strings.fromStackTrace(Thread.currentThread().getStackTrace()));
		}
		return newEntity(user1.getAccountId(), firstPart + PRIVATE_CHAT_DELIMITER + secondPart);
	}

	@Nullable
	@Override
	public Chat getPrivateChat(@Nonnull Entity user1, @Nonnull final Entity user2) {
		return this.getChatById(getPrivateChatId(user1, user2));
	}

	@Nonnull
	@Override
	public Chat getOrCreatePrivateChat(@Nonnull Entity user1, @Nonnull Entity user2) throws AccountException {
		Chat result = this.getPrivateChat(user1, user2);
		if (result == null) {
			result = this.newPrivateChat(user1, user2);
			cache.put(result);
		}

		return result;
	}

	@Nonnull
	@Override
	public List<User> getParticipants(@Nonnull Entity chat) {
		List<User> participants = this.participants.get(chat);

		if (participants == ThreadSafeMultimap.NO_VALUE) {
			synchronized (lock) {
				participants = chatDao.readParticipants(chat.getEntityId());
			}

			this.participants.put(chat, participants);
		} else {
			participants = toActualUsers(participants);
		}

		return participants;
	}

	@Nonnull
	private List<User> toActualUsers(@Nonnull List<User> users) {
		return newArrayList(transform(users, new Function<User, User>() {
			@Override
			public User apply(User user) {
				return userService.getUserById(user.getEntity());
			}
		}));
	}

	@Nonnull
	@Override
	public List<User> getParticipantsExcept(@Nonnull Entity chat, @Nonnull final Entity user) {
		final List<User> participants = getParticipants(chat);
		return newArrayList(filter(participants, new Predicate<User>() {
			@Override
			public boolean apply(@javax.annotation.Nullable User input) {
				return input != null && !input.getEntity().equals(user);
			}
		}));
	}

	@Nullable
	@Override
	public Message getLastMessage(@Nonnull Entity chat) {
		return lastMessages.getLastMessage(chat);
	}

	/*
	**********************************************************************
	*
	*                           LISTENERS
	*
	**********************************************************************
	*/

	@Override
	public boolean addListener(@Nonnull JEventListener<ChatEvent> listener) {
		return this.listeners.addListener(listener);
	}

	@Override
	public boolean removeListener(@Nonnull JEventListener<ChatEvent> listener) {
		return this.listeners.removeListener(listener);
	}

	@Override
	public void fireEvent(@Nonnull ChatEvent event) {
		this.listeners.fireEvent(event);
	}

	@Override
	public void fireEvents(@Nonnull Collection<ChatEvent> events) {
		this.listeners.fireEvents(events);
	}

	@Override
	public void removeListeners() {
		this.listeners.removeListeners();
	}

	private final class ChatEventListener extends AbstractJEventListener<ChatEvent> {

		private ChatEventListener() {
			super(ChatEvent.class);
		}

		@Override
		public void onEvent(@Nonnull ChatEvent event) {
			cache.onEvent(event);
			participants.onEvent(event);
			lastMessages.onEvent(event);
		}
	}
}
