package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 1:25 AM
 */
public interface UserEventListeners {

    boolean addListener(@Nonnull UserEventListener listener);

    boolean removeListener(@Nonnull UserEventListener listener);

    void fireUserEvent(@Nonnull User user, @Nonnull UserEventType userEventType, @Nullable Object data);

    void fireUserEvents(@Nonnull List<UserEvent> userEvents);
}
