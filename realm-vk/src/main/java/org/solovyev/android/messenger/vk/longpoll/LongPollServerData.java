package org.solovyev.android.messenger.vk.longpoll;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/23/12
 * Time: 11:58 PM
 */
public class LongPollServerData {

    @Nonnull
    private String key;

    @Nonnull
    private String serverUri;

    @Nonnull
    private Long timeStamp;

    public LongPollServerData(@Nonnull String key, @Nonnull String serverUri, @Nonnull Long timeStamp) {
        this.key = key;
        this.serverUri = serverUri;
        this.timeStamp = timeStamp;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nonnull
    public String getServerUri() {
        return serverUri;
    }

    @Nonnull
    public Long getTimeStamp() {
        return timeStamp;
    }
}
