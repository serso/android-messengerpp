package org.solovyev.android.messenger.chats;

import android.content.Context;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.getAccountService;

public class RecentChatsAdapter extends AbstractChatsAdapter {

	public RecentChatsAdapter(@Nonnull Context context) {
		super(context);
	}

	@Override
	protected boolean canAddChat(@Nonnull Chat chat) {
		return true;
	}

	@Override
	public void onEvent(@Nonnull ChatEvent event) {
		super.onEvent(event);

		final Chat eventChat = event.getChat();

		switch (event.getType()) {
			case last_message_changed:
				try {
					final User user = getAccountService().getAccountById(eventChat.getEntity().getAccountId()).getUser();
					final ChatListItem chatListItem = findInAllElements(user, eventChat);
					if (chatListItem == null) {
						addListItem(user, eventChat);
					}
				} catch (UnsupportedAccountException e) {
					App.getExceptionHandler().handleException(e);
				}
				break;
		}
	}
}
