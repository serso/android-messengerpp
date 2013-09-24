package org.solovyev.android.messenger.users;

import android.content.Context;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.AccountService;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:03 PM
 */
public final class ContactsAdapter extends AbstractContactsAdapter {

	public ContactsAdapter(@Nonnull Context context, @Nonnull AccountService accountService) {
		super(context);
	}

	@Override
	protected void onListItemChanged(@Nonnull User contact) {
	}

	@Override
	protected boolean canAddContact(@Nonnull User contact) {
		return true;
	}
}
