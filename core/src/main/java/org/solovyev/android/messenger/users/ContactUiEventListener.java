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
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatUiEventType;
import org.solovyev.android.messenger.messages.Message;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.App.*;
import static org.solovyev.android.messenger.chats.ChatUiEventType.chat_message_read;
import static org.solovyev.android.messenger.users.ContactUiEventType.*;

public final class ContactUiEventListener implements EventListener<ContactUiEvent> {

	@Nonnull
	private final BaseFragmentActivity activity;

	@Nonnull
	private final AccountService accountService;

	public ContactUiEventListener(@Nonnull BaseFragmentActivity activity, @Nonnull AccountService accountService) {
		this.activity = activity;
		this.accountService = accountService;
	}

	@Override
	public void onEvent(@Nonnull ContactUiEvent event) {
		final User contact = event.getContact();
		final ContactUiEventType type = event.getType();

		final Account account = accountService.getAccountByEntity(contact.getEntity());
		switch (type) {
			case contact_clicked:
				fireEvent(open_contact_chat.newEvent(contact));
				break;
			case call_contact:
				if (account.isCompositeUser(contact)) {
					if (!account.isCompositeUserDefined(contact)) {
						fireEvent(show_composite_user_dialog.newEvent(contact, call_contact));
					} else {
						onCallContactChat(contact);
					}
				} else {
					onCallContactChat(contact);
				}
				break;
			case open_contact_chat:
				onOpenContactChat(contact);
				break;
			case edit_contact:
				onEditContact(contact);
				break;
			case view_contact:
				onViewContact(contact);
				break;
			case mark_all_messages_read:
				onMarkAllMessagesRead(contact);
				break;
			case show_composite_user_dialog:
				onShowCompositeUserDialog(contact, event.getDataAsEventType());
				break;
		}
	}

	private void onShowCompositeUserDialog(@Nonnull User contact, @Nonnull ContactUiEventType nextEventType) {
		CompositeUserDialogFragment.show(contact, nextEventType, activity);
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

	private void onEditContact(@Nonnull User contact) {
		Users.tryShowEditUserFragment(contact, activity);
	}

	private void onViewContact(@Nonnull User contact) {
		Users.showViewUserFragment(contact, activity);
	}

	private void fireEvent(@Nonnull ContactUiEvent event) {
		getEventManager(activity).fire(event);
	}

	private void onOpenContactChat(final User contact) {
		new MessengerAsyncTask<Void, Void, Chat>() {

			@Override
			protected Chat doWork(@Nonnull List<Void> params) {
				Chat result = null;

				try {
					final User user = activity.getAccountService().getAccountById(contact.getEntity().getAccountId()).getUser();
					result = getChatService().getOrCreatePrivateChat(user.getEntity(), contact.getEntity());
				} catch (AccountException e) {
					throwException(e);
				}

				return result;
			}

			@Override
			protected void onSuccessPostExecute(@Nullable Chat chat) {
				if (chat != null) {
					activity.getEventManager().fire(ChatUiEventType.chat_clicked.newEvent(chat));
				}
			}

		}.executeInParallel();
	}
}
