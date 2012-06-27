package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EventListener;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 4:17 PM
 */
public interface ChatEventListener extends EventListener {
    void onChatEvent(@NotNull Chat eventChat, @NotNull ChatEventType chatEventType, @Nullable Object data);
}
