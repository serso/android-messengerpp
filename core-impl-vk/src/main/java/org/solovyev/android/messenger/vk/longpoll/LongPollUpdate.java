package org.solovyev.android.messenger.vk.longpoll;

import android.content.Context;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.android.messenger.users.UserService;

import java.lang.reflect.Type;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:53 AM
 */
public interface LongPollUpdate {

    void doUpdate(@NotNull User user, @NotNull Context context);

    public static class Adapter implements JsonDeserializer<LongPollUpdate> {

        @Override
        public LongPollUpdate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            if (json.isJsonArray()) {

                final JsonArray jsonArray = json.getAsJsonArray();

                switch (jsonArray.get(0).getAsInt()) {
                    case 0:
                        return new RemoveMessage(jsonArray.get(1).getAsInt());
                    //case 3: todo serso: implement ONE message update
                    case 4:
                        int flags = jsonArray.get(2).getAsInt();
                        int chatUserId = jsonArray.get(3).getAsInt();
                        // todo serso: uncomment after answer
                        if (/*MessageFlag.chat.isApplied(flags) && */chatUserId >= 2000000000) {

                            // MAGIC MAGIC MAGIC
                            int chatId = chatUserId - 2000000000;

                            return new MessageAdded(String.valueOf(chatId), chatUserId);

                        } else {
                            int userId = chatUserId;
                            return new MessageAdded(userId, chatUserId);
                        }
                    case 8:
                        return new FriendOnline(-jsonArray.get(1).getAsInt(), true);
                    case 9:
                        return new FriendOnline(-jsonArray.get(1).getAsInt(), false);
                    case 51:
                        return new ChatChanged(jsonArray.get(1).getAsInt());
                    case 61:
                        return new UserStartTypingInPrivateChat(jsonArray.get(1).getAsInt());
                    case 62:
                        return new UserStartTypingInChat(jsonArray.get(1).getAsInt(), jsonArray.get(2).getAsString());
                }

                return new EmptyLongPollUpdate();


                /*final JsonArray responseArray = response.getAsJsonArray("response");

                boolean first = true;

                result.response = new ArrayList<JsonMessage>();
                for (JsonElement e : responseArray.getAsJsonArray()) {
                    if (first) {
                        result.count = e.getAsInt();
                        first = false;
                    } else {
                        result.response.add((JsonMessage) context.deserialize(e, JsonMessage.class));
                    }
                }*/

            } else {
                throw new JsonParseException("Unexpected JSON type: " + json.getClass());
            }
        }
    }

    static class UserStartTypingInChat implements LongPollUpdate {

        private final int userId;

        @NotNull
        private final String chatId;

        public UserStartTypingInChat(int userId, @NotNull String chatId) {
            this.userId = userId;
            this.chatId = chatId;
        }

        @Override
        public void doUpdate(@NotNull User user, @NotNull Context context) {
            // not self
            if (!user.getId().equals(userId)) {
                Chat chat = getChatService().getChatById(chatId, context);
                if (chat != null) {
                    getChatService().fireChatEvent(chat, ChatEventType.user_start_typing, userId);
                }
            }
        }


        @NotNull
        private static ChatService getChatService() {
            return MessengerConfigurationImpl.getInstance().getServiceLocator().getChatService();
        }
    }

    static class UserStartTypingInPrivateChat implements LongPollUpdate {

        @NotNull
        private Integer userId;

        public UserStartTypingInPrivateChat(@NotNull Integer userId) {
            this.userId = userId;
        }

        @Override
        public void doUpdate(@NotNull User user, @NotNull Context context) {
            // not self
            if (!user.getId().equals(userId)) {
                final String chatId = getChatService().createPrivateChatId(user.getId(), userId);
                Chat chat = getChatService().getChatById(chatId, context);
                if (chat != null) {
                    getChatService().fireChatEvent(chat, ChatEventType.user_start_typing, userId);
                }
            }
        }


        @NotNull
        private static ChatService getChatService() {
            return MessengerConfigurationImpl.getInstance().getServiceLocator().getChatService();
        }
    }

    static class ChatChanged implements LongPollUpdate {

        private final int chatId;

        public ChatChanged(int chatId) {
            this.chatId = chatId;
        }

        @Override
        public void doUpdate(@NotNull User user, @NotNull Context context) {
            getChatService().syncChat(String.valueOf(chatId), user.getId(), context);
        }


        @NotNull
        private ChatService getChatService() {
            return MessengerConfigurationImpl.getInstance().getServiceLocator().getChatService();
        }
    }

    static class EmptyLongPollUpdate implements LongPollUpdate {

        @Override
        public void doUpdate(@NotNull User user, @NotNull Context context) {
            // do nothing
        }


    }

    static class MessageAdded implements LongPollUpdate {

        @Nullable
        private final Integer friendId;

        @Nullable
        private final String chatId;

        @NotNull
        private final Integer chatUserId;

        public MessageAdded(@NotNull Integer friendId, @NotNull Integer chatUserId) {
            this.friendId = friendId;
            this.chatUserId = chatUserId;
            this.chatId = null;
        }

        public MessageAdded(@NotNull String chatId, @NotNull Integer chatUserId) {
            this.chatUserId = chatUserId;
            this.friendId = null;
            this.chatId = chatId;
        }

        @Override
        public void doUpdate(@NotNull User user, @NotNull Context context) {
            final String chatId;
            if (this.chatId != null) {
                chatId = this.chatId;
            } else {
                assert friendId != null;
                chatId = getChatService().createPrivateChatId(user.getId(), friendId);
            }

            getChatService().syncNewerChatMessagesForChat(chatId, user.getId(), context);
        }

        @NotNull
        private ChatService getChatService() {
            return MessengerConfigurationImpl.getInstance().getServiceLocator().getChatService();
        }
    }
    static class FriendOnline implements LongPollUpdate {

        @NotNull
        private final Integer friendId;

        private final boolean online;

        public FriendOnline(@NotNull Integer friendId, boolean online) {
            this.friendId = friendId;
            this.online = online;
        }

        @Override
        public void doUpdate(@NotNull User user, @NotNull Context context) {
            final User friend = getUserService().getUserById(friendId, context).cloneWithNewStatus(online);
            if (online) {
                getUserService().fireUserEvent(user, UserEventType.friend_online, friend);
            } else {
                getUserService().fireUserEvent(user, UserEventType.friend_offline, friend);
            }
        }

        private UserService getUserService() {
            return MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService();
        }
    }
    static class RemoveMessage implements LongPollUpdate {

        @NotNull
        private final Integer messageId;

        public RemoveMessage(@NotNull Integer messageId) {
            this.messageId = messageId;
        }

        @Override
        public void doUpdate(@NotNull User user, @NotNull Context context) {
            // todo serso: implement
        }
    }

}
