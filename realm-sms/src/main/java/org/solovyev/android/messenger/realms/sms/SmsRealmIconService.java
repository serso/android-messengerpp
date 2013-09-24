package org.solovyev.android.messenger.realms.sms;

import android.content.Context;
import android.widget.ImageView;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.users.User;

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
		imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_user_empty));
	}

	@Override
	public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_user_empty));
	}

	@Override
	public void fetchUsersIcons(@Nonnull List<User> users) {

	}

	@Override
	public void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView) {
		imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_users));
	}
}
