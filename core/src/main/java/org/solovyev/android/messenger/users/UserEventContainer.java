package org.solovyev.android.messenger.users;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 1:25 AM
 */
public interface UserEventContainer {

    void addUserEventListener(@NotNull UserEventListener userEventListener);

    void removeUserEventListener(@NotNull UserEventListener userEventListener);

    void fireUserEvent(@NotNull User user, @NotNull UserEventType userEventType, @Nullable Object data);

    void fireUserEvents(@NotNull List<UserEvent> userEvents);

    public static class UserEvent {

        @NotNull
        private User user;

        @NotNull
        private UserEventType userEventType;

        @Nullable
        private Object data;

        public UserEvent(@NotNull User user, @NotNull UserEventType userEventType, Object data) {
            this.user = user;
            this.userEventType = userEventType;
            this.data = data;
        }

        @NotNull
        public User getUser() {
            return user;
        }

        @NotNull
        public UserEventType getUserEventType() {
            return userEventType;
        }

        @Nullable
        public Object getData() {
            return data;
        }
    }
}
