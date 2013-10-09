package org.solovyev.android.messenger.accounts.tasks;

import android.support.v4.app.FragmentActivity;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.users.User;
import org.solovyev.android.tasks.ContextCallback;
import org.solovyev.android.tasks.Tasks;

import com.google.common.util.concurrent.FutureCallback;

public class UserSaverCallback implements ContextCallback<FragmentActivity, User> {

	private UserSaverCallback() {
	}

	@Override
	public void onSuccess(@Nonnull FragmentActivity context, User result) {
		context.getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onFailure(@Nonnull FragmentActivity context, Throwable t) {

	}

	public static FutureCallback<User> newUserSaverCallback(@Nonnull FragmentActivity activity) {
		return Tasks.toUiThreadFutureCallback(activity, new UserSaverCallback());
	}
}
