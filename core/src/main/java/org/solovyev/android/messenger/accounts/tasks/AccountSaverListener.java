package org.solovyev.android.messenger.accounts.tasks;

import android.app.Activity;
import android.widget.Toast;
import com.google.common.util.concurrent.FutureCallback;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MessengerContextCallback;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountAlreadyExistsException;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.App.showToast;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.FinishedState.saved;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_edit_finished;
import static org.solovyev.android.tasks.Tasks.toUiThreadFutureCallback;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:47 PM
 */
public final class AccountSaverListener extends MessengerContextCallback<Activity, Account> {

	private AccountSaverListener() {
	}

	@Nonnull
	public static FutureCallback<Account> newAccountSaverListener(@Nonnull Activity activity) {
		return toUiThreadFutureCallback(activity, new AccountSaverListener());
	}

	@Override
	public void onSuccess(@Nonnull Activity context, Account account) {
		getEventManager(context).fire(account_edit_finished.newEvent(account, saved));
	}

	@Override
	public void onFailure(@Nonnull Activity context, Throwable e) {
		if (e instanceof InvalidCredentialsException) {
			showToast(R.string.mpp_invalid_credentials);
		} else if (e instanceof AccountAlreadyExistsException) {
			showToast(R.string.mpp_same_account_configured);
		} else {
			super.onFailure(context, e);
		}
	}
}
