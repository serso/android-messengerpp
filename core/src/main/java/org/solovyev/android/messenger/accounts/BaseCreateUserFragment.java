package org.solovyev.android.messenger.accounts;

import com.google.inject.Inject;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Arrays.asList;

public abstract class BaseCreateUserFragment<A extends Account<?>> extends BaseAccountFragment<A> {

	@Inject
	@Nonnull
	private UserService userService;

	protected BaseCreateUserFragment(int layoutResId) {
		super(layoutResId);
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
		final User contact = validateData();
		if (contact != null) {
			userService.mergeUserContacts(getAccount().getUser().getEntity(), asList(contact), false, true);
		}
	}

	@Nullable
	protected abstract User validateData();

	@Override
	protected void onBackButtonPressed() {

	}

	@Override
	protected boolean isRemoveButtonVisible() {
		return false;
	}

	@Override
	protected void onRemoveButtonPressed() {

	}
}
