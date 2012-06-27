package org.solovyev.android.messenger.users;

import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.common.utils.ListListenersContainer;

import java.util.Arrays;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 1:27 AM
 */
public class ListUserEventContainer implements UserEventContainer {

    @NotNull
    private static final String TAG = "UserEvent";

    @NotNull
    private final ListListenersContainer<UserEventListener> listeners = new ListListenersContainer<UserEventListener>();

    @Override
    public void addUserEventListener(@NotNull UserEventListener userEventListener) {
        this.listeners.addListener(userEventListener);
    }

    @Override
    public void removeUserEventListener(@NotNull UserEventListener userEventListener) {
        this.listeners.removeListener(userEventListener);
    }

    @Override
    public void fireUserEvent(@NotNull User user, @NotNull UserEventType userEventType, @Nullable Object data) {
        fireUserEvents(Arrays.asList(new UserEvent(user, userEventType, data)));
    }

    @Override
    public void fireUserEvents(@NotNull List<UserEvent> userEvents) {
        final List<UserEventListener> listeners = this.listeners.getListeners();

        for (UserEvent userEvent : userEvents) {
            Log.d(TAG, "Event: " + userEvent.getUserEventType() + " for user: " + userEvent.getUser().getId() + " with data: " + userEvent.getData());
            for (UserEventListener listener : listeners) {
                listener.onUserEvent(userEvent.getUser(), userEvent.getUserEventType(), userEvent.getData());
            }
        }
    }
}
