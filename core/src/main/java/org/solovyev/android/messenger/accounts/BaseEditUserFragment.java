package org.solovyev.android.messenger.accounts;

import android.os.Bundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.tasks.UserSaverCallable;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import com.google.inject.Inject;

import static org.solovyev.android.messenger.accounts.tasks.UserSaverCallback.newUserSaverCallback;
import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.android.messenger.users.Users.newUser;

public abstract class BaseEditUserFragment<A extends Account<?>> extends BaseAccountFragment<A> {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/

	@Nonnull
	public static final String ARG_USER_ID = "user_id";

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	@Inject
	@Nonnull
	private UserService userService;

	@Nullable
	private User user;

	protected BaseEditUserFragment(int layoutResId) {
		super(layoutResId);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments != null) {
			final String userId = arguments.getString(ARG_USER_ID);
			if (userId != null) {
				user = userService.getUserById(newEntityFromEntityId(userId));
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		getTaskListeners().addTaskListener(UserSaverCallable.TASK_NAME, newUserSaverCallback(getActivity()), getActivity(), R.string.mpp_saving_user_title, R.string.mpp_saving_user_message);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected boolean isBackButtonVisible() {
		// in multi pane layout we don't want to show 'Back' button as there is no 'Back' (in one pane we reuse pane for showing more than one fragment and back means to return to the previous fragment)
		return !(isNewUser() && getMultiPaneManager().isDualPane(getActivity()));
	}

	@Override
	protected CharSequence getFragmentTitle() {
		return null;
	}

	@Override
	protected void onSaveButtonPressed() {
		final MutableUser contact = validateData();
		if (contact != null) {
			getTaskListeners().run(UserSaverCallable.TASK_NAME, new UserSaverCallable(getAccount(), contact), newUserSaverCallback(getActivity()), getActivity(), R.string.mpp_saving_user_title, R.string.mpp_saving_user_message);
		}
	}

	protected boolean isNewUser() {
		return user == null;
	}

	@Nullable
	protected User getUser() {
		return user;
	}

	@Nullable
	protected abstract MutableUser validateData();

	@Override
	protected void onBackButtonPressed() {
		getFragmentManager().popBackStack();
	}

	@Override
	protected boolean isRemoveButtonVisible() {
		if (user != null) {
			final A account = getAccount();
			final boolean accountUser = account.getUser().equals(user);
			return !accountUser;
		} else {
			return false;
		}
	}

	@Override
	protected void onRemoveButtonPressed() {
		assert user != null;
		userService.removeUser(user);
		getActivity().getSupportFragmentManager().popBackStack();
	}

	@Nonnull
	protected MutableUser getOrCreateUser() {
		final MutableUser result;
		if(user != null) {
			result = newUser(user.getEntity(), user.getUserSyncData(), user.getProperties().getPropertiesCollection());
		} else {
			result = newEmptyUser(generateEntity(getAccount()));
		}
		return result;
	}

	@Nonnull
	public static Bundle newCreateUserArguments(@Nonnull Account account) {
		final Bundle arguments = new Bundle();
		arguments.putString(ARG_ACCOUNT_ID, account.getId());
		return arguments;
	}

	@Nonnull
	public static Bundle newEditUserArguments(@Nonnull Account account, @Nonnull User user) {
		final Bundle arguments = new Bundle();
		arguments.putString(ARG_USER_ID, user.getId());
		arguments.putString(ARG_ACCOUNT_ID, account.getId());
		return arguments;
	}

}
