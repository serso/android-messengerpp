package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.joda.time.DateTimeZone;
import org.solovyev.common.datetime.FastDateTimeZoneProvider;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;

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
}
