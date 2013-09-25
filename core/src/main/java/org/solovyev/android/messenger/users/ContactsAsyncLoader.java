package org.solovyev.android.messenger.users;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountService;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 3:12 PM
 */
final class ContactsAsyncLoader extends AbstractAsyncLoader<UiContact, ContactListItem> {

	ContactsAsyncLoader(@Nonnull Context context,
						@Nonnull ListAdapter<ContactListItem> adapter,
						@Nullable Runnable onPostExecute) {
		super(context, adapter, onPostExecute);
	}

	@Nonnull
	protected List<UiContact> getElements(@Nonnull Context context) {
		final List<UiContact> result = new ArrayList<UiContact>();

		final AccountService accountService = App.getAccountService();
		final UserService userService = App.getUserService();

		for (User user : accountService.getEnabledAccountUsers()) {
			for (User contact : userService.getUserContacts(user.getEntity())) {
				result.add(UiContact.newInstance(contact, userService.getUnreadMessagesCount(contact.getEntity())));
			}
		}

		return result;
	}

	@Nonnull
	@Override
	protected ContactListItem createListItem(@Nonnull UiContact uiContact) {
		return ContactListItem.newInstance(uiContact);
	}
}
