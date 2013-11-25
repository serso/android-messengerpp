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

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import static org.solovyev.android.messenger.App.newTag;

/**
 * Implementation of this class must provide thread safeness
 */
@ThreadSafe
public interface UserService {

	final String TAG = newTag("UserService");


	// initial initialization: will be called once on application start
	void init();

    /*
	**********************************************************************
    *
    *                           USER OPERATIONS
    *
    **********************************************************************
    */

	/**
	 * NOTE: finding user by id always return user object, if real user cannot be found via API (e.g. user was removed) service must return dummy user object
	 *
	 * @param user user to be found
	 * @return user instance identified by specified <var>user</var> entity
	 */
	@Nonnull
	User getUserById(@Nonnull Entity user);

	/**
	 * NOTE: finding user by id always return user object, if real user cannot be found via API (e.g. user was removed) service must return dummy user object
	 *
	 * @param user             user to be found
	 * @param tryFindInAccount user search will be done in account service if user was not found in persistence and this parameter is set to true
	 * @return user instance identified by specified <var>user</var> entity
	 */
	@Nonnull
	User getUserById(@Nonnull Entity user, boolean tryFindInAccount);

	@Nonnull
	User getUserById(@Nonnull Entity user, boolean tryFindInAccount, boolean createFakeUser) throws NoSuchElementException;

	/**
	 * @param user user
	 * @return all user chats
	 */
	@Nonnull
	List<Chat> getChats(@Nonnull Entity user);

	/**
	 * Method updates user in application
	 *
	 * @param user user to be updated
	 */
	void updateUser(@Nonnull User user);

	void saveUser(@Nonnull User user);

	// special method for saving account users
	void saveAccountUser(@Nonnull User user);

	void removeUser(@Nonnull User user);

	@Nonnull
	UserIconsService getIconsService();

	/*
	**********************************************************************
    *
    *                           CONTACTS
    *
    **********************************************************************
    */

	/**
	 * @param user user
	 * @return list of all user contacts
	 */
	@Nonnull
	List<User> getContacts(@Nonnull Entity user);

	/**
	 * NOTE: method do not check real status of user on the current moment of time but get one from the cache => it might be different
	 *
	 * @param user user
	 * @return list of all online user contacts
	 */
	@Nonnull
	List<User> getOnlineContacts(@Nonnull Entity user);

	/**
	 * Call this method when presence of user's contact has been changed.
	 *
	 * @param user      user
	 * @param contact   user's contact which presence has been changed
	 * @param available new presence value
	 */
	void onContactPresenceChanged(@Nonnull User user, @Nonnull User contact, boolean available);

	@Nonnull
	List<UiContact> findContacts(@Nonnull User user, @Nullable String query, int count);

	@Nonnull
	List<UiContact> findContacts(@Nonnull User user, @Nullable String query, int count, @Nonnull Collection<UiContact> except);

	@Nonnull
	List<UiContact> findContacts(@Nullable String query, int count);

	@Nonnull
	List<UiContact> findContacts(@Nullable String query, int count, @Nonnull Collection<UiContact> except);

	@Nonnull
	List<UiContact> getLastChatedContacts(int count);

    /*
    **********************************************************************
    *
    *                           SYNC
    *
    **********************************************************************
    */

	/**
	 * Method synchronizes local users contacts with remote user contacts
	 * <p/>
	 * NOTE: some realms do not support user contacts retrieval, in that case empty list is returned
	 *
	 * @param account account for which synchronization must be done
	 * @return updated list of user contacts
	 */
	@Nonnull
	List<User> syncContacts(@Nonnull Account<?> account) throws AccountException;

	/**
	 * Method synchronizes local users chats with remote user chats
	 * <p/>
	 * NOTE: some realms do not support user chats retrieval, in that case empty list is returned
	 *
	 *
	 * @param account account in which synchronization must be done
	 * @return updated list of chats
	 */
	@Nonnull
	List<Chat> syncChats(@Nonnull Account account) throws AccountException;

	void mergeChats(@Nonnull Account account, @Nonnull List<? extends AccountChat> apiChats) throws AccountException;

	void mergeContacts(@Nonnull Account account, @Nonnull List<User> contacts, boolean allowRemoval, boolean allowUpdate);

	/**
	 * Method synchronizes user contacts statuses (presences/availabilities)
	 *
	 * @param account account for which synchronization should be done
	 */
	void syncContactStatuses(@Nonnull Account account) throws AccountException;

    /*
    **********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

	/**
	 * Method subscribes listener for user events notifications
	 *
	 * @param listener listener to be subscribed
	 * @return true if was added, false if listener already exists
	 */
	boolean addListener(@Nonnull JEventListener<UserEvent> listener);

	/**
	 * Method unsubscribes listener from user events notifications
	 *
	 * @param listener listener to be unsubscribed
	 * @return true if listener was successfully unsubscribed, false if no such listener was found
	 */
	boolean removeListener(@Nonnull JEventListener<UserEvent> listener);


	void onUnreadMessagesCountChanged(@Nonnull Entity contact, @Nonnull Integer unreadMessagesCount);

	int getUnreadMessagesCount(@Nonnull Entity contact);

	void fireEvents(@Nonnull Collection<UserEvent> userEvents);
}
