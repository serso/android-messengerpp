package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.widget.ImageView;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.users.User;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface ChatMessageService {

    @NotNull
    List<ChatMessage> getChatMessages(@NotNull RealmEntity realmChat, @NotNull Context context);

    void setMessageIcon(@NotNull ImageView imageView, @NotNull ChatMessage message, @NotNull Chat chat, @NotNull User user, @NotNull Context context);
}
