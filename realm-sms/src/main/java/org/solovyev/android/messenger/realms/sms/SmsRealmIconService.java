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

package org.solovyev.android.messenger.realms.sms;

import android.content.Context;
import android.widget.ImageView;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import java.util.List;

import static org.solovyev.android.messenger.App.getIconGenerator;

final class SmsRealmIconService implements RealmIconService {

	@Nonnull
	private final Context context;

	// todo serso: make proper singleton
	private static SmsIconLoader iconLoader;

	SmsRealmIconService(@Nonnull Context context) {
		this.context = context;
	}

	@Override
	public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
		loadContactPhoto(user, imageView);
	}

	@Override
	public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		loadContactPhoto(user, imageView);
	}

	@Override
	public void fetchUsersIcons(@Nonnull List<User> users) {

	}

	@Override
	public void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView) {
		imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_users_green));
	}

	private void loadContactPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		final String accountEntityId = user.getEntity().getAccountEntityId();

		if (user.getEntity().isAccountEntityIdSet() && !SmsRealm.USER_ID.equals(accountEntityId)) {
			if (iconLoader == null) {
				iconLoader = new SmsIconLoader(context, "messenger-sms", App.getUiHandler());
			}
			iconLoader.loadImage(accountEntityId, imageView, getIconGenerator().getIconResId(user));
		} else {
			imageView.setImageDrawable(getIconGenerator().getIcon(user));
		}
	}
}
