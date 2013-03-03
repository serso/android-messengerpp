package org.solovyev.android.messenger.vk.chats;

import android.content.Context;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.MessengerApplication;
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

    @Nonnull
    private User user;

    @Nonnull
    private Context context;

    protected VkMessagesGetHttpTransaction(@Nonnull Realm realm, @Nonnull User user, @Nonnull Context context) {
        super(realm, "messages.get");
        this.user = user;
        this.context = context;
    }

    @Nonnull
    @Override
    public List<NameValuePair> getRequestParameters() {
        final List<NameValuePair> requestParameters = super.getRequestParameters();

        if (count != null) {
            requestParameters.add(new BasicNameValuePair("count", String.valueOf(count)));
        }

        return requestParameters;
    }

    @Override
    protected List<ChatMessage> getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
        final List<ApiChat> chats = new JsonChatConverter(user, null, null, MessengerApplication.getServiceLocator().getUserService(), getRealm()).convert(json);

        // todo serso: optimize - convert json to the messages directly
        final List<ChatMessage> messages = new ArrayList<ChatMessage>(chats.size() * 10);
        for (ApiChat chat : chats) {
            messages.addAll(chat.getMessages());
        }

        return messages;
    }
}
