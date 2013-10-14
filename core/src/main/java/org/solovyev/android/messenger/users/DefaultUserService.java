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
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
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
import static java.util.Arrays.asList;
import static org.solovyev.android.messenger.users.ContactsDisplayMode.all_contacts;
import static org.solovyev.android.messenger.users.UiContact.loadUiContact;
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

	@Nonnull
	private final UserContacts userContacts = new UserContacts();

	@Nonnull
	private final UserChats userChats = new UserChats();

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
		userChats.init();
	}

	@Nonnull
	@Override
	public User getUserById(@Nonnull Entity user) {
		return getUserById(user, true);
	}

	@Nonnull
	@Override
	public User getUserById(@Nonnull Entity user, boolean tryFindInAccount) {
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
				if (tryFindInAccount) {
					try {
						final Account account = getAccountByEntity(user);
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
				saveUser(result);
			}
		}

		return result;
	}

	@Nonnull
	private Account getAccountByEntity(@Nonnull Entity entity) throws UnsupportedAccountException {
		return accountService.getAccountById(entity.getAccountId());
	}

	@Nullable
	private Account getAccountByEntityOrNull(@Nonnull Entity entity){
		return accountService.getAccountByIdOrNull(entity.getAccountId());
	}

	@Override
	public void saveUser(@Nonnull User user) {
		boolean saved = false;

		synchronized (lock) {
			final User userFromDb = userDao.read(user.getEntity().getEntityId());
			if (userFromDb == null) {
				userDao.create(user);
				saved = true;
			}
		}

		if (saved) {
			listeners.fireEvent(UserEventType.added.newEvent(user));
		}
	}

	@Nonnull
	@Override
	public List<Chat> getUserChats(@Nonnull Entity user) {
		List<Chat> result = userChats.getChats(user);

		if (result == ThreadSafeMultimap.NO_VALUE) {
			synchronized (lock) {
				result = chatService.loadUserChats(user);
			}
			userChats.updateChats(user, result);
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

		onUserChanged(user);

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
		List<User> result = userContacts.getContacts(user);

		if (result == ThreadSafeMultimap.NO_VALUE) {
			synchronized (lock) {
				result = userDao.readContacts(user.getEntityId());
			}
			calculateDisplayNames(result);
			userContacts.update(user, result);
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
		onContactsPresenceChanged(user, asList(contact.cloneWithNewStatus(available)));
	}

	private void onContactsPresenceChanged(@Nonnull User user, @Nonnull List<User> contacts) {
		synchronized (lock) {
			for (User contact : contacts) {
				userDao.updateOnlineStatus(contact);
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

		final Account account = accountService.getAccountByEntityOrNull(user.getEntity());

		for (final User contact : contacts) {
			if (!isExceptedUser(except, contact)) {
				if (filter.apply(contact)) {
					result.add(loadUiContact(contact, account));
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
		final int accountsSize = accountUsers.size();
		if (accountsSize > 0) {
			final int accountCount = max(2 * except.size() / accountsSize + count / accountsSize, 1);
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
				result.add(loadUiContact(contact));
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
		User user = getAccountByEntity(userEntity).getAccountUserService().getUserById(userEntity.getAccountEntityId());
		if (user != null) {
			user = user.updatePropertiesSyncDate();
			updateUser(user, false);
		}
	}

	@Override
	@Nonnull
	public List<User> syncUserContacts(@Nonnull Entity user) throws AccountException {
		final Account account = getAccountByEntity(user);
		final List<User> contacts = account.getAccountUserService().getUserContacts(user.getAccountEntityId());

		if (!contacts.isEmpty()) {
			calculateDisplayNames(contacts);
			userContacts.update(user, contacts);

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
			result = userDao.mergeLinkedEntities(userEntity.getEntityId(), contacts, allowRemoval, allowUpdate);

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
			onUserChanged(updatedContact);
			userEvents.add(UserEventType.changed.newEvent(updatedContact));
		}

		if(!result.getUpdatedObjects().isEmpty()) {
			userEvents.add(contacts_presence_changed.newEvent(user, result.getUpdatedObjects()));
		}

		listeners.fireEvents(userEvents);
	}

	private void onUserChanged(@Nonnull User user) {
		// user changed => update it in contacts cache
		userContacts.onContactChanged(user);
		synchronized (usersCache) {
			usersCache.put(user.getEntity(), user);
		}
	}

	@Nonnull
	@Override
	public List<Chat> syncUserChats(@Nonnull Entity user) throws AccountException {
		final List<ApiChat> apiChats = getAccountByEntity(user).getAccountChatService().getChats(user.getAccountEntityId());

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
		final List<User> contacts = getAccountByEntity(userEntity).getAccountUserService().checkOnlineUsers(getUserContacts(userEntity));

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
		realmIconService.fetchUsersIcons(asList(user));

		// fetch icons for all contacts
		final List<User> contacts = getUserContacts(user.getEntity());
		realmIconService.fetchUsersIcons(contacts);

		// update sync data
		user = user.updateUserIconsSyncDate();
		updateUser(user, false);
	}

	@Nonnull
	private RealmIconService getRealmIconServiceByUser(@Nonnull User user) throws UnsupportedAccountException {
		return getAccountByEntity(user.getEntity()).getRealm().getRealmIconService();
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
				final Chat chat = chatService.getPrivateChat(getAccountByEntity(contact).getUser().getEntity(), contact);
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
			userChats.onEvent(event);
			userContacts.onEvent(event);
		}
	}
}
