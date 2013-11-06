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
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.CompositeUserChoice;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

import java.util.List;

public interface Account<C extends AccountConfiguration> extends Identifiable {

	@Nonnull
	String getId();

	@Nonnull
	Realm getRealm();

	@Nonnull
	User getUser();

	void setUser(@Nonnull User user);

	@Nonnull
	C getConfiguration();

	void setConfiguration(@Nonnull C configuration);

	@Nonnull
	AccountState getState();

	boolean isEnabled();

	@Nonnull
	Entity newEntity(@Nonnull String accountEntityId);

	@Nonnull
	Entity newEntity(@Nonnull String accountEntityId, @Nonnull String entityId);

	@Nonnull
	Entity newUserEntity(@Nonnull String accountUserId);

	@Nonnull
	Entity newChatEntity(@Nonnull String accountChatId);

	@Nonnull
	Entity newMessageEntity(@Nonnull String accountMessageId);

	@Nonnull
	Entity newMessageEntity(@Nonnull String accountMessageId, @Nonnull String entityId);

	boolean same(@Nonnull Account that);

	@Nonnull
	String getDisplayName(@Nonnull Context context);

	@Nonnull
	Account copyForNewState(@Nonnull AccountState newState);

	boolean isAccountUser(@Nonnull String accountUserId);
	boolean isAccountUser(@Nonnull Entity entity);

	/*
	**********************************************************************
	*
	*                           COMPOSITE USER
	*
	**********************************************************************
	*/

	boolean isCompositeUser(@Nonnull User user);

	/**
	 * Should be called only after {@link Account#isCompositeUser(org.solovyev.android.messenger.users.User)} returned true
	 * @param user user for which check should be done
	 *
	 * @return true if default value is set for composite user
	 */
	boolean isCompositeUserDefined(@Nonnull	User user);

	@Nonnull
	List<CompositeUserChoice> getCompositeUserChoices(@Nonnull User user);

	@Nonnull
	User applyCompositeChoice(@Nonnull CompositeUserChoice compositeUserChoice, @Nonnull User user);

	boolean isCompositeUserChoicePersisted();

	/*
	**********************************************************************
	*
	*                           SPECIAL FEATURES
	*
	**********************************************************************
	*/

	boolean canSendMessage(@Nonnull Chat chat);

	boolean canCall(@Nonnull User contact);

	void call(@Nonnull User contact, @Nonnull Context context);
	/*
	**********************************************************************
	*
	*                           Account Services
	*
	**********************************************************************
	*/
	@Nonnull
	AccountUserService getAccountUserService();

	@Nonnull
	AccountChatService getAccountChatService();

	@Nonnull
	AccountConnection newConnection(@Nonnull Context context);

	int getCompositeDialogTitleResId();
}
