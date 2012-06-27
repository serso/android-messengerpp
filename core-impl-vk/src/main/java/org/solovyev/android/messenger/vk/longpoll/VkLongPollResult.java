package org.solovyev.android.messenger.vk.longpoll;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.longpoll.LongPollResult;
import org.solovyev.android.messenger.users.User;

import java.util.List;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:47 AM
 */
public class VkLongPollResult implements LongPollResult {

    @NotNull
    private Long lastUpdate;

    @NotNull
    private List<LongPollUpdate> updates;

    public VkLongPollResult(@NotNull Long lastUpdate, @NotNull List<LongPollUpdate> updates) {
        this.lastUpdate = lastUpdate;
        this.updates = updates;
    }

    @Override
    public Object updateLongPollServerData(@Nullable Object longPollServerData) {
        if ( longPollServerData instanceof LongPollServerData ) {
            final LongPollServerData lpsd = (LongPollServerData) longPollServerData;
            // NOTE: new timestamp
            return new LongPollServerData(lpsd.getKey(), lpsd.getServerUri(), lastUpdate);
        }

        return longPollServerData;
    }

    @Override
    public void doUpdates(@NotNull User user, @NotNull Context context) {
        for (LongPollUpdate update : updates) {
            update.doUpdate(user, context);
        }
    }
}
