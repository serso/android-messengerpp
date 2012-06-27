package org.solovyev.android.messenger;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.chats.ChatDao;
import org.solovyev.android.messenger.messages.ChatMessageDao;
import org.solovyev.android.messenger.users.UserDao;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:32 PM
 */
public interface DaoLocator {

    @NotNull
    UserDao getUserDao(@NotNull Context context);

    @NotNull
    ChatDao getChatDao(@NotNull Context context);

    @NotNull
    ChatMessageDao getChatMessageDao(@NotNull Context context);
}
