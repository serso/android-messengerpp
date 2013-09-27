package org.solovyev.android.messenger.chats;

import android.content.Context;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.users.UserEventType;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
