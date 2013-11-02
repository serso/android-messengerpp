package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment.newCreateAccountConfigurationFragmentDef;

/**
 * User: serso
 * Date: 3/8/13
 * Time: 11:46 AM
 */
public class RealmUiEventListener implements EventListener<RealmUiEvent> {

	@Nonnull
	private BaseFragmentActivity activity;

	public RealmUiEventListener(@Nonnull BaseFragmentActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onEvent(@Nonnull RealmUiEvent event) {
		final Realm realm = event.getRealm();

		final MessengerMultiPaneFragmentManager mpfm = activity.getMultiPaneFragmentManager();
		switch (event.getType()) {
			case realm_clicked:
				if (activity.isDualPane()) {
					mpfm.setSecondFragment(newCreateAccountConfigurationFragmentDef(activity, realm, false));
				} else {
					mpfm.setMainFragment(newCreateAccountConfigurationFragmentDef(activity, realm, true));
				}
				break;
			case realm_edit_finished:
				mpfm.goBack();
				break;
		}
	}
}
