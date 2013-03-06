package org.solovyev.android.messenger.users;

import android.util.Log;
import org.solovyev.common.listeners.JListeners;
import org.solovyev.common.listeners.Listeners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 1:27 AM
 */
public class ListUserEventListeners implements UserEventListeners {

    @Nonnull
    private static final String TAG = "M++/UserEvent";

    @Nonnull
    private final JListeners<UserEventListener> listeners = Listeners.newWeakRefListeners();

    @Override
    public boolean addListener(@Nonnull UserEventListener userEventListener) {
        return this.listeners.addListener(userEventListener);
    }

    @Override
    public boolean removeListener(@Nonnull UserEventListener userEventListener) {
        return this.listeners.removeListener(userEventListener);
    }

    @Override
    public void fireUserEvent(@Nonnull User user, @Nonnull UserEventType userEventType, @Nullable Object data) {
        fireUserEvents(Arrays.asList(new UserEvent(user, userEventType, data)));
    }

    @Override
    public void fireUserEvents(@Nonnull List<UserEvent> userEvents) {
        final Collection<UserEventListener> listeners = this.listeners.getListeners();

        for (UserEvent userEvent : userEvents) {
            Log.d(TAG, "Event: " + userEvent.getUserEventType() + " for user: " + userEvent.getUser().getRealmUser().getEntityId() + " with data: " + userEvent.getData());
            for (UserEventListener listener : listeners) {
                listener.onUserEvent(userEvent.getUser(), userEvent.getUserEventType(), userEvent.getData());
            }
        }
    }
}
