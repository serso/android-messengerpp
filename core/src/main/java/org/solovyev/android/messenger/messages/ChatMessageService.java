package org.solovyev.android.messenger.messages;

import android.widget.ImageView;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmException;

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
public interface ChatMessageService {

	final String NO_REALM_MESSAGE_ID = "empty";

	void init();

	@Nonnull
	Entity generateEntity(@Nonnull Realm realm);

	@Nonnull
	List<ChatMessage> getChatMessages(@Nonnull Entity realmChat);

	void setMessageIcon(@Nonnull ChatMessage message, @Nonnull ImageView imageView);

	@Nullable
	ChatMessage sendChatMessage(@Nonnull Entity user, @Nonnull Chat chat, @Nonnull ChatMessage chatMessage) throws RealmException;

	/**
	 * @return total number of unread messages in the application
	 */
	int getUnreadMessagesCount();

	void removeAllMessagesInRealm(@Nonnull String realmId);
}
