package org.solovyev.android.messenger.vk.chats;

import android.content.Context;
import android.util.Log;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.ApiChatImpl;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.messenger.vk.messages.JsonMessage;
import org.solovyev.android.messenger.vk.messages.JsonMessageTypedAttachment;
import org.solovyev.android.messenger.vk.messages.JsonMessages;
import org.solovyev.common.Converter;
import org.solovyev.common.collections.CollectionsUtils;
import org.solovyev.common.text.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:33 PM
 */
public class JsonChatConverter implements Converter<String, List<ApiChat>> {

    @NotNull
    private final User user;

    @Nullable
    private final String explicitChatId;

    @Nullable
    private final String explicitUserId;
    
    @NotNull
    private final UserService userService;

    @NotNull
    private final Context context;

    public JsonChatConverter(@NotNull User user,
                             @Nullable String explicitChatId,
                             @Nullable String explicitUserId,
                             @NotNull UserService userService,
                             @NotNull Context context) {
        this.user = user;
        this.explicitChatId = explicitChatId;
        this.explicitUserId = explicitUserId;
        this.userService = userService;
        this.context = context;
    }

    @NotNull
    @Override
    public List<ApiChat> convert(@NotNull String json) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(JsonMessages.class, new JsonMessages.Adapter())
                .registerTypeAdapter(JsonMessageTypedAttachment.class, new JsonMessageTypedAttachment.Adapter())
                .create();

        final JsonMessages jsonMessagesResult = gson.fromJson(json, JsonMessages.class);

        final List<JsonMessage> jsonMessages = jsonMessagesResult.getResponse();

        // key: chat id, value: chat
        final Map<String, ApiChatImpl> chats = new HashMap<String, ApiChatImpl>();

        // key: id of second user, value: chat
        final Map<String, ApiChatImpl> fakeChats = new HashMap<String, ApiChatImpl>();

        try {
            final Splitter splitter = Splitter.on(",");

            if (!CollectionsUtils.isEmpty(jsonMessages)) {
                for (JsonMessage jsonMessage : jsonMessages) {
                    final ChatMessage message = jsonMessage.toChatMessage(user, explicitUserId, userService, context);

                    final Integer apiChatId = jsonMessage.getChat_id();
                    if (apiChatId == null && explicitChatId == null) {

                        // fake chat (message from user to another without explicitly created chat)
                        final User secondUser = message.getSecondUser(user);

                        if (secondUser != null) {
                            final String chatId = MessengerConfigurationImpl.getInstance().getServiceLocator().getChatService().createPrivateChatId(user.getId(), secondUser.getId());

                            ApiChatImpl chat = fakeChats.get(chatId);
                            if (chat == null) {
                                chat = new ApiChatImpl(chatId, jsonMessagesResult.getCount(), true);

                                chat.addParticipant(user);
                                chat.addParticipant(secondUser);

                                fakeChats.put(chatId, chat);
                            }

                            chat.addMessage(message);
                        } else {
                            Log.e(this.getClass().getSimpleName(), "Recipient is null for message " + message);
                        }

                    } else {
                        // real chat
                        final String chatId = apiChatId == null ? explicitChatId : String.valueOf(apiChatId);

                        ApiChatImpl chat = chats.get(chatId);
                        if (chat == null) {
                            // create new chat object
                            chat = new ApiChatImpl(chatId, jsonMessagesResult.getCount(), false);

                            final String participantsStr = jsonMessage.getChat_active();
                            if (!StringUtils.isEmpty(participantsStr)) {
                                for (Integer participantId : Iterables.transform(splitter.split(participantsStr), ToIntFunction.getInstance())) {
                                    chat.addParticipant(userService.getUserById(String.valueOf(participantId), context));
                                }
                            }

                            chat.addParticipant(user);

                            chats.put(chatId, chat);
                        }

                        chat.addMessage(message);
                    }
                }
            }
        } catch (IllegalJsonException e) {
            throw new IllegalJsonRuntimeException(e);
        }

        final List<ApiChat> result = new ArrayList<ApiChat>(chats.size() + fakeChats.size());
        result.addAll(chats.values());
        result.addAll(fakeChats.values());
        return result;
    }

    private static class ToIntFunction implements Function<String, Integer> {

        @NotNull
        private static final ToIntFunction instance = new ToIntFunction();

        private ToIntFunction() {
        }

        @NotNull
        public static ToIntFunction getInstance() {
            return instance;
        }

        @Override
        public Integer apply(@Nullable String input) {
            return Integer.valueOf(input);
        }
    }
}
