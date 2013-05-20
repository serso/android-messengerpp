package org.solovyev.android.messenger.users;

import android.app.Application;
import android.util.Log;
import android.widget.ImageView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.ThreadSafeMultimap;
import org.solovyev.android.Threads;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MessengerExceptionHandler;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.realms.*;
import org.solovyev.common.collections.Collections;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.*;
import java.util.concurrent.ExecutorService;

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
	private RealmService realmService;

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
	private MessengerExceptionHandler exceptionHandler;

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
	@GuardedBy("userChatsCache")
	@Nonnull
	private final Map<Entity, List<Chat>> userChatsCache = new HashMap<Entity, List<Chat>>();

	// key: user entity, value: user object
	@GuardedBy("usersCache")
	@Nonnull
	private final Map<Entity, User> usersCache = new HashMap<Entity, User>();

	@Inject
	public DefaultUserService(@Nonnull PersistenceLock lock, @Nonnull ExecutorService eventExecutor) {
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
				result = userDao.loadUserById(user.getEntityId());
			}

			if (result == null) {
				saved = false;
			}

			if (result == null) {
				if (tryFindInRealm) {
					try {
						final Realm realm = getRealmByEntity(user);
						result = realm.getRealmUserService().getUserById(user.getRealmEntityId());
					} catch (RealmException e) {
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
	private Realm getRealmByEntity(@Nonnull Entity entity) throws UnsupportedRealmException {
		return realmService.getRealmById(entity.getRealmId());
	}

	private void insertUser(@Nonnull User user) {
		boolean inserted = false;

		synchronized (lock) {
			final User userFromDb = userDao.loadUserById(user.getEntity().getEntityId());
			if (userFromDb == null) {
				userDao.insertUser(user);
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
		List<Chat> result;

		synchronized (userChatsCache) {
			result = userChatsCache.get(user);
			if (result == null) {
				result = chatService.loadUserChats(user);
				if (!Collections.isEmpty(result)) {
					userChatsCache.put(user, result);
				}
			}
		}

		// result list might be in cache and might updates due to some user events => must COPY
		return new ArrayList<Chat>(result);
	}

	@Override
	public void updateUser(@Nonnull User user) {
		updateUser(user, true);
	}

	private void updateUser(@Nonnull User user, boolean fireChangeEvent) {
		synchronized (lock) {
			userDao.updateUser(user);
		}

		if (fireChangeEvent) {
			listeners.fireEvent(UserEventType.changed.newEvent(user));
		}
	}

	@Override
	public void removeUsersInRealm(@Nonnull final String realmId) {
		synchronized (lock) {
			this.userDao.deleteAllUsersInRealm(realmId);
		}

		synchronized (userChatsCache) {
			Iterators.removeIf(userChatsCache.entrySet().iterator(), EntityMapEntryMatcher.forRealm(realmId));
		}

		userContactsCache.update(new UsersRemovedMapUpdater(realmId));

		synchronized (usersCache) {
			Iterators.removeIf(usersCache.entrySet().iterator(), EntityMapEntryMatcher.forRealm(realmId));
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
				result = userDao.loadUserContacts(user.getEntityId());
			}
			if (!Collections.isEmpty(result)) {
				userContactsCache.update(user, new UserListWholeListUpdater(result));
			}
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
		final UserEventType userEventType = available ? UserEventType.contact_online : UserEventType.contact_offline;

		userContactsCache.update(user.getEntity(), new UserListContactStatusUpdater(contact, available));

		this.listeners.fireEvent(userEventType.newEvent(user, contact));
	}

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

	@Override
	public void syncUser(@Nonnull Entity userEntity) throws RealmException {
		User user = getRealmByEntity(userEntity).getRealmUserService().getUserById(userEntity.getRealmEntityId());
		if (user != null) {
			user = user.updatePropertiesSyncDate();
			updateUser(user, true);
		}
	}

	@Override
	@Nonnull
	public List<User> syncUserContacts(@Nonnull Entity user) throws RealmException {
		final Realm realm = getRealmByEntity(user);
		final List<User> contacts = realm.getRealmUserService().getUserContacts(user.getRealmEntityId());

		if (!contacts.isEmpty()) {
			userContactsCache.update(user, new UserListWholeListUpdater(contacts));

			mergeUserContacts(user, contacts, false, true);
		} else {
			Log.w(TAG, "User contacts synchronization returned empty list for realm " + realm.getId());
		}

		return java.util.Collections.unmodifiableList(contacts);
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
			final UserEventType type = updatedContact.isOnline() ? UserEventType.contact_online : UserEventType.contact_offline;
			userEvents.add(type.newEvent(user, updatedContact));
		}

		listeners.fireEvents(userEvents);
	}

	@Nonnull
	@Override
	public List<Chat> syncUserChats(@Nonnull Entity user) throws RealmException {
		final List<ApiChat> apiChats = getRealmByEntity(user).getRealmChatService().getUserChats(user.getRealmEntityId());

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
	public void mergeUserChats(@Nonnull Entity userEntity, @Nonnull List<? extends ApiChat> apiChats) throws RealmException {
		User user = this.getUserById(userEntity);

		final MergeDaoResult<ApiChat, String> result;
		synchronized (lock) {
			result = chatService.mergeUserChats(userEntity, apiChats);

			// update sync data
			user = user.updateChatsSyncDate();
			updateUser(user, false);
		}

		final List<UserEvent> userEvents = new ArrayList<UserEvent>(apiChats.size());
		final List<ChatEvent> chatEvents = new ArrayList<ChatEvent>(apiChats.size());

		final List<Chat> addedChatLinks = Lists.transform(result.getAddedObjectLinks(), new Function<ApiChat, Chat>() {
			@Override
			public Chat apply(@javax.annotation.Nullable ApiChat apiChat) {
				assert apiChat != null;
				return apiChat.getChat();
			}
		});

		if (!addedChatLinks.isEmpty()) {
			userEvents.add(UserEventType.chat_added_batch.newEvent(user, addedChatLinks));
		}

		final List<Chat> addedChats = Lists.transform(result.getAddedObjects(), new Function<ApiChat, Chat>() {
			@Override
			public Chat apply(@javax.annotation.Nullable ApiChat apiChat) {
				assert apiChat != null;
				return apiChat.getChat();
			}
		});

		for (Chat addedChat : addedChats) {
			chatEvents.add(ChatEventType.added.newEvent(addedChat));
		}
		if (!addedChats.isEmpty()) {
			userEvents.add(UserEventType.chat_added_batch.newEvent(user, addedChats));
		}

		for (String removedChatId : result.getRemovedObjectIds()) {
			userEvents.add(UserEventType.chat_removed.newEvent(user, removedChatId));
		}

		for (ApiChat updatedChat : result.getUpdatedObjects()) {
			chatEvents.add(ChatEventType.changed.newEvent(updatedChat.getChat()));
		}

		listeners.fireEvents(userEvents);
		chatService.fireEvents(chatEvents);
	}

	@Override
	public void syncUserContactsStatuses(@Nonnull Entity userEntity) throws RealmException {
		final List<User> contacts = getRealmByEntity(userEntity).getRealmUserService().checkOnlineUsers(getUserContacts(userEntity));

		final User user = getUserById(userEntity);

		final List<UserEvent> userEvents = new ArrayList<UserEvent>(contacts.size());

		for (User contact : contacts) {
			final UserEventType type = contact.isOnline() ? UserEventType.contact_online : UserEventType.contact_offline;
			userEvents.add(type.newEvent(user, contact));
		}

		listeners.fireEvents(userEvents);
	}

    /*
    **********************************************************************
    *
    *                           USER ICONS
    *
    **********************************************************************
    */

	@Override
	public void fetchUserAndContactsIcons(@Nonnull User user) throws UnsupportedRealmException {
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
	private RealmIconService getRealmIconServiceByUser(@Nonnull User user) throws UnsupportedRealmException {
		return getRealmByEntity(user.getEntity()).getRealmDef().getRealmIconService();
	}

	@Override
	public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
		try {
			getRealmIconServiceByUser(user).setUserIcon(user, imageView);
		} catch (UnsupportedRealmException e) {
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_user_empty));
			exceptionHandler.handleException(e);
		}
	}

	@Override
	public void setUsersIcon(@Nonnull Realm realm, @Nonnull List<User> users, ImageView imageView) {
		realm.getRealmDef().getRealmIconService().setUsersIcon(users, imageView);
	}

	@Override
	public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		try {
			getRealmIconServiceByUser(user).setUserPhoto(user, imageView);
		} catch (UnsupportedRealmException e) {
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
				return unreadMessagesCounter.getUnreadMessagesCountForChat(chat.getEntity());
			} else {
				return 0;
			}
		} catch (RealmException e) {
			return 0;
		}
	}

	private final class UserEventListener extends AbstractJEventListener<UserEvent> {

		private UserEventListener() {
			super(UserEvent.class);
		}

		@Override
		public void onEvent(@Nonnull UserEvent event) {
			final UserEventType type = event.getType();
			final User eventUser = event.getUser();


			if (type == UserEventType.changed) {
				// user changed => update it in contacts cache
				userContactsCache.update(new UserChangedMapUpdater(eventUser));
			}

			if (type == UserEventType.contact_added) {
				// contact added => need to add to list of cached contacts
				final User contact = event.getDataAsUser();
				userContactsCache.update(eventUser.getEntity(), new UserListAddedContactUpdater(contact));
			}

			if (type == UserEventType.contact_added_batch) {
				// contacts added => need to add to list of cached contacts
				final List<User> contacts = event.getDataAsUsers();
				for (final User contact : contacts) {
					userContactsCache.update(eventUser.getEntity(), new UserListAddedContactUpdater(contact));
				}
			}

			if (type == UserEventType.contact_removed) {
				// contact removed => try to remove from cached contacts
				final String removedContactId = event.getDataAsUserId();
				userContactsCache.update(eventUser.getEntity(), new UserListRemovedContactUpdater(removedContactId));
			}

			synchronized (userChatsCache) {
				if (type == UserEventType.chat_added) {
					final Chat chat = event.getDataAsChat();
					final List<Chat> chats = userChatsCache.get(eventUser.getEntity());
					if (chats != null) {
						if (!Iterables.contains(chats, chat)) {
							chats.add(chat);
						}
					}
				}

				if (type == UserEventType.chat_added_batch) {
					final List<Chat> chats = event.getDataAsChats();
					final List<Chat> chatsFromCache = userChatsCache.get(eventUser.getEntity());
					if (chatsFromCache != null) {
						for (Chat chat : chats) {
							if (!Iterables.contains(chatsFromCache, chat)) {
								chatsFromCache.add(chat);
							}
						}
					}
				}

				if (type == UserEventType.chat_removed) {
					final Chat chat = event.getDataAsChat();
					final List<Chat> chats = userChatsCache.get(eventUser.getEntity());
					if (chats != null) {
						chats.remove(chat);
					}
				}
			}

			synchronized (usersCache) {
				if (type == UserEventType.changed) {
					usersCache.put(eventUser.getEntity(), eventUser);
				}
			}
		}

	}

	private final class ChatEventListener extends AbstractJEventListener<ChatEvent> {

		private ChatEventListener() {
			super(ChatEvent.class);
		}

		@Override
		public void onEvent(@Nonnull ChatEvent event) {
			synchronized (userChatsCache) {

				final Chat eventChat = event.getChat();
				if (event.getType() == ChatEventType.changed) {
					for (List<Chat> chats : userChatsCache.values()) {
						for (int i = 0; i < chats.size(); i++) {
							final Chat chat = chats.get(i);
							if (chat.equals(eventChat)) {
								chats.set(i, eventChat);
							}
						}
					}
				}

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


	private static class UserListWholeListUpdater implements ThreadSafeMultimap.ListUpdater<User> {

		@Nonnull
		private final List<User> contacts;

		public UserListWholeListUpdater(@Nonnull List<User> contacts) {
			this.contacts = contacts;
		}

		@Nonnull
		@Override
		public List<User> update(@Nonnull List<User> values) {
			return contacts;
		}
	}

	private static class UserListAddedContactUpdater implements ThreadSafeMultimap.ListUpdater<User> {

		@Nonnull
		private final User contact;

		public UserListAddedContactUpdater(@Nonnull User contact) {
			this.contact = contact;
		}

		@Nullable
		@Override
		public List<User> update(@Nonnull List<User> values) {
			if (values == ThreadSafeMultimap.NO_VALUE) {
				return null;
			} else if (!Iterables.contains(values, contact)) {
				final List<User> result = ThreadSafeMultimap.copy(values);
				result.add(contact);
				return result;
			} else {
				return null;
			}
		}
	}

	private static class UserListRemovedContactUpdater implements ThreadSafeMultimap.ListUpdater<User> {

		@Nonnull
		private final String removedContactId;

		public UserListRemovedContactUpdater(@Nonnull String removedContactId) {
			this.removedContactId = removedContactId;
		}

		@Nullable
		@Override
		public List<User> update(@Nonnull List<User> values) {
			if (values == ThreadSafeMultimap.NO_VALUE) {
				return null;
			} else {
				final List<User> result = ThreadSafeMultimap.copy(values);
				Iterables.removeIf(result, new Predicate<User>() {
					@Override
					public boolean apply(@Nullable User contact) {
						return contact != null && contact.getEntity().getEntityId().equals(removedContactId);
					}
				});
				return result;
			}
		}
	}

	private static class UserListContactStatusUpdater implements ThreadSafeMultimap.ListUpdater<User> {

		@Nonnull
		private final User contact;

		private final boolean available;

		public UserListContactStatusUpdater(@Nonnull User contact, boolean available) {
			this.contact = contact;
			this.available = available;
		}

		@Nullable
		@Override
		public List<User> update(@Nonnull List<User> values) {
			final int index = Iterables.indexOf(values, new Predicate<User>() {
				@Override
				public boolean apply(@Nullable User user) {
					return contact.equals(user);
				}
			});

			if (index >= 0) {
				// contact found => update status locally (persistence is not updated at status change is too frequent event)
				final List<User> result = ThreadSafeMultimap.copy(values);
				result.set(index, result.get(index).cloneWithNewStatus(available));
				return result;
			} else {
				return null;
			}
		}
	}

	private static class UserChangedMapUpdater implements ThreadSafeMultimap.MapUpdater<Entity, User> {

		@Nonnull
		private final User user;

		public UserChangedMapUpdater(@Nonnull User user) {
			this.user = user;
		}

		@Nullable
		@Override
		public Map<Entity, List<User>> update(@Nonnull Map<Entity, List<User>> map) {
			final Map<Entity, List<User>> result = ThreadSafeMultimap.copy(map);

			for (List<User> contacts : result.values()) {
				for (int i = 0; i < contacts.size(); i++) {
					final User contact = contacts.get(i);
					if (contact.equals(user)) {
						contacts.set(i, user);
					}
				}
			}

			return result;
		}
	}

	private static class UsersRemovedMapUpdater implements ThreadSafeMultimap.MapUpdater<Entity, User> {

		@Nonnull
		private final String realmId;

		public UsersRemovedMapUpdater(@Nonnull String realmId) {
			this.realmId = realmId;
		}

		@Nonnull
		@Override
		public Map<Entity, List<User>> update(@Nonnull Map<Entity, List<User>> map) {
			final Map<Entity, List<User>> result = new HashMap<Entity, List<User>>(map.size());

			for (Map.Entry<Entity, List<User>> entry : map.entrySet()) {
				if (!entry.getKey().getRealmId().equals(realmId)) {
					result.put(entry.getKey(), entry.getValue());
				}
			}

			return result;
		}
	}
}
