package org.solovyev.android.messenger.vk.messages;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.LiteChatMessage;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.vk.http.AbstractVkHttpTransaction;
import org.solovyev.common.utils.Strings2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * User: serso
 * Date: 6/25/12
 * Time: 11:25 PM
 */
public class VkMessagesSendHttpTransaction extends AbstractVkHttpTransaction<String> {

    @NotNull
    private final ChatMessage chatMessage;

    @NotNull
    private final Chat chat;

    public VkMessagesSendHttpTransaction(@NotNull ChatMessage chatMessage, @NotNull Chat chat) {
        super("messages.send");
        this.chatMessage = chatMessage;
        this.chat = chat;
    }

    @NotNull
    @Override
    public List<NameValuePair> getRequestParameters() {
        final List<NameValuePair> result = super.getRequestParameters();

        try {

            if (chat.isPrivate()) {
                result.add(new BasicNameValuePair("uid", String.valueOf(chat.getSecondUserId())));
            }

            if (!chat.isPrivate()) {
                result.add(new BasicNameValuePair("chat_id", chat.getId()));
            }

            result.add(new BasicNameValuePair("message", URLEncoder.encode(chatMessage.getBody(), "utf-8")));

            result.add(new BasicNameValuePair("title", URLEncoder.encode(chatMessage.getTitle(), "utf-8")));
            result.add(new BasicNameValuePair("type", chatMessage.isPrivate() ? "0" : "1"));

            final List<LiteChatMessage> fwdMessages = chatMessage.getFwdMessages();
            if (!fwdMessages.isEmpty()) {
                final String fwdMessagesParam = Strings2.getAllValues(Lists.transform(fwdMessages, new Function<LiteChatMessage, String>() {
                    @Override
                    public String apply(@Nullable LiteChatMessage fwdMessage) {
                        assert fwdMessage != null;
                        return fwdMessage.getId();
                    }
                }));

                result.add(new BasicNameValuePair("forward_messages", fwdMessagesParam));
            }

        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }

        return result;
    }

    @Override
    protected String getResponseFromJson(@NotNull String json) throws IllegalJsonException {
        return new Gson().fromJson(json, JsonResult.class).response;
    }

    public static class JsonResult {

        @Nullable
        private String response;
    }
}
