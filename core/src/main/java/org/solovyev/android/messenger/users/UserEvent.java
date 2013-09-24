package org.solovyev.android.messenger.users;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.common.listeners.AbstractTypedJEvent;

/**
 * User: serso
 * Date: 3/9/13
 * Time: 2:44 PM
 */
public class UserEvent extends AbstractTypedJEvent<User, UserEventType> {

	UserEvent(@Nonnull User user, @Nonnull UserEventType type, @Nullable Object data) {
		super(user, type, data);
	}

	@Nonnull
	public User getUser() {
		return getEventObject();
	}

	@Nonnull
	public User getDataAsUser() {
		return (User) getData();
	}

	@Nonnull
	public List<User> getDataAsUsers() {
		return (List<User>) getData();
	}

	@Nonnull
	public String getDataAsUserId() {
		return (String) getData();
	}

	@Nonnull
	public String getDataAsChatId() {
		return (String) getData();
	}

	@Nonnull
	public Chat getDataAsChat() {
		return (Chat) getData();
	}

	@Nonnull
	public List<Chat> getDataAsChats() {
		return (List<Chat>) getData();
	}

	@Nonnull
	public Integer getDataAsInteger() {
		return (Integer) getData();
	}
}
