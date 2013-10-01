package org.solovyev.android.messenger;

import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountUiEvent;
import org.solovyev.android.messenger.accounts.MessengerPickAccountFragment;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.realms.Realm;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import java.util.Collection;

import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.accounts.BaseAccountFragment.ARG_ACCOUNT_ID;
import static org.solovyev.android.messenger.fragments.MessengerPrimaryFragment.pick_account;
import static org.solovyev.android.messenger.fragments.MessengerPrimaryFragment.realms;
import static org.solovyev.android.messenger.users.Users.CONTACTS_FRAGMENT_TAG;
import static org.solovyev.android.messenger.users.Users.CREATE_USER_FRAGMENT_TAG;

/**
 * User: serso
 * Date: 3/8/13
 * Time: 10:35 PM
 */
public class UiEventListener implements EventListener<UiEvent> {

	@Nonnull
	private final MessengerMainActivity activity;

	@Nonnull
	private final EventListener<AccountUiEvent> accountEventListener = new AccountUiEventListener();

	public UiEventListener(@Nonnull MessengerMainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onEvent(@Nonnull UiEvent event) {
		switch (event.getType()) {
			case show_realms:
				onShowRealmsEvent();
				break;
			case new_message:
				onNewMessageEvent();
				break;
			case new_contact:
				onNewContactEvent();
				break;
		}
	}

	private void onNewContactEvent() {
		final Collection<Account> accounts = getAccountService().getAccountsCreatingUsers();
		final int size = accounts.size();
		if(size > 0) {
			final RoboListeners listeners = activity.getListeners();
			listeners.remove(AccountUiEvent.class, accountEventListener);
			listeners.add(AccountUiEvent.class, accountEventListener);
			activity.getMultiPaneFragmentManager().setMainFragment(pick_account, MessengerPickAccountFragment.createArguments(accounts));
		}
	}

	private void onNewMessageEvent() {
		final ActionBar.Tab tab = activity.findTabByTag(CONTACTS_FRAGMENT_TAG);
		if(tab != null) {
			tab.select();
		}
	}

	private void onShowRealmsEvent() {
		activity.getMultiPaneFragmentManager().setMainFragment(realms);
	}

	private class AccountUiEventListener implements EventListener<AccountUiEvent> {
		@Override
		public void onEvent(AccountUiEvent event) {
			switch (event.getType()){
				case account_picked:
					onAccountPicked(event.getAccount());
					break;
			}
		}

		private void onAccountPicked(@Nonnull Account account) {
			final Realm realm = account.getRealm();
			if(realm.canCreateUsers()) {
				final Bundle fragmentArgs = new Bundle();
				fragmentArgs.putString(ARG_ACCOUNT_ID, account.getId());
				final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();
				fm.setSecondOrMainFragment(realm.getCreateUserFragmentClass(), fragmentArgs, CREATE_USER_FRAGMENT_TAG);
			}
		}
	}
}
