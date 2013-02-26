package org.solovyev.android.messenger.vk.chats;

import android.content.Context;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.AbstractMessengerApplication;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.vk.http.AbstractVkHttpTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/10/12
 * Time: 10:05 PM
 */
public class VkMessagesGetHttpTransaction extends AbstractVkHttpTransaction<List<ChatMessage>> {

    @Nullable
    private Integer count;

    @NotNull
    private User user;

    @NotNull
    private Context context;

    protected VkMessagesGetHttpTransaction(@NotNull Realm realm, @NotNull User user, @NotNull Context context) {
        super(realm, "messages.get");
        this.user = user;
        this.context = context;
    }

    @NotNull
    @Override
    public List<NameValuePair> getRequestParameters() {
        final List<NameValuePair> requestParameters = super.getRequestParameters();

        if (count != null) {
            requestParameters.add(new BasicNameValuePair("count", String.valueOf(count)));
        }

        return requestParameters;
    }

    @Override
    protected List<ChatMessage> getResponseFromJson(@NotNull String json) throws IllegalJsonException {
        final List<ApiChat> chats = new JsonChatConverter(user, null, null, AbstractMessengerApplication.getServiceLocator().getUserService(), getRealm()).convert(json);

        // todo serso: optimize - convert json to the messages directly
        final List<ChatMessage> messages = new ArrayList<ChatMessage>(chats.size() * 10);
        for (ApiChat chat : chats) {
            messages.addAll(chat.getMessages());
        }

        return messages;
    }
}
