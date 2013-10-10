package org.solovyev.android.messenger.users;

import android.support.v4.app.Fragment;
import roboguice.event.EventListener;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.Fragments2;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatUiEventType;
import org.solovyev.android.messenger.messages.ChatMessage;
import org.solovyev.common.Builder;

import static org.solovyev.android.messenger.App.getAccountService;
import static org.solovyev.android.messenger.App.getChatMessageService;
import static org.solovyev.android.messenger.App.getChatService;
import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.App.getExceptionHandler;
import static org.solovyev.android.messenger.chats.ChatUiEventType.chat_message_read;
import static org.solovyev.android.messenger.users.ContactUiEventType.open_contact_chat;
import static org.solovyev.android.messenger.users.ContactUiEventType.show_composite_user_dialog;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:54 PM
 */
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

		try {
			final Account account = accountService.getAccountByEntityAware(contact);
			switch (type) {
				case contact_clicked:
					if (account.isCompositeUser(contact)) {
						if (!account.isCompositeUserDefined(contact)) {
							if(activity.isDualPane()) {
								activity.getMultiPaneFragmentManager().setSecondFragment(new Builder<Fragment>() {
									@Nonnull
									@Override
									public Fragment build() {
										return CompositeContactFragment.newForContact(contact);
									}
								}, null, CompositeContactFragment.FRAGMENT_TAG);
							}
							fireEvent(show_composite_user_dialog.newEvent(contact));
						} else {
							fireEvent(open_contact_chat.newEvent(contact));
						}
					} else {
						fireEvent(open_contact_chat.newEvent(contact));
					}
					break;
				case open_contact_chat:
					onOpenContactChat(contact);
					break;
				case edit_contact:
					onEditContact(contact);
					break;
				case mark_all_messages_read:
					onMarkAllMessagesRead(contact);
					break;
				case show_composite_user_dialog:
					Fragments2.showDialog(new CompositeUserDialogFragment(contact), CompositeUserDialogFragment.FRAGMENT_TAG, activity.getSupportFragmentManager());
					break;
			}
		} catch (UnsupportedAccountException e) {
			// should not happen
			getExceptionHandler().handleException(e);
		}
	}

	private void onMarkAllMessagesRead(@Nonnull User contact) throws UnsupportedAccountException {
		final Account account = getAccountService().getAccountByEntityAware(contact);
		try {
			final Chat chat = getChatService().getPrivateChat(account.getUser().getEntity(), contact.getEntity());
			if (chat != null) {
				for (ChatMessage message : getChatMessageService().getChatMessages(chat.getEntity())) {
					if(!message.isRead()) {
						getEventManager(activity).fire(chat_message_read.newEvent(chat, message.cloneRead()));
					}
				}
			}
		} catch (AccountException e) {
			getExceptionHandler().handleException(e);
		}
	}

	private void onEditContact(@Nonnull User contact) {
		Users.tryShowEditUserFragment(contact, activity);
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
