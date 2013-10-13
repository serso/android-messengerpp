package org.solovyev.android.messenger.messages;

import android.widget.ImageView;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.entities.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */

/**
 * Implementation of this class must provide thread safeness
 */
@ThreadSafe
public interface MessageService {

	void init();

	@Nonnull
	List<ChatMessage> getMessages(@Nonnull Entity chat);

	void setMessageIcon(@Nonnull ChatMessage message, @Nonnull ImageView imageView);

	@Nullable
	ChatMessage sendMessage(@Nonnull Entity user, @Nonnull Chat chat, @Nonnull ChatMessage chatMessage) throws AccountException;

	/**
	 * @return total number of unread messages in the application
	 */
	int getUnreadMessagesCount();

}
