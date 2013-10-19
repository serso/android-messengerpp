package org.solovyev.android.messenger.accounts;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.EditButtons;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.accounts.tasks.AccountSaverCallable;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmFragmentReuseCondition;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.accounts.tasks.AccountRemoverListener.newAccountRemoverListener;
import static org.solovyev.android.messenger.accounts.tasks.AccountSaverListener.newAccountSaverListener;

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

	@Nonnull
	private final EditButtons buttons = new AccountEditButtons<A>(this);

	protected BaseAccountConfigurationFragment(int layoutResId) {
		super(layoutResId);
	}

	@Nonnull
	public static MultiPaneFragmentDef newEditAccountConfigurationFragmentDef(@Nonnull Context context,
																			  @Nonnull Account account,
																			  boolean addToBackStack) {
		final Realm realm = account.getRealm();
		final JPredicate<Fragment> reuseCondition = new RealmFragmentReuseCondition(realm);
		final Bundle args = newAccountArguments(account);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, realm.getConfigurationFragmentClass(), context, args, reuseCondition);
	}

	@Nonnull
	public static MultiPaneFragmentDef newCreateAccountConfigurationFragmentDef(@Nonnull Context context,
																				@Nonnull Realm realm,
																				boolean addToBackStack) {
		final JPredicate<Fragment> reuseCondition = new RealmFragmentReuseCondition(realm);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, realm.getConfigurationFragmentClass(), context, null, reuseCondition);
	}


	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		buttons.onViewCreated(root, savedInstanceState);
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

	protected final void removeAccount(@Nonnull Account account) {
		getTaskListeners().run(AccountRemoverCallable.TASK_NAME, new AccountRemoverCallable(account), newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
	}

	public void saveAccount(@Nonnull AccountBuilder accountBuilder) {
		getTaskListeners().run(AccountSaverCallable.TASK_NAME, new AccountSaverCallable(accountBuilder), newAccountSaverListener(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
	}

	@Nullable
	public abstract AccountConfiguration validateData();

	@Nonnull
	public abstract Realm getRealm();

	@Nonnull
	protected CharSequence getFragmentTitle() {
		final String realmName = getString(getRealm().getNameResId());
		return getString(R.string.mpp_realm_configuration, realmName);
	}

}
