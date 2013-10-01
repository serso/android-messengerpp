package org.solovyev.android.messenger;

import com.actionbarsherlock.app.ActionBar;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

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
		// todo serso:
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
