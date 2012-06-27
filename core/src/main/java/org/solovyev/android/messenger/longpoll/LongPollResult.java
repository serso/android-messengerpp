package org.solovyev.android.messenger.longpoll;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:18 AM
 */
public interface LongPollResult {

    @Nullable
    Object updateLongPollServerData(@Nullable Object longPollServerData);

    void doUpdates(@NotNull User user, @NotNull Context context);
}
