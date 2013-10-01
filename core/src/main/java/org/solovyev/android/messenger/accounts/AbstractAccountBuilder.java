package org.solovyev.android.messenger.accounts;

import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractAccountBuilder<A extends Account<C>, C extends AccountConfiguration> implements AccountBuilder<A> {

	@Nonnull
	private Realm realm;

	@Nonnull
	private C configuration;

	@Nullable
	private A editedAccount;

	protected AbstractAccountBuilder(@Nonnull Realm realm,
									 @Nonnull C configuration,
									 @Nullable A editedAccount) {
		this.realm = realm;
		this.editedAccount = editedAccount;
		this.configuration = configuration;
	}

	@Nonnull
	public C getConfiguration() {
		return configuration;
	}

	@Nonnull
	@Override
	public final A build(@Nonnull Data data) {
		final String accountId = data.getAccountId();

		final User user = getAccountUser(accountId);

		return newAccount(accountId, user, AccountState.enabled);
	}

	@Nonnull
	protected abstract User getAccountUser(@Nonnull String accountId);

	@Nonnull
	public Realm getRealm() {
		return realm;
	}

	@Nullable
	@Override
	public A getEditedAccount() {
		return this.editedAccount;
	}

	@Nonnull
	protected abstract A newAccount(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state);
}
