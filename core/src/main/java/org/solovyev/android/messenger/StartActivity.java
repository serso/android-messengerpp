package org.solovyev.android.messenger;

import android.content.Context;
import android.os.Bundle;
import roboguice.activity.RoboActivity;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.sync.SyncAllTaskIsAlreadyRunning;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.User;

import com.google.inject.Inject;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:47 PM
 */
public class StartActivity extends RoboActivity {

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private SyncService syncService;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Collection<Account> accounts = accountService.getEnabledAccounts();
		// todo serso: maybe move to Application or Service?
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
				syncService.syncAll(syncDone);
			} catch (SyncAllTaskIsAlreadyRunning syncAllTaskIsAlreadyRunning) {
				// do not care
			}
		}

		// we must start service from here because Android can cache application
		// and Application#onCreate() is never called!
		App.startBackgroundService();

		MainActivity.startActivity(this);
		this.finish();
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
