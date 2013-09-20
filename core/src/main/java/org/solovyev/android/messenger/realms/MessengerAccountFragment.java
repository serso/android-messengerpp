package org.solovyev.android.messenger.realms;

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
import org.solovyev.android.Activities;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerMultiPaneManager;
import org.solovyev.android.messenger.Threads2;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.MessengerSyncAllAsyncTask;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.tasks.TaskListeners;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.listeners.AbstractJEventListener;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/1/13
 * Time: 8:57 PM
 */
public class MessengerAccountFragment extends RoboSherlockFragment {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	@Nonnull
	public static final String ARGS_REALM_ID = "account_id";

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
	private MessengerMultiPaneManager multiPaneManager;

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
	private RealmEventListener realmEventListener;

	@Nonnull
	private final TaskListeners taskListeners = new TaskListeners(MessengerApplication.getServiceLocator().getTaskService());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments != null) {
			final String realmId = arguments.getString(ARGS_REALM_ID);
			if (realmId != null) {
				try {
					account = accountService.getAccountById(realmId);
				} catch (UnsupportedAccountException e) {
					MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
					Activities.restartActivity(getActivity());
				}
			}
		}

		if (account == null) {
			// remove fragment
			getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
		} else {
			realmEventListener = new RealmEventListener();
			accountService.addListener(realmEventListener);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View result = ViewFromLayoutBuilder.newInstance(R.layout.mpp_fragment_realm).build(this.getActivity());

		multiPaneManager.onCreatePane(this.getActivity(), container, result);

		result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		return result;
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final ImageView realmIconImageView = (ImageView) root.findViewById(R.id.mpp_realm_def_icon_imageview);
		realmIconImageView.setImageDrawable(getResources().getDrawable(account.getRealmDef().getIconResId()));

		final TextView realmNameTextView = (TextView) root.findViewById(R.id.mpp_fragment_title);
		realmNameTextView.setText(account.getDisplayName(getActivity()));

		final Button realmBackButton = (Button) root.findViewById(R.id.mpp_realm_back_button);
		realmBackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eventManager.fire(AccountGuiEventType.newAccountViewCancelledEvent(account));
			}
		});
		if (multiPaneManager.isDualPane(getActivity())) {
			// in multi pane layout we don't want to show 'Back' button as there is no 'Back' (in one pane we reuse pane for showing more than one fragment and back means to return to the previous fragment)
			realmBackButton.setVisibility(View.GONE);
		} else {
			realmBackButton.setVisibility(View.VISIBLE);
		}

		final Button realmRemoveButton = (Button) root.findViewById(R.id.mpp_realm_remove_button);
		realmRemoveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				removeRealm();
			}
		});

		final Button realmEditButton = (Button) root.findViewById(R.id.mpp_realm_edit_button);
		realmEditButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editRealm();
			}
		});

		final Button realmSyncButton = (Button) root.findViewById(R.id.mpp_realm_sync_button);
		realmSyncButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MessengerSyncAllAsyncTask.newForRealm(getActivity(), syncService, account).executeInParallel((Void) null);
			}
		});

		final Button realmStateButton = (Button) root.findViewById(R.id.mpp_realm_state_button);
		realmStateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeState();
			}
		});

		onRealmStateChanged(root);

		multiPaneManager.onPaneCreated(getActivity(), root);
	}

	@Override
	public void onResume() {
		super.onResume();

		taskListeners.addTaskListener(AccountChangeStateCallable.TASK_NAME, AccountChangeStateListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_realm_title, R.string.mpp_saving_realm_message);
		taskListeners.addTaskListener(AccountRemoverCallable.TASK_NAME, RealmRemoverListener.newInstance(getActivity()), getActivity(), R.string.mpp_removing_realm_title, R.string.mpp_removing_realm_message);
	}

	@Override
	public void onPause() {
		taskListeners.removeAllTaskListeners();

		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (realmEventListener != null) {
			accountService.removeListener(realmEventListener);
		}

		super.onDestroy();
	}

	private void onRealmStateChanged(@Nonnull View root) {
		final Button realmSyncButton = (Button) root.findViewById(R.id.mpp_realm_sync_button);
		final Button realmStateButton = (Button) root.findViewById(R.id.mpp_realm_state_button);
		if (account.isEnabled()) {
			realmStateButton.setText(R.string.mpp_disable);
			realmSyncButton.setVisibility(View.VISIBLE);
		} else {
			realmStateButton.setText(R.string.mpp_enable);
			realmSyncButton.setVisibility(View.GONE);
		}
	}

	private void changeState() {
		taskListeners.run(AccountChangeStateCallable.TASK_NAME, new AccountChangeStateCallable(account), AccountChangeStateListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_realm_title, R.string.mpp_saving_realm_message);
	}

	private void editRealm() {
		eventManager.fire(AccountGuiEventType.newAccountEditRequestedEvent(account));
	}

	@Nonnull
	public Account getAccount() {
		return account;
	}


	private void removeRealm() {
		taskListeners.run(AccountRemoverCallable.TASK_NAME, new AccountRemoverCallable(getAccount()), RealmRemoverListener.newInstance(getActivity()), getActivity(), R.string.mpp_removing_realm_title, R.string.mpp_removing_realm_message);
	}

	private final class RealmEventListener extends AbstractJEventListener<AccountEvent> {

		protected RealmEventListener() {
			super(AccountEvent.class);
		}

		@Override
		public void onEvent(@Nonnull AccountEvent event) {
			final Account eventAccount = event.getRealm();
			switch (event.getType()) {
				case changed:
					if (eventAccount.equals(account)) {
						account = eventAccount;
					}
					break;
				case state_changed:
					if (eventAccount.equals(account)) {
						account = eventAccount;
						Threads2.tryRunOnUiThread(MessengerAccountFragment.this, new Runnable() {
							@Override
							public void run() {
								final View view = getView();
								if (view != null) {
									onRealmStateChanged(view);
								}
							}
						});
					}
					break;
			}
		}
	}
}
