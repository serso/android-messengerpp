package org.solovyev.android.messenger.messages;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/25/12
 * Time: 11:00 PM
 */
public class SendMessageAsyncTask extends MessengerAsyncTask<SendMessageAsyncTask.Input, Void, List<ChatMessage>> {

    @NotNull
    private final Chat chat;

    public SendMessageAsyncTask(@NotNull Context context, @NotNull Chat chat) {
        super(context);
        this.chat = chat;
    }

    @Override
    protected List<ChatMessage> doWork(@NotNull List<Input> inputs) {
        final List<ChatMessage> result = new ArrayList<ChatMessage>(inputs.size());

        for (Input input : inputs) {
            final Context context = getContext();
            if (context != null) {
                assert chat.equals(input.chat);

                result.add(input.sendChatMessage(context));
            }
        }

        return result;
    }

    @Override
    protected void onSuccessPostExecute(@Nullable List<ChatMessage> result) {
        if (result != null) {
            //getChatService().fireChatEvent(chat, ChatEventType.message_added_batch, result);
        }
    }

    @NotNull
    private static ChatService getChatService() {
        return MessengerApplication.getServiceLocator().getChatService();
    }

    @NotNull
    private static UserService getUserService() {
        return MessengerApplication.getServiceLocator().getUserService();
    }

    public static class Input {

        @NotNull
        private final User author;

        @NotNull
        private String message;

        @Nullable
        private String title;

        @NotNull
        private final List<Object> attachments = new ArrayList<Object>();

        @NotNull
        private final List<LiteChatMessage> fwdMessages = new ArrayList<LiteChatMessage>();

        @NotNull
        private final Chat chat;

        public Input(@NotNull User author, @NotNull String message, @NotNull Chat chat) {
            this.author = author;
            this.message = message;
            this.chat = chat;
        }

        public void setTitle(@Nullable String title) {
            this.title = title;
        }

        public boolean addAttachment(Object attachment) {
            return attachments.add(attachment);
        }

        public boolean addFwdMessage(@NotNull LiteChatMessage fwdMessage) {
            return fwdMessages.add(fwdMessage);
        }

        @NotNull
        public ChatMessage sendChatMessage(@NotNull Context context) {
            final LiteChatMessageImpl liteChatMessage = LiteChatMessageImpl.newInstance("");
            liteChatMessage.setAuthor(author);
            liteChatMessage.setBody(message);

            liteChatMessage.setTitle(title == null ? "" : title);
            liteChatMessage.setSendDate(DateTime.now());

            final ChatMessageImpl chatMessage = new ChatMessageImpl(liteChatMessage);
            chatMessage.setRead(true);
            chatMessage.setDirection(MessageDirection.out);
            for (LiteChatMessage fwdMessage : fwdMessages) {
                chatMessage.addFwdMessage(fwdMessage);
            }

            return getChatService().sendChatMessage(author.getId(), chat, chatMessage, context);
        }

    }
}


