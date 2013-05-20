package org.solovyev.android.messenger;

import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.realms.RealmConnectionException;
import org.solovyev.android.messenger.realms.RealmException;
import org.solovyev.android.messenger.realms.RealmRuntimeException;
import org.solovyev.android.messenger.realms.UnsupportedRealmException;
import org.solovyev.common.msg.MessageType;

import javax.annotation.Nonnull;

@Singleton
public final class DefaultMessengerExceptionHandler implements MessengerExceptionHandler {

	@Inject
	@Nonnull
	private NotificationService notificationService;


	public DefaultMessengerExceptionHandler() {
	}

	@Override
	public void handleException(@Nonnull final Throwable e) {
		if (e instanceof UnsupportedRealmException) {
			notificationService.addNotification(R.string.mpp_notification_realm_unsupported_exception, MessageType.error);
		} else if (e instanceof RealmConnectionException) {
			notificationService.addNotification(R.string.mpp_notification_realm_connection_exception, MessageType.error);
		} else if (e instanceof RealmException) {
			notificationService.addNotification(R.string.mpp_notification_realm_exception, MessageType.error);
		} else if (e instanceof HttpRuntimeIoException) {
			notificationService.addNotification(R.string.mpp_notification_network_problem, MessageType.warning);
		} else if (e instanceof IllegalJsonRuntimeException) {
			notificationService.addNotification(R.string.mpp_notification_invalid_response, MessageType.error);
		} else if (e instanceof RealmRuntimeException) {
			handleException(new RealmException(e));
		} else {
			notificationService.addNotification(R.string.mpp_notification_undefined_error, MessageType.error);
		}

		Log.e(MessengerApplication.TAG, e.getMessage(), e);
	}
}
