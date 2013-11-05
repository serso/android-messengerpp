package org.solovyev.android.messenger.messages;

import android.widget.ImageView;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.entities.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

/**
 * Implementation of this class must provide thread safeness
 */
@ThreadSafe
public interface MessageService {

	void init();

	@Nonnull
	List<Message> getMessages(@Nonnull Entity chat);

	@Nullable
	Message getSameMessage(@Nonnull String body, @Nonnull DateTime sendTime, @Nonnull Entity author, @Nonnull Entity recipient);

	@Nullable
	Message getMessage(@Nonnull String messageId);

	void setMessageIcon(@Nonnull Message message, @Nonnull ImageView imageView);

	@Nonnull
	Message sendMessage(@Nonnull Chat chat, @Nonnull Message message) throws AccountException;

	@Nullable
	Message getLastMessage(@Nonnull String chatId);

	/**
	 * @return total number of unread messages in the application
	 */
	int getUnreadMessagesCount();

}
