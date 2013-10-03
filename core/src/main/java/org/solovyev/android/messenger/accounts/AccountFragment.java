package org.solovyev.android.messenger.accounts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.ExceptionHandler;
import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.Threads2;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverListener;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.SyncAllAsyncTask;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.tasks.TaskListeners;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.listeners.AbstractJEventListener;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.solovyev.android.Activities.restartActivity;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_edit_requested;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_view_cancelled;

/**
 * User: serso
 * Date: 3/1/13
 * Time: 8:57 PM
 */
public class AccountFragment extends RoboSherlockFragment {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	@Nonnull
	public static final String ARGS_ACCOUNT_ID = "account_id";

	@Nonnull
	public static final String FRAGMENT_TAG = "account";

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
	private SyncService syncService;

	@Inject
	@Nonnull
	private MultiPaneManager multiPaneManager;

	@Inject
	@Nonnull
	private ExceptionHandler exceptionHandler;

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

	private Account account;

	@Nullable
	private AccountEventListener accountEventListener;

	@Nonnull
	private final TaskListeners taskListeners = new TaskListeners(App.getTaskService());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments != null) {
			final String accountId = arguments.getString(ARGS_ACCOUNT_ID);
			if (accountId != null) {
				try {
					account = accountService.getAccountById(accountId);
				} catch (UnsupportedAccountException e) {
					exceptionHandler.handleException(e);
					restartActivity(getActivity());
				}
			}
		}

		if (account == null) {
			// remove fragment
			getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
		} else {
			accountEventListener = new AccountEventListener();
			accountService.addListener(accountEventListener);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View result = ViewFromLayoutBuilder.newInstance(R.layout.mpp_fragment_account).build(this.getActivity());

		multiPaneManager.onCreatePane(this.getActivity(), container, result);

		result.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

		return result;
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final ImageView realmIconImageView = (ImageView) root.findViewById(R.id.mpp_realm_icon_imageview);
		realmIconImageView.setImageDrawable(getResources().getDrawable(account.getRealm().getIconResId()));

		final TextView nameTextView = (TextView) root.findViewById(R.id.mpp_fragment_title);
		nameTextView.setText(account.getDisplayName(getActivity()));

		final Button backButton = (Button) root.findViewById(R.id.mpp_account_back_button);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eventManager.fire(account_view_cancelled.newEvent(account));
			}
		});
		if (multiPaneManager.isDualPane(getActivity())) {
			// in multi pane layout we don't want to show 'Back' button as there is no 'Back' (in one pane we reuse pane for showing more than one fragment and back means to return to the previous fragment)
			backButton.setVisibility(View.GONE);
		} else {
			backButton.setVisibility(View.VISIBLE);
		}

		final Button removeButton = (Button) root.findViewById(R.id.mpp_account_remove_button);
		removeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				removeAccount();
			}
		});

		final Button editButton = (Button) root.findViewById(R.id.mpp_account_edit_button);
		editButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editAccount();
			}
		});

		final Button syncButton = (Button) root.findViewById(R.id.mpp_account_sync_button);
		syncButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SyncAllAsyncTask.newForAccount(getActivity(), syncService, account).executeInParallel((Void) null);
			}
		});

		final Button changeStateButton = (Button) root.findViewById(R.id.mpp_account_state_button);
		changeStateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeState();
			}
		});

		onAccountStateChanged(root);

		multiPaneManager.onPaneCreated(getActivity(), root);
	}

	@Override
	public void onResume() {
		super.onResume();

		taskListeners.addTaskListener(AccountChangeStateCallable.TASK_NAME, AccountChangeStateListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
		taskListeners.addTaskListener(AccountRemoverCallable.TASK_NAME, AccountRemoverListener.newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
	}

	@Override
	public void onPause() {
		taskListeners.removeAllTaskListeners();

		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (accountEventListener != null) {
			accountService.removeListener(accountEventListener);
		}

		super.onDestroy();
	}

	private void onAccountStateChanged(@Nonnull View root) {
		final Button syncButton = (Button) root.findViewById(R.id.mpp_account_sync_button);
		final Button changeStateButton = (Button) root.findViewById(R.id.mpp_account_state_button);
		if (account.isEnabled()) {
			changeStateButton.setText(R.string.mpp_disable);
			syncButton.setVisibility(View.VISIBLE);
		} else {
			changeStateButton.setText(R.string.mpp_enable);
			syncButton.setVisibility(View.GONE);
		}
	}

	private void changeState() {
		taskListeners.run(AccountChangeStateCallable.TASK_NAME, new AccountChangeStateCallable(account), AccountChangeStateListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
	}

	private void editAccount() {
		eventManager.fire(account_edit_requested.newEvent(account));
	}

	@Nonnull
	public Account getAccount() {
		return account;
	}


	private void removeAccount() {
		taskListeners.run(AccountRemoverCallable.TASK_NAME, new AccountRemoverCallable(getAccount()), AccountRemoverListener.newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
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
						account = eventAccount;
					}
					break;
				case state_changed:
					if (eventAccount.equals(account)) {
						account = eventAccount;
						Threads2.tryRunOnUiThread(AccountFragment.this, new Runnable() {
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
