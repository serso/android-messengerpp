/*
 * Copyright 2014 serso aka se.solovyev
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

package org.solovyev.android.messenger.view;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

public final class IconGenerator {

	private final int[] userIcons = new int[]{
			R.drawable.mpp_icon_user_blue,
			R.drawable.mpp_icon_user_black,
			R.drawable.mpp_icon_user_green,
			R.drawable.mpp_icon_user_orange,
			R.drawable.mpp_icon_user_pink,
			R.drawable.mpp_icon_user_red,
			R.drawable.mpp_icon_user_purple
	};

	private final int[] usersIcons = new int[]{
			R.drawable.mpp_icon_users_blue,
			R.drawable.mpp_icon_users_black,
			R.drawable.mpp_icon_users_green,
			R.drawable.mpp_icon_users_orange,
			R.drawable.mpp_icon_users_pink,
			R.drawable.mpp_icon_users_red,
			R.drawable.mpp_icon_users_purple
	};

	@Nonnull
	private final Context context;

	public IconGenerator(@Nonnull Application context) {
		this.context = context;
	}

	public int getIconResId(@Nonnull User user) {
		return getIconResId(user, userIcons);
	}

	private int getIconResId(@Nonnull Identifiable identifiable, int[] icons) {
		final int hashCode = identifiable.getId().hashCode();
		return icons[getPosition(hashCode)];
	}

	public int getIconResId(@Nonnull Chat chat) {
		return getIconResId(chat, usersIcons);
	}

	@Nonnull
	public Drawable getIcon(@Nonnull User user) {
		return context.getResources().getDrawable(getIconResId(user));
	}

	@Nonnull
	public Drawable getIcon(@Nonnull Chat chat) {
		return context.getResources().getDrawable(getIconResId(chat));
	}

	private int getPosition(int hashCode) {
		final int position = hashCode % userIcons.length;
		return position >= 0 ? position : -position;
	}
}
