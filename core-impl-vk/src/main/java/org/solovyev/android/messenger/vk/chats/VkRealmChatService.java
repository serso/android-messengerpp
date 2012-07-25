package org.solovyev.android.messenger.vk.chats;

import android.content.Context;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.RuntimeIoException;
import org.solovyev.android.http.AndroidHttpUtils;
import org.solovyev.android.http.HttpTransaction;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.messenger.vk.messages.VkMessagesSendHttpTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:30 PM
 */
public class VkRealmChatService implements RealmChatService {

    @NotNull
    private static final String TAG = VkRealmChatService.class.getSimpleName();

    /*@NotNull
    @Override
    public List<Chat> getUserChats(@NotNull Integer userId) {
        try {
            final List<Chat> result = new ArrayList<Chat>();
            for (VkMessagesGetDialogsHttpTransaction vkMessagesGetDialogsHttpTransaction : VkMessagesGetDialogsHttpTransaction.newInstances(100)) {
                result.addAll(AndroidHttpUtils.execute(vkMessagesGetDialogsHttpTransaction));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }*/

    @NotNull
    @Override
    public List<ChatMessage> getChatMessages(@NotNull String userId, @NotNull Context context) {
        try {
            return AndroidHttpUtils.execute(new VkMessagesGetHttpTransaction(getUser(userId, context), context));
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @NotNull
    @Override
    public List<ChatMessage> getNewerChatMessagesForChat(@NotNull String chatId, @NotNull String userId, @NotNull Context context) {
        return getChatMessagesForChat(chatId, userId, context, new VkHttpTransactionForMessagesForChatProvider() {
            @NotNull
            @Override
            public List<? extends HttpTransaction<List<ChatMessage>>> getForPrivateChat(@NotNull User user, @NotNull String secondUserId, @NotNull Context context) {
                return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forUser(secondUserId, user, context));
            }

            @NotNull
            @Override
            public List<? extends HttpTransaction<List<ChatMessage>>> getForChat(@NotNull User user, @NotNull String chatId, @NotNull Context context) {
                return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forChat(chatId, user, context));
            }
        });
    }

    private List<ChatMessage> getChatMessagesForChat(@NotNull String chatId, @NotNull String userId, @NotNull Context context, @NotNull VkHttpTransactionForMessagesForChatProvider p) {
        final Chat chat = getChatService().getChatById(chatId, context);

        if (chat != null) {
            try {
                if (chat.isPrivate()) {
                    final int index = chatId.indexOf("_");
                    if (index >= 0) {

                        final String secondUserId = chatId.substring(index + 1, chatId.length());
                        final List<ChatMessage> result = new ArrayList<ChatMessage>(100);
                        for (List<ChatMessage> messages : AndroidHttpUtils.execute(p.getForPrivateChat(getUser(userId, context), secondUserId, context))) {
                            result.addAll(messages);
                        }
                        return result;

                    } else {
                        Log.e(TAG, "Chat is private but don't have '_', chat id: " + chatId);
                        return Collections.emptyList();
                    }

                } else {
                    final List<ChatMessage> result = new ArrayList<ChatMessage>(100);
                    for (List<ChatMessage> messages : AndroidHttpUtils.execute(p.getForChat(getUser(userId, context), chatId, context))) {
                        result.addAll(messages);
                    }
                    return result;
                }
            } catch (IOException e) {
                throw new RuntimeIoException(e);
            }
        } else {
            Log.e(TAG, "Chat is not found for chat id: " + chatId);
            return Collections.emptyList();
        }
    }

    @NotNull
    @Override
    public List<ChatMessage> getOlderChatMessagesForChat(@NotNull String chatId, @NotNull String userId, @NotNull final Integer offset, @NotNull Context context) {
        return getChatMessagesForChat(chatId, userId, context, new VkHttpTransactionForMessagesForChatProvider() {
            @NotNull
            @Override
            public List<? extends HttpTransaction<List<ChatMessage>>> getForPrivateChat(@NotNull User user, @NotNull String secondUserId, @NotNull Context context) {
                return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forUser(secondUserId, user, offset, context));
            }

            @NotNull
            @Override
            public List<? extends HttpTransaction<List<ChatMessage>>> getForChat(@NotNull User user, @NotNull String chatId, @NotNull Context context) {
                return Arrays.asList(VkMessagesGetHistoryHttpTransaction.forChat(chatId, user, offset, context));
            }
        });
    }

    private static interface VkHttpTransactionForMessagesForChatProvider {
        @NotNull
        List<? extends HttpTransaction<List<ChatMessage>>> getForPrivateChat(@NotNull User user, @NotNull String secondUserId, @NotNull Context context);

        @NotNull
        List<? extends HttpTransaction<List<ChatMessage>>> getForChat(@NotNull User user, @NotNull String chatId, @NotNull Context context);

    }

    @NotNull
    private User getUser(@NotNull String userId, @NotNull Context context) {
        return getUserService().getUserById(userId, context);
    }

    @NotNull
    private UserService getUserService() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService();
    }

    @NotNull
    private ChatService getChatService() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator().getChatService();
    }


    @NotNull
    @Override
    public List<ApiChat> getUserChats(@NotNull String userId, @NotNull Context context) {
        try {
            final User user = MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService().getUserById(userId, context);
            return AndroidHttpUtils.execute(VkMessagesGetDialogsHttpTransaction.newInstance(user, context));
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @NotNull
    @Override
    public String sendChatMessage(@NotNull Chat chat, @NotNull ChatMessage chatMessage, @NotNull Context context) {
        try {
            return AndroidHttpUtils.execute(new VkMessagesSendHttpTransaction(chatMessage, chat));
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }
}
