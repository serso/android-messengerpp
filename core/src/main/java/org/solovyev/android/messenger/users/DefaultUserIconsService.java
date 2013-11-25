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

import android.widget.ImageView;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.icons.RealmIconService;

import static java.util.Arrays.asList;

class DefaultUserIconsService implements UserIconsService {

	@Nonnull
	private final UserService userService;

	@Nonnull
	private final AccountService accountService;

	public DefaultUserIconsService(@Nonnull UserService userService, @Nonnull AccountService accountService) {
		this.userService = userService;
		this.accountService = accountService;
	}

	@Override
	public void fetchUserAndContactsIcons(@Nonnull Account account) throws UnsupportedAccountException {
		final RealmIconService realmIconService = account.getRealm().getRealmIconService();
		final User user = account.getUser();

		// fetch self icon
		realmIconService.fetchUsersIcons(asList(user));

		// fetch icons for all contacts
		final List<User> contacts = userService.getContacts(user.getEntity());
		realmIconService.fetchUsersIcons(contacts);

		// update sync data
		accountService.saveAccountSyncData(account.updateUserIconsSyncDate());
	}

	@Nonnull
	private RealmIconService getRealmIconServiceByUser(@Nonnull User user) throws UnsupportedAccountException {
		return getAccountByEntity(user.getEntity()).getRealm().getRealmIconService();
	}

	@Nonnull
	private Account getAccountByEntity(@Nonnull Entity entity) throws UnsupportedAccountException {
		return App.getAccountService().getAccountByEntity(entity);
	}

	@Override
	public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
		getRealmIconServiceByUser(user).setUserIcon(user, imageView);
	}

	@Override
	public void setUsersIcon(@Nonnull Account account, @Nonnull List<User> users, ImageView imageView) {
		account.getRealm().getRealmIconService().setUsersIcon(users, imageView);
	}

	@Override
	public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		getRealmIconServiceByUser(user).setUserPhoto(user, imageView);
	}
}
