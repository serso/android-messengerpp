package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.joda.time.DateTimeZone;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.common.datetime.FastDateTimeZoneProvider;

import com.google.inject.Inject;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:16 PM
 */

@ReportsCrashes(formKey = "",
		mailTo = "se.solovyev+programming+messengerpp+crashes+1.0@gmail.com",
		mode = ReportingInteractionMode.SILENT)
public class MessengerApplication extends Application {

	/*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private AccountService accountService;

	public MessengerApplication() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		ACRA.init(this);

		// initialize Joda time for android
		System.setProperty("org.joda.time.DateTimeZone.Provider", FastDateTimeZoneProvider.class.getName());

		DateTimeZone.setDefault(DateTimeZone.UTC);

		MessengerPreferences.setDefaultValues(this);

		App.init(this);

		RoboGuice.getBaseApplicationInjector(this).injectMembers(this);
	}

	public void exit(@Nonnull Activity activity) {
		accountService.stopAllRealmConnections();

		final Intent serviceIntent = new Intent();
		serviceIntent.setClass(this, OngoingNotificationService.class);
		stopService(serviceIntent);

		activity.finish();
	}
}
