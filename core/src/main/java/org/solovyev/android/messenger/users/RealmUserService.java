package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.realms.RealmConnectionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:33 PM
 */
public interface RealmUserService {

	/**
	 * @param realmUserId realm user id
	 * @return fully loaded user which is identified by <var>realmUserId</var> in current realm
	 */
	@Nullable
	User getUserById(@Nonnull String realmUserId) throws RealmConnectionException;

	/**
	 * @param realmUserId realm user id
	 * @return list of user contacts (users to which current user can write messages and is aware of theirs presence in chat)
	 */
	@Nonnull
	List<User> getUserContacts(@Nonnull String realmUserId) throws RealmConnectionException;

	/**
	 * Method checks if user in <var>users</var> list is online and returns list of online users
	 *
	 * @param users list of users for which online check should be done
	 * @return list of user which are currently online (sub list of specified list)
	 */
	@Nonnull
	List<User> checkOnlineUsers(@Nonnull List<User> users) throws RealmConnectionException;

}
