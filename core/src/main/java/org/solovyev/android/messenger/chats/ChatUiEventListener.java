package org.solovyev.android.messenger.chats;

import android.support.v4.app.Fragment;
import com.actionbarsherlock.app.ActionBar;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.fragments.MultiPaneFragmentManager;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MessagesFragment;
import org.solovyev.android.messenger.users.ContactFragment;
import org.solovyev.android.messenger.users.ContactFragmentReuseCondition;
import org.solovyev.android.messenger.users.ContactsInfoFragment;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.Builder;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.chats.Chats.CHATS_FRAGMENT_TAG;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:59 PM
 */
public class ChatUiEventListener implements EventListener<ChatUiEvent> {

	@Nonnull
	private static final String TAG = ChatUiEventListener.class.getSimpleName();

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
			case chat_open_requested:
				onChatOpenRequestedEvent(chat);
				break;
			case chat_clicked:
				onChatClickedEvent(chat);
				break;
			case chat_message_read:
				onMessageReadEvent(chat, event.getDataAsMessage());
				break;
		}
	}

	private void onChatOpenRequestedEvent(@Nonnull final Chat chat) {
		final MultiPaneFragmentManager fragmentService = activity.getMultiPaneFragmentManager();
		if (activity.getMultiPaneManager().isDualPane(activity)) {
			if (!fragmentService.isFragmentShown(CHATS_FRAGMENT_TAG)) {
				final ActionBar.Tab tab = activity.findTabByTag(CHATS_FRAGMENT_TAG);
				if (tab != null) {
					tab.select();
				}
			}

			final AbstractChatsFragment fragment = fragmentService.getFragment(CHATS_FRAGMENT_TAG);
			if (fragment != null) {
				fragment.clickItemById(chat.getId());
			}
		} else {
			fragmentService.goBackTillStart();
			fragmentService.setMainFragment(MultiPaneFragmentDef.newInstance(MessagesFragment.FRAGMENT_TAG, true, new Builder<Fragment>() {
				@Nonnull
				@Override
				public Fragment build() {
					return new MessagesFragment(chat);
				}
			}, MessagesFragmentReuseCondition.forChat(chat)));
		}
	}

	private void onMessageReadEvent(@Nonnull Chat chat, @Nonnull Message message) {
		chatService.onMessageRead(chat, message);
	}

	private void onChatClickedEvent(@Nonnull final Chat chat) {
		final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();

		if (activity.isDualPane()) {
			fm.setSecondFragment(new Builder<Fragment>() {
				@Nonnull
				@Override
				public Fragment build() {
					return new MessagesFragment(chat);
				}
			}, MessagesFragmentReuseCondition.forChat(chat), MessagesFragment.FRAGMENT_TAG);

			if (activity.isTriplePane()) {
				if (chat.isPrivate()) {
					fm.setThirdFragment(new Builder<Fragment>() {
						@Nonnull
						@Override
						public Fragment build() {
							return ContactFragment.newForContact(chat.getSecondUser());
						}
					}, ContactFragmentReuseCondition.forContact(chat.getSecondUser()), ContactFragment.FRAGMENT_TAG);
				} else {
					fm.setThirdFragment(new Builder<Fragment>() {
						@Nonnull
						@Override
						public Fragment build() {
							final List<User> participants = new ArrayList<User>();
							try {
								final Account account = activity.getAccountService().getAccountByEntity(chat.getEntity());
								participants.addAll(activity.getChatService().getParticipantsExcept(chat.getEntity(), account.getUser().getEntity()));
							} catch (UnsupportedAccountException e) {
								App.getExceptionHandler().handleException(e);
							}
							return new ContactsInfoFragment(participants);
						}
					}, null, ContactsInfoFragment.FRAGMENT_TAG);
				}
			}

		} else {
			fm.setMainFragment(new Builder<Fragment>() {
				@Nonnull
				@Override
				public Fragment build() {
					return new MessagesFragment(chat);
				}
			}, MessagesFragmentReuseCondition.forChat(chat), MessagesFragment.FRAGMENT_TAG, true);
		}
	}
}
