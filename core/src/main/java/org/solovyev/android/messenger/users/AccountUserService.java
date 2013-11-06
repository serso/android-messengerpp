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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.AccountConnectionException;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:33 PM
 */
public interface AccountUserService {

	/**
	 * @param accountUserId account user id
	 * @return fully loaded user which is identified by <var>accountUserId</var> in current account
	 */
	@Nullable
	User getUserById(@Nonnull String accountUserId) throws AccountConnectionException;

	/**
	 * @param accountUserId account user id
	 * @return list of user contacts (users to which current user can write messages and is aware of theirs presence in chat)
	 */
	@Nonnull
	List<User> getUserContacts(@Nonnull String accountUserId) throws AccountConnectionException;

	/**
	 * Method checks if user in <var>users</var> list is online
	 *
	 * @param users list of users for which online check should be done
	 * @return list of users with updated statuses (online/offline)
	 */
	@Nonnull
	List<User> checkOnlineUsers(@Nonnull List<User> users) throws AccountConnectionException;

	@Nonnull
	User saveUser(@Nonnull User user);

}
