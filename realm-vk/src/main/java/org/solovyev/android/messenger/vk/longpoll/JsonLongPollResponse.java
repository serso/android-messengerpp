package org.solovyev.android.messenger.vk.longpoll;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    @NotNull
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
