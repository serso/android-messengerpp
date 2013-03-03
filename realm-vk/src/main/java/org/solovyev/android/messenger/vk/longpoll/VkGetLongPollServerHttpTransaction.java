package org.solovyev.android.messenger.vk.longpoll;

import com.google.gson.Gson;
import javax.annotation.Nonnull;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.vk.http.AbstractVkHttpTransaction;

/**
 * User: serso
 * Date: 6/23/12
 * Time: 11:56 PM
 */
public class VkGetLongPollServerHttpTransaction extends AbstractVkHttpTransaction<LongPollServerData> {

    public VkGetLongPollServerHttpTransaction(@Nonnull Realm realm) {
        super(realm, "messages.getLongPollServer");
    }

    @Override
    protected LongPollServerData getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
        final Gson gson = new Gson();
        final JsonLongPollResponse jsonLongPollResponse = gson.fromJson(json, JsonLongPollResponse.class);
        return jsonLongPollResponse.toLongPollServerData();
    }
}
