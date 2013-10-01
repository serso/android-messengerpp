package org.solovyev.android.messenger;

import com.actionbarsherlock.app.ActionBar;

import android.widget.Toast;
import roboguice.event.EventListener;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountUiEvent;
import org.solovyev.android.messenger.accounts.MessengerPickAccountFragment;

import static android.widget.Toast.LENGTH_LONG;
import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.App.getApplication;
import static org.solovyev.android.messenger.fragments.MessengerPrimaryFragment.realms;
import static org.solovyev.android.messenger.users.Users.CONTACTS_FRAGMENT_TAG;

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
			activity.getListeners().remove(AccountUiEvent.class, accountEventListener);
			activity.getListeners().add(AccountUiEvent.class, accountEventListener);
			activity.getMultiPaneFragmentManager().setSecondOrMainFragment(MessengerPickAccountFragment.class, MessengerPickAccountFragment.createArguments(accounts), MessengerPickAccountFragment.FRAGMENT_TAG, true);
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

	private static class AccountUiEventListener implements EventListener<AccountUiEvent> {
		@Override
		public void onEvent(AccountUiEvent event) {
			switch (event.getType()){
				case account_picked:
					Toast.makeText(getApplication(), "Account picked: " + event.getAccount().getDisplayName(getApplication()), LENGTH_LONG).show();
					break;
			}
		}
	}
}
