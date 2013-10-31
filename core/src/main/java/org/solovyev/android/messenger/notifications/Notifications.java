package org.solovyev.android.messenger.notifications;

import android.content.Intent;
import org.acra.ACRA;
import org.solovyev.android.Activities2;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.core.R;
import org.solovyev.common.msg.MessageLevel;
import org.solovyev.common.msg.MessageType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.provider.Settings.ACTION_WIRELESS_SETTINGS;

/**
 * User: serso
 * Date: 6/5/13
 * Time: 3:03 PM
 */
public final class Notifications {


	private Notifications() {
		throw new AssertionError();
	}

	public static final Notification NO_INTERNET_NOTIFICATION = Notification.newInstance(R.string.mpp_notification_network_problem, MessageType.warning).solvedBy(new NoInternetConnectionSolution());
	public static final Notification ACCOUNT_NOT_SUPPORTED_NOTIFICATION = Notification.newInstance(R.string.mpp_notification_account_unsupported_exception, MessageType.error);

	@Nonnull
	public static Notification newInvalidResponseNotification() {
		return Notification.newInstance(R.string.mpp_notification_invalid_response, MessageType.error);
	}

	@Nonnull
	public static Notification newUndefinedErrorNotification() {
		return Notification.newInstance(R.string.mpp_notification_undefined_error, MessageType.error);
	}

	@Nonnull
	public static Notification newAccountErrorNotification() {
		return Notification.newInstance(R.string.mpp_notification_account_exception, MessageType.error);
	}

	@Nonnull
	public static Notification newAccountConnectionErrorNotification() {
		return Notification.newInstance(R.string.mpp_notification_account_connection_exception, MessageType.error);
	}

	@Nonnull
	public static Notification newNotification(int messageResId, @Nonnull MessageLevel messageLevel, @Nullable Object... params) {
		return Notification.newInstance(messageResId, messageLevel, params);
	}

	@Nonnull
	public static NotificationSolution newOpenAccountConfSolution(@Nonnull Account account) {
		return new OpenAccountSolution(account);
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER CLASSES
	*
	**********************************************************************
	*/

	static final class NotifyDeveloperSolution implements NotificationSolution {

		@Nonnull
		public static final NotifyDeveloperSolution instance = new NotifyDeveloperSolution();

		private NotifyDeveloperSolution() {
		}

		@Nonnull
		static NotifyDeveloperSolution getInstance() {
			return instance;
		}

		@Override
		public void solve(@Nonnull Notification notification) {
			final Throwable cause = notification.getCause();
			if (cause != null) {
				ACRA.getErrorReporter().handleException(cause);
			}
			notification.dismiss();
		}
	}

	private static final class NoInternetConnectionSolution implements NotificationSolution {

		@Override
		public void solve(@Nonnull Notification notification) {
			Activities2.startActivity(App.getApplication(), new Intent(ACTION_WIRELESS_SETTINGS));
		}
	}

	private static class OpenAccountSolution implements NotificationSolution {
		@Nonnull
		private final Account account;

		public OpenAccountSolution(@Nonnull Account account) {
			this.account = account;
		}

		@Override
		public void solve(@Nonnull Notification notification) {
			// todo serso: open configuration for specified realm
			notification.dismiss();
		}
	}
}
