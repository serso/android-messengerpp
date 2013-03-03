package org.solovyev.android.messenger.vk.chats;

import android.content.Context;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import javax.annotation.Nonnull;
import org.solovyev.android.messenger.MessengerApplication;
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

    @Nonnull
    private static final Integer MAX_COUNT = 100;

    @Nonnull
    private final Integer count;

    @Nonnull
    private final User user;

    @Nonnull
    private final Context context;

    private VkMessagesGetDialogsHttpTransaction(@Nonnull Realm realm, @Nonnull Integer count, @Nonnull User user, @Nonnull Context context) {
        super(realm, "messages.getDialogs");
        this.count = count;
        this.user = user;
        this.context = context;
    }

    @Nonnull
    public static VkMessagesGetDialogsHttpTransaction newInstance(@Nonnull Realm realm, @Nonnull User user, @Nonnull Context context) {
        return new VkMessagesGetDialogsHttpTransaction(realm, MAX_COUNT, user, context);
    }

    @Nonnull
    public static List<VkMessagesGetDialogsHttpTransaction> newInstances(@Nonnull Realm realm, @Nonnull Integer count, @Nonnull User user, @Nonnull Context context) {
        final List<VkMessagesGetDialogsHttpTransaction> result = new ArrayList<VkMessagesGetDialogsHttpTransaction>();

        for (int i = 0; i < count / MAX_COUNT; i++) {
            result.add(new VkMessagesGetDialogsHttpTransaction(realm, MAX_COUNT, user, context));
        }

        if (count % MAX_COUNT != 0) {
            result.add(new VkMessagesGetDialogsHttpTransaction(realm, count % MAX_COUNT, user, context));
        }

        return result;
    }

    @Nonnull
    @Override
    public List<NameValuePair> getRequestParameters() {
        final List<NameValuePair> result = super.getRequestParameters();

        result.add(new BasicNameValuePair("count", String.valueOf(count)));

        return result;
    }

    @Override
    protected List<ApiChat> getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
        return new JsonChatConverter(user, null, null, MessengerApplication.getServiceLocator().getUserService(), getRealm()).convert(json);
    }
}
