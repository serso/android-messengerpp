package org.solovyev.android.messenger;

import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.notifications.Notification;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.realms.*;
import org.solovyev.android.network.NetworkState;
import org.solovyev.android.network.NetworkStateService;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.notifications.Notifications.*;

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
	private RealmService realmService;

	@Inject
	@Nonnull
	private NetworkStateService networkStateService;


	public DefaultMessengerExceptionHandler() {
	}

	@Override
	public void handleException(@Nonnull final Throwable e) {
		Notification notification = null;

		if (e instanceof UnsupportedRealmException) {
			notification = REALM_NOT_SUPPORTED_NOTIFICATION;
		} else if (e instanceof RealmConnectionException) {
			final boolean handled = handleRealmException((RealmException) e);
			if (!handled) {
				if (networkStateService.getNetworkData().getState() == NetworkState.CONNECTED) {
					// if we are not connected show nothing
					notification = newRealmConnectionErrorNotification();
				}
			}
		} else if (e instanceof RealmException) {
			final boolean handled = handleRealmException((RealmException) e);
			if (!handled) {
				notification = newRealmErrorNotification();
			}
		} else if (e instanceof HttpRuntimeIoException) {
			notification = NO_INTERNET_NOTIFICATION;
		} else if (e instanceof IllegalJsonRuntimeException) {
			notification = newInvalidResponseNotification();
		} else if (e instanceof RealmRuntimeException) {
			handleException(new RealmException((RealmRuntimeException) e));
		} else {
			notification = newUndefinedErrorNotification();
		}

		if (notification != null) {
			notification.causedBy(e);
			notificationService.add(notification);
			Log.e(MessengerApplication.TAG, e.getMessage(), e);
		}
	}

	private boolean handleRealmException(@Nonnull RealmException e) {
		boolean handled = false;

		final String realmId = e.getRealmId();

		try {
			final Account account = realmService.getRealmById(realmId);
			final Throwable cause = e.getCause();

			if (cause != e) {
				handled = account.getRealmDef().handleException(cause, account);
			} else {
				handled = account.getRealmDef().handleException(e, account);
			}
		} catch (UnsupportedRealmException e1) {
			handleException(e1);
		}

		return handled;
	}
}
