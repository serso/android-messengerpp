package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.joda.time.DateTimeZone;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.sync.SyncAllTaskIsAlreadyRunning;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.datetime.FastDateTimeZoneProvider;
import roboguice.RoboGuice;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.joda.time.DateTimeZone.UTC;
import static org.solovyev.android.messenger.App.getAccountService;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:16 PM
 */

@ReportsCrashes(formKey = "",
		mailTo = "se.solovyev+programming+messengerpp+crashes+1.0@gmail.com",
		mode = ReportingInteractionMode.SILENT)
public class MessengerApplication extends Application {

	public MessengerApplication() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		ACRA.init(this);

		// initialize Joda time for android
		System.setProperty("org.joda.time.DateTimeZone.Provider", FastDateTimeZoneProvider.class.getName());

		DateTimeZone.setDefault(UTC);

		MessengerPreferences.setDefaultValues(this);

		App.init(this);

		RoboGuice.getBaseApplicationInjector(this).injectMembers(this);

		fillCaches();
	}

	private void fillCaches() {
		final Collection<Account> accounts = getAccountService().getEnabledAccounts();

		// prefetch data and do synchronization

		boolean syncDone = true;

		for (Account account : accounts) {
			final User user = account.getUser();

			if (!user.getUserSyncData().isFirstSyncDone()) {
				syncDone = false;
			} else {
				// prefetch data
				new PreloadCachedData(this).executeInParallel(user);
			}
		}

		if (!syncDone) {
			// todo serso: actually synchronization must be done only for not synced realms (NOT for all as it is now)
			// user is logged first time => sync all data
			try {
				App.getSyncService().syncAll(false);
			} catch (SyncAllTaskIsAlreadyRunning syncAllTaskIsAlreadyRunning) {
				// do not care
			}
		}
	}

	public static void exit(@Nonnull Application application, @Nonnull Activity activity) {
		getAccountService().stopAllRealmConnections();

		final Intent serviceIntent = new Intent();
		serviceIntent.setClass(application, OngoingNotificationService.class);
		application.stopService(serviceIntent);

		activity.finish();
	}

	public static void startBackgroundService(@Nonnull Application application) {
		final Intent serviceIntent = new Intent();
		serviceIntent.setClass(application, OngoingNotificationService.class);
		application.startService(serviceIntent);
	}

	private static final class PreloadCachedData extends MessengerAsyncTask<User, Void, Void> {

		private PreloadCachedData(@Nonnull Context context) {
			super(context);
		}

		@Override
		protected Void doWork(@Nonnull List<User> users) {
			Context context = getContext();
			if (context != null) {
				for (User user : users) {
					App.getUserService().getUserContacts(user.getEntity());
					App.getUserService().getUserChats(user.getEntity());
				}
			}

			return null;
		}

		@Override
		protected void onSuccessPostExecute(@Nullable Void result) {

		}
	}
}
