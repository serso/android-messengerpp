package org.solovyev.android.messenger;

import com.actionbarsherlock.app.ActionBar;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountUiEvent;
import org.solovyev.android.messenger.accounts.PickAccountFragment;
import org.solovyev.android.messenger.users.Users;

import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import java.util.Collection;

import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.fragments.PrimaryFragment.pick_account;
import static org.solovyev.android.messenger.fragments.PrimaryFragment.realms;
import static org.solovyev.android.messenger.users.Users.CONTACTS_FRAGMENT_TAG;

/**
 * User: serso
 * Date: 3/8/13
 * Time: 10:35 PM
 */
public class UiEventListener implements EventListener<UiEvent> {

	@Nonnull
	private final MainActivity activity;

	public UiEventListener(@Nonnull MainActivity activity) {
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
			activity.getMultiPaneFragmentManager().setMainFragment(pick_account, PickAccountFragment.createArguments(accounts));
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
}
