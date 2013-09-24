package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;

import com.google.inject.Inject;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 9:15 PM
 */
public class TestAccountConfigurationFragment extends BaseAccountConfigurationFragment<TestAccount> {

	@Inject
	@Nonnull
	private TestRealm realmDef;

	public TestAccountConfigurationFragment() {
		super(0);
	}

	@Nullable
	@Override
	protected AccountConfiguration validateData() {
		return new TestAccountConfiguration("test", 42);
	}

	@Nonnull
	@Override
	public Realm getRealm() {
		return realmDef;
	}
}
