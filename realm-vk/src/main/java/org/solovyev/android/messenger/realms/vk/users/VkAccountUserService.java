package org.solovyev.android.messenger.realms.vk.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.collections.Collections;

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
			final List<User> users = HttpTransactions.execute(VkUsersGetHttpTransaction.newInstance(account, accountUserId, null));
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
			return HttpTransactions.execute(VkFriendsGetHttpTransaction.newInstance(account, accountUserId));
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
			for (VkUsersGetHttpTransaction vkUsersGetHttpTransaction : VkUsersGetHttpTransaction.newInstancesForUsers(account, users, Arrays.asList(ApiUserField.online))) {
				result.addAll(HttpTransactions.execute(vkUsersGetHttpTransaction));
			}
		} catch (HttpRuntimeIoException e) {
			throw new AccountConnectionException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountConnectionException(account.getId(), e);
		}

		return result;
	}

	@Nonnull
	@Override
	public User saveUser(@Nonnull User user) {
		return user;
	}
}
