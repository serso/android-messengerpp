package org.solovyev.android.messenger.accounts.tasks;


import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.fragments.PrimaryFragment;
import org.solovyev.android.messenger.users.BaseEditUserFragment;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.tasks.ContextCallback;
import org.solovyev.android.tasks.Tasks;

import com.google.common.util.concurrent.FutureCallback;

public class UserSaverCallback implements ContextCallback<BaseFragmentActivity, User> {

	@Nonnull
	private final WeakReference<BaseEditUserFragment<?>> fragmentRef;

	private UserSaverCallback(@Nonnull BaseEditUserFragment<?> fragment) {
		this.fragmentRef = new WeakReference<BaseEditUserFragment<?>>(fragment);
	}

	@Override
	public void onSuccess(@Nonnull BaseFragmentActivity context, User result) {
		final BaseEditUserFragment<?> fragment = fragmentRef.get();
		if (fragment != null) {
			fragment.dismiss();
		}
	}

	@Override
	public void onFailure(@Nonnull BaseFragmentActivity context, Throwable t) {
	}

	public static FutureCallback<User> newUserSaverCallback(@Nonnull BaseEditUserFragment<?> fragment) {
		return Tasks.toUiThreadFutureCallback(fragment.getFragmentActivity(), new UserSaverCallback(fragment));
	}
}
