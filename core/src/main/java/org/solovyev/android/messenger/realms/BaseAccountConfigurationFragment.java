package org.solovyev.android.messenger.realms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.solovyev.android.Activities;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerMultiPaneManager;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.tasks.TaskListeners;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.tasks.TaskService;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseAccountConfigurationFragment<T extends Account<?>> extends RoboSherlockFragment {

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
    *                           FIELDS
    *
    **********************************************************************
    */

	private T editedRealm;

	private int layoutResId;

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

	@Nonnull
	private final TaskListeners taskListeners = new TaskListeners(MessengerApplication.getServiceLocator().getTaskService());

	protected BaseAccountConfigurationFragment(int layoutResId) {
		this.layoutResId = layoutResId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments != null) {
			final String accountId = arguments.getString(ARGS_ACCOUNT_ID);
			if (accountId != null) {
				try {
					editedRealm = (T) accountService.getAccountById(accountId);
				} catch (UnsupportedAccountException e) {
					MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
					Activities.restartActivity(getActivity());
				}
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View result = ViewFromLayoutBuilder.newInstance(layoutResId).build(this.getActivity());

		getMultiPaneManager().onCreatePane(this.getActivity(), container, result);

		result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		return result;
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		removeButton = (Button) root.findViewById(R.id.mpp_realm_conf_remove_button);
		if (isNewRealm()) {
			removeButton.setVisibility(View.GONE);
		} else {
			removeButton.setVisibility(View.VISIBLE);
			removeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					removeRealm(getEditedRealm());
				}
			});
		}

		backButton = (Button) root.findViewById(R.id.mpp_realm_conf_back_button);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				backButtonPressed();
			}
		});
		if (isNewRealm() && getMultiPaneManager().isDualPane(getActivity())) {
			// in multi pane layout we don't want to show 'Back' button as there is no 'Back' (in one pane we reuse pane for showing more than one fragment and back means to return to the previous fragment)
			backButton.setVisibility(View.GONE);
		} else {
			backButton.setVisibility(View.VISIBLE);
		}


		saveButton = (Button) root.findViewById(R.id.mpp_realm_conf_save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveRealm();
			}
		});

		final TextView fragmentTitle = (TextView) root.findViewById(R.id.mpp_fragment_title);
		fragmentTitle.setText(getFragmentTitle());

		getMultiPaneManager().onPaneCreated(getActivity(), root);
	}

	@Override
	public void onResume() {
		super.onResume();

		taskListeners.addTaskListener(AccountSaverCallable.TASK_NAME, AccountSaverListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_realm_title, R.string.mpp_saving_realm_message);
		taskListeners.addTaskListener(AccountRemoverCallable.TASK_NAME, RealmRemoverListener.newInstance(getActivity()), getActivity(), R.string.mpp_removing_realm_title, R.string.mpp_removing_realm_message);
	}

	public T getEditedRealm() {
		return editedRealm;
	}

	public boolean isNewRealm() {
		return editedRealm == null;
	}

	protected final void removeRealm(@Nonnull Account account) {
		taskListeners.run(AccountRemoverCallable.TASK_NAME, new AccountRemoverCallable(account), RealmRemoverListener.newInstance(getActivity()), getActivity(), R.string.mpp_removing_realm_title, R.string.mpp_removing_realm_message);
	}

	private void saveRealm(@Nonnull AccountBuilder accountBuilder) {
		taskListeners.run(AccountSaverCallable.TASK_NAME, new AccountSaverCallable(accountBuilder), AccountSaverListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_realm_title, R.string.mpp_saving_realm_message);
	}

	protected final void saveRealm() {
		final AccountConfiguration configuration = validateData();
		if (configuration != null) {
			final AccountBuilder accountBuilder = getRealmDef().newRealmBuilder(configuration, getEditedRealm());
			saveRealm(accountBuilder);
		}
	}

	@Nullable
	protected abstract AccountConfiguration validateData();

	@Override
	public void onPause() {
		taskListeners.removeAllTaskListeners();

		super.onPause();
	}

	protected void backButtonPressed() {
		T editedRealm = getEditedRealm();
		if (editedRealm != null) {
			eventManager.fire(AccountGuiEventType.newAccountEditFinishedEvent(editedRealm, AccountGuiEventType.FinishedState.back));
		} else {
			eventManager.fire(RealmDefGuiEventType.newRealmDefEditFinishedEvent(getRealmDef()));
		}
	}

	@Nonnull
	protected MessengerMultiPaneManager getMultiPaneManager() {
		return multiPaneManager;
	}

	@Nonnull
	public abstract RealmDef getRealmDef();

	@Nonnull
	protected CharSequence getFragmentTitle() {
		final String realmName = getString(getRealmDef().getNameResId());
		return getString(R.string.mpp_realm_configuration, realmName);
	}

}
