package org.solovyev.android.messenger.realms;

import android.content.Context;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.AbstractAccount;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TestAccount extends AbstractAccount<TestAccountConfiguration> {

	@Inject
	public TestAccount(@Nonnull TestRealm realm) {
		this(realm, 1);
	}

	public TestAccount(@Nonnull TestRealm realm, int index) {
		super(realm.getId() + "~" + index, realm, Users.newEmptyUser(Entities.newEntity(realm.getId() + "~" + index, "user" + index)), new TestAccountConfiguration("test_field", 42), AccountState.enabled);
	}


	public TestAccount(@Nonnull String id, @Nonnull Realm realm, @Nonnull User user, @Nonnull TestAccountConfiguration configuration) {
		super(id, realm, user, configuration, AccountState.enabled);
	}

	@Nonnull
	@Override
	protected AccountConnection createConnection(@Nonnull Context context) {
		return new TestAccountConnection(this, context);
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		return context.getString(getRealm().getNameResId());
	}

	@Nonnull
	@Override
	public AccountUserService getAccountUserService() {
		return new TestAccountService();
	}

	@Nonnull
	@Override
	public AccountChatService getAccountChatService() {
		return new TestAccountService();
	}
}
