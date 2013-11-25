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

package org.solovyev.android.messenger.accounts;

import android.content.Context;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public interface AccountService {

	@Nonnull
	static String TAG = "AccountService";

	String NO_ACCOUNT_ID = "empty";

	/**
	 * Method initializes service, must be called once before any other operations with current service
	 */
	void init();

	@Nonnull
	Collection<Account> getAccounts();

	@Nonnull
	Collection<Account> getEnabledAccounts();

	/**
	 * @return collection of users in all configured accounts
	 */
	@Nonnull
	Collection<User> getAccountUsers();

	/**
	 * @return collection of users in all configured ENABLED accounts
	 */
	@Nonnull
	Collection<User> getEnabledAccountUsers();

	@Nonnull
	Account getAccountById(@Nonnull String accountId) throws UnsupportedAccountException;

	@Nonnull
	Account getAccountByEntity(@Nonnull Entity entity) throws UnsupportedAccountException;

	@Nonnull
	<A extends Account> A saveAccount(@Nonnull AccountBuilder<A> accountBuilder) throws InvalidCredentialsException, AccountAlreadyExistsException;

	@Nonnull
	Account changeAccountState(@Nonnull Account account, @Nonnull AccountState newState);

	void saveAccountSyncData(@Nonnull Account account);

	void removeAccount(@Nonnull String accountId);

	boolean isOneAccount();

	boolean isOneAccount(@Nonnull Realm realm);

	boolean canCreateUsers();

	@Nonnull
	Collection<Account> getAccountsCreatingUsers();

	List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context);

    /*
	**********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

	void addListener(@Nonnull JEventListener<AccountEvent> listener);

	void removeListener(@Nonnull JEventListener<AccountEvent> listener);
}
