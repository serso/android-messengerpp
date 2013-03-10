package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 4:17 PM
 */
public interface ChatEventContainer {

    void addChatEventListener(@Nonnull ChatEventListener chatEventListener);

    void removeChatEventListener(@Nonnull ChatEventListener chatEventListener);

    void fireChatEvent(@Nonnull Chat chat, @Nonnull ChatEventType chatEventType, @Nullable Object data);

    void fireChatEvents(@Nonnull List<ChatEvent> chatEvents);

}
