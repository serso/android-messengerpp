/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.accounts;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import com.actionbarsherlock.app.ActionBar;
import com.google.inject.Inject;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.EmptyFutureCallback;
import org.solovyev.android.messenger.accounts.connection.AccountConnectionsService;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.accounts.tasks.AccountSaverCallable;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmFragmentReuseCondition;
import org.solovyev.android.network.NetworkData;
import org.solovyev.android.network.NetworkStateListener;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.actionbarsherlock.app.ActionBar.*;
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
	*                           AUTO INJECTED FIELDS
	*
	**********************************************************************
	*/

	@Inject
	@Nonnull
	private NetworkStateService networkStateService;

	@Inject
	@Nonnull
	private AccountConnectionsService accountConnectionsService;

    /*
	**********************************************************************
    *
    *                           VIEWS
    *
    **********************************************************************
    */

	@Nonnull
	private final AccountEditButtons buttons = new AccountEditButtons<A>(this);

	@Nullable
	private NetworkStateListener networkListener;

	protected BaseAccountConfigurationFragment(int layoutResId) {
		super(layoutResId);
	}

	@Nonnull
	public static MultiPaneFragmentDef newEditAccountConfigurationFragmentDef(@Nonnull Context context,
																			  @Nonnull Account account,
																			  boolean addToBackStack) {
		final Realm realm = account.getRealm();
		final JPredicate<Fragment> reuseCondition = new AccountFragmentReuseCondition(account, realm.getConfigurationFragmentClass());
		final Bundle args = Accounts.newAccountArguments(account);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, realm.getConfigurationFragmentClass(), context, args, reuseCondition);
	}

	@Nonnull
	public static MultiPaneFragmentDef newEditAccountConfigurationFragmentDef(@Nonnull Context context,
																			  @Nonnull Bundle arguments,
																			  boolean addToBackStack) {
		final String accountId = Accounts.getAccountIdFromArguments(arguments);
		final Class<? extends BaseAccountFragment> fragmentClass = Accounts.getEditAccountFragmentClassFromArguments(arguments);
		final JPredicate<Fragment> reuseCondition = new AccountFragmentReuseCondition(accountId, fragmentClass);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, fragmentClass, context, arguments, reuseCondition);
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

		prepareButtons(root);

	}

	private void prepareButtons(@Nonnull View root) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && !isDialog() && getSherlockActivity() instanceof AccountActivity && false) {
			prepareActionbarButtons(root);
		} else {
			prepareButtonsPreHoneycomb(root);
		}
	}

	private void prepareActionbarButtons(@Nonnull View root) {
		final ActionBar actionBar = getSherlockActivity().getSupportActionBar();

		final LayoutInflater inflater = LayoutInflater.from(actionBar.getThemedContext());
		final View actionBarView = inflater.inflate(R.layout.mpp_ab_done_cancel, null);
		actionBarView.findViewById(R.id.mpp_ab_done).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						buttons.onSaveButtonPressed();
					}
				});
		actionBarView.findViewById(R.id.mpp_ab_cancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						getSherlockActivity().onBackPressed();
					}
				});

		actionBar.setDisplayOptions(
				DISPLAY_SHOW_CUSTOM,
				DISPLAY_SHOW_CUSTOM | DISPLAY_SHOW_HOME | DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(MATCH_PARENT, MATCH_PARENT));

		actionBar.setDisplayHomeAsUpEnabled(false);

		buttons.hide(root);
	}

	private void prepareButtonsPreHoneycomb(@Nonnull View root) {
		buttons.onViewCreated(root);
	}

	@Override
	public void onResume() {
		super.onResume();

		final Realm realm = getRealm();
		if (realm.isInternetConnectionRequired()) {
			if (!accountConnectionsService.isInternetConnectionExists()) {
				NoInternetConnectionDialog.show(getActivity());
			} else {
				networkListener = new AccountNetworkStateListener();
				networkStateService.addListener(networkListener);
			}
		}


		getTaskListeners().addTaskListener(AccountSaverCallable.TASK_NAME, newAccountSaverListener(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
		getTaskListeners().addTaskListener(AccountRemoverCallable.TASK_NAME, EmptyFutureCallback.instance, getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
	}

	@Override
	public void onPause() {
		if (networkListener != null) {
			networkStateService.removeListener(networkListener);
			networkListener = null;
		}

		super.onPause();
	}

	public A getEditedAccount() {
		return getAccount();
	}

	public boolean isNewAccount() {
		return getEditedAccount() == null;
	}

	@Nullable
	public abstract AccountConfiguration validateData();

	@Nonnull
	public abstract Realm getRealm();

	@Nonnull
	protected CharSequence getFragmentTitle() {
		final String realmName = getString(getRealm().getNameResId());
		return getString(R.string.mpp_account_configuration, realmName);
	}

	private class AccountNetworkStateListener implements NetworkStateListener {
		@Override
		public void onNetworkEvent(@Nonnull NetworkData networkData) {
			switch (networkData.getState()) {
				case UNKNOWN:
				case NOT_CONNECTED:
					NoInternetConnectionDialog.show(getActivity());
					break;
			}
		}
	}
}
