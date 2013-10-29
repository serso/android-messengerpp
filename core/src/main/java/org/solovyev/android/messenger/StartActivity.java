package org.solovyev.android.messenger;

import android.content.Context;
import android.content.Intent;
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
import org.solovyev.common.Objects;

import com.google.inject.Inject;

import static org.solovyev.common.Objects.areEqual;

public class StartActivity extends RoboActivity {

	private static final String INTENT_SHOW_UNREAD_MESSAGES_ACTION = "show_unread_messages";

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private SyncService syncService;

	@Nonnull
	public static Intent newUnreadMessagesStartIntent(@Nonnull Context context) {
		final Intent intent = new Intent(context, StartActivity.class);
		intent.setAction(StartActivity.INTENT_SHOW_UNREAD_MESSAGES_ACTION);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		if(areEqual(intent.getAction(), INTENT_SHOW_UNREAD_MESSAGES_ACTION)) {
			MainActivity.startActivityForUnreadMessages(this);
		} else {
			MainActivity.startActivity(this);
		}

		// we must start service from here because Android can cache application
		// and Application#onCreate() is never called!
		App.startBackgroundService();

		this.finish();
	}
}
