package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.security.base64.ABase64StringDecoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 3/14/13
 * Time: 9:04 PM
 */
public class XmppRealmIconService implements RealmIconService {

	@Nonnull
	private final Context context;

	private final int defaultUserIconResId;

	private final int defaultUsersIconResId;

	public XmppRealmIconService(@Nonnull Context context, int defaultUserIconResId, int defaultUsersIconResId) {
		this.context = context;
		this.defaultUserIconResId = defaultUserIconResId;
		this.defaultUsersIconResId = defaultUsersIconResId;
	}

	@Override
	public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
		final BitmapDrawable drawable = getUserIcon(user);
		if (drawable != null) {
			imageView.setImageDrawable(drawable);
		} else {
			imageView.setImageDrawable(context.getResources().getDrawable(defaultUserIconResId));
		}
	}

	@Override
	public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		setUserIcon(user, imageView);
	}

	@Override
	public void fetchUsersIcons(@Nonnull List<User> users) {
		// everything is already fetched
	}

	@Override
	public void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView) {
		imageView.setImageDrawable(context.getResources().getDrawable(defaultUsersIconResId));
	}

	@Nullable
	private BitmapDrawable getUserIcon(@Nonnull User user) {
		BitmapDrawable result = null;

		final String userIconBase64 = user.getPropertyValueByName(XmppRealm.USER_PROPERTY_AVATAR_BASE64);
		if (userIconBase64 != null) {
			try {
				final byte[] userIconBytes = ABase64StringDecoder.getInstance().convert(userIconBase64);
				result = new BitmapDrawable(BitmapFactory.decodeByteArray(userIconBytes, 0, userIconBytes.length));
			} catch (IllegalArgumentException e) {
				Log.e("XmppRealmDef", e.getMessage(), e);
			}
		}

		return result;
	}
}
