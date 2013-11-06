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

import android.app.Activity;
import com.google.common.util.concurrent.FutureCallback;
import org.solovyev.android.messenger.MessengerContextCallback;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.tasks.Tasks;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.FinishedState.removed;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_edit_finished;
import static org.solovyev.android.tasks.Tasks.toUiThreadFutureCallback;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:44 PM
 */
public final class AccountRemoverListener extends MessengerContextCallback<Activity, Account> {

	private AccountRemoverListener() {
	}

	@Nonnull
	public static FutureCallback<Account> newAccountRemoverListener(@Nonnull Activity activity) {
		return toUiThreadFutureCallback(activity, new AccountRemoverListener());
	}

	@Override
	public void onSuccess(@Nonnull Activity activity, Account account) {
		getEventManager(activity).fire(account_edit_finished.newEvent(account, removed));
	}
}
