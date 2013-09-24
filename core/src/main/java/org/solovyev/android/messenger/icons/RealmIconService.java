package org.solovyev.android.messenger.icons;

import android.widget.ImageView;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 3/13/13
 * Time: 9:46 PM
 */
public interface RealmIconService {

	void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView);

	void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView);

	/**
	 * Method fetches user icons for specified <var>users</var>
	 *
	 * @param users for which icon fetching must be done
	 */
	void fetchUsersIcons(@Nonnull List<User> users);

	void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView);
}
