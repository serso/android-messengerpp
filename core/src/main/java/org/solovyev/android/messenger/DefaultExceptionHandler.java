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

package org.solovyev.android.messenger;

import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.notifications.Notification;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.network.NetworkState;
import org.solovyev.android.network.NetworkStateService;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.notifications.Notifications.*;

@Singleton
public final class DefaultExceptionHandler implements ExceptionHandler {

	/*
	**********************************************************************
	*
	*                           AUTO INJECTED FIELDS
	*
	**********************************************************************
	*/

	@Inject
	@Nonnull
	private NotificationService notificationService;

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private NetworkStateService networkStateService;


	public DefaultExceptionHandler() {
	}

	@Override
	public void handleException(@Nonnull final Throwable e) {
		Notification notification = null;

		if (e instanceof UnsupportedAccountException) {
			notification = ACCOUNT_NOT_SUPPORTED_NOTIFICATION;
		} else if (e instanceof AccountConnectionException) {
			final boolean handled = handleAccountException((AccountException) e);
			if (!handled) {
				if (networkStateService.getNetworkData().getState() == NetworkState.CONNECTED) {
					// if we are not connected show nothing
					notification = newAccountConnectionErrorNotification();
				}
			}
		} else if (e instanceof AccountException) {
			final boolean handled = handleAccountException((AccountException) e);
			if (!handled) {
				notification = newAccountErrorNotification();
			}
		} else if (e instanceof HttpRuntimeIoException) {
			notification = NO_INTERNET_NOTIFICATION;
		} else if (e instanceof IllegalJsonRuntimeException) {
			notification = newInvalidResponseNotification();
		} else if (e instanceof AccountRuntimeException) {
			handleException(new AccountException((AccountRuntimeException) e));
		} else {
			notification = newUndefinedErrorNotification();
		}

		if (notification != null) {
			notification.causedBy(e);
			notificationService.add(notification);
		}

		Log.e(App.TAG, e.getMessage(), e);
	}

	private boolean handleAccountException(@Nonnull AccountException e) {
		boolean handled = false;

		final String accountId = e.getAccountId();

		try {
			final Account account = accountService.getAccountById(accountId);
			final Throwable cause = e.getCause();

			if (cause != e && cause != null) {
				handled = account.getRealm().handleException(cause, account);
			} else {
				handled = account.getRealm().handleException(e, account);
			}
		} catch (UnsupportedAccountException e1) {
			Log.e(App.TAG, e1.getMessage(), e1);
		}

		return handled;
	}
}
