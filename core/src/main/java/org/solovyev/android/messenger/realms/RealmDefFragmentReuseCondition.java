package org.solovyev.android.messenger.realms;

import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 9:32 PM
 */
public class RealmDefFragmentReuseCondition implements JPredicate<Fragment> {

	@Nonnull
	private final Realm realm;

	public RealmDefFragmentReuseCondition(@Nonnull Realm realm) {
		this.realm = realm;
	}

	@Override
	public boolean apply(@Nullable Fragment fragment) {
		if (fragment instanceof BaseAccountConfigurationFragment) {
			final BaseAccountConfigurationFragment oldRealmFragment = ((BaseAccountConfigurationFragment) fragment);
			if (realm.equals(oldRealmFragment.getRealmDef())) {
				// do nothing - configuration fragment for this item is already opened
				return true;
			}
		}

		return false;
	}
}
