package org.solovyev.android.messenger.vk.chats;

import android.content.Context;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.AbstractMessengerApplication;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.vk.http.AbstractVkHttpTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:03 PM
 */
public class VkMessagesGetDialogsHttpTransaction extends AbstractVkHttpTransaction<List<ApiChat>> {

    @NotNull
    private static final Integer MAX_COUNT = 100;

    @NotNull
    private final Integer count;

    @NotNull
    private final User user;

    @NotNull
    private final Context context;

    private VkMessagesGetDialogsHttpTransaction(@NotNull Realm realm, @NotNull Integer count, @NotNull User user, @NotNull Context context) {
        super(realm, "messages.getDialogs");
        this.count = count;
        this.user = user;
        this.context = context;
    }

    @NotNull
    public static VkMessagesGetDialogsHttpTransaction newInstance(@NotNull Realm realm, @NotNull User user, @NotNull Context context) {
        return new VkMessagesGetDialogsHttpTransaction(realm, MAX_COUNT, user, context);
    }

    @NotNull
    public static List<VkMessagesGetDialogsHttpTransaction> newInstances(@NotNull Realm realm, @NotNull Integer count, @NotNull User user, @NotNull Context context) {
        final List<VkMessagesGetDialogsHttpTransaction> result = new ArrayList<VkMessagesGetDialogsHttpTransaction>();

        for (int i = 0; i < count / MAX_COUNT; i++) {
            result.add(new VkMessagesGetDialogsHttpTransaction(realm, MAX_COUNT, user, context));
        }

        if (count % MAX_COUNT != 0) {
            result.add(new VkMessagesGetDialogsHttpTransaction(realm, count % MAX_COUNT, user, context));
        }

        return result;
    }

    @NotNull
    @Override
    public List<NameValuePair> getRequestParameters() {
        final List<NameValuePair> result = super.getRequestParameters();

        result.add(new BasicNameValuePair("count", String.valueOf(count)));

        return result;
    }

    @Override
    protected List<ApiChat> getResponseFromJson(@NotNull String json) throws IllegalJsonException {
        return new JsonChatConverter(user, null, null, AbstractMessengerApplication.getServiceLocator().getUserService(), getRealm()).convert(json);
    }
}
