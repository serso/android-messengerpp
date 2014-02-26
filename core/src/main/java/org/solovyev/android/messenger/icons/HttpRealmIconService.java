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

package org.solovyev.android.messenger.icons;

import android.content.Context;
import android.widget.ImageView;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.view.IconGenerator;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class HttpRealmIconService implements RealmIconService {

	@Nonnull
	private final Context context;

	@Nonnull
	private final ImageLoader imageLoader;

	@Nonnull
	private final UrlGetter iconUrlGetter;

	@Nonnull
	private final UrlGetter photoUrlGetter;

	@Nonnull
	private final IconGenerator iconGenerator;

	public HttpRealmIconService(@Nonnull Context context,
								@Nonnull ImageLoader imageLoader,
								@Nonnull UrlGetter iconUrlGetter,
								@Nonnull UrlGetter photoUrlGetter) {
		this.context = context;
		this.imageLoader = imageLoader;
		this.iconUrlGetter = iconUrlGetter;
		this.photoUrlGetter = photoUrlGetter;
		this.iconGenerator = App.getIconGenerator();
	}

	@Override
	public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
		final String userIconUrl = iconUrlGetter.getUrl(user);
		if (!Strings.isEmpty(userIconUrl)) {
			assert userIconUrl != null;
			this.imageLoader.loadImage(userIconUrl, imageView, iconGenerator.getIconResId(user));
		} else {
			imageView.setImageDrawable(iconGenerator.getIcon(user));
		}
	}

	@Override
	public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		final String userPhotoUrl = photoUrlGetter.getUrl(user);
		if (!Strings.isEmpty(userPhotoUrl)) {
			assert userPhotoUrl != null;
			this.imageLoader.loadImage(userPhotoUrl, imageView, iconGenerator.getIconResId(user));
		} else {
			imageView.setImageDrawable(iconGenerator.getIcon(user));
		}
	}

	@Override
	public void fetchUsersIcons(@Nonnull List<User> users) {
		for (User contact : users) {
			fetchUserIcon(contact);
		}
	}

	@Override
	public void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView) {
		imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_users_red));
	}

	public void fetchUserIcon(@Nonnull User user) {
		final String userIconUrl = iconUrlGetter.getUrl(user);
		if (!Strings.isEmpty(userIconUrl)) {
			assert userIconUrl != null;
			this.imageLoader.loadImage(userIconUrl);
		}
	}

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	public static interface UrlGetter {

		@Nullable
		String getUrl(@Nonnull User user);

	}

	private static final class UrlFromPropertyGetter implements UrlGetter {

		@Nonnull
		private final String propertyName;

		private UrlFromPropertyGetter(@Nonnull String propertyName) {
			this.propertyName = propertyName;
		}

		@Nullable
		@Override
		public String getUrl(@Nonnull User user) {
			return user.getPropertyValueByName(propertyName);
		}
	}

	@Nonnull
	public static UrlGetter newUrlFromPropertyGetter(@Nonnull String propertyName) {
		return new UrlFromPropertyGetter(propertyName);
	}
}
