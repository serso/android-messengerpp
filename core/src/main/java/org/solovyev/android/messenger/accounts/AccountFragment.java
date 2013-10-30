package org.solovyev.android.messenger.accounts;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.google.inject.Inject;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.ExceptionHandler;
import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverCallable;
import org.solovyev.android.messenger.accounts.tasks.AccountRemoverListener;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.sync.SyncAllAsyncTask;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.common.JPredicate;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_edit_requested;
import static org.solovyev.android.messenger.accounts.AccountUiEventType.account_view_cancelled;

/**
 * User: serso
 * Date: 3/1/13
 * Time: 8:57 PM
 */
public class AccountFragment extends BaseAccountFragment<Account<?>> {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

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

	public AccountFragment() {
		super(R.layout.mpp_fragment_account);
	}

	@Nonnull
	public static MultiPaneFragmentDef newAccountFragmentDef(@Nonnull Context context, @Nonnull Account account, boolean addToBackStack) {
		final Bundle args = newAccountArguments(account);
		final JPredicate<Fragment> reuseCondition = AccountFragmentReuseCondition.forAccount(account);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, AccountFragment.class, context, args, reuseCondition);
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final Account<?> account = getAccount();
		final Realm realm = account.getRealm();

		final ImageView realmIconImageView = (ImageView) root.findViewById(R.id.mpp_realm_icon_imageview);
		realmIconImageView.setImageDrawable(getResources().getDrawable(realm.getIconResId()));

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
		if(!realm.isEnabled()) {
			changeStateButton.setEnabled(false);
		}

		onAccountStateChanged(root);
	}

	@Override
	protected CharSequence getFragmentTitle() {
		return getAccount().getDisplayName(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();

		getTaskListeners().addTaskListener(AccountChangeStateCallable.TASK_NAME, AccountChangeStateListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
		getTaskListeners().addTaskListener(AccountRemoverCallable.TASK_NAME, AccountRemoverListener.newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
	}

	private void changeState() {
		getTaskListeners().run(AccountChangeStateCallable.TASK_NAME, new AccountChangeStateCallable(getAccount()), AccountChangeStateListener.newInstance(getActivity()), getActivity(), R.string.mpp_saving_account_title, R.string.mpp_saving_account_message);
	}

	private void editAccount() {
		eventManager.fire(account_edit_requested.newEvent(getAccount()));
	}

	private void removeAccount() {
		getTaskListeners().run(AccountRemoverCallable.TASK_NAME, new AccountRemoverCallable(getAccount()), AccountRemoverListener.newAccountRemoverListener(getActivity()), getActivity(), R.string.mpp_removing_account_title, R.string.mpp_removing_account_message);
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER
	*
	**********************************************************************
	*/

	@Nonnull
	public static Bundle newAccountArguments(@Nonnull Account account) {
		return BaseAccountFragment.newAccountArguments(account);
	}
}
