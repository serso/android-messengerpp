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
import org.solovyev.android.Threads;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MessengerExceptionHandler;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.EntitiesRemovedMapUpdater;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAwareRemovedUpdater;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.realms.*;
import org.solovyev.common.collections.multimap.*;
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
	@Nonnull
	private final ThreadSafeMultimap<Entity, Chat> userChatsCache = ThreadSafeMultimap.newInstance();

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

		userChatsCache.update(EntitiesRemovedMapUpdater.<Chat>newInstance(realmId));
		userContactsCache.update(EntitiesRemovedMapUpdater.<User>newInstance(realmId));

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
			userContactsCache.update(user, new WholeListUpdater<User>(contacts));

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
				case contact_online:
					break;
				case contact_offline:
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

}
