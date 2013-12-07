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

package org.solovyev.android.messenger.chats;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountRunnable;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.users.ContactUiEventType;
import org.solovyev.android.messenger.users.User;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import java.util.List;

import static org.solovyev.android.messenger.App.executeInBackground;
import static org.solovyev.android.messenger.accounts.Accounts.withAccountException;
import static org.solovyev.android.messenger.chats.Chats.CHATS_FRAGMENT_TAG;
import static org.solovyev.android.messenger.messages.MessagesFragment.newMessagesFragmentDef;
import static org.solovyev.android.messenger.users.ContactFragment.newViewContactFragmentDef;
import static org.solovyev.android.messenger.users.ContactsInfoFragment.newViewContactsFragmentDef;
import static org.solovyev.android.messenger.users.Users.showViewUsersFragment;

public class ChatUiEventListener implements EventListener<ChatUiEvent> {

	@Nonnull
	private final BaseFragmentActivity activity;

	@Nonnull
	private final ChatService chatService;

	public ChatUiEventListener(@Nonnull BaseFragmentActivity activity, @Nonnull ChatService chatService) {
		this.activity = activity;
		this.chatService = chatService;
	}

	@Override
	public void onEvent(ChatUiEvent event) {
		final Chat chat = event.getChat();
		final ChatUiEventType type = event.getType();

		switch (type) {
			case open_chat:
				onOpenChatEvent(chat);
				break;
			case chat_clicked:
				onChatClickedEvent(chat);
				break;
			case chat_message_read:
				onMessageReadEvent(chat, event.getDataAsMessage());
				break;
			case show_participants:
				onShowParticipants(chat);
				break;
		}
	}

	private void onShowParticipants(@Nonnull Chat chat) {
		if (chat.isPrivate()) {
			final Entity contactId = chatService.getSecondUser(chat);
			if (contactId != null) {
				final User contact = App.getUserService().getUserById(contactId);
				App.getEventManager(activity).fire(ContactUiEventType.view_contact.newEvent(contact));
			}
		} else {
			final Account account = App.getAccountService().getAccountByEntity(chat.getEntity());
			final List<User> participants = chatService.getParticipantsExcept(chat.getEntity(), account.getUser().getEntity());
			showViewUsersFragment(participants, activity);
		}
	}

	private void onOpenChatEvent(@Nonnull final Chat chat) {
		final MessengerMultiPaneFragmentManager mpfm = activity.getMultiPaneFragmentManager();
		if (activity.getMultiPaneManager().isDualPane(activity)) {
			final BaseChatsFragment fragment = mpfm.getFragment(CHATS_FRAGMENT_TAG);
			if (fragment != null && fragment.isVisible()) {
				if (!fragment.clickItemById(chat.getId())) {
					mpfm.setSecondFragment(newMessagesFragmentDef(activity, chat, true));
				}
			} else {
				mpfm.setSecondFragment(newMessagesFragmentDef(activity, chat, true));
			}
		} else {
			mpfm.setMainFragment(newMessagesFragmentDef(activity, chat, true));
		}
	}

	private void onMessageReadEvent(@Nonnull final Chat chat, @Nonnull final Message message) {
		executeInBackground(withAccountException(new AccountRunnable() {
			@Override
			public void run() throws AccountConnectionException {
				chatService.markMessageRead(chat, message);
			}
		}));
	}

	private void onChatClickedEvent(@Nonnull final Chat chat) {
		final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();

		if (activity.isDualPane()) {
			fm.clearBackStack();
			fm.setSecondFragment(newMessagesFragmentDef(activity, chat, false));
			if (activity.isTriplePane()) {
				final Account account = activity.getAccountService().getAccountByEntity(chat.getEntity());

				if (chat.isPrivate()) {
					fm.setThirdFragment(newViewContactFragmentDef(activity, account, chat.getSecondUser(), false));
				} else {
					final List<User> participants = activity.getChatService().getParticipantsExcept(chat.getEntity(), account.getUser().getEntity());
					fm.setThirdFragment(newViewContactsFragmentDef(activity, participants, false));
				}
			}

		} else {
			fm.setMainFragment(newMessagesFragmentDef(activity, chat, true));
		}
	}
}
