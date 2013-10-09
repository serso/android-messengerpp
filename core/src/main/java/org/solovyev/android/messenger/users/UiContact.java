package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.accounts.Account;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.getAccountService;

/**
 * User: serso
 * Date: 3/25/13
 * Time: 10:09 PM
 */
final class UiContact implements Identifiable {

	@Nonnull
	private final User contact;

	private final int unreadMessagesCount;

	@Nullable
	private Account account;

	private UiContact(@Nonnull User contact, int unreadMessagesCount, @Nullable Account account) {
		this.contact = contact;
		this.unreadMessagesCount = unreadMessagesCount;
		this.account = account;
	}

	@Nonnull
	static UiContact newUiContact(@Nonnull User contact, int unreadMessagesCount, @Nullable Account account) {
		return new UiContact(contact, unreadMessagesCount, account);
	}

	@Nonnull
	static UiContact loadUiContact(@Nonnull User contact) {
		return newUiContact(contact, getUnreadMessagesCount(contact), getAccount(contact));
	}

	@Nonnull
	static UiContact loadUiContact(@Nonnull User contact, @Nullable Account account) {
		return newUiContact(contact, getUnreadMessagesCount(contact), account);
	}

	@Nullable
	private static Account getAccount(@Nonnull User contact) {
		return getAccountService().getAccountByEntityOrNull(contact.getEntity());
	}

	private static int getUnreadMessagesCount(@Nonnull User contact) {
		return App.getUserService().getUnreadMessagesCount(contact.getEntity());
	}

	@Nonnull
	@Override
	public String getId() {
		return contact.getId();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final UiContact that = (UiContact) o;

		if (!contact.equals(that.contact)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return contact.hashCode();
	}

	@Nonnull
	public User getContact() {
		return contact;
	}

	public int getUnreadMessagesCount() {
		return unreadMessagesCount;
	}

	@Nullable
	public Account getAccount() {
		if (account == null) {
			account = getAccount(contact);
		}
		return account;
	}

	@Nonnull
	String getDisplayName() {
		return this.contact.getDisplayName();
	}

	@Nonnull
	public UiContact copyForNewUser(@Nonnull User newContact) {
		return newUiContact(newContact, this.unreadMessagesCount, this.account);
	}

	@Nonnull
	public UiContact copyForNewUnreadMessagesCount(@Nonnull Integer unreadMessagesCount) {
		return newUiContact(this.contact, unreadMessagesCount, this.account);
	}
}
