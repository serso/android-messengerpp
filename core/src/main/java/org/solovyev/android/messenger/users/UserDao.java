package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.LinkedEntitiesDao;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:12 PM
 */

public interface UserDao extends LinkedEntitiesDao<User> {

    /*
	**********************************************************************
    *
    *                           USERS
    *
    **********************************************************************
    */

	/**
	 * Method save user with his properties to persistence storage
	 * Note: this method doesn't check if user with same ID is already in the storage.
	 *
	 * @param user user to be inserted in persistence storage
	 */
	long create(@Nonnull User user);

	/**
	 * Method loads user by if from storage
	 *
	 * @param userId user id
	 * @return user previously saved into storage identified by <var>userId</var>, null if no such user exists in storage
	 */
	@Nullable
	User read(@Nonnull String userId);

	/**
	 * Method loads user properties
	 *
	 * @param userId user id
	 * @return properties of a user previously saved into storage, empty list if no such user exists in storage or no properties are set for him
	 */
	@Nonnull
	List<AProperty> readPropertiesById(@Nonnull String userId);

	/**
	 * Method updates user and his properties in the storage
	 *
	 * @param user user to be save in the storage, nothing is done if user is not yet in the storage
	 */
	long update(@Nonnull User user);

	/**
	 * @return all ids of users saved in the storage
	 */
	@Nonnull
	Collection<String> readAllIds();

	/**
	 * Method deletes all users and their properties from the storage
	 */
	void deleteAll();

	/*
    **********************************************************************
    *
    *                           CONTACTS
    *
    **********************************************************************
    */

	/**
	 * @param userId id of a user for which list of contacts should be returned
	 * @return list of ids of contacts of a user identified by user id
	 */
	@Nonnull
	List<String> readLinkedEntityIds(@Nonnull String userId);

	/**
	 * @param userId id of a user for which list of contacts should be returned
	 * @return list of contacts of a user identified by user id
	 */
	@Nonnull
	List<User> readContacts(@Nonnull String userId);

	/**
	 * Method merges passed user <var>contacts</var> with contacts stored in the storage.
	 * The result of an operation might be adding, removal, updating of user contacts.
	 *
	 * @param userId       id of a user for which merge should be done
	 * @param contacts     list of ALL contacts of a user
	 * @param allowRemoval allow contacts removal
	 * @param allowUpdate  allow contacts update
	 * @return merge result
	 * @see org.solovyev.android.messenger.MergeDaoResult
	 */
	@Nonnull
	MergeDaoResult<User, String> mergeLinkedEntities(@Nonnull String userId,
													 @Nonnull List<User> contacts,
													 boolean allowRemoval,
													 boolean allowUpdate);

	void updateOnlineStatus(@Nonnull User contact);
}
