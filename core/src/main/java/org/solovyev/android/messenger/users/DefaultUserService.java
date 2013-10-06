package org.solovyev.android.messenger.users;

import android.app.Application;
import android.util.Log;
import android.widget.ImageView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.Threads;
import org.solovyev.android.messenger.ExceptionHandler;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAwareRemovedUpdater;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.common.collections.multimap.*;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.*;
import java.util.concurrent.Executor;

import static com.google.common.collect.Iterables.find;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.solovyev.android.messenger.users.ContactsDisplayMode.all_contacts;
import static org.solovyev.android.messenger.users.UserEventType.contacts_presence_changed;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 10:30 PM
 */

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
	private Application context;

	@Inject
	@Nonnull
	private ExceptionHandler exceptionHandler;

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

	// key: user entity, value: list of user contacts
	@Nonnull
	private final ThreadSafeMultimap<Entity, User> userContactsCache = ThreadSafeMultimap.newInstance();

	// key: user entity, value: list of user chats
	@Nonnull
	private final ThreadSafeMultimap<Entity, Chat> userChatsCache = ThreadSafeMultimap.newInstance();

	// key: user entity, value: user object
	@GuardedBy("usersCache")
	@Nonnull
	private final Map<Entity, User> usersCache = new HashMap<Entity, User>();

	@Inject
	public DefaultUserService(@Nonnull PersistenceLock lock, @Nonnull Executor eventExecutor) {
		this.listeners = Listeners.newEventListenersBuilderFor(UserEvent.class).withHardReferences().withExecutor(eventExecutor).create();
		this.listeners.addListener(new UserEventListener());
		this.lock = lock;
	}

	@Override
	public void init() {
		chatService.addListener(new ChatEventListener());
	}

	@Nonnull
	@Override
	public User getUserById(@Nonnull Entity user) {
		return getUserById(user, true);
	}

	@Nonnull
	@Override
	public User getUserById(@Nonnull Entity user, boolean tryFindInRealm) {
		boolean saved = true;

		User result;

		synchronized (usersCache) {
			result = usersCache.get(user);
		}

		if (result == null) {

			synchronized (lock) {
				result = userDao.read(user.getEntityId());
			}

			if (result == null) {
				saved = false;
			}

			if (result == null) {
				if (tryFindInRealm) {
					try {
						final Account account = getRealmByEntity(user);
						result = account.getAccountUserService().getUserById(user.getAccountEntityId());
					} catch (AccountException e) {
						// unable to load from realm => just return empty user
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}

			if (result == null) {
				result = Users.newEmptyUser(user);
			} else {
				// user was loaded either from dao or from API => cache
				synchronized (usersCache) {
					usersCache.put(user, result);
				}
			}

			if (!saved) {
				insertUser(result);
			}
		}

		return result;
	}

	@Nonnull
	private Account getRealmByEntity(@Nonnull Entity entity) throws UnsupportedAccountException {
		return accountService.getAccountById(entity.getAccountId());
	}

	private void insertUser(@Nonnull User user) {
		boolean inserted = false;

		synchronized (lock) {
			final User userFromDb = userDao.read(user.getEntity().getEntityId());
			if (userFromDb == null) {
				userDao.create(user);
				inserted = true;
			}
		}

		if (inserted) {
			listeners.fireEvent(UserEventType.added.newEvent(user));
		}
	}

	@Nonnull
	@Override
	public List<Chat> getUserChats(@Nonnull Entity user) {
		List<Chat> result = userChatsCache.get(user);

		if (result == ThreadSafeMultimap.NO_VALUE) {
			synchronized (lock) {
				result = chatService.loadUserChats(user);
			}
			userChatsCache.update(user, new WholeListUpdater<Chat>(result));
		}

		return result;
	}

	@Override
	public void updateUser(@Nonnull User user) {
		updateUser(user, true);
	}

	private void updateUser(@Nonnull User user, boolean fireChangeEvent) {
		synchronized (lock) {
			userDao.update(user);
		}

		synchronized (usersCache) {
			usersCache.put(user.getEntity(), user);
		}
		for (Account account : accountService.getAccounts()) {
			if(account.getUser().equals(user)) {
				account.setUser(user);
			}
		}

		if (fireChangeEvent) {
			listeners.fireEvent(UserEventType.changed.newEvent(user));
		}
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
	public List<User> getUserContacts(@Nonnull Entity user) {
		List<User> result = userContactsCache.get(user);

		if (result == ThreadSafeMultimap.NO_VALUE) {
			synchronized (lock) {
				result = userDao.readUserContacts(user.getEntityId());
			}
			calculateDisplayNames(result);
			userContactsCache.update(user, new WholeListUpdater<User>(result));
		}

		return result;
	}

	@Nonnull
	@Override
	public List<User> getOnlineUserContacts(@Nonnull Entity user) {
		return Lists.newArrayList(Iterables.filter(getUserContacts(user), new Predicate<User>() {
			@Override
			public boolean apply(@javax.annotation.Nullable User contact) {
				return contact != null && contact.isOnline();
			}
		}));
	}

	@Override
	public void onContactPresenceChanged(@Nonnull User user, @Nonnull final User contact, final boolean available) {
		onContactsPresenceChanged(user, Arrays.asList(contact.cloneWithNewStatus(available)));
	}

	private void onContactsPresenceChanged(@Nonnull User user, @Nonnull List<User> contacts) {
		synchronized (lock) {
			for (User contact : contacts) {
				userDao.updateUserOnlineStatus(contact);
			}
		}

		listeners.fireEvent(contacts_presence_changed.newEvent(user, contacts));
	}

	@Nonnull
	@Override
	public List<UiContact> findContacts(@Nonnull User user, @Nullable String query, int count, @Nonnull Collection<UiContact> except) {
		Log.d(TAG, "Find contacts for user: " + user.getLogin() + ", query: " + query);
		final List<UiContact> result = new ArrayList<UiContact>();

		final List<User> contacts = getUserContacts(user.getEntity());
		final ContactFilter filter = new ContactFilter(query, all_contacts);

		for (final User contact : contacts) {
			if (!isExceptedUser(except, contact)) {
				if (filter.apply(contact)) {
					result.add(UiContact.newInstance(contact, getUnreadMessagesCount(contact.getEntity())));
					if (result.size() >= count) {
						break;
					}
				}
			}
		}

		Log.d(TAG, "Find contacts result: " + result.size());

		return result;
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
	public List<UiContact> findContacts(@Nonnull User user, @Nullable String query, int count) {
		return findContacts(user, query, count, Collections.<UiContact>emptyList());
	}

	@Nonnull
	@Override
	public List<UiContact> findContacts(@Nullable String query, int count, @Nonnull Collection<UiContact> except) {
		final List<UiContact> result = new ArrayList<UiContact>();

		final Collection<User> accountUsers = accountService.getEnabledAccountUsers();
		if (accountUsers.size() > 0) {
			final int accountCount = max(count / accountUsers.size(), 1);
			for (User user : accountUsers) {
				result.addAll(findContacts(user, query, accountCount, except));
			}
		}

		return result.subList(0, min(result.size(), count));
	}

	@Nonnull
	@Override
	public List<UiContact> findContacts(@Nullable String query, int count) {
		return findContacts(query, count, Collections.<UiContact>emptyList());
	}

	@Nonnull
	@Override
	public List<UiContact> getLastChatedContacts(int count) {
		final List<UiChat> chats = chatService.getLastChats(count);
		final List<UiContact> result = new ArrayList<UiContact>(chats.size());
		for (UiChat uiChat : chats) {
			final Chat chat = uiChat.getChat();
			if (chat.isPrivate()) {
				final User contact = getUserById(chat.getSecondUser());
				result.add(UiContact.newInstance(contact, getUnreadMessagesCount(contact.getEntity())));
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
	public void syncUser(@Nonnull Entity userEntity) throws AccountException {
		User user = getRealmByEntity(userEntity).getAccountUserService().getUserById(userEntity.getAccountEntityId());
		if (user != null) {
			user = user.updatePropertiesSyncDate();
			updateUser(user, false);
		}
	}

	@Override
	@Nonnull
	public List<User> syncUserContacts(@Nonnull Entity user) throws AccountException {
		final Account account = getRealmByEntity(user);
		final List<User> contacts = account.getAccountUserService().getUserContacts(user.getAccountEntityId());

		if (!contacts.isEmpty()) {
			calculateDisplayNames(contacts);
			userContactsCache.update(user, new WholeListUpdater<User>(contacts));

			mergeUserContacts(user, contacts, false, true);
		} else {
			Log.w(TAG, "User contacts synchronization returned empty list for realm " + account.getId());
		}

		return java.util.Collections.unmodifiableList(contacts);
	}

	private void calculateDisplayNames(@Nonnull List<User> contacts) {
		for (User contact : contacts) {
			// update cached value
			contact.getDisplayName();
		}
	}

	@Override
	public void mergeUserContacts(@Nonnull Entity userEntity, @Nonnull List<User> contacts, boolean allowRemoval, boolean allowUpdate) {
		User user = getUserById(userEntity);
		final MergeDaoResult<User, String> result;
		synchronized (lock) {
			result = userDao.mergeUserContacts(userEntity.getEntityId(), contacts, allowRemoval, allowUpdate);

			// update sync data
			user = user.updateContactsSyncDate();
			updateUser(user, false);
		}

		final List<UserEvent> userEvents = new ArrayList<UserEvent>(contacts.size());

		userEvents.add(UserEventType.contact_added_batch.newEvent(user, result.getAddedObjectLinks()));

		final List<User> addedContacts = result.getAddedObjects();
		for (User addedContact : addedContacts) {
			userEvents.add(UserEventType.added.newEvent(addedContact));
		}
		userEvents.add(UserEventType.contact_added_batch.newEvent(user, addedContacts));


		for (String removedContactId : result.getRemovedObjectIds()) {
			userEvents.add(UserEventType.contact_removed.newEvent(user, removedContactId));
		}

		for (User updatedContact : result.getUpdatedObjects()) {
			userEvents.add(UserEventType.changed.newEvent(updatedContact));
		}

		if(!result.getUpdatedObjects().isEmpty()) {
			userEvents.add(contacts_presence_changed.newEvent(user, result.getUpdatedObjects()));
		}

		listeners.fireEvents(userEvents);
	}

	@Nonnull
	@Override
	public List<Chat> syncUserChats(@Nonnull Entity user) throws AccountException {
		final List<ApiChat> apiChats = getRealmByEntity(user).getAccountChatService().getUserChats(user.getAccountEntityId());

		final List<Chat> chats = Lists.newArrayList(Iterables.transform(apiChats, new Function<ApiChat, Chat>() {
			@Override
			public Chat apply(@javax.annotation.Nullable ApiChat input) {
				assert input != null;
				return input.getChat();
			}
		}));

		mergeUserChats(user, apiChats);

		return java.util.Collections.unmodifiableList(chats);
	}

	@Override
	public void mergeUserChats(@Nonnull Entity userEntity, @Nonnull List<? extends ApiChat> apiChats) throws AccountException {
		User user = this.getUserById(userEntity);

		chatService.mergeUserChats(userEntity, apiChats);

		// update sync data
		user = user.updateChatsSyncDate();
		updateUser(user, false);
	}

	@Override
	public void syncUserContactsStatuses(@Nonnull Entity userEntity) throws AccountException {
		final List<User> contacts = getRealmByEntity(userEntity).getAccountUserService().checkOnlineUsers(getUserContacts(userEntity));

		final User user = getUserById(userEntity);

		onContactsPresenceChanged(user, contacts);
	}

    /*
    **********************************************************************
    *
    *                           USER ICONS
    *
    **********************************************************************
    */

	@Override
	public void fetchUserAndContactsIcons(@Nonnull User user) throws UnsupportedAccountException {
		final RealmIconService realmIconService = getRealmIconServiceByUser(user);

		// fetch self icon
		realmIconService.fetchUsersIcons(Arrays.asList(user));

		// fetch icons for all contacts
		final List<User> contacts = getUserContacts(user.getEntity());
		realmIconService.fetchUsersIcons(contacts);

		// update sync data
		user = user.updateUserIconsSyncDate();
		updateUser(user, false);
	}

	@Nonnull
	private RealmIconService getRealmIconServiceByUser(@Nonnull User user) throws UnsupportedAccountException {
		return getRealmByEntity(user.getEntity()).getRealm().getRealmIconService();
	}

	@Override
	public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
		try {
			getRealmIconServiceByUser(user).setUserIcon(user, imageView);
		} catch (UnsupportedAccountException e) {
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_user_empty));
			exceptionHandler.handleException(e);
		}
	}

	@Override
	public void setUsersIcon(@Nonnull Account account, @Nonnull List<User> users, ImageView imageView) {
		account.getRealm().getRealmIconService().setUsersIcon(users, imageView);
	}

	@Override
	public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		try {
			getRealmIconServiceByUser(user).setUserPhoto(user, imageView);
		} catch (UnsupportedAccountException e) {
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_user_empty));
			exceptionHandler.handleException(e);
		}
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
	public void onUnreadMessagesCountChanged(@Nonnull Entity contactEntity, @Nonnull Integer unreadMessagesCount) {
		final User contact = getUserById(contactEntity);
		this.listeners.fireEvent(UserEventType.unread_messages_count_changed.newEvent(contact, unreadMessagesCount));
	}

	@Override
	public int getUnreadMessagesCount(@Nonnull Entity contact) {
		try {
			if (!Threads.isUiThread()) {
				final Chat chat = chatService.getPrivateChat(getRealmByEntity(contact).getUser().getEntity(), contact);
				if (chat != null) {
					return unreadMessagesCounter.getUnreadMessagesCountForChat(chat.getEntity());
				} else {
					return 0;
				}
			} else {
				return 0;
			}
		} catch (AccountException e) {
			return 0;
		}
	}

	@Override
	public void fireEvents(@Nonnull Collection<UserEvent> userEvents) {
		this.listeners.fireEvents(userEvents);
	}

	private final class UserEventListener extends AbstractJEventListener<UserEvent> {

		private UserEventListener() {
			super(UserEvent.class);
		}

		@Override
		public void onEvent(@Nonnull UserEvent event) {
			final User eventUser = event.getUser();

			switch (event.getType()) {
				case added:
					break;
				case changed:
					// user changed => update it in contacts cache
					userContactsCache.update(new ObjectChangedMapUpdater<User>(eventUser));
					synchronized (usersCache) {
						usersCache.put(eventUser.getEntity(), eventUser);
					}
					break;
				case contact_added:
					// contact added => need to add to list of cached contacts
					final User contact = event.getDataAsUser();
					userContactsCache.update(eventUser.getEntity(), new ObjectAddedUpdater<User>(contact));
					break;
				case contact_added_batch:
					// contacts added => need to add to list of cached contacts
					final List<User> contacts = event.getDataAsUsers();
					userContactsCache.update(eventUser.getEntity(), new ObjectsAddedUpdater<User>(contacts));
					break;
				case contact_removed:
					// contact removed => try to remove from cached contacts
					final String removedContactId = event.getDataAsUserId();
					userContactsCache.update(eventUser.getEntity(), new EntityAwareRemovedUpdater<User>(removedContactId));
					break;
				case chat_added:
					final Chat chat = event.getDataAsChat();
					userChatsCache.update(eventUser.getEntity(), new ObjectAddedUpdater<Chat>(chat));
					break;
				case chat_added_batch:
					final List<Chat> chats = event.getDataAsChats();
					userChatsCache.update(eventUser.getEntity(), new ObjectsAddedUpdater<Chat>(chats));
					break;
				case chat_removed:
					final Chat removedChat = event.getDataAsChat();
					userChatsCache.update(eventUser.getEntity(), new EntityAwareRemovedUpdater<Chat>(removedChat.getId()));
					break;
				case contacts_presence_changed:
					userContactsCache.update(eventUser.getEntity(), new UserListContactStatusUpdater(event.getDataAsUsers()));
					break;
				case unread_messages_count_changed:
					break;
			}
		}

	}

	private final class ChatEventListener extends AbstractJEventListener<ChatEvent> {

		private ChatEventListener() {
			super(ChatEvent.class);
		}

		@Override
		public void onEvent(@Nonnull ChatEvent event) {
			final Chat eventChat = event.getChat();

			switch (event.getType()) {
				case changed:
					userChatsCache.update(new ObjectChangedMapUpdater<Chat>(eventChat));
					break;
			}
		}

	}
    /*
    **********************************************************************
    *
    *                           CACHES
    *
    **********************************************************************
    */


	private static class UserListContactStatusUpdater implements ThreadSafeMultimap.ListUpdater<User> {

		@Nonnull
		private final List<User> contacts;

		public UserListContactStatusUpdater(@Nonnull List<User> contacts) {
			this.contacts = contacts;
		}

		@Nullable
		@Override
		public List<User> update(@Nonnull List<User> values) {
			if (contacts.size() == 1) {
				final User contact = contacts.get(0);

				final int index = Iterables.indexOf(values, new Predicate<User>() {
					@Override
					public boolean apply(@Nullable User user) {
						return contact.equals(user);
					}
				});

				if (index >= 0) {
					// contact found => update status locally (persistence is not updated at status change is too frequent event)
					final List<User> result = ThreadSafeMultimap.copy(values);
					result.set(index, result.get(index).cloneWithNewStatus(contact.isOnline()));
					return result;
				} else {
					return null;
				}
			} else {
				final List<User> result = ThreadSafeMultimap.copy(values);

				for (int i = 0; i < result.size(); i++) {
					final User user = result.get(i);
					final User contact = find(contacts, new Predicate<User>() {
						@Override
						public boolean apply(@Nullable User contact) {
							return user.equals(contact);
						}
					}, null);

					if(contact != null) {
						result.set(i, user.cloneWithNewStatus(contact.isOnline()));
					}
				}

				return result;
			}
		}
	}

}
