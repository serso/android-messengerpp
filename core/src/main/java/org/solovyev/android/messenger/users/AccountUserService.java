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

import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface AccountUserService {

	/**
	 * @param accountUserId account user id
	 * @return fully loaded user which is identified by <var>accountUserId</var> in current account
	 */
	@Nullable
	User getUserById(@Nonnull String accountUserId) throws AccountConnectionException;

	/**
	 * @return list of user contacts (users to which current user can write messages and is aware of theirs presence in chat)
	 */
	@Nonnull
	List<User> getContacts() throws AccountConnectionException;

	/**
	 * @return list of users who are online now
	 */
	@Nonnull
	List<User> getOnlineUsers() throws AccountConnectionException;

	@Nonnull
	User saveUser(@Nonnull User user);

}
