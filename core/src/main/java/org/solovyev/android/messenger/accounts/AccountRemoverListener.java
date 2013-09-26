package org.solovyev.android.messenger.accounts;

import android.app.Activity;
import com.google.common.util.concurrent.FutureCallback;
import org.solovyev.android.messenger.MessengerContextCallback;
import org.solovyev.android.tasks.Tasks;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.FinishedState.removed;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_edit_finished;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:44 PM
 */
final class AccountRemoverListener extends MessengerContextCallback<Activity, Account> {

	private AccountRemoverListener() {
	}

	@Nonnull
	static FutureCallback<Account> newInstance(@Nonnull Activity activity) {
		return Tasks.toUiThreadFutureCallback(activity, new AccountRemoverListener());
	}

	@Override
	public void onSuccess(@Nonnull Activity activity, Account account) {
		getEventManager(activity).fire(account_edit_finished.newEvent(account, removed));
	}
}
