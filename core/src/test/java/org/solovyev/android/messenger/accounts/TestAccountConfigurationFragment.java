package org.solovyev.android.messenger.accounts;

import com.google.inject.Inject;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.TestRealm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	public AccountConfiguration validateData() {
		return new TestAccountConfiguration("test", 42);
	}

	@Nonnull
	@Override
	public Realm getRealm() {
		return realmDef;
	}
}
