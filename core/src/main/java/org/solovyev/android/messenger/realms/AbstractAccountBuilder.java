package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractAccountBuilder<C extends AccountConfiguration> implements AccountBuilder {

	@Nonnull
	private RealmDef realmDef;

	@Nonnull
	private C configuration;

	@Nullable
	private Account editedAccount;

	protected AbstractAccountBuilder(@Nonnull RealmDef realmDef,
									 @Nonnull C configuration,
									 @Nullable Account editedAccount) {
		this.realmDef = realmDef;
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
		final String realmId = data.getRealmId();

		final User user = getRealmUser(realmId);

		return newRealm(realmId, user, AccountState.enabled);
	}

	@Nonnull
	protected abstract User getRealmUser(@Nonnull String realmId);

	@Nonnull
	public RealmDef getRealmDef() {
		return realmDef;
	}

	@Nullable
	@Override
	public Account getEditedAccount() {
		return this.editedAccount;
	}

	@Nonnull
	protected abstract Account newRealm(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state);
}
