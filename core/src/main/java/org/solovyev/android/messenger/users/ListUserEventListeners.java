package org.solovyev.android.messenger.users;

import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.common.listeners.JListeners;
import org.solovyev.common.listeners.Listeners;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 1:27 AM
 */
public class ListUserEventListeners implements UserEventListeners {

    @NotNull
    private static final String TAG = "UserEvent";

    @NotNull
    private final JListeners<UserEventListener> listeners = Listeners.newWeakRefListeners();

    @Override
    public boolean addListener(@NotNull UserEventListener userEventListener) {
        return this.listeners.addListener(userEventListener);
    }

    @Override
    public boolean removeListener(@NotNull UserEventListener userEventListener) {
        return this.listeners.removeListener(userEventListener);
    }

    @Override
    public void fireUserEvent(@NotNull User user, @NotNull UserEventType userEventType, @Nullable Object data) {
        fireUserEvents(Arrays.asList(new UserEvent(user, userEventType, data)));
    }

    @Override
    public void fireUserEvents(@NotNull List<UserEvent> userEvents) {
        final Collection<UserEventListener> listeners = this.listeners.getListeners();

        for (UserEvent userEvent : userEvents) {
            Log.d(TAG, "Event: " + userEvent.getUserEventType() + " for user: " + userEvent.getUser().getRealmUser().getEntityId() + " with data: " + userEvent.getData());
            for (UserEventListener listener : listeners) {
                listener.onUserEvent(userEvent.getUser(), userEvent.getUserEventType(), userEvent.getData());
            }
        }
    }
}
