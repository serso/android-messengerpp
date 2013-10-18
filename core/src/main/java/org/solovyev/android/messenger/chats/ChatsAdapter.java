package org.solovyev.android.messenger.chats;

import android.content.Context;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:48 PM
 */
public class ChatsAdapter extends AbstractChatsAdapter {

	public ChatsAdapter(@Nonnull Context context) {
		super(context);
	}

	@Override
	protected boolean canAddChat(@Nonnull Chat chat) {
		return true;
	}
}
