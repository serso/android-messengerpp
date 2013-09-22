package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 8:33 PM
 */
public interface AccountUserService {

	/**
	 * @param accountUserId account user id
	 * @return fully loaded user which is identified by <var>accountUserId</var> in current account
	 */
	@Nullable
	User getUserById(@Nonnull String accountUserId) throws AccountConnectionException;

	/**
	 * @param accountUserId account user id
	 * @return list of user contacts (users to which current user can write messages and is aware of theirs presence in chat)
	 */
	@Nonnull
	List<User> getUserContacts(@Nonnull String accountUserId) throws AccountConnectionException;

	/**
	 * Method checks if user in <var>users</var> list is online
	 *
	 * @param users list of users for which online check should be done
	 * @return list of users with updated statuses (online/offline)
	 */
	@Nonnull
	List<User> checkOnlineUsers(@Nonnull List<User> users) throws AccountConnectionException;

}
