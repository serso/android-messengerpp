package org.solovyev.android.messenger.accounts;

import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractAccountBuilder<C extends AccountConfiguration> implements AccountBuilder {

	@Nonnull
	private Realm realm;

	@Nonnull
	private C configuration;

	@Nullable
	private Account editedAccount;

	protected AbstractAccountBuilder(@Nonnull Realm realm,
									 @Nonnull C configuration,
									 @Nullable Account editedAccount) {
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
	public final Account build(@Nonnull Data data) {
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
	public Account getEditedAccount() {
		return this.editedAccount;
	}

	@Nonnull
	protected abstract Account newAccount(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state);
}
