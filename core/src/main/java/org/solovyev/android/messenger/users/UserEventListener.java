package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EventListener;

/**
* User: serso
* Date: 6/2/12
* Time: 1:27 AM
*/
public interface UserEventListener extends EventListener {
    void onUserEvent(@NotNull User eventUser, @NotNull UserEventType userEventType, @Nullable Object data);
}
