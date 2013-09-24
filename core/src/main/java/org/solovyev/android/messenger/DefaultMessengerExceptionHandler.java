package org.solovyev.android.messenger;

import android.util.Log;

import javax.annotation.Nonnull;

import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountRuntimeException;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.notifications.Notification;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.network.NetworkState;
import org.solovyev.android.network.NetworkStateService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.solovyev.android.messenger.notifications.Notifications.NO_INTERNET_NOTIFICATION;
import static org.solovyev.android.messenger.notifications.Notifications.REALM_NOT_SUPPORTED_NOTIFICATION;
import static org.solovyev.android.messenger.notifications.Notifications.newInvalidResponseNotification;
import static org.solovyev.android.messenger.notifications.Notifications.newRealmConnectionErrorNotification;
import static org.solovyev.android.messenger.notifications.Notifications.newRealmErrorNotification;
import static org.solovyev.android.messenger.notifications.Notifications.newUndefinedErrorNotification;

@Singleton
public final class DefaultMessengerExceptionHandler implements MessengerExceptionHandler {

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


	public DefaultMessengerExceptionHandler() {
	}

	@Override
	public void handleException(@Nonnull final Throwable e) {
		Notification notification = null;

		if (e instanceof UnsupportedAccountException) {
			notification = REALM_NOT_SUPPORTED_NOTIFICATION;
		} else if (e instanceof AccountConnectionException) {
			final boolean handled = handleRealmException((AccountException) e);
			if (!handled) {
				if (networkStateService.getNetworkData().getState() == NetworkState.CONNECTED) {
					// if we are not connected show nothing
					notification = newRealmConnectionErrorNotification();
				}
			}
		} else if (e instanceof AccountException) {
			final boolean handled = handleRealmException((AccountException) e);
			if (!handled) {
				notification = newRealmErrorNotification();
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
			Log.e(App.TAG, e.getMessage(), e);
		}
	}

	private boolean handleRealmException(@Nonnull AccountException e) {
		boolean handled = false;

		final String realmId = e.getAccountId();

		try {
			final Account account = accountService.getAccountById(realmId);
			final Throwable cause = e.getCause();

			if (cause != e) {
				handled = account.getRealm().handleException(cause, account);
			} else {
				handled = account.getRealm().handleException(e, account);
			}
		} catch (UnsupportedAccountException e1) {
			handleException(e1);
		}

		return handled;
	}
}
