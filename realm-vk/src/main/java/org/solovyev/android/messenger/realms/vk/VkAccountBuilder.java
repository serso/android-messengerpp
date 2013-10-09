package org.solovyev.android.messenger.realms.vk;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.accounts.AbstractAccountBuilder;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.vk.auth.JsonAuthResult;
import org.solovyev.android.messenger.realms.vk.auth.VkAuth;
import org.solovyev.android.messenger.realms.vk.users.VkUsersGetHttpTransaction;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;

import static org.solovyev.android.http.HttpTransactions.execute;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.android.messenger.users.Users.newUser;

public class VkAccountBuilder extends AbstractAccountBuilder<VkAccount, VkAccountConfiguration> {

	protected VkAccountBuilder(@Nonnull Realm realm, @Nullable VkAccount editedAccount, @Nonnull VkAccountConfiguration configuration) {
		super(realm, configuration, editedAccount);
	}

	@Nonnull
	@Override
	protected MutableUser getAccountUser(@Nonnull String accountId) {
		final String userId = getConfiguration().getUserId();
		final MutableUser defaultUser = newEmptyUser(newEntity(accountId, userId));

		MutableUser result;
		try {
			final List<User> users = execute(VkUsersGetHttpTransaction.newInstance(new VkAccount(accountId, getRealm(), defaultUser, getConfiguration(), AccountState.removed), userId, null));
			if (users.isEmpty()) {
				result = defaultUser;
			} else {
				final User user = users.get(0);
				result = newUser(user.getEntity(), user.getUserSyncData(), user.getProperties());
			}
		} catch (IOException e) {
			Log.e("VkRealmBuilder", e.getMessage(), e);
			result = defaultUser;
		}

		return result;
	}

	@Nonnull
	@Override
	protected VkAccount newAccount(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state) {
		return new VkAccount(id, getRealm(), user, getConfiguration(), state);
	}

	@Override
	public void connect() throws ConnectionException {
	}

	@Override
	public void disconnect() throws ConnectionException {
	}

	@Override
	public void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
		final VkAccountConfiguration configuration = getConfiguration();

		final JsonAuthResult result = VkAuth.doOauth2Authorization(configuration.getLogin(), configuration.getPassword());
		configuration.setAccessParameters(result.getAccessToken(), result.getUserId());
	}

}
