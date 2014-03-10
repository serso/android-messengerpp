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

package org.solovyev.android.messenger.users;

import android.util.Log;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.EntityAwareByIdFinder;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.common.collections.multimap.ThreadSafeMultimap;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Executor;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.solovyev.android.Threads.isUiThread;
import static org.solovyev.android.messenger.users.ContactsDisplayMode.all_contacts;
import static org.solovyev.android.messenger.users.UiContact.loadRecentUiContact;
import static org.solovyev.android.messenger.users.UiContact.loadUiContact;
import static org.solovyev.android.messenger.users.UserEventType.*;
import static org.solovyev.android.messenger.users.UserService.ContactsSearchStrategy.alphabetically;
import static org.solovyev.android.messenger.users.UserService.ContactsSearchStrategy.evenly_between_accounts;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.common.text.Strings.fromStackTrace;

@Singleton
public class DefaultUserService implements UserService {

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

	@Inject
	@Nonnull
	private ChatService chatService;

	@Inject
	@Nonnull
	private UserDao userDao;

	@Inject
	@Nonnull
	private UnreadMessagesCounter unreadMessagesCounter;

    /*
	**********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	/**
	 * Lock for all operations with persistence state. Should guarantee that all operations done over DAOs are thread safe and not corrupt data.
	 */
	@Nonnull
	private final Object lock;

	@Nonnull
	private final JEventListeners<JEventListener<? extends UserEvent>, UserEvent> listeners;

	@Nonnull
	private final UserContacts contacts = new UserContacts();

	@Nonnull
	private final UserChats chats = new UserChats();

	@Nonnull
	private final UserCache cache = new UserCache();

	@Nonnull
	private UserIconsService iconsService;

	@Inject
	public DefaultUserService(@Nonnull PersistenceLock lock, @Nonnull Executor eventExecutor) {
		this.listeners = Listeners.newEventListenersBuilderFor(UserEvent.class).withHardReferences().withExecutor(eventExecutor).create();
		this.listeners.addListener(new UserEventListener());
		this.lock = lock;
	}

	@Override
	public void init() {
		chats.init();
		iconsService = new DefaultUserIconsService(this, accountService);
	}

	@Override
	@Nonnull
	public UserIconsService getIconsService() {
		return iconsService;
	}

	@Nonnull
	@Override
	public User getUserById(@Nonnull Entity user) {
		return getUserById(user, false);
	}

	@Nonnull
	@Override
	public User getUserById(@Nonnull Entity user, boolean tryFindInAccount) {
		return getUserById(user, tryFindInAccount, true);
	}

	@Nonnull
	@Override
	public User getUserById(@Nonnull Entity user, boolean tryFindInAccount, boolean createFakeUser) {
		boolean saved = true;

		User result = cache.get(user);

		if (result == null) {

			synchronized (lock) {
				result = userDao.read(user.getEntityId());
			}

			if (result == null) {
				saved = false;
			}

			if (result == null) {
				if (tryFindInAccount) {
					if (!isUiThread()) {
						try {
							final Account account = getAccountByEntity(user);
							result = account.getAccountUserService().getUserById(user.getAccountEntityId());
						} catch (AccountException e) {
							// unable to load from realm => just return empty user
							Log.e(TAG, e.getMessage(), e);
						}
					} else {
						final Exception e = new Exception();
						Log.e(TAG, "Trying to load users on UI thread " + fromStackTrace(e.getStackTrace()));
					}
				}
			}

			if (result == null) {
				if (createFakeUser) {
					result = newEmptyUser(user);
				} else {
					throw new NoSuchElementException("User with id: " + user.getEntityId() + " doesn't exist");
				}
			} else {
				// user was loaded either from dao or from API => cache
				cache.put(result);
			}

			if (!saved) {
				saveUser(result);
			}
		}

		return result;
	}

	@Nonnull
	private Account getAccountByEntity(@Nonnull Entity entity) throws UnsupportedAccountException {
		return accountService.getAccountById(entity.getAccountId());
	}

