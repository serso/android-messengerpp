package org.solovyev.android.messenger.users;

import android.content.Context;
import android.widget.ImageView;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.icons.RealmIconService;

import static java.util.Arrays.asList;

class DefaultUserIconsService implements UserIconsService {

	@Nonnull
	private final Context context;

	@Nonnull
	private final UserService userService;

	public DefaultUserIconsService(@Nonnull Context context, @Nonnull UserService userService) {
		this.context = context;
		this.userService = userService;
	}

	@Override
	public void fetchUserAndContactsIcons(@Nonnull User user) throws UnsupportedAccountException {
		final RealmIconService realmIconService = getRealmIconServiceByUser(user);

		// fetch self icon
		realmIconService.fetchUsersIcons(asList(user));

		// fetch icons for all contacts
		final List<User> contacts = userService.getUserContacts(user.getEntity());
		realmIconService.fetchUsersIcons(contacts);

		// update sync data
		user = user.updateUserIconsSyncDate();
		userService.updateUser(user);
	}

	@Nonnull
	private RealmIconService getRealmIconServiceByUser(@Nonnull User user) throws UnsupportedAccountException {
		return getAccountByEntity(user.getEntity()).getRealm().getRealmIconService();
	}

	@Nonnull
	private Account getAccountByEntity(@Nonnull Entity entity) throws UnsupportedAccountException {
		return App.getAccountService().getAccountByEntity(entity);
	}

	@Override
	public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
		try {
			getRealmIconServiceByUser(user).setUserIcon(user, imageView);
		} catch (UnsupportedAccountException e) {
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_user_empty));
			App.getExceptionHandler().handleException(e);
		}
	}

	@Override
	public void setUsersIcon(@Nonnull Account account, @Nonnull List<User> users, ImageView imageView) {
		account.getRealm().getRealmIconService().setUsersIcon(users, imageView);
	}

	@Override
	public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
		try {
			getRealmIconServiceByUser(user).setUserPhoto(user, imageView);
		} catch (UnsupportedAccountException e) {
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mpp_icon_user_empty));
			App.getExceptionHandler().handleException(e);
		}
	}
}
