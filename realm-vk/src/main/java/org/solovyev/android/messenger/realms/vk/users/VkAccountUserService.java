package org.solovyev.android.messenger.realms.vk.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.EntityAwareByIdFinder;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.collections.Collections;

import com.google.common.collect.Iterables;

import static com.google.common.collect.Iterables.find;
import static java.util.Arrays.asList;
import static org.solovyev.android.http.HttpTransactions.execute;
import static org.solovyev.android.messenger.realms.vk.users.ApiUserField.online;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 1:18 PM
 */
public class VkAccountUserService implements AccountUserService {

	@Nonnull
	private final VkAccount account;

	public VkAccountUserService(@Nonnull VkAccount account) {
		this.account = account;
	}

	@Override
	public User getUserById(@Nonnull String accountUserId) throws AccountConnectionException {
		try {
			final List<User> users = execute(VkUsersGetHttpTransaction.newInstance(account, accountUserId, null));
			return Collections.getFirstListElement(users);
		} catch (HttpRuntimeIoException e) {
			throw new AccountConnectionException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountConnectionException(account.getId(), e);
		}
	}

	@Nonnull
	@Override
	public List<User> getUserContacts(@Nonnull String accountUserId) throws AccountConnectionException {
		try {
			return execute(VkFriendsGetHttpTransaction.newInstance(account, accountUserId));
		} catch (HttpRuntimeIoException e) {
			throw new AccountConnectionException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountConnectionException(account.getId(), e);
		}
	}


	@Nonnull
	@Override
	public List<User> checkOnlineUsers(@Nonnull List<User> users) throws AccountConnectionException {
		final List<User> result = new ArrayList<User>(users.size());

		try {
			for (VkUsersGetHttpTransaction vkUsersGetHttpTransaction : VkUsersGetHttpTransaction.newInstancesForUsers(account, users, asList(online))) {
				result.addAll(execute(vkUsersGetHttpTransaction));
			}
		} catch (HttpRuntimeIoException e) {
			throw new AccountConnectionException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountConnectionException(account.getId(), e);
		}

		return actualizeOnlineUsers(users, result);
	}

	@Nonnull
	private List<User> actualizeOnlineUsers(@Nonnull List<User> actualUsers, @Nonnull List<User> users) {
		final List<User> result = new ArrayList<User>(users.size());

		final boolean sameSize = actualUsers.size() == users.size();
		for (int i = 0; i < users.size(); i++) {
			final User user = users.get(i);

			boolean actualized = false;
			if (sameSize) {
				// if we have same size => try same index first
				final User actualUser = actualUsers.get(i);
				if (user.equals(actualUser)) {
					result.add(actualUser.cloneWithNewStatus(user.isOnline()));
					actualized = true;
				}
			}

			if (!actualized) {
				final User actualUser = find(actualUsers, new EntityAwareByIdFinder(user.getId()));
				if (actualUser == null) {
					result.add(user);
				} else {
					result.add(actualUser.cloneWithNewStatus(user.isOnline()));
				}
			}
		}

		return result;
	}

	@Nonnull
	@Override
	public User saveUser(@Nonnull User user) {
		return user;
	}
}