	@Override
	public void saveAccountUser(@Nonnull User user) {
		saveUser(user, null);
	}

	@Override
	public void saveUser(@Nonnull User user) {
		final Account account = getAccountByEntity(user.getEntity());
		if (!account.getUser().equals(user)) {
			saveUser(user, account);
		} else {
			saveAccountUser(user);
		}
	}

	private void saveUser(@Nonnull User newUser, @Nullable Account account) {
		synchronized (lock) {
			final User userFromDb = userDao.read(newUser.getEntity().getEntityId());
			if (userFromDb == null) {
				if (account != null) {
					final User user = account.getUser();
					userDao.createContact(user.getId(), newUser);
					listeners.fireEvent(contacts_added.newEvent(user, asList(newUser)));
				} else {
					userDao.create(newUser);
					listeners.fireEvent(added.newEvent(newUser));
				}
			} else {
				userDao.update(newUser);
				listeners.fireEvent(changed.newEvent(newUser));
			}
		}
	}

	@Override
	public void removeUser(@Nonnull User user) {
		final Account account = accountService.getAccountByEntity(user.getEntity());
		final User accountUser = account.getUser();
		if (!accountUser.equals(user)) {
			synchronized (lock) {
				userDao.delete(user);
			}
			listeners.fireEvent(contact_removed.newEvent(accountUser, user.getId()));

			final Entity chat = chatService.getPrivateChatId(accountUser.getEntity(), user.getEntity());
			chatService.removeChat(chat);
			listeners.fireEvent(chat_removed.newEvent(accountUser, chat.getEntityId()));
		}
	}

	@Nonnull
	@Override
	public List<Chat> getChats(@Nonnull Entity user) {
		List<Chat> result = chats.getChats(user);

		if (result == ThreadSafeMultimap.NO_VALUE) {
			synchronized (lock) {
				result = chatService.loadChats(user);
			}
			chats.update(user, result);
		}

		return result;
	}

	@Override
	public void updateUser(@Nonnull User user) {
		synchronized (lock) {
			userDao.update(user);
		}

		listeners.fireEvent(changed.newEvent(user));
	}

	/*
	**********************************************************************
    *
    *                           CONTACTS
    *
    **********************************************************************
    */

	@Nonnull
	@Override
	public List<User> getContacts(@Nonnull Entity user) {
		List<User> result = contacts.getContacts(user);

		if (result == ThreadSafeMultimap.NO_VALUE) {
			synchronized (lock) {
				result = userDao.readContacts(user.getEntityId());
			}
			contacts.update(user, result);
		}

		return result;
	}

	@Nonnull
	@Override
	public List<User> getOnlineContacts(@Nonnull Entity user) {
		return newArrayList(filter(getContacts(user), new Predicate<User>() {
			@Override
			public boolean apply(@javax.annotation.Nullable User contact) {
				return contact != null && contact.isOnline();
			}
		}));
	}

	@Override
	public void onContactPresenceChanged(@Nonnull User user, @Nonnull final User contact, final boolean available) {
		final User newContact = contact.cloneWithNewStatus(available);
		synchronized (lock) {
			userDao.updateOnlineStatus(newContact);
		}

		listeners.fireEvent(contacts_presence_changed.newEvent(user, asList(newContact)));
	}

	@Nonnull
	@Override
	public List<UiContact> findContacts(@Nonnull User user, @Nullable String query, int count, @Nonnull ContactsSearchStrategy strategy, @Nonnull Collection<UiContact> except) {
		Log.d(TAG, "Find contacts for user: " + user.getLogin() + ", query: " + query);
		final List<UiContact> result = new ArrayList<UiContact>(count);

		final List<User> contacts = getContacts(user.getEntity());
		final ContactFilter filter = new ContactFilter(query, all_contacts);

		final Account account = accountService.getAccountByEntity(user.getEntity());

		for (final User contact : contacts) {
			if (!isExceptedUser(except, contact)) {
				if (filter.apply(contact)) {
					result.add(loadUiContact(contact, account));
					if (strategy == evenly_between_accounts) {
						if (result.size() >= count) {
							break;
						}
					}
				}
			}
		}

		if (strategy == evenly_between_accounts) {
			Log.d(TAG, "Found contacts count: " + result.size());

			return result;
		} else if (strategy == alphabetically) {
			Collections.sort(result, UiContact.getComparator());
			return result.subList(0, min(result.size(), count));
		} else {
			throw new UnsupportedOperationException(strategy + " is not supported");
		}
	}

