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
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.solovyev.android.Activities;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.Threads2;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.tasks.TaskListeners;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.listeners.AbstractJEventListener;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.solovyev.android.messenger.App.getExceptionHandler;
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

	protected BaseAccountFragment(int layoutResId) {
		this.layoutResId = layoutResId;
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
				try {
					account = (A) accountService.getAccountById(accountId);

					accountEventListener = new AccountEventListener();
					accountService.addListener(accountEventListener);
				} catch (UnsupportedAccountException e) {
					getExceptionHandler().handleException(e);
					Activities.restartActivity(getActivity());
				}
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
		final View result = ViewFromLayoutBuilder.newInstance(layoutResId).build(themeContext);

		getMultiPaneManager().onCreatePane(this.getActivity(), container, result);

		result.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

		return result;
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final TextView fragmentTitle = (TextView) root.findViewById(R.id.mpp_fragment_title);
		fragmentTitle.setText(getFragmentTitle());

		getMultiPaneManager().onPaneCreated(getActivity(), root);
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
