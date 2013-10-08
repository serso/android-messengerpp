package org.solovyev.android.messenger.realms;

import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;

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
		final Realm realm = event.getRealmDef();

		switch (event.getType()) {
			case realm_clicked:
				// todo serso: reuse condition
				// in case of creation of new account old accounts should not be used. For now - just recreate fragment (reuse condition is null)
				if (activity.isDualPane()) {
					activity.getMultiPaneFragmentManager().setSecondFragment(realm.getConfigurationFragmentClass(), null, null, BaseAccountConfigurationFragment.FRAGMENT_TAG, false);
				} else {
					activity.getMultiPaneFragmentManager().setMainFragment(realm.getConfigurationFragmentClass(), null, null, BaseAccountConfigurationFragment.FRAGMENT_TAG, true);
				}
				break;
			case realm_edit_finished:
				activity.getMultiPaneFragmentManager().goBack();
				break;
		}
	}
}
