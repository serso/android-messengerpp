package org.solovyev.android.messenger.realms;

import android.content.Context;

import java.util.List;

import org.solovyev.android.messenger.MessengerEntity;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.CompositeUserChoice;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

public interface Realm<C extends AccountConfiguration> extends MessengerEntity {

	@Nonnull
	String getId();

	@Nonnull
	RealmDef getRealmDef();

	@Nonnull
	User getUser();

	@Nonnull
	C getConfiguration();

	@Nonnull
	RealmState getState();

	boolean isEnabled();

	@Nonnull
	Entity newRealmEntity(@Nonnull String realmEntityId);

	@Nonnull
	Entity newRealmEntity(@Nonnull String realmEntityId, @Nonnull String entityId);

	@Nonnull
	Entity newUserEntity(@Nonnull String realmUserId);

	@Nonnull
	Entity newChatEntity(@Nonnull String realmUserId);

	@Nonnull
	Entity newMessageEntity(@Nonnull String realmMessageId);

	@Nonnull
	Entity newMessageEntity(@Nonnull String realmMessageId, @Nonnull String entityId);

	boolean same(@Nonnull Realm that);

	@Nonnull
	String getDisplayName(@Nonnull Context context);

	@Nonnull
	Realm copyForNewState(@Nonnull RealmState newState);

	/*
	**********************************************************************
	*
	*                           COMPOSITE USER
	*
	**********************************************************************
	*/

	boolean isCompositeUser(@Nonnull User user);

	/**
	 * Should be called only after {@link Realm#isCompositeUser(org.solovyev.android.messenger.users.User)} returned true
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
	*                           Realm Services
	*
	**********************************************************************
	*/
	@Nonnull
	RealmUserService getRealmUserService();

	@Nonnull
	RealmChatService getRealmChatService();

	@Nonnull
	RealmConnection newRealmConnection(@Nonnull Context context);

	int getCompositeDialogTitleResId();
}
