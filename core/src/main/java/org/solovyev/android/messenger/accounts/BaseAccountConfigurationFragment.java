package org.solovyev.android.messenger.accounts;

import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.accounts.tasks.AccountSaverCallable;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.accounts.tasks.AccountRemoverListener.newAccountRemoverListener;
import static org.solovyev.android.messenger.accounts.tasks.AccountSaverListener.newAccountSaverListener;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.FinishedState.back;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_edit_finished;
import static org.solovyev.android.messenger.realms.RealmUiEventType.realm_edit_finished;

public abstract class BaseAccountConfigurationFragment<A extends Account<?>> extends BaseAccountFragment<A> {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	@Nonnull
	public static final String FRAGMENT_TAG = "account-configuration";

	@Nonnull
	private static final String TAG = "AccountConfiguration";

    /*
    **********************************************************************
    *
    *                           VIEWS
    *
    **********************************************************************
    */

	protected BaseAccountConfigurationFragment(int layoutResId) {
		super(layoutResId);
	}

	@Override
	public void onResume() {
		super.onResume();

		getTaskListeners().addTaskListener(AccountSaverCallable.TASK_NAME, newAccountSaverListener(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
		getTaskListeners().addTaskListener(AccountRemoverCallable.TASK_NAME, newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
	}

	public A getEditedAccount() {
		return getAccount();
	}

	public boolean isNewAccount() {
		return getEditedAccount() == null;
	}

	@Override
	protected boolean isRemoveButtonVisible() {
		return !isNewAccount();
	}

	@Override
	protected void onRemoveButtonPressed() {
		removeAccount(getEditedAccount());
	}

	protected final void removeAccount(@Nonnull Account account) {
		getTaskListeners().run(AccountRemoverCallable.TASK_NAME, new AccountRemoverCallable(account), newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
	}

	private void saveAccount(@Nonnull AccountBuilder accountBuilder) {
		getTaskListeners().run(AccountSaverCallable.TASK_NAME, new AccountSaverCallable(accountBuilder), newAccountSaverListener(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
	}

	protected final void onSaveButtonPressed() {
		final AccountConfiguration configuration = validateData();
		if (configuration != null) {
			final AccountBuilder accountBuilder = getRealm().newAccountBuilder(configuration, getEditedAccount());
			saveAccount(accountBuilder);
		}
	}

	@Nullable
	protected abstract AccountConfiguration validateData();

	protected void onBackButtonPressed() {
		A editedRealm = getEditedAccount();
		if (editedRealm != null) {
			getEventManager().fire(account_edit_finished.newEvent(editedRealm, back));
		} else {
			getEventManager().fire(realm_edit_finished.newEvent(getRealm()));
		}
	}

	@Nonnull
	public abstract Realm getRealm();

	@Override
	protected boolean isBackButtonVisible() {
		// in multi pane layout we don't want to show 'Back' button as there is no 'Back' (in one pane we reuse pane for showing more than one fragment and back means to return to the previous fragment)
		return !(isNewAccount() && getMultiPaneManager().isDualPane(getActivity()));
	}

	@Nonnull
	protected CharSequence getFragmentTitle() {
		final String realmName = getString(getRealm().getNameResId());
		return getString(R.string.mpp_realm_configuration, realmName);
	}

}
