package org.solovyev.android.messenger.vk.longpoll;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 6/23/12
 * Time: 11:58 PM
 */
public class LongPollServerData {

    @NotNull
    private String key;

    @NotNull
    private String serverUri;

    @NotNull
    private Long timeStamp;

    public LongPollServerData(@NotNull String key, @NotNull String serverUri, @NotNull Long timeStamp) {
        this.key = key;
        this.serverUri = serverUri;
        this.timeStamp = timeStamp;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    @NotNull
    public String getServerUri() {
        return serverUri;
    }

    @NotNull
    public Long getTimeStamp() {
        return timeStamp;
    }
}
