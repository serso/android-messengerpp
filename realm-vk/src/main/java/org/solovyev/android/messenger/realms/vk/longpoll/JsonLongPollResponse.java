package org.solovyev.android.messenger.realms.vk.longpoll;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.http.IllegalJsonException;

/**
 * User: serso
 * Date: 6/23/12
 * Time: 11:59 PM
 */
public class JsonLongPollResponse {

    @Nullable
    private JsonLongPollServerData response;

    @Nullable
    public JsonLongPollServerData getResponse() {
        return response;
    }

    @Nonnull
    public LongPollServerData toLongPollServerData() throws IllegalJsonException {
        if ( response == null ) {
            throw new IllegalJsonException();
        }

        if ( response.getKey() == null || response.getServer() == null || response.getTs() == null ) {
            throw new IllegalJsonException();
        }

        return new LongPollServerData(response.getKey(), response.getServer(), response.getTs());
    }
}
