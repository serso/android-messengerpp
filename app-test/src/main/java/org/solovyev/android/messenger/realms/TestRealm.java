package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.widget.ImageView;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.test.R;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.security.Cipherer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@Singleton
public class TestRealm extends AbstractRealm {

	@Nonnull
	public static final String REALM_ID = "test";

	public TestRealm() {
		super(REALM_ID, R.string.mpp_test_realm_name, R.drawable.mpp_test_icon, TestAccountConfigurationFragment.class, TestAccountConfiguration.class, false);
	}

	@Nonnull
	public AccountConnection createRealmConnection(@Nonnull Context context, @Nonnull Account account) {
		return null;
	}

	@Nonnull
	public static Entity newEntity(@Nonnull String realmEntityId) {
		return EntityImpl.newInstance(REALM_ID, realmEntityId);
	}

	@Nonnull
	@Override
	public Account<TestAccountConfiguration> newAccount(@Nonnull String accountId, @Nonnull User user, @Nonnull AccountConfiguration configuration, @Nonnull AccountState state) {
		return new TestAccount(accountId, this, user, (TestAccountConfiguration) configuration);
	}

	@Nonnull
	@Override
	public AccountBuilder newAccountBuilder(@Nonnull AccountConfiguration configuration, @Nullable Account editedAccount) {
		return null;
	}

	@Nonnull
	@Override
	public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public RealmIconService getRealmIconService() {
		return new RealmIconService() {
			@Override
			public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {

			}

			@Override
			public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
			}

			@Override
			public void fetchUsersIcons(@Nonnull List<User> users) {
			}

			@Override
			public void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		};
	}

	@Nullable
	@Override
	public Cipherer<AccountConfiguration, AccountConfiguration> getCipherer() {
		return null;
	}

}
