package org.solovyev.android.messenger.accounts.tasks;


import com.google.common.util.concurrent.FutureCallback;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.tasks.ContextCallback;
import org.solovyev.android.tasks.Tasks;

import javax.annotation.Nonnull;

public class UserSaverCallback implements ContextCallback<BaseFragmentActivity, User> {

	private UserSaverCallback() {
	}

	@Override
	public void onSuccess(@Nonnull BaseFragmentActivity context, User result) {
		context.getMultiPaneFragmentManager().goBack();
	}

	@Override
	public void onFailure(@Nonnull BaseFragmentActivity context, Throwable t) {
	}

	public static FutureCallback<User> newUserSaverCallback(@Nonnull BaseFragmentActivity activity) {
		return Tasks.toUiThreadFutureCallback(activity, new UserSaverCallback());
	}
}
