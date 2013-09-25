package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.MessengerEntity;

/**
 * User: serso
 * Date: 3/25/13
 * Time: 10:09 PM
 */
final class UiContact implements MessengerEntity {

	@Nonnull
	private final User contact;

	private final int unreadMessagesCount;

	private UiContact(@Nonnull User contact, int unreadMessagesCount) {
		this.contact = contact;
		this.unreadMessagesCount = unreadMessagesCount;
	}

	@Nonnull
	static UiContact newInstance(@Nonnull User contact, int unreadMessagesCount) {
		return new UiContact(contact, unreadMessagesCount);
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

	@Nonnull
	String getDisplayName() {
		return this.contact.getDisplayName();
	}

	@Nonnull
	public UiContact copyForNewUser(@Nonnull User newContact) {
		return UiContact.newInstance(newContact, this.unreadMessagesCount);
	}

	@Nonnull
	public UiContact copyForNewUnreadMessagesCount(@Nonnull Integer unreadMessagesCount) {
		return UiContact.newInstance(this.contact, unreadMessagesCount);
	}
}
