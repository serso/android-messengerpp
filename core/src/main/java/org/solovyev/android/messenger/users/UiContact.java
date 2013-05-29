package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.MessengerEntity;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/25/13
 * Time: 10:09 PM
 */
final class UiContact implements MessengerEntity {

	@Nonnull
	private final User contact;

	private final int unreadMessagesCount;

	// precached display name in order to calculate it before shown (e.g. for sorting)
	@Nonnull
	private final String displayName;

	private UiContact(@Nonnull User contact, int unreadMessagesCount, @Nonnull String displayName) {
		this.contact = contact;
		this.unreadMessagesCount = unreadMessagesCount;
		this.displayName = displayName;
	}

	@Nonnull
	static UiContact newInstance(@Nonnull User contact, int unreadMessagesCount, @Nonnull String displayName) {
		return new UiContact(contact, unreadMessagesCount, displayName);
	}

	@Nonnull
	static UiContact newInstance(@Nonnull User contact, int unreadMessagesCount) {
		return newInstance(contact, unreadMessagesCount, "");
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
		return displayName;
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
