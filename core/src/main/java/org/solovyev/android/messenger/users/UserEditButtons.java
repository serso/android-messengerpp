package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.EditButtons;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.tasks.UserSaverCallable;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.PrimaryFragment;

import static org.solovyev.android.messenger.accounts.tasks.UserSaverCallback.newUserSaverCallback;

public class UserEditButtons<A extends Account<?>> extends EditButtons<BaseEditUserFragment<A>> {

	public UserEditButtons(@Nonnull BaseEditUserFragment<A> fragment) {
		super(fragment);
	}

	@Override
	protected void onBackButtonPressed() {
		getActivity().getMultiPaneFragmentManager().setMainFragment(PrimaryFragment.contacts);
	}

	@Override
	protected boolean isRemoveButtonVisible() {
		final User user = getFragment().getUser();
		if (user != null) {
			final A account = getFragment().getAccount();
			final boolean accountUser = account.getUser().equals(user);
			return !accountUser;
		} else {
			return false;
		}
	}

	@Override
	protected void onRemoveButtonPressed() {
		App.getUserService().removeUser(getFragment().getUser());
		getActivity().getSupportFragmentManager().popBackStack();
	}

	@Override
	protected boolean isBackButtonVisible() {
		// in multi pane layout we don't want to show 'Back' button as there is no 'Back' (in one pane we reuse pane for showing more than one fragment and back means to return to the previous fragment)
		return !(getFragment().isNewUser() && getFragment().getMultiPaneManager().isDualPane(getActivity()));
	}

	@Override
	protected void onSaveButtonPressed() {
		final MutableUser contact = getFragment().validateData();
		if (contact != null) {
			getFragment().getTaskListeners().run(UserSaverCallable.TASK_NAME, new UserSaverCallable(getFragment().getAccount(), contact), newUserSaverCallback(getActivity()), getActivity(), R.string.mpp_saving_user_title, R.string.mpp_saving_user_message);
		}
	}
}
