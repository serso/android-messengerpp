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
import org.solovyev.android.messenger.accounts.AccountAlreadyExistsException;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.App.showToast;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.FinishedState.saved;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_edit_finished;
import static org.solovyev.android.tasks.Tasks.toUiThreadFutureCallback;

public final class AccountSaverListener extends MessengerContextCallback<Activity, Account> {

	private AccountSaverListener() {
	}

	@Nonnull
	public static FutureCallback<Account> newAccountSaverListener(@Nonnull Activity activity) {
		return toUiThreadFutureCallback(activity, new AccountSaverListener());
	}

	@Override
	public void onSuccess(@Nonnull Activity context, Account account) {
		getEventManager(context).fire(account_edit_finished.newEvent(account, saved));
	}

	@Override
	public void onFailure(@Nonnull Activity context, Throwable e) {
		if (e instanceof InvalidCredentialsException) {
			showToast(R.string.mpp_invalid_credentials);
		} else if (e instanceof AccountAlreadyExistsException) {
			showToast(R.string.mpp_same_account_configured);
		} else {
			showToast(R.string.mpp_notification_undefined_error);
			super.onFailure(context, e);
		}
	}
}
