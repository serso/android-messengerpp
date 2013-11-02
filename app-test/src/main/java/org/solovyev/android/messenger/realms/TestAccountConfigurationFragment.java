package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment;

import com.google.inject.Inject;

public class TestAccountConfigurationFragment extends BaseAccountConfigurationFragment<TestAccount> {

	@Inject
	@Nonnull
	private TestRealm realm;

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
		return realm;
	}
}
