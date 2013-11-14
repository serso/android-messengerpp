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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.Threads2;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.tasks.TaskListeners;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.listeners.AbstractJEventListener;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.solovyev.android.messenger.App.getTaskService;

public abstract class BaseAccountFragment<A extends Account<?>> extends RoboSherlockFragment {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/
	
	@Nonnull
	protected static final String ARG_ACCOUNT_ID = "account_id";
	
	/*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private MultiPaneManager multiPaneManager;

	@Inject
	@Nonnull
	private EventManager eventManager;
	
	/*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	private A account;

	private final int layoutResId;

	@Nonnull
	private Context themeContext;

	@Nonnull
	private final TaskListeners taskListeners = new TaskListeners(getTaskService());

	@Nullable
	private AccountEventListener accountEventListener;

	private final boolean addPadding;

	protected BaseAccountFragment(int layoutResId, boolean addPadding) {
		this.layoutResId = layoutResId;
		this.addPadding = addPadding;
	}

	public BaseFragmentActivity getFragmentActivity() {
		return (BaseFragmentActivity) super.getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments != null) {
			final String accountId = arguments.getString(ARG_ACCOUNT_ID);
			if (accountId != null) {
				account = (A) accountService.getAccountById(accountId);

				accountEventListener = new AccountEventListener();
				accountService.addListener(accountEventListener);
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		themeContext = new ContextThemeWrapper(activity, R.style.mpp_theme_metro_fragment);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = ViewFromLayoutBuilder.newInstance(layoutResId).build(themeContext);

		getMultiPaneManager().onCreatePane(this.getActivity(), container, root);

		if (addPadding) {
			final int padding = getThemeContext().getResources().getDimensionPixelSize(R.dimen.mpp_fragment_padding);
			root.setPadding(padding, 0, padding, 0);
		}

		root.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

		return root;
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		getMultiPaneManager().showTitle(getSherlockActivity(), root, getFragmentTitle());
	}

	@Nullable
	protected abstract CharSequence getFragmentTitle();

	protected void onAccountStateChanged(@Nonnull View root) {
		final Button syncButton = (Button) root.findViewById(R.id.mpp_account_sync_button);
		final Button changeStateButton = (Button) root.findViewById(R.id.mpp_account_state_button);
		if (getAccount().isEnabled()) {
			changeStateButton.setText(R.string.mpp_disable);
			syncButton.setVisibility(View.VISIBLE);
		} else {
			changeStateButton.setText(R.string.mpp_enable);
			syncButton.setVisibility(View.GONE);
		}
	}


	@Override
	public void onPause() {
		getTaskListeners().removeAllTaskListeners();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (accountEventListener != null) {
			accountService.removeListener(accountEventListener);
			accountEventListener = null;
		}

		super.onDestroy();
	}

	@Nonnull
	protected AccountService getAccountService() {
		return accountService;
	}

	@Nonnull
	public MultiPaneManager getMultiPaneManager() {
		return multiPaneManager;
	}

	@Nonnull
	protected EventManager getEventManager() {
		return eventManager;
	}

	public A getAccount() {
		return account;
	}

	@Nonnull
	public Context getThemeContext() {
		return themeContext;
	}

	@Nonnull
	public TaskListeners getTaskListeners() {
		return taskListeners;
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER
	*
	**********************************************************************
	*/

	@Nonnull
	protected static Bundle newAccountArguments(@Nonnull Account account) {
		final Bundle result = new Bundle();
		result.putString(ARG_ACCOUNT_ID, account.getId());
		return result;
	}

	private final class AccountEventListener extends AbstractJEventListener<AccountEvent> {

		protected AccountEventListener() {
			super(AccountEvent.class);
		}

		@Override
		public void onEvent(@Nonnull AccountEvent event) {
			final Account eventAccount = event.getAccount();
			switch (event.getType()) {
				case changed:
					if (eventAccount.equals(account)) {
						account = (A) eventAccount;
					}
					break;
				case state_changed:
					if (eventAccount.equals(account)) {
						account = (A) eventAccount;
						Threads2.tryRunOnUiThread(BaseAccountFragment.this, new Runnable() {
							@Override
							public void run() {
								final View view = getView();
								if (view != null) {
									onAccountStateChanged(view);
								}
							}
						});
					}
					break;
			}
		}
	}
}