	private boolean isExceptedUser(@Nonnull Collection<UiContact> exceptions, @Nonnull final User contact) {
		return find(exceptions, new Predicate<UiContact>() {
			@Override
			public boolean apply(@Nullable UiContact uiContact) {
				return uiContact != null && uiContact.getContact().equals(contact);
			}
		}, null) != null;
	}

	@Nonnull
	@Override
	public List<UiContact> findContacts(@Nullable String query, int count, @Nonnull ContactsSearchStrategy strategy, @Nonnull Collection<UiContact> except) {
		final List<UiContact> result = new ArrayList<UiContact>(count);

		final Collection<User> accountUsers = accountService.getEnabledAccountUsers();
		final int accountsSize = accountUsers.size();
		if (accountsSize > 0) {
			final int accountCount;
			switch (strategy) {
				case alphabetically:
					accountCount = count;
					break;
				case evenly_between_accounts:
					accountCount = max(2 * except.size() / accountsSize + count / accountsSize, 1);
					break;
				default:
					accountCount = 0;
			}

			for (User user : accountUsers) {
				result.addAll(findContacts(user, query, accountCount, strategy, except));
			}
		}

		switch (strategy) {
			case alphabetically:
				Collections.sort(result, UiContact.getComparator());
				break;
		}

		return result.subList(0, min(result.size(), count));
	}

	@Nonnull
	@Override
	public List<UiContact> findContacts(@Nullable String query, int count, @Nonnull ContactsSearchStrategy strategy) {
		return findContacts(query, count, strategy, Collections.<UiContact>emptyList());
	}

	@Nonnull
	@Override
	public List<UiContact> getLastChatedContacts(int count) {
		final List<Chat> chats = chatService.getLastChats(true, count);
		final List<UiContact> result = new ArrayList<UiContact>(chats.size());

		boolean lastMessageExists = true;
		for (Chat chat : chats) {
			if (chat.isPrivate()) {
				final User contact = getUserById(chat.getSecondUser());

				final Message lastMessage;
				if (lastMessageExists) {
					lastMessage = chatService.getLastMessage(chat.getEntity());
					lastMessageExists = lastMessage != null;
				} else {
					// as chats are sorted according to the date of the last message there is no point of
					// checking last message of a chat which has no messages at all
					lastMessage = null;
				}
				result.add(loadRecentUiContact(contact, lastMessage != null ? lastMessage.getSendDate() : null));
			}
		}
		return result;
	}

