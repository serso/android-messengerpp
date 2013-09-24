package org.solovyev.android.messenger.realms;

import android.support.v4.app.Fragment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.common.JPredicate;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 9:32 PM
 */
public class RealmFragmentReuseCondition implements JPredicate<Fragment> {

	@Nonnull
	private final Realm realm;

	public RealmFragmentReuseCondition(@Nonnull Realm realm) {
		this.realm = realm;
	}

	@Override
	public boolean apply(@Nullable Fragment fragment) {
		if (fragment instanceof BaseAccountConfigurationFragment) {
			final BaseAccountConfigurationFragment oldRealmFragment = ((BaseAccountConfigurationFragment) fragment);
			if (realm.equals(oldRealmFragment.getRealm())) {
				// do nothing - configuration fragment for this item is already opened
				return true;
			}
		}

		return false;
	}
}
