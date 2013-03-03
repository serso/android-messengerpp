package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.EventListener;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 4:17 PM
 */
public interface ChatEventListener extends EventListener {
    void onChatEvent(@Nonnull Chat eventChat, @Nonnull ChatEventType chatEventType, @Nullable Object data);
}
