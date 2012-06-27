package org.solovyev.android.messenger.chats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 4:17 PM
 */
public interface ChatEventContainer {

    void addChatEventListener(@NotNull ChatEventListener chatEventListener);

    void removeChatEventListener(@NotNull ChatEventListener chatEventListener);

    void fireChatEvent(@NotNull Chat chat, @NotNull ChatEventType chatEventType, @Nullable Object data);

    void fireChatEvents(@NotNull List<ChatEvent> chatEvents);

    public static class ChatEvent {

        @NotNull
        private Chat chat;

        @NotNull
        private ChatEventType chatEventType;

        @Nullable
        private Object data;

        public ChatEvent(@NotNull Chat chat, @NotNull ChatEventType chatEventType, Object data) {
            this.chat = chat;
            this.chatEventType = chatEventType;
            this.data = data;
        }

        @NotNull
        public Chat getChat() {
            return chat;
        }

        @NotNull
        public ChatEventType getChatEventType() {
            return chatEventType;
        }

        @Nullable
        public Object getData() {
            return data;
        }
    }
}
