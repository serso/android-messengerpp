package org.solovyev.android.messenger.accounts;

import android.content.Context;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.users.CompositeUser;
import org.solovyev.android.messenger.users.CompositeUserChoice;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class AbstractAccount<C extends AccountConfiguration> extends JObject implements Account<C> {

	@Nonnull
	private String id;

	@Nonnull
	private RealmDef realmDef;

	@Nonnull
	private User user;

	@Nonnull
	private C configuration;

	@Nonnull
	private AccountState state;

	/**
	 * Last created realm connection
	 */
	@Nullable
	private volatile AccountConnection accountConnection;

	public AbstractAccount(@Nonnull String id,
						   @Nonnull RealmDef realmDef,
						   @Nonnull User user,
						   @Nonnull C configuration,
						   @Nonnull AccountState state) {
		if (!user.getEntity().getAccountId().equals(id)) {
			throw new IllegalArgumentException("User must belong to realm!");
		}

		this.id = id;
		this.realmDef = realmDef;
		this.user = user;
		this.configuration = configuration;
		this.state = state;
	}

	@Nonnull
	@Override
	public final String getId() {
		return this.id;
	}

	@Nonnull
	@Override
	public final RealmDef getRealmDef() {
		return realmDef;
	}

	@Nonnull
	@Override
	public final User getUser() {
		return this.user;
	}

	@Nonnull
	@Override
	public final C getConfiguration() {
		return this.configuration;
	}

	@Nonnull
	@Override
	public final AccountState getState() {
		return state;
	}

	@Override
	public boolean isEnabled() {
		return state == AccountState.enabled;
	}

	@Nonnull
	@Override
	public Entity newRealmEntity(@Nonnull String realmEntityId) {
		return EntityImpl.newInstance(getId(), realmEntityId);
	}

	@Nonnull
	@Override
	public Entity newRealmEntity(@Nonnull String realmEntityId, @Nonnull String entityId) {
		return EntityImpl.newInstance(getId(), realmEntityId, entityId);
	}

	@Nonnull
	@Override
	public Entity newUserEntity(@Nonnull String realmUserId) {
		return newRealmEntity(realmUserId);
	}

	@Nonnull
	@Override
	public Entity newChatEntity(@Nonnull String realmUserId) {
		return newRealmEntity(realmUserId);
	}

	@Nonnull
	@Override
	public Entity newMessageEntity(@Nonnull String realmMessageId) {
		return newRealmEntity(realmMessageId);
	}

	@Nonnull
	@Override
	public Entity newMessageEntity(@Nonnull String realmMessageId, @Nonnull String entityId) {
		return newRealmEntity(realmMessageId, entityId);
	}

	@Nonnull
	@Override
	public final Account copyForNewState(@Nonnull AccountState newState) {
		final AbstractAccount clone = clone();
		clone.state = newState;
		return clone;
	}

	@Nonnull
	@Override
	public AbstractAccount clone() {
		final AbstractAccount clone = (AbstractAccount) super.clone();

		clone.user = this.user.clone();
		clone.configuration = this.configuration.clone();

		return clone;
	}

	@Nonnull
	@Override
	public final synchronized AccountConnection newRealmConnection(@Nonnull Context context) {
		final AccountConnection accountConnection = newRealmConnection0(context);
		this.accountConnection = accountConnection;
		return accountConnection;
	}

	@Nonnull
	protected abstract AccountConnection newRealmConnection0(@Nonnull Context context);

	@Nullable
	protected synchronized AccountConnection getAccountConnection() {
		return accountConnection;
	}

	@Override
	public boolean same(@Nonnull Account r) {
		if (r instanceof AbstractAccount) {
			final AbstractAccount that = (AbstractAccount) r;
			return this.configuration.equals(that.configuration);
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AbstractAccount)) return false;

		final AbstractAccount that = (AbstractAccount) o;

		if (!id.equals(that.id)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean isCompositeUser(@Nonnull User user) {
		return user instanceof CompositeUser;
	}

	@Override
	public boolean isCompositeUserDefined(@Nonnull User user) {
		return ((CompositeUser) user).isDefined();
	}

	@Nonnull
	@Override
	public List<CompositeUserChoice> getCompositeUserChoices(@Nonnull User user) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public User applyCompositeChoice(@Nonnull CompositeUserChoice compositeUserChoice, @Nonnull User user) {
		return user;
	}

	@Override
	public boolean isCompositeUserChoicePersisted() {
		return false;
	}

	@Override
	public int getCompositeDialogTitleResId() {
		// todo serso: set proper title
		return 0;
	}
}
