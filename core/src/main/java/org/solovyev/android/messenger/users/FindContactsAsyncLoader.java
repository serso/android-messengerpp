package org.solovyev.android.messenger.users;

import android.content.Context;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.accounts.AccountService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.App.getUserService;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 3:12 PM
 */
final class FindContactsAsyncLoader extends AbstractAsyncLoader<UiContact, ContactListItem> {

	@Nullable
	private String query;
	private final int maxCount;

	FindContactsAsyncLoader(@Nonnull Context context,
							@Nonnull ListAdapter<ContactListItem> adapter,
							@Nullable Runnable onPostExecute,
							@Nullable String query,
							int maxCount) {
		super(context, adapter, onPostExecute);
		this.query = query;
		this.maxCount = maxCount;
	}

	@Nonnull
	protected List<UiContact> getElements(@Nonnull Context context) {
		final List<UiContact> result = new ArrayList<UiContact>();

		final AccountService accountService = getAccountService();
		final UserService userService = getUserService();

		final Collection<User> accountUsers = accountService.getEnabledAccountUsers();
		if (accountUsers.size() > 0) {
			final int count = max(maxCount / accountUsers.size(), 1);
			for (User user : accountUsers) {
				result.addAll(userService.findContacts(user, query, count));
			}

		}

		return result.subList(0, min(result.size(), maxCount));
	}

	@Nonnull
	@Override
	protected ContactListItem createListItem(@Nonnull UiContact uiContact) {
		return ContactListItem.newInstance(uiContact);
	}
}
