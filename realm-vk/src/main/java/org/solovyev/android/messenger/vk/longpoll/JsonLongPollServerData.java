package org.solovyev.android.messenger.vk.longpoll;

import org.jetbrains.annotations.Nullable;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:00 AM
 */
public class JsonLongPollServerData {

    @Nullable
    private String key;

    @Nullable
    private String server;

    @Nullable
    private Long ts;

    @Nullable
    public String getKey() {
        return key;
    }

    @Nullable
    public String getServer() {
        return server;
    }

    @Nullable
    public Long getTs() {
        return ts;
    }
}
