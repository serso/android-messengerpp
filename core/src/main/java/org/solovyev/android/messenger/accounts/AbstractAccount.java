/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.accounts;

import android.content.Context;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.Realm;
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
	private Realm realm;

	@Nonnull
	private User user;

	@Nonnull
	private C configuration;

	@Nonnull
	private AccountState state;

	/**
	 * Last created account connection
	 */
	@Nullable
	private volatile AccountConnection accountConnection;

	public AbstractAccount(@Nonnull String id,
						   @Nonnull Realm realm,
						   @Nonnull User user,
						   @Nonnull C configuration,
						   @Nonnull AccountState state) {
		if (!user.getEntity().getAccountId().equals(id)) {
			throw new IllegalArgumentException("User must belong to account!");
		}

		this.id = id;
		this.realm = realm;
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
	public Realm getRealm() {
		return realm;
	}

	@Nonnull
	@Override
	public final User getUser() {
		return this.user;
	}

	@Override
	public void setUser(@Nonnull User user) {
		this.user = user;
	}

	@Nonnull
	@Override
	public final C getConfiguration() {
		return this.configuration;
	}

	public void setConfiguration(@Nonnull C configuration) {
		this.configuration = configuration;
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
	public Entity newEntity(@Nonnull String accountEntityId) {
		return Entities.newEntity(getId(), accountEntityId);
	}

	@Nonnull
	@Override
	public Entity newEntity(@Nonnull String accountEntityId, @Nonnull String entityId) {
		return Entities.newEntity(getId(), accountEntityId, entityId);
	}

	@Nonnull
	@Override
	public Entity newUserEntity(@Nonnull String accountUserId) {
		return newEntity(accountUserId);
	}

	@Nonnull
	@Override
	public Entity newChatEntity(@Nonnull String accountChatId) {
		return newEntity(accountChatId);
	}

	@Nonnull
	@Override
	public Entity newMessageEntity(@Nonnull String accountMessageId) {
		return newEntity(accountMessageId);
	}

	@Nonnull
	@Override
	public Entity newMessageEntity(@Nonnull String accountMessageId, @Nonnull String entityId) {
		return newEntity(accountMessageId, entityId);
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
	public final synchronized AccountConnection newConnection(@Nonnull Context context) {
		final AccountConnection connection = createConnection(context);
		this.accountConnection = connection;
		return connection;
	}

	@Nonnull
	protected abstract AccountConnection createConnection(@Nonnull Context context);

	@Nullable
	protected synchronized AccountConnection getAccountConnection() {
		return accountConnection;
	}

	@Override
	public boolean same(@Nonnull Account r) {
		if (r instanceof AbstractAccount) {
			final AbstractAccount that = (AbstractAccount) r;
			return this.configuration.isSameAccount(that.configuration);
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

	@Override
	public final boolean isAccountUser(@Nonnull String accountUserId) {
		return getUser().getEntity().getAccountEntityId().equals(accountUserId);
	}

	@Override
	public final boolean isAccountUser(@Nonnull Entity entity) {
		return getUser().getEntity().equals(entity);
	}

	@Override
	public boolean canSendMessage(@Nonnull Chat chat) {
		return true;
	}

	@Override
	public boolean canCall(@Nonnull User contact) {
		return false;
	}

	@Override
	public void call(@Nonnull User contact, @Nonnull Context context) {
	}
}