	/*
	**********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

	@Override
	@Nonnull
	public List<User> syncContacts(@Nonnull Account<?> account) throws AccountException {
		final List<User> contacts = account.getAccountUserService().getContacts();

		if (!contacts.isEmpty()) {
			mergeContacts(account, contacts, false, true);
		} else {
			Log.w(TAG, "User contacts synchronization returned empty list for realm " + account.getId());
		}

		return unmodifiableList(contacts);
	}

	@Override
	public void mergeContacts(@Nonnull Account account, @Nonnull List<User> contacts, boolean allowRemoval, boolean allowUpdate) {
		final User user = account.getUser();

		final MergeDaoResult<User, String> result;
		synchronized (lock) {
			result = userDao.mergeLinkedEntities(user.getId(), contacts, allowRemoval, allowUpdate);
		}

		// update sync data
		accountService.saveAccountSyncData(account.updateContactsSyncDate());

		final List<UserEvent> userEvents = new ArrayList<UserEvent>(contacts.size());

		final List<User> addedContacts = result.getAddedObjects();
		for (User addedContact : addedContacts) {
			userEvents.add(UserEventType.added.newEvent(addedContact));
		}

		if (!addedContacts.isEmpty()) {
			userEvents.add(UserEventType.contacts_added.newEvent(user, addedContacts));
		}


		for (String removedContactId : result.getRemovedObjectIds()) {
			userEvents.add(UserEventType.contact_removed.newEvent(user, removedContactId));
		}

		if (!result.getUpdatedObjects().isEmpty()) {
			userEvents.add(contacts_changed.newEvent(user, result.getUpdatedObjects()));
			userEvents.add(contacts_presence_changed.newEvent(user, result.getUpdatedObjects()));
		}

		listeners.fireEvents(userEvents);
	}

	@Nonnull
	@Override
	public List<Chat> syncChats(@Nonnull Account account) throws AccountException {
		final List<AccountChat> accountChats = account.getAccountChatService().getChats();

		final List<Chat> chats = newArrayList(transform(accountChats, new Function<AccountChat, Chat>() {
			@Override
			public Chat apply(AccountChat accountChat) {
				return accountChat.getChat();
			}
		}));

		mergeChats(account, accountChats);

		return unmodifiableList(chats);
	}

	@Override
	public void mergeChats(@Nonnull Account account, @Nonnull List<? extends AccountChat> apiChats) throws AccountException {
		chatService.mergeChats(account.getUser().getEntity(), apiChats);

		// update sync data
		accountService.saveAccountSyncData(account.updateChatsSyncDate());
	}

	@Override
	public void syncContactStatuses(@Nonnull Account account) throws AccountException {
		final List<User> contacts = account.getAccountUserService().getOnlineUsers();

		final User user = account.getUser();

		final List<User> offlineContacts = new ArrayList<User>();

		synchronized (lock) {
			for (User contact : contacts) {
				userDao.updateOnlineStatus(contact);
			}

			final List<User> oldContacts = getOnlineContacts(user.getEntity());
			for (User oldContact : oldContacts) {
				if (!any(contacts, new EntityAwareByIdFinder(oldContact.getId()))) {
					// contact was online, but now is not => update database
					final User offlineContact = oldContact.cloneWithNewStatus(false);
					userDao.updateOnlineStatus(offlineContact);
					offlineContacts.add(offlineContact);
				}
			}
		}

		listeners.fireEvent(contacts_presence_changed.newEvent(user, contacts));
		listeners.fireEvent(contacts_presence_changed.newEvent(user, offlineContacts));
	}

    /*
	**********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

	@Override
	public boolean addListener(@Nonnull JEventListener<UserEvent> listener) {
		return this.listeners.addListener(listener);
	}

	@Override
	public boolean removeListener(@Nonnull JEventListener<UserEvent> listener) {
		return this.listeners.removeListener(listener);
	}

	@Override
	public void fireEvents(@Nonnull Collection<UserEvent> userEvents) {
		this.listeners.fireEvents(userEvents);
	}

	/*
	**********************************************************************
	*
	*                           UNREAD MESSAGES
	*
	**********************************************************************
	*/

	@Override
	public void onUnreadMessagesCountChanged(@Nonnull Entity contactEntity, @Nonnull Integer unreadMessagesCount) {
		final User contact = getUserById(contactEntity);
		this.listeners.fireEvent(UserEventType.unread_messages_count_changed.newEvent(contact, unreadMessagesCount));
	}

	@Override
	public int getUnreadMessagesCount(@Nonnull Entity contact) {
		if (!isUiThread()) {
			final Chat chat = chatService.getPrivateChat(getAccountByEntity(contact).getUser().getEntity(), contact);
			if (chat != null) {
				return unreadMessagesCounter.getUnreadMessagesCountForChat(chat.getEntity());
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER
	*
	**********************************************************************
	*/

	private final class UserEventListener extends AbstractJEventListener<UserEvent> {

		private UserEventListener() {
			super(UserEvent.class);
		}

		@Override
		public void onEvent(@Nonnull UserEvent event) {
			cache.onEvent(event);
			contacts.onEvent(event);
			chats.onEvent(event);
		}
	}
}
