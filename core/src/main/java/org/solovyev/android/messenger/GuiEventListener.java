package org.solovyev.android.messenger;

import org.solovyev.android.messenger.fragments.MessengerPrimaryFragment;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/8/13
 * Time: 10:35 PM
 */
public class GuiEventListener implements EventListener<GuiEvent> {

	@Nonnull
	private final MessengerMainActivity activity;

	public GuiEventListener(@Nonnull MessengerMainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onEvent(@Nonnull GuiEvent event) {
		switch (event.getType()) {
			case show_realm_defs:
				handleShowRealmDefsEvent();
				break;
		}
	}

	private void handleShowRealmDefsEvent() {
		activity.getMultiPaneFragmentManager().setMainFragment(MessengerPrimaryFragment.realm_defs);
	}
}
