/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.users;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.android.messenger.messages.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Comparator;

import static org.solovyev.android.messenger.App.getAccountService;

final class UiContact implements Identifiable, EntityAware {

	@Nonnull
	private final User contact;

	private final int unreadMessagesCount;

	@Nullable
	private final DateTime lastMessageDate;

	@Nullable
	private Account account;

	private UiContact(@Nonnull User contact, int unreadMessagesCount, @Nullable DateTime lastMessageDate, @Nullable Account account) {
		this.contact = contact;
		this.unreadMessagesCount = unreadMessagesCount;
		this.lastMessageDate = lastMessageDate;
		this.account = account;
	}

	@Nonnull
	static UiContact newUiContact(@Nonnull User contact, int unreadMessagesCount, @Nullable DateTime lastMessageDate, @Nullable Account account) {
		return new UiContact(contact, unreadMessagesCount, lastMessageDate, account);
	}

	@Nonnull
	static UiContact loadRecentUiContact(@Nonnull User contact) {
		final Account account = getAccount(contact);
		final ChatService chatService = App.getChatService();
		final Chat chat = chatService.getPrivateChat(account.getUser().getEntity(), contact.getEntity());

		DateTime lastMessageDate = null;
		if (chat != null) {
			final Message lastMessage = chatService.getLastMessage(chat.getEntity());
			if (lastMessage != null) {
				lastMessageDate = lastMessage.getSendDate();
			}
		}
		return loadRecentUiContact(contact, lastMessageDate);
	}

	@Nonnull
	static UiContact loadRecentUiContact(@Nonnull User contact, @Nullable DateTime lastMessageDate) {
		return newUiContact(contact, getUnreadMessagesCount(contact), lastMessageDate, getAccount(contact));
	}

	@Nonnull
	static UiContact loadUiContact(@Nonnull User contact, @Nullable Account account) {
		return newUiContact(contact, getUnreadMessagesCount(contact), null, account);
	}

	@Nonnull
	private static Account getAccount(@Nonnull User contact) {
		return getAccountService().getAccountByEntity(contact.getEntity());
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
	public DateTime getLastMessageDate() {
		return lastMessageDate;
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
		return newUiContact(newContact, this.unreadMessagesCount, this.lastMessageDate, this.account);
	}

	@Nonnull
	public UiContact copyForNewUnreadMessagesCount(@Nonnull Integer unreadMessagesCount) {
		return newUiContact(this.contact, unreadMessagesCount, this.lastMessageDate, this.account);
	}

	@Nonnull
	public UiContact copyForNewLastMessageDate(@Nonnull DateTime lastMessageDate) {
		return newUiContact(this.contact, unreadMessagesCount, lastMessageDate, this.account);
	}

	@Nonnull
	@Override
	public Entity getEntity() {
		return contact.getEntity();
	}

	@Nonnull
	public static Comparator<UiContact> getComparator() {
		return UserComparator.getInstance();
	}

}
