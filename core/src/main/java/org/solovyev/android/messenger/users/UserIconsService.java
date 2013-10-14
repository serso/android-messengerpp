package org.solovyev.android.messenger.users;

import android.widget.ImageView;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;

public interface UserIconsService {
	/**
	 * Method sets icon of <var>user</var> in <var>imageView</var>
	 *
	 * @param user      user for whom icon shall be set
	 * @param imageView view to which icon shall be set
	 */
	void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView);

	/**
	 * Method sets some icon which represents set of <var>users</var> in <var>imageView</var>
	 *
	 * @param account     realm
	 * @param users     users for whom icon shall be set
	 * @param imageView view to which icon shall be set
	 */
	void setUsersIcon(@Nonnull Account account, @Nonnull List<User> users, ImageView imageView);

	void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView);

	/**
	 * Method fetches user icons for specified <var>user</var> and for ALL user contacts
	 *
	 * @param user for which icon fetching must be done
	 */
	void fetchUserAndContactsIcons(@Nonnull User user) throws UnsupportedAccountException;
}
