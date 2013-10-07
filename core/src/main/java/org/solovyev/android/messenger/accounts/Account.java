package org.solovyev.android.messenger.accounts;

import android.content.Context;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
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
