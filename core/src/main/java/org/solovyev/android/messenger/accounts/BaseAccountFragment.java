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
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MessengerMultiPaneManager;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.tasks.TaskListeners;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.tasks.TaskService;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.solovyev.android.messenger.App.getExceptionHandler;

public abstract class BaseAccountFragment<A extends Account<?>> extends RoboSherlockFragment {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/
	
	@Nonnull
	public static final String ARG_ACCOUNT_ID = "account_id";
	
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
	private MessengerMultiPaneManager multiPaneManager;

	@Inject
	@Nonnull
	private EventManager eventManager;

	@Inject
	@Nonnull
	private TaskService taskService;

	/*
	**********************************************************************
	*
	*                           VIEWS
	*
	**********************************************************************
	*/

	@Nonnull
	private Button backButton;

	@Nonnull
	private Button saveButton;

	@Nonnull
	private Button removeButton;
	
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
	private final TaskListeners taskListeners = new TaskListeners(App.getTaskService());

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

		backButton = (Button) root.findViewById(R.id.mpp_account_back_button);

		if (isBackButtonVisible()) {
			backButton.setVisibility(VISIBLE);
			backButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackButtonPressed();
				}
			});
		} else {
			backButton.setVisibility(GONE);
		}

		removeButton = (Button) root.findViewById(R.id.mpp_account_remove_button);
		if (isRemoveButtonVisible()) {
			removeButton.setVisibility(VISIBLE);
			removeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onRemoveButtonPressed();
				}
			});
		} else {
			removeButton.setVisibility(GONE);
		}


		saveButton = (Button) root.findViewById(R.id.mpp_account_save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSaveButtonPressed();
			}
		});

		final TextView fragmentTitle = (TextView) root.findViewById(R.id.mpp_fragment_title);
		fragmentTitle.setText(getFragmentTitle());

		getMultiPaneManager().onPaneCreated(getActivity(), root);
	}

	protected abstract boolean isRemoveButtonVisible();

	protected abstract void onRemoveButtonPressed();

	protected abstract boolean isBackButtonVisible();

	protected abstract CharSequence getFragmentTitle();

	protected abstract void onSaveButtonPressed();

	protected abstract void onBackButtonPressed();

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
	protected MessengerMultiPaneManager getMultiPaneManager() {
		return multiPaneManager;
	}

	@Nonnull
	protected EventManager getEventManager() {
		return eventManager;
	}

	@Nonnull
	protected TaskService getTaskService() {
		return taskService;
	}

	public A getAccount() {
		return account;
	}

	@Nonnull
	public Context getThemeContext() {
		return themeContext;
	}

	@Nonnull
	protected TaskListeners getTaskListeners() {
		return taskListeners;
	}
}
