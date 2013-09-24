package org.solovyev.android.messenger.users;

import android.content.Context;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.AccountService;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 6:01 PM
 */
public class OnlineContactsAdapter extends AbstractContactsAdapter {

	public OnlineContactsAdapter(@Nonnull Context context, @Nonnull AccountService accountService) {
		super(context);
	}

	@Override
	public void onEvent(@Nonnull UserEvent event) {
		super.onEvent(event);

		switch (event.getType()) {
			case contacts_presence_changed:
				for (User contact : event.getDataAsUsers()) {
					if(contact.isOnline()) {
						tryAddOnlineContact(contact);
					}
				}
				break;
		}
	}

	private void tryAddOnlineContact(@Nonnull User contact) {
		final ContactListItem listItem = findInAllElements(contact);
		if (listItem == null) {
			addListItem(contact);
		}
	}

	@Override
	protected void onListItemChanged(@Nonnull User contact) {
		if (!contact.isOnline()) {
			removeListItem(contact);
		}
	}

	@Override
	protected boolean canAddContact(@Nonnull User contact) {
		return contact.isOnline();
	}
}
