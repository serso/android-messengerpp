package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractRealmBuilder<C extends AccountConfiguration> implements RealmBuilder {

	@Nonnull
	private RealmDef realmDef;

	@Nonnull
	private C configuration;

	@Nullable
	private Realm editedRealm;

	protected AbstractRealmBuilder(@Nonnull RealmDef realmDef,
								   @Nonnull C configuration,
								   @Nullable Realm editedRealm) {
		this.realmDef = realmDef;
		this.editedRealm = editedRealm;
		this.configuration = configuration;
	}

	@Nonnull
	public C getConfiguration() {
		return configuration;
	}

	@Nonnull
	@Override
	public final Realm build(@Nonnull Data data) {
		final String realmId = data.getRealmId();

		final User user = getRealmUser(realmId);

		return newRealm(realmId, user, RealmState.enabled);
	}

	@Nonnull
	protected abstract User getRealmUser(@Nonnull String realmId);

	@Nonnull
	public RealmDef getRealmDef() {
		return realmDef;
	}

	@Nullable
	@Override
	public Realm getEditedRealm() {
		return this.editedRealm;
	}

	@Nonnull
	protected abstract Realm newRealm(@Nonnull String id, @Nonnull User user, @Nonnull RealmState state);
}
