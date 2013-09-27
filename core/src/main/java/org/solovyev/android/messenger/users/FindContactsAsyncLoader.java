package org.solovyev.android.messenger.users;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountService;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.App.getUserService;
import static org.solovyev.android.messenger.users.Users.MAX_SEARCH_CONTACTS;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 3:12 PM
 */
final class FindContactsAsyncLoader extends AbstractAsyncLoader<UiContact, ContactListItem> {

	@Nullable
	private String query;

	FindContactsAsyncLoader(@Nonnull Context context,
							@Nonnull ListAdapter<ContactListItem> adapter,
							@Nullable Runnable onPostExecute,
							@Nullable String query) {
		super(context, adapter, onPostExecute);
		this.query = query;
	}

	@Nonnull
	protected List<UiContact> getElements(@Nonnull Context context) {
		final List<UiContact> result = new ArrayList<UiContact>();

		final AccountService accountService = getAccountService();
		final UserService userService = getUserService();

		final Collection<User> enabledAccount = accountService.getEnabledAccountUsers();
		if (enabledAccount.size() > 0) {
			final int count = max(MAX_SEARCH_CONTACTS / enabledAccount.size(), 1);
			for (User user : enabledAccount) {
				result.addAll(userService.findContacts(user, query, count));
			}

		}

		return result.subList(0, min(result.size(), MAX_SEARCH_CONTACTS));
	}

	@Nonnull
	@Override
	protected ContactListItem createListItem(@Nonnull UiContact uiContact) {
		return ContactListItem.newInstance(uiContact);
	}
}
