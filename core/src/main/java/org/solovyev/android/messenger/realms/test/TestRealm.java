/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.realms.test;

import android.content.Context;
import android.widget.ImageView;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.security.Cipherer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Singleton
public class TestRealm extends AbstractRealm {

	@Nonnull
	public static final String REALM_ID = "test";

	public TestRealm() {
		super(REALM_ID, R.string.mpp_test_account, R.drawable.mpp_test_realm, TestAccountConfigurationFragment.class, TestAccountConfiguration.class, false, null, false);
	}

	@Nonnull
	public AccountConnection createRealmConnection(@Nonnull Context context, @Nonnull Account account) {
		return new TestAccountConnection((TestAccount) account, context);
	}

	@Nonnull
	public static Entity newEntity(@Nonnull String realmEntityId) {
		return Entities.newEntity(REALM_ID, realmEntityId);
	}

	@Nonnull
	@Override
	public Account<TestAccountConfiguration> newAccount(@Nonnull String accountId, @Nonnull User user, @Nonnull AccountConfiguration configuration, @Nonnull AccountState state, @Nonnull AccountSyncData syncData) {
		return new TestAccount(accountId, this, user, (TestAccountConfiguration) configuration, state);
	}

	@Nonnull
	@Override
	public AccountBuilder newAccountBuilder(@Nonnull AccountConfiguration configuration, @Nullable Account editedAccount) {
		return new TestAccountBuilder(this, (TestAccountConfiguration) configuration, (TestAccount) editedAccount);
	}

	@Nonnull
	@Override
	public RealmIconService getRealmIconService() {
		return new RealmIconService() {
			@Override
			public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
				imageView.setImageDrawable(App.getApplication().getResources().getDrawable(getContactIconResId(user)));
			}

			private int getContactIconResId(User user) {
				int iconResId = R.drawable.mpp_icon_user;

				try {
					final Integer accountEntityId = Integer.valueOf(user.getEntity().getAccountEntityId());
					switch (accountEntityId) {
						case 0:
							iconResId = R.drawable.mpp_test_contact_0_icon;
							break;
						case 1:
							iconResId = R.drawable.mpp_test_contact_1_icon;
							break;
						case 2:
							iconResId = R.drawable.mpp_test_contact_2_icon;
							break;
						case 3:
							iconResId = R.drawable.mpp_test_contact_3_icon;
							break;
					}
				} catch (NumberFormatException e) {
				}
				return iconResId;
			}

			@Override
			public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
				imageView.setImageDrawable(App.getApplication().getResources().getDrawable(getContactIconResId(user)));
			}

			@Override
			public void fetchUsersIcons(@Nonnull List<User> users) {
			}

			@Override
			public void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView) {
				imageView.setImageDrawable(App.getApplication().getResources().getDrawable(R.drawable.mpp_icon_users));
			}
		};
	}

	@Nullable
	@Override
	public Cipherer<TestAccountConfiguration, TestAccountConfiguration> getCipherer() {
		return null;
	}
}
