package org.solovyev.android.messenger.accounts;

import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.messenger.users.Users;

import com.google.inject.Inject;

import static org.solovyev.android.messenger.App.getUserService;
import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

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
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected boolean isBackButtonVisible() {
		return true;
	}

	@Override
	protected CharSequence getFragmentTitle() {
		return null;
	}

	@Override
	protected void onSaveButtonPressed() {
		final MutableUser contact = validateData();
		if (contact != null) {
			final List<User> users = Arrays.<User>asList(contact);
			getUserService().mergeUserContacts(getAccount().getUser().getEntity(), users, false, true);
			getFragmentManager().popBackStack();
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
		return user != null;
	}

	@Override
	protected void onRemoveButtonPressed() {

	}

	@Nonnull
	protected MutableUser getOrCreateUser() {
		final MutableUser result;
		if(user != null) {
			result = Users.newUser(user.getEntity(), user.getUserSyncData(), user.getProperties().getPropertiesCollection());
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
