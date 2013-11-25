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

package org.solovyev.android.messenger.accounts.tasks;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static org.solovyev.android.messenger.App.getUserService;

public class UserSaverCallable implements Callable<User> {

	@Nonnull
	public static final String TASK_NAME = "user-save";

	@Nonnull
	private final Account account;

	@Nonnull
	private final User user;

	public UserSaverCallable(@Nonnull Account account, @Nonnull User user) {
		this.account = account;
		this.user = user;
	}

	@Override
	public User call() {
		getUserService().mergeContacts(account, asList(user), false, true);
		return user;
	}
}
