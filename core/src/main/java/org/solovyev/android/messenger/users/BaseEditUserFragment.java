package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.view.View;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.EditButtons;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.tasks.UserSaverCallable;
import org.solovyev.android.messenger.core.R;

import static org.solovyev.android.messenger.accounts.tasks.UserSaverCallback.newUserSaverCallback;
import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.android.messenger.users.Users.newUser;

public abstract class BaseEditUserFragment<A extends Account<?>> extends BaseUserFragment<A> {

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	@Nonnull
	private final EditButtons editButtons = new UserEditButtons<A>(this);

	protected BaseEditUserFragment(int layoutResId) {
		super(layoutResId);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		editButtons.onViewCreated(root, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		getTaskListeners().addTaskListener(UserSaverCallable.TASK_NAME, newUserSaverCallback(this), getActivity(), R.string.mpp_saving_user_title, R.string.mpp_saving_user_message);
	}

	@Override
	protected CharSequence getFragmentTitle() {
		return null;
	}

	@Nullable
	protected abstract MutableUser validateData();

	@Nonnull
	protected MutableUser getOrCreateUser() {
		final MutableUser result;

		final User user = getUser();
		if(user != null) {
			result = newUser(user.getEntity(), user.getUserSyncData(), user.getProperties().getPropertiesCollection());
		} else {
			result = newEmptyUser(generateEntity(getAccount()));
		}

		return result;
	}

	@Nonnull
	public static Bundle newCreateUserArguments(@Nonnull Account account) {
		return newAccountArguments(account);
	}

	@Nonnull
	public static Bundle newEditUserArguments(@Nonnull Account account, @Nonnull User user) {
		return newUserArguments(account, user);
	}
}
