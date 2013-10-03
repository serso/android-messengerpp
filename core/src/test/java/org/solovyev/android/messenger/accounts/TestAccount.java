package org.solovyev.android.messenger.accounts;

import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.accounts.connection.TestAccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

@Singleton
public class TestAccount extends AbstractAccount<TestAccountConfiguration> {

	@Inject
	public TestAccount(@Nonnull TestRealm realm) {
		this(realm, 1);
	}

	public TestAccount(@Nonnull TestRealm realm, int index) {
		super(realm.getId() + "~" + index, realm, newEmptyUser(newEntity(realm.getId() + "~" + index, "user" + index)), new TestAccountConfiguration("test_field", 42), AccountState.enabled);
	}


	public TestAccount(@Nonnull String id, @Nonnull Realm realm, @Nonnull User user, @Nonnull TestAccountConfiguration configuration, @Nonnull AccountState state) {
		super(id, realm, user, configuration, state);
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
