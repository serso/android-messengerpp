package org.solovyev.android.messenger.users;

import android.os.Bundle;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.BaseAccountFragment;

import com.google.inject.Inject;

import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;

public abstract class BaseUserFragment<A extends Account<?>> extends BaseAccountFragment<A> {
	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/

	@Nonnull
	protected static final String ARG_USER_ID = "user_id";

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

	private User user;

	protected BaseUserFragment(int layoutResId) {
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


	protected boolean isNewUser() {
		return user == null;
	}

	public User getUser() {
		return user;
	}

	@Nonnull
	protected static Bundle newUserArguments(@Nonnull Account account, @Nonnull User user) {
		final Bundle arguments = new Bundle();
		arguments.putString(ARG_USER_ID, user.getId());
		arguments.putString(ARG_ACCOUNT_ID, account.getId());
		return arguments;
	}
}
