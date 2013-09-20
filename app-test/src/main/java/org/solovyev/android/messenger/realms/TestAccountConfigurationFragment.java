package org.solovyev.android.messenger.realms;

import com.google.inject.Inject;

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
	private TestRealmDef realmDef;

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
	public RealmDef getRealmDef() {
		return realmDef;
	}
}
