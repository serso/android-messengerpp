package org.solovyev.android.messenger.accounts;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

import org.solovyev.android.Activities;
import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.tasks.TaskListeners;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;

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

	protected BaseAccountFragment(int layoutResId) {
		this.layoutResId = layoutResId;
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

	protected abstract CharSequence getFragmentTitle();

	@Override
	public void onPause() {
		getTaskListeners().removeAllTaskListeners();
		super.onPause();
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
}
