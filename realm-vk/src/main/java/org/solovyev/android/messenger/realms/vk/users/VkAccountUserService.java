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

package org.solovyev.android.messenger.realms.vk.users;

import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.collections.Collections;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.http.HttpTransactions.execute;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 1:18 PM
 */
public class VkAccountUserService implements AccountUserService {

	@Nonnull
	private final VkAccount account;

	public VkAccountUserService(@Nonnull VkAccount account) {
		this.account = account;
	}

	@Override
	public User getUserById(@Nonnull String accountUserId) throws AccountConnectionException {
		try {
			final List<User> users = execute(VkUsersGetHttpTransaction.newInstance(account, accountUserId, null));
			return Collections.getFirstListElement(users);
		} catch (HttpRuntimeIoException e) {
			throw new AccountConnectionException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountConnectionException(account.getId(), e);
		}
	}

	@Nonnull
	@Override
	public List<User> getUserContacts() throws AccountConnectionException {
		try {
			return execute(VkFriendsGetHttpTransaction.newInstance(account, account.getUser().getEntity().getAccountEntityId()));
		} catch (HttpRuntimeIoException e) {
			throw new AccountConnectionException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountConnectionException(account.getId(), e);
		}
	}


	@Nonnull
	@Override
	public List<User> getOnlineUsers() throws AccountConnectionException {
		final List<User> result = new ArrayList<User>();

		try {
			final UserService userService = App.getUserService();
			for (String accountUserId : execute(new VkFriendsGetOnlineHttpTransaction(account))) {
				result.add(userService.getUserById(account.newUserEntity(accountUserId)).cloneWithNewStatus(true));
			}
		} catch (HttpRuntimeIoException e) {
			throw new AccountConnectionException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountConnectionException(account.getId(), e);
		}

		return result;
	}

	@Nonnull
	@Override
	public User saveUser(@Nonnull User user) {
		return user;
	}
}
