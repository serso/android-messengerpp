package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.EventListener;

/**
* User: serso
* Date: 6/2/12
* Time: 1:27 AM
*/
public interface UserEventListener extends EventListener {
    void onUserEvent(@Nonnull User eventUser, @Nonnull UserEventType userEventType, @Nullable Object data);
}
