package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.widget.ImageView;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface ChatMessageService {

    @Nonnull
    List<ChatMessage> getChatMessages(@Nonnull RealmEntity realmChat);

    void setMessageIcon(@Nonnull ImageView imageView, @Nonnull ChatMessage message, @Nonnull Chat chat, @Nonnull User user, @Nonnull Context context);
}
