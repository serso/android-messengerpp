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

import android.app.Application;
import android.content.Context;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.joda.time.DateTimeZone;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.sync.SyncAllTaskIsAlreadyRunning;
import org.solovyev.common.datetime.FastDateTimeZoneProvider;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.joda.time.DateTimeZone.UTC;
import static org.solovyev.android.messenger.App.getAccountService;

@ReportsCrashes(formKey = "",
		mailTo = "se.solovyev+programming+messengerpp+crashes+0.000@gmail.com",
		mode = ReportingInteractionMode.SILENT)
public class MessengerApplication extends Application {

	public MessengerApplication() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// if we rename package we need to find module
		RoboGuice.setModulesResourceId(R.array.roboguice_modules);

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

		final List<Account> syncedAccounts = new ArrayList<Account>(accounts.size());
		for (Account account : accounts) {
			if (!account.getSyncData().isFirstSyncDone()) {
				syncDone = false;
			} else {
				syncedAccounts.add(account);
			}
		}

		// prefetch data
		new PreloadCachedData(this).executeInParallel(syncedAccounts.toArray(new Account[syncedAccounts.size()]));

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

	private static final class PreloadCachedData extends MessengerAsyncTask<Account, Void, Void> {

		private PreloadCachedData(@Nonnull Context context) {
			super(context);
		}

		@Override
		protected Void doWork(@Nonnull List<Account> accounts) {
			Context context = getContext();
			if (context != null) {
				for (Account account : accounts) {
					App.getUserService().getContacts(account.getUser().getEntity());
				}

				for (Account account : accounts) {
					App.getUserService().getChats(account.getUser().getEntity());
				}
			}

			return null;
		}

		@Override
		protected void onSuccessPostExecute(@Nullable Void result) {
		}
	}
}
