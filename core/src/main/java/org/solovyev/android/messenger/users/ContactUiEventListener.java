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

import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.messages.Message;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.*;
import static org.solovyev.android.messenger.chats.ChatUiEventType.chat_message_read;
import static org.solovyev.android.messenger.users.ContactUiEventType.call_contact;

public final class ContactUiEventListener implements EventListener<ContactUiEvent.Typed> {

	@Nonnull
	private final BaseFragmentActivity activity;

	@Nonnull
	private final AccountService accountService;

	public ContactUiEventListener(@Nonnull BaseFragmentActivity activity, @Nonnull AccountService accountService) {
		this.activity = activity;
		this.accountService = accountService;
	}

	@Override
	public void onEvent(@Nonnull ContactUiEvent.Typed event) {
		final User contact = event.contact;

		final Account account = accountService.getAccountByEntity(contact.getEntity());
		switch (event.type) {
			case call_contact:
				if (account.isCompositeUser(contact)) {
					if (!account.isCompositeUserDefined(contact)) {
						fireEvent(new ContactUiEvent.ShowCompositeDialog(contact, call_contact));
					} else {
						onCallContactChat(contact);
					}
				} else {
					onCallContactChat(contact);
				}
				break;
			case view_contact:
				ContactActivity.open(activity, contact, false);
				break;
			case mark_all_messages_read:
				onMarkAllMessagesRead(contact);
				break;
		}
	}

	private void onCallContactChat(@Nonnull User contact) throws UnsupportedAccountException {
		final Account account = getAccountService().getAccountByEntity(contact.getEntity());
		if (account.canCall(contact)) {
			account.call(contact, activity);
		}
	}

	private void onMarkAllMessagesRead(@Nonnull User contact) throws UnsupportedAccountException {
		final Account account = getAccountService().getAccountByEntity(contact.getEntity());
		final Chat chat = getChatService().getPrivateChat(account.getUser().getEntity(), contact.getEntity());
		if (chat != null) {
			for (Message message : getMessageService().getMessages(chat.getEntity())) {
				if (message.canRead()) {
					getEventManager(activity).fire(chat_message_read.newEvent(chat, message.cloneRead()));
				}
			}
		}
	}

	private void fireEvent(@Nonnull ContactUiEvent event) {
		getEventManager(activity).fire(event);
	}
}
