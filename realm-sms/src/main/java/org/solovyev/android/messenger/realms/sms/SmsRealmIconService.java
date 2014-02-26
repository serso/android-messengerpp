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

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.List;

import static org.solovyev.android.messenger.App.getIconGenerator;

final class SmsRealmIconService implements RealmIconService {

	@Nonnull
	private final Context context;

	SmsRealmIconService(@Nonnull Context context) {
		this.context = context;
	}

	@Override
	public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
		imageView.setImageDrawable(loadContactPhoto(user));
	}

	@Override
	public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		imageView.setImageDrawable(loadContactPhoto(user));
	}

	@Override
	public void fetchUsersIcons(@Nonnull List<User> users) {

	}

	@Override
	public void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView) {
		imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_users_green));
	}

	@Nonnull
	private Drawable loadContactPhoto(@Nonnull User user) {
		final String accountEntityId = user.getEntity().getAccountEntityId();

		if (user.getEntity().isAccountEntityIdSet()) {
			try {
				final Integer contactId = Integer.valueOf(accountEntityId);
				final Bitmap bitmap = loadContactPhoto(contactId);
				if (bitmap != null) {
					return new BitmapDrawable(bitmap);
				}
			} catch (NumberFormatException e) {
				Log.e(SmsRealm.TAG, e.getMessage(), e);
			}
		}

		return getIconGenerator().getIcon(user);
	}

	private Bitmap loadContactPhoto(long id) {
		final Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
		final InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
		if (input == null) {
			return null;
		}
		return BitmapFactory.decodeStream(input);
	}
}
