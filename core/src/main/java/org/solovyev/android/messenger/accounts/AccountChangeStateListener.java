package org.solovyev.android.messenger.accounts;

import android.app.Activity;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.MessengerContextCallback;
import org.solovyev.android.tasks.Tasks;

import com.google.common.util.concurrent.FutureCallback;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:33 PM
 */
final class AccountChangeStateListener extends MessengerContextCallback<Activity, Account> {

	private AccountChangeStateListener() {
	}

	@Nonnull
	static FutureCallback<Account> newInstance(@Nonnull Activity activity) {
		return Tasks.toUiThreadFutureCallback(activity, new AccountChangeStateListener());
	}

	@Override
	public void onSuccess(@Nonnull Activity context, Account account) {
		final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
		eventManager.fire(AccountUiEventType.newAccountEditFinishedEvent(account, AccountUiEventType.FinishedState.status_changed));
	}
}
