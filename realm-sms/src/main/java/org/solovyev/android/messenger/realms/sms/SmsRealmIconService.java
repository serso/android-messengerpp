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
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.List;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 8:53 PM
 */
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
		imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_users));
	}

	@Nonnull
	private Drawable loadContactPhoto(@Nonnull User user) {
		final String accountEntityId = user.getEntity().getAccountEntityId();

		if (!UserService.NO_ACCOUNT_USER_ID.equals(accountEntityId)) {
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

		return context.getResources().getDrawable(R.drawable.mpp_icon_user_empty);
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
