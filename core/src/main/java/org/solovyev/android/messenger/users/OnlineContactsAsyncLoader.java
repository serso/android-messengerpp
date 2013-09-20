package org.solovyev.android.messenger.users;

import android.content.Context;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.realms.AccountService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:24 PM
 */
public class OnlineContactsAsyncLoader extends AbstractAsyncLoader<UiContact, ContactListItem> {

	@Nonnull
	private final AccountService accountService;

	OnlineContactsAsyncLoader(@Nonnull Context context,
							  @Nonnull ListAdapter<ContactListItem> adapter,
							  @Nullable Runnable onPostExecute,
							  @Nonnull AccountService accountService) {
		super(context, adapter, onPostExecute);
		this.accountService = accountService;
	}

	@Nonnull
	protected List<UiContact> getElements(@Nonnull Context context) {
		final List<UiContact> result = new ArrayList<UiContact>();

		final UserService userService = MessengerApplication.getServiceLocator().getUserService();

		for (User user : accountService.getEnabledRealmUsers()) {
			for (User contact : userService.getOnlineUserContacts(user.getEntity())) {
				result.add(UiContact.newInstance(contact, userService.getUnreadMessagesCount(contact.getEntity()), contact.getDisplayName()));
			}
		}

		return result;
	}

	@Nonnull
	@Override
	protected ContactListItem createListItem(@Nonnull UiContact contact) {
		return ContactListItem.newInstance(contact);
	}
}
