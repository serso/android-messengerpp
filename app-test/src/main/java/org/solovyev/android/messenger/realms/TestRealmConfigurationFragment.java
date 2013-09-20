package org.solovyev.android.messenger.realms;

import com.google.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 9:15 PM
 */
public class TestRealmConfigurationFragment extends BaseRealmConfigurationFragment<TestRealm> {

	@Inject
	@Nonnull
	private TestRealmDef realmDef;

	public TestRealmConfigurationFragment() {
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
