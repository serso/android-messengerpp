package org.solovyev.android.messenger.accounts;

import android.app.Activity;
import com.google.common.util.concurrent.FutureCallback;
import org.solovyev.android.messenger.MessengerContextCallback;
import org.solovyev.android.tasks.Tasks;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.FinishedState.status_changed;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_edit_finished;

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
		getEventManager(context).fire(account_edit_finished.newEvent(account, status_changed));
	}
}
