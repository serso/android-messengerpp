package org.solovyev.android.messenger.users;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.Mergeable;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.android.properties.AProperties;
import org.solovyev.android.properties.AProperty;

public interface User extends MutableUserSyncData, Identifiable, EntityAware, Mergeable<User> {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */


	@Nonnull
	static final String PROPERTY_ONLINE = "online";

	@Nonnull
	static final String PROPERTY_FIRST_NAME = "first_name";

	@Nonnull
	static final String PROPERTY_LAST_NAME = "last_name";

	@Nonnull
	static final String PROPERTY_NICKNAME = "nick_name";


	/**
	 * Property 'sex' must contain only string representations of enum {@link Gender}
	 */
	@Nonnull
	static final String PROPERTY_SEX = "sex";

	/**
	 * Primary phone number, phone number used by default
	 */
	@Nonnull
	static final String PROPERTY_PHONE = "phone";

	/**
	 * List of all phone numbers separated by ';'
	 */
	@Nonnull
	static final String PROPERTY_PHONES = "phones";

	@Nonnull
	static final String PROPERTY_PHONES_SEPARATOR = ";";

	@Nonnull
	static final String PROPERTY_EMAIL = "email";


    /*
    **********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */

	@Nonnull
	String getLogin();

	@Nullable
	Gender getGender();

	boolean isOnline();

	@Nullable
	String getFirstName();

	@Nullable
	String getLastName();

	@Nullable
	String getPhoneNumber();

	@Nonnull
	Set<String> getPhoneNumbers();

	@Nonnull
	Collection<AProperty> getPropertiesCollection();

	@Nonnull
	AProperties getProperties();

	@Nonnull
	Entity getEntity();

	@Nullable
	String getPropertyValueByName(@Nonnull String name);

	@Nonnull
	UserSyncData getUserSyncData();

	@Nonnull
	String getDisplayName();

	@Nonnull
	User clone();

	@Nonnull
	User cloneWithNewStatus(boolean online);

	@Nonnull
	User cloneWithNewProperty(@Nonnull AProperty property);

	@Nonnull
	User cloneWithNewProperties(@Nonnull AProperties properties);

    /*
    **********************************************************************
    *
    *                           UPDATE SYNC DATA
    *
    **********************************************************************
    */

	@Nonnull
	User updateChatsSyncDate();

	@Nonnull
	User updatePropertiesSyncDate();

	@Nonnull
	User updateContactsSyncDate();

	@Nonnull
	User updateUserIconsSyncDate();
}
