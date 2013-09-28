package org.solovyev.android.messenger.accounts;


import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TestAccountBuilder extends AbstractAccountBuilder<TestAccountConfiguration> {

	public TestAccountBuilder(@Nonnull Realm realm, @Nonnull TestAccountConfiguration configuration, @Nullable Account editedAccount) {
		super(realm, configuration, editedAccount);
	}

	@Nonnull
	@Override
	protected User getAccountUser(@Nonnull String accountId) {
		return Users.newEmptyUser(EntityImpl.newEntity(accountId, "test_user"));
	}

	@Nonnull
	@Override
	protected Account newRealm(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state) {
		return new TestAccount(id, getRealm(), user, getConfiguration(), state);
	}

	@Override
	public void connect() throws ConnectionException {

	}

	@Override
	public void disconnect() throws ConnectionException {

	}

	@Override
	public void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {

	}
}
